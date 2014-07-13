package net.blogracy.controller.addendum;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import net.blogracy.config.Configurations;
import net.blogracy.controller.CommentsController;
import net.blogracy.controller.CommentsControllerImpl;
import net.blogracy.controller.FileSharing;
import net.blogracy.controller.FileSharingImpl;
import net.blogracy.model.hashes.Hashes;
import net.blogracy.model.users.User;
import net.blogracy.model.users.Users;

public class DelegateController implements DelegateApprovableMessageListener, DelegateDecisionalMessageListener, UserDelegatesChangedListener {

	/**
	 * Represents the user to which the communication channel belongs to
	 * 
	 */
	protected User currentUser;

	/**
	 * Represents the other users in the communication channel
	 */
	protected List<User> listOfUsers = new ArrayList<User>();

	private FileSharing sharing;

	private CommentsController commentsController;

	protected User currentDelegate = null;
	protected BullyAlgorithmState currentState = BullyAlgorithmState.IDLE;

	private List<String> answerList = new ArrayList<String>();

	private final long BULLY_WAIT_TIME_MS = 10000 + (new Random()).nextInt(1000);
	private final long DELEGATE_CONTENT_UNHANDLED_TIMEOUT_MS = 10000 + (new Random()).nextInt(1000);

	private volatile String lastContentId;

	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	private ScheduledFuture<?> contentTimerHandle;
	private ScheduledFuture<?> answersTimerHandle;
	private ScheduledFuture<?> coordinatorTimerHandle;
	private static final Logger log = Logger.getLogger("net.blogracy.controller.DelegateController");

