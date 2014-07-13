package net.blogracy.controller.addendum;

import java.io.File;
import java.io.FileReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.shindig.protocol.conversion.BeanConverter;
import org.apache.shindig.protocol.conversion.BeanJsonConverter;
import org.apache.shindig.social.opensocial.model.ActivityEntry;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Module;
import com.google.inject.name.Names;

import net.blogracy.config.Configurations;
import net.blogracy.controller.DistributedHashTable;
import net.blogracy.controller.FileSharing;
import net.blogracy.controller.FileSharingImpl;
import net.blogracy.model.hashes.Hashes;
import net.blogracy.model.users.User;
import net.blogracy.model.users.UserAddendumData;
import net.blogracy.model.users.Users;

public class AddendumController {
	static final DateFormat ISO_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	static final String CACHE_FOLDER = Configurations.getPathConfig().getCachedFilesDirectoryPath();

	private static final AddendumController theInstance = new AddendumController();
	private static final FileSharing sharing = FileSharingImpl.getSingleton();
	private static final DistributedHashTable dht = DistributedHashTable.getSingleton();

	private static BeanJsonConverter CONVERTER = new BeanJsonConverter(Guice.createInjector(new Module() {
		@Override
		public void configure(Binder b) {
			b.bind(BeanConverter.class).annotatedWith(Names.named("shindig.bean.converter.json")).to(BeanJsonConverter.class);
		}
	}));

	public static AddendumController getSingleton() {
		return theInstance;
	}

	private AddendumController() {
		ISO_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));

		delegates = new HashMap<User, DelegateController>();
		delegates.put(Configurations.getUserConfig().getUser(), DelegateController.Create(Configurations.getUserConfig().getUser()));
		for (User friend : Configurations.getUserConfig().getFriends()) {
			delegates.put(friend, DelegateController.Create(friend));
		}

	}

	protected Map<User, DelegateController> delegates = new HashMap<User, DelegateController>();

	/**
	 * Fetched the user Addendum ActivityStream from the User's DB
	 * 
	 * @param user
	 * @return
	 */
	public static List<ActivityEntry> getFeed(String userId) {
		List<ActivityEntry> result = new ArrayList<ActivityEntry>();
		String hash = Hashes.hash(userId + "-addendum");
		System.out.println("Getting Addendum feed: " + hash + " (user id: " + userId + ")");
		JSONObject record = dht.getRecord(hash);
		if (record != null) {
			try {
				String latestASHash = FileSharingImpl.getHashFromMagnetURI(record.getString("uri"));

				File dbFile = new File(CACHE_FOLDER + File.separator + latestASHash + ".json");
				if (!dbFile.exists() && record.has("prev")) {
					latestASHash = FileSharingImpl.getHashFromMagnetURI(record.getString("prev"));
					dbFile = new File(CACHE_FOLDER + File.separator + latestASHash + ".json");
				}
				if (dbFile.exists()) {
					System.out.println("Getting Addendum feed: " + dbFile.getAbsolutePath());
					JSONObject db = new JSONObject(new JSONTokener(new FileReader(dbFile)));

					JSONArray items = db.getJSONArray("addendumItems");
					for (int i = 0; i < items.length(); ++i) {
						JSONObject item = items.getJSONObject(i);
						ActivityEntry entry = (ActivityEntry) CONVERTER.convertToObject(item, ActivityEntry.class);
						result.add(entry);
					}
					System.out.println("Addendum Feed loaded");
				} else {
					System.out.println("Addendum Feed not found");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	public void addAddendumEntry(String userId, ActivityEntry addendumEntry) {
		try {
			final String publishDate = ISO_DATE_FORMAT.format(new Date());

			UserAddendumData userAddendumData = sharing.getUserAddendumData(userId);
			userAddendumData.addAddendumEntry(addendumEntry);

			String dbUri = sharing.seedUserAddendumData(userAddendumData);
			String ddbKey = Hashes.hash(userId + "-addendum");
			DistributedHashTable.getSingleton().store(userId, ddbKey, dbUri, publishDate);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void removeUserContent(String contentRecipientUserId, String contentId) {
		SalmonDbController.getSingleton().removeUserContent(contentRecipientUserId, contentId);
		User channelUser = Users.newUser(Hashes.fromString(contentRecipientUserId));
		if (delegates.containsKey(channelUser)) {
			delegates.get(channelUser).delegateDecisionalMessageReceived(contentId);
		} 
	}

	public JSONArray getUserAllContent(String queryUserId) {
		return SalmonDbController.getSingleton().getUserAllContent(queryUserId);
	}
	
	public void addUserContent(String contentRecipientUserId, String contentId, JSONObject newContentData) {
		SalmonDbController.getSingleton().addUserContent(contentRecipientUserId, contentId, newContentData);
		User channelUser = Users.newUser(Hashes.fromString(contentRecipientUserId));
		if (delegates.containsKey(channelUser)) {
			delegates.get(channelUser).delegateApprovableMessageReceived(contentId, contentRecipientUserId);
		} 
	}

	public String getCurrentDelegate(String channelUserId) {
		User channelUser = Users.newUser(Hashes.fromString(channelUserId));
		if (delegates.containsKey(channelUser)) {
			User delegate = delegates.get(channelUser).getCurrentDelegate();
			return (delegate != null) ? delegate.getHash().toString() : null;
		} else
			return null;

	}

	public void electionMessageReceived(String channelUserId, String senderUserId) {
		User channelUser = Users.newUser(Hashes.fromString(channelUserId));
		if (delegates.containsKey(channelUser))
			delegates.get(channelUser).electionMessageReceived(senderUserId);
	}

	public void answerMessageReceived(String channelUserId, String senderUserId) {
		User channelUser = Users.newUser(Hashes.fromString(channelUserId));
		if (delegates.containsKey(channelUser))
			delegates.get(channelUser).answerMessageReceived(senderUserId);
	}

	public void coordinatorMessageReceived(String channelUserId, String senderUserId) {
		User channelUser = Users.newUser(Hashes.fromString(channelUserId));
		if (delegates.containsKey(channelUser))
			delegates.get(channelUser).coordinatorMessageReceived(senderUserId);
	}
	
	public DelegateController getDelegateController(String channelUserId) {
		User channelUser = Users.newUser(Hashes.fromString(channelUserId));
		if (delegates.containsKey(channelUser))
			return delegates.get(channelUser);
		return null;
	}
	
}
