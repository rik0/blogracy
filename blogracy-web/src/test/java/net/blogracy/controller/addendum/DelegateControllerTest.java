package net.blogracy.controller.addendum;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import net.blogracy.config.Configurations;
import net.blogracy.controller.CommentsController;
import net.blogracy.controller.FileSharing;
import net.blogracy.controller.addendum.DelegateController;
import net.blogracy.controller.addendum.DelegateController.BullyAlgorithmState;
import net.blogracy.model.hashes.Hashes;
import net.blogracy.model.users.User;
import net.blogracy.model.users.Users;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.*;

public class DelegateControllerTest {

	@Mock
	private CommentsController commentsController = null;
	@Mock
	private FileSharing fileSharing = null;

	private DelegateController delegateController = null;
	private User currentChannelUser = Users.newUser("USER_A", Hashes.newHash("USER_A"));

	private User currentUser = Configurations.getUserConfig().getUser();

	private List<User> currentChannelDelegates = Arrays.asList(Users.newUser("USER_B", Hashes.newHash("USER_B")), Users.newUser("USER_C", Hashes.newHash("USER_C")), currentUser,
			Users.newUser("USER_D", Hashes.newHash("USER_D")), Users.newUser("USER_E", Hashes.newHash("USER_E")));

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		Mockito.when(fileSharing.getDelegates(currentChannelUser.getHash().toString())).thenReturn(currentChannelDelegates);
		delegateController = DelegateController.Create(currentChannelUser, fileSharing, commentsController);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testDelegateApprovableMessageReceived() {
		String contentId = UUID.randomUUID().toString();
		delegateController.delegateApprovableMessageReceived(contentId, currentChannelUser.getHash().toString());
		assertEquals(contentId, delegateController.getLastContentId());
		assertTrue(delegateController.isContentTimerActive());
	}

	@Test
	public void testDelegateApprovableMessageReceived_null() {
		String contentId = null;
		delegateController.delegateApprovableMessageReceived(contentId, currentChannelUser.getHash().toString());
		assertEquals(contentId, delegateController.getLastContentId());
		assertFalse(delegateController.isContentTimerActive());
	}

	@Test
	public void testDelegateApprovableMessageReceived_SameAsBefore() {
		String contentId = UUID.randomUUID().toString();

		try {
			Field lastContentIdField = DelegateController.class.getDeclaredField("lastContentId");
			lastContentIdField.setAccessible(true);
			lastContentIdField.set(delegateController, contentId);
		} catch (Exception e) {
			e.printStackTrace();
		}

		delegateController.delegateApprovableMessageReceived(contentId, "");
		assertEquals(contentId, delegateController.getLastContentId());
		assertFalse(delegateController.isContentTimerActive());
	}

	@Test
	public void testDelegateApprovableMessageReceived_TwoDifferent() {
		String contentId = UUID.randomUUID().toString();

		delegateController.delegateApprovableMessageReceived(contentId, currentChannelUser.getHash().toString());

		contentId = UUID.randomUUID().toString();
		delegateController.delegateApprovableMessageReceived(contentId, currentChannelUser.getHash().toString());
		assertEquals(contentId, delegateController.getLastContentId());
		assertTrue(delegateController.isContentTimerActive());
	}

	@Test
	public void testDelegateDecisionalMessageReceived() {
		String contentId = UUID.randomUUID().toString();
		delegateController.delegateApprovableMessageReceived(contentId, currentChannelUser.getHash().toString());

		delegateController.delegateDecisionalMessageReceived(contentId);
		assertNull(delegateController.getLastContentId());
		assertFalse(delegateController.isContentTimerActive());
	}

	@Test
	public void testDelegateDecisionalMessageReceived_null() {
		String contentId = null;
		delegateController.delegateApprovableMessageReceived(contentId, currentChannelUser.getHash().toString());

		delegateController.delegateDecisionalMessageReceived(contentId);
		assertNull(delegateController.getLastContentId());
		assertFalse(delegateController.isContentTimerActive());
	}

	@Test
	public void testDelegateDecisionalMessageReceived_TwoDifferent() {
		String contentId = UUID.randomUUID().toString();
		delegateController.delegateApprovableMessageReceived(contentId, currentChannelUser.getHash().toString());
		String secondContentId = UUID.randomUUID().toString();
		delegateController.delegateDecisionalMessageReceived(secondContentId);
		assertEquals(contentId, delegateController.getLastContentId());
		assertTrue(delegateController.isContentTimerActive());
	}

	@Test
	public void testElectionMessageReceived_FromUserHigherThanMe() {

		delegateController.electionMessageReceived(currentChannelDelegates.get(0).getHash().toString());

		assertFalse(delegateController.isContentTimerActive());
		assertFalse(delegateController.isCoordinatorTimerActive());
		assertTrue(delegateController.isAnswersTimerActive());
		assertNull(delegateController.getCurrentDelegate());

		try {
			Field delegateControllerStateField = DelegateController.class.getDeclaredField("currentState");
			delegateControllerStateField.setAccessible(true);
			BullyAlgorithmState value = (BullyAlgorithmState) delegateControllerStateField.get(delegateController);
			assertEquals(BullyAlgorithmState.WAITING_FOR_ANSWERS, value);
		} catch (Exception e) {
			e.printStackTrace();
		}

		Mockito.verify(commentsController).sendBullyAnswerMessage(Matchers.eq(currentChannelUser.getHash().toString()), Matchers.eq(currentUser.getHash().toString()));
	}