	private DelegateController(User user, FileSharing sharing, CommentsController commentsController) {
		this.currentUser = user;
		this.sharing = sharing;
		this.commentsController = commentsController;

		try {
			if (log.getHandlers().length <= 0) {
				log.addHandler(new FileHandler("delegateController.log"));
				log.getHandlers()[0].setFormatter(new SimpleFormatter());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		List<User> userFriends = this.sharing.getDelegates(this.currentUser.getHash().toString());
		this.listOfUsers.addAll(userFriends);

		log.info("listOfUsers channel:" + currentUser.getHash().toString() + " populated friends #:" + listOfUsers.size() + " " + " currentState:" + this.currentState + " ["
				+ writeAllUsers(listOfUsers) + "]");
	}

	public static DelegateController Create(User user) {
		FileSharing sharing = FileSharingImpl.getSingleton();
		CommentsController commentsController = CommentsControllerImpl.getInstance();
		return Create(user, sharing, commentsController);
	}

	// Used mainly for testing purposes
	public static DelegateController Create(User user, FileSharing sharing, CommentsController commentsController) {
		DelegateController c = new DelegateController(user, sharing, commentsController);
		sharing.addUserDelegatesChangedListener(c);
		return c;
	}

	/**
	 * Gets the score for the delegate. 0 is the top score.
	 * 
	 * @param userId
	 * @return
	 */
	public int getDelegateScore(String userId) {
		if (userId.compareToIgnoreCase(currentUser.getHash().toString()) == 0)
			return 0;

		for (int i = 0; i < listOfUsers.size(); ++i) {
			User u = listOfUsers.get(i);
			if (userId.compareToIgnoreCase(u.getHash().toString()) == 0)
				return i + 1;
		}

		log.info("getDelegateScore channel:" + currentUser.getHash().toString() + " userId:" + userId + " currentState:" + this.currentState);

		return Integer.MAX_VALUE;
	}

	public User getCurrentDelegate() {
		if (currentDelegate != null && currentState == BullyAlgorithmState.IDLE)
			return currentDelegate;

		return null; // No Delegate Present at the moment
	}

	private void sendCoordinatorMessage() {
		String senderUserId = Configurations.getUserConfig().getUser().getHash().toString();
		log.info("sendCoordinatorMessage channel:" + currentUser.getHash().toString() + " senderUserId:" + senderUserId + " currentState:" + this.currentState);
		commentsController.sendBullyCoordinatorMessage(currentUser.getHash().toString(), senderUserId);
	}

	private void sendElectionMessage() {
		String senderUserId = Configurations.getUserConfig().getUser().getHash().toString();
		log.info("sendElectionMessage channel:" + currentUser.getHash().toString() + " senderUserId:" + senderUserId + " currentState:" + this.currentState);
		commentsController.sendBullyElectionMessage(currentUser.getHash().toString(), senderUserId);
	}

	private void sendAnswerMessage() {
		String senderUserId = Configurations.getUserConfig().getUser().getHash().toString();
		log.info("sendAnswerMessage channel:" + currentUser.getHash().toString() + " senderUserId:" + senderUserId + " currentState:" + this.currentState);
		commentsController.sendBullyAnswerMessage(currentUser.getHash().toString(), senderUserId);
	}

	public void electionMessageReceived(String messageSenderUserId) {
		log.info("electionMessageReceived channel:" + currentUser.getHash().toString() + " messageSenderUserId:" + messageSenderUserId + " currentState:" + this.currentState);

		// If I receive and election message, i'm updating my state to reflect
		// that we're looking for a new delegate
		if (isContentTimerActive())
			stopContentTimer();
		if (isCoordinatorTimerActive())
			stopCoordinatorTimer();

		this.currentState = BullyAlgorithmState.WAITING_FOR_ANSWERS;
		this.currentDelegate = null;
		if (!isAnswersTimerActive())
			startAnswersTimer();

		//int myScore = this.getDelegateScore(Configurations.getUserConfig().getUser().getHash().toString());
		//if (this.getDelegateScore(messageSenderUserId) > myScore)
			sendAnswerMessage();
	}

	public void answerMessageReceived(String messageSenderUserId) {
		log.info("answerMessageReceived channel:" + currentUser.getHash().toString() + " messageSenderUserId:" + messageSenderUserId + " currentState:" + this.currentState);
		if (!answerList.contains(messageSenderUserId))
			answerList.add(messageSenderUserId);
	}

	public void coordinatorMessageReceived(String messageSenderUserId) {
		log.info("coordinatorMessageReceived channel:" + currentUser.getHash().toString() + " messageSenderUserId (new proposed coordinator):" + messageSenderUserId + " currentState:"
				+ this.currentState);
		if (isCoordinatorTimerActive())
			stopCoordinatorTimer();

		if (isAnswersTimerActive())
			stopAnswersTimer();

		this.currentState = BullyAlgorithmState.IDLE;
		User newDelegate = Users.newUser(Hashes.fromString(messageSenderUserId));
		this.currentDelegate = newDelegate;

		int myScore = getDelegateScore(Configurations.getUserConfig().getUser().getHash().toString());

		if (Configurations.getUserConfig().getUser().getHash().toString().compareToIgnoreCase(messageSenderUserId) == 0) {
			// I am the new delegate
			commentsController.getContentList(this.currentUser.getHash().toString());
		}
		// Check if the delegate has a score (at least) bigger than mine
		else if (getDelegateScore(messageSenderUserId) > myScore)
			startBullyElection();
	}

	public synchronized void delegateApprovableMessageReceived(String contentId, String contentRecipientUserId) {
		if (contentId == null)
			return;

		log.info("delegateApprovableMessageReceived channel:" + currentUser.getHash().toString() + " contentId:" + contentId + " currentState:" + this.currentState);
		if (this.lastContentId != contentId) {
			this.lastContentId = contentId;

			if (isContentTimerActive())
				stopContentTimer();

			startContentTimer();
		}
	}

	public synchronized void delegateDecisionalMessageReceived(String contentId) {
		if (contentId == null)
			return;

		log.info("delegateDecisionalMessageReceived channel:" + currentUser.getHash().toString() + " contentId:" + contentId + " currentState:" + this.currentState);

		if (this.lastContentId == contentId) {
			this.lastContentId = null;
			stopContentTimer();
		}
	}

	private void startContentTimer() {
		contentTimerHandle = scheduler.schedule(new Runnable() {
			@Override
			public void run() {
				if (currentState == BullyAlgorithmState.IDLE)
					startBullyElection();
			}

		}, DELEGATE_CONTENT_UNHANDLED_TIMEOUT_MS, TimeUnit.MILLISECONDS);
	}

	private void stopContentTimer() {
		if (contentTimerHandle == null)
			return;

		if (contentTimerHandle.isDone())
			return;

		if (!contentTimerHandle.isDone()) {
			contentTimerHandle.cancel(false);
		}
	}

	public boolean isContentTimerActive() {
		if (contentTimerHandle == null)
			return false;

		if (contentTimerHandle.isDone())
			return false;

		return true;
	}

	protected void startBullyElection() {
		// Bully algorithm delegate calculation already started
		if (this.currentState == BullyAlgorithmState.WAITING_FOR_ANSWERS || this.currentState == BullyAlgorithmState.WAITING_FOR_COORDINATOR)
			return;

		if (this.currentState == BullyAlgorithmState.IDLE) {
			this.currentState = BullyAlgorithmState.WAITING_FOR_ANSWERS;
			this.startAnswersTimer();
			this.sendElectionMessage();
			this.currentDelegate = null;
		}
	}

	private void startAnswersTimer() {
		// Reset answers list
		answerList.clear();
		answersTimerHandle = scheduler.schedule(new Runnable() {
			@Override
			public void run() {
				if (currentState == BullyAlgorithmState.WAITING_FOR_ANSWERS)
					bullyProcessAnswers();
			}

		}, BULLY_WAIT_TIME_MS, TimeUnit.MILLISECONDS);

	}

	protected void bullyProcessAnswers() {
		int myScore = getDelegateScore(Configurations.getUserConfig().getUser().getHash().toString());

		String winningUserId = Configurations.getUserConfig().getUser().getHash().toString();

		for (String userId : this.answerList) {
			if (getDelegateScore(userId) < myScore)
				winningUserId = userId;
		}

		if (winningUserId.compareToIgnoreCase(Configurations.getUserConfig().getUser().getHash().toString()) == 0) {
			// I'm the delegate!
			this.sendCoordinatorMessage();
			this.currentDelegate = Configurations.getUserConfig().getUser();
			this.currentState = BullyAlgorithmState.IDLE;
		} else {
			// waits another T-time for coordination messages
			this.currentState = BullyAlgorithmState.WAITING_FOR_COORDINATOR;
			this.startCoordinatorTimer();
		}
	}

	private void stopAnswersTimer() {
		if (answersTimerHandle == null)
			return;

		if (answersTimerHandle.isDone())
			return;

		if (!answersTimerHandle.isDone()) {
			answersTimerHandle.cancel(false);
		}

	}

	public boolean isAnswersTimerActive() {
		if (answersTimerHandle == null)
			return false;

		if (answersTimerHandle.isDone())
			return false;

		return true;
	}

	private void startCoordinatorTimer() {

		coordinatorTimerHandle = scheduler.schedule(new Runnable() {
			@Override
			public void run() {
				if (currentState == BullyAlgorithmState.WAITING_FOR_COORDINATOR) {
					// No coordinator found :(
					startBullyElection();
				}
			}

		}, BULLY_WAIT_TIME_MS, TimeUnit.MILLISECONDS);

	}

	private void stopCoordinatorTimer() {
		if (coordinatorTimerHandle == null)
			return;

		if (coordinatorTimerHandle.isDone())
			return;

		if (!coordinatorTimerHandle.isDone()) {
			coordinatorTimerHandle.cancel(false);
		}

	}

	public boolean isCoordinatorTimerActive() {
		if (coordinatorTimerHandle == null)
			return false;

		if (coordinatorTimerHandle.isDone())
			return false;

		return true;
	}

	public String getLastContentId() {
		return lastContentId;
	}

	protected void setLastContentId(String lastContentId) {
		this.lastContentId = lastContentId;
	}

	public enum BullyAlgorithmState {
		IDLE, WAITING_FOR_ANSWERS, WAITING_FOR_COORDINATOR
	}

	@Override
	public void onUserDelegatesChanged(String userId, List<User> newDelegates) {
		if (userId.equalsIgnoreCase(this.currentUser.getHash().toString())) {
			boolean delegatesChanged = false;
			if (this.listOfUsers.size() != newDelegates.size())
				delegatesChanged = true;

			if (!delegatesChanged) {
				for (int i = 0; i < this.listOfUsers.size(); ++i) {
					if (!this.listOfUsers.get(i).getHash().toString().equalsIgnoreCase(newDelegates.get(i).getHash().toString())) {
						delegatesChanged = true;
						break;
					}
				}
			}

			if (delegatesChanged) {
				this.listOfUsers.clear();
				this.listOfUsers.addAll(newDelegates);

				log.info("listOfUsers changed. Channel:" + currentUser.getHash().toString() + " populated friends #:" + listOfUsers.size() + " " + " currentState:" + this.currentState + " ["
						+ writeAllUsers(listOfUsers) + "]");
			}
		}

	}

	private String writeAllUsers(List<User> list) {
		String s = new String();
		for (User u : list) {
			s += u.getHash().toString() + ", ";
		}
		return s;
	}

	public BullyAlgorithmState getCurrentState() {
		return currentState;
	}
}