	@Test
	public void testElectionMessageReceived_FromUserLowerThanMe() {

		delegateController.electionMessageReceived(currentChannelDelegates.get(currentChannelDelegates.size() - 1).getHash().toString());

		assertFalse(delegateController.isContentTimerActive());
		assertFalse(delegateController.isCoordinatorTimerActive());
		assertTrue(delegateController.isAnswersTimerActive());
		assertNull(delegateController.getCurrentDelegate());

		try {
			Field delegateControllerStateField = DelegateController.class.getDeclaredField("currentState");
			delegateControllerStateField.setAccessible(true);
			BullyAlgorithmState value = (BullyAlgorithmState) delegateControllerStateField.get(delegateController);
			assertEquals(BullyAlgorithmState.WAITING_FOR_ANSWERS, value);
		} catch (Exception e) {
			e.printStackTrace();
		}

		Mockito.verify(commentsController).sendBullyAnswerMessage(Matchers.eq(currentChannelUser.getHash().toString()), Matchers.eq(currentUser.getHash().toString()));
	}

	@Test
	public void testAnswerMessageReceived() {
		String userId= currentChannelDelegates.get(currentChannelDelegates.size() - 1).getHash().toString();
		delegateController.answerMessageReceived(userId);
		
		try {
			Field answerListField = DelegateController.class.getDeclaredField("answerList");
			answerListField.setAccessible(true);
			List<String> value = (List<String>) answerListField.get(delegateController);
			assertTrue(value.contains(userId));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testStartBullyElection() {
		try {
			Method startBullyElectionMethod = DelegateController.class.getDeclaredMethod("startBullyElection");
			startBullyElectionMethod.setAccessible(true);
			startBullyElectionMethod.invoke(delegateController);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		assertNull(delegateController.getCurrentDelegate());
		assertTrue(delegateController.isAnswersTimerActive());
		Mockito.verify(commentsController).sendBullyElectionMessage(Matchers.eq(currentChannelUser.getHash().toString()), Matchers.eq(currentUser.getHash().toString()));
	}
	
	@Test
	public void testStartBullyElection_StateWaitingForAnswer() {
		try {
			
			Field delegateControllerStateField = DelegateController.class.getDeclaredField("currentState");
			delegateControllerStateField.setAccessible(true);
			delegateControllerStateField.set(delegateController, BullyAlgorithmState.WAITING_FOR_ANSWERS);
			
			Method startBullyElectionMethod = DelegateController.class.getDeclaredMethod("startBullyElection");
			startBullyElectionMethod.setAccessible(true);
			startBullyElectionMethod.invoke(delegateController);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		assertNull(delegateController.getCurrentDelegate());
		assertFalse(delegateController.isAnswersTimerActive());
		Mockito.verify(commentsController, Mockito.times(0)).sendBullyElectionMessage(Matchers.eq(currentChannelUser.getHash().toString()), Matchers.eq(currentUser.getHash().toString()));
	}
	
	@Test
	public void testStartBullyElection_StateWaitingForCoordinator() {
		try {
			
			Field delegateControllerStateField = DelegateController.class.getDeclaredField("currentState");
			delegateControllerStateField.setAccessible(true);
			delegateControllerStateField.set(delegateController, BullyAlgorithmState.WAITING_FOR_COORDINATOR);
			
			Method startBullyElectionMethod = DelegateController.class.getDeclaredMethod("startBullyElection");
			startBullyElectionMethod.setAccessible(true);
			startBullyElectionMethod.invoke(delegateController);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		assertNull(delegateController.getCurrentDelegate());
		assertFalse(delegateController.isAnswersTimerActive());
		Mockito.verify(commentsController,Mockito.times(0)).sendBullyElectionMessage(Matchers.eq(currentChannelUser.getHash().toString()), Matchers.eq(currentUser.getHash().toString()));
	}
	
	@Test
	public void testCoordinatorMessageReceived() {
		String userId= currentChannelDelegates.get(1).getHash().toString();
		delegateController.coordinatorMessageReceived(userId);
		
		try {
			
			Field delegateControllerStateField = DelegateController.class.getDeclaredField("currentState");
			delegateControllerStateField.setAccessible(true);
			assertEquals(BullyAlgorithmState.IDLE,(BullyAlgorithmState)delegateControllerStateField.get(delegateController));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		assertEquals(userId, delegateController.getCurrentDelegate().getHash().toString());
	}
	
	@Test
	public void testBullyProcessAnswers_noAnswers() {
		try {	
			Method bullyProcessAnswersMethod = DelegateController.class.getDeclaredMethod("bullyProcessAnswers");
			bullyProcessAnswersMethod.setAccessible(true);
			bullyProcessAnswersMethod.invoke(delegateController);
			
			Field delegateControllerStateField = DelegateController.class.getDeclaredField("currentState");
			delegateControllerStateField.setAccessible(true);
			assertEquals(BullyAlgorithmState.IDLE,(BullyAlgorithmState)delegateControllerStateField.get(delegateController));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		assertEquals(currentUser, delegateController.getCurrentDelegate());
	}
	
	@Test
	public void testBullyProcessAnswers_higherThanMeAnswers() {
		try {
			
			String winningUserId= currentChannelDelegates.get(1).getHash().toString();
			delegateController.answerMessageReceived(winningUserId);
			delegateController.answerMessageReceived(currentChannelDelegates.get(2).getHash().toString());
			
			Method bullyProcessAnswersMethod = DelegateController.class.getDeclaredMethod("bullyProcessAnswers");
			bullyProcessAnswersMethod.setAccessible(true);
			bullyProcessAnswersMethod.invoke(delegateController);
			
			Field delegateControllerStateField = DelegateController.class.getDeclaredField("currentState");
			delegateControllerStateField.setAccessible(true);
			assertEquals(BullyAlgorithmState.WAITING_FOR_COORDINATOR,(BullyAlgorithmState)delegateControllerStateField.get(delegateController));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		assertNull(delegateController.getCurrentDelegate());
	}
}
