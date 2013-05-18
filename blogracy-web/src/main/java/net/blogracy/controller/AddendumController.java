package net.blogracy.controller;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
import net.blogracy.model.hashes.Hashes;
import net.blogracy.model.users.UserAddendumData;

public class AddendumController {
	static final DateFormat ISO_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	static final String CACHE_FOLDER = Configurations.getPathConfig().getCachedFilesDirectoryPath();

	private static final AddendumController theInstance = new AddendumController();
	private static final FileSharing sharing = FileSharing.getSingleton();
	// private static final ActivitiesController activities = ActivitiesController.getSingleton();
	private static final DistributedHashTable dht = new DistributedHashTable();
	
	  private static BeanJsonConverter CONVERTER = new BeanJsonConverter(
	            Guice.createInjector(new Module() {
	                @Override
	                public void configure(Binder b) {
	                    b.bind(BeanConverter.class)
	                            .annotatedWith(
	                                    Names.named("shindig.bean.converter.json"))
	                            .to(BeanJsonConverter.class);
	                }
	            }));
	
	public static AddendumController getSingleton() {
		return theInstance;
	}

	private AddendumController() {
		ISO_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
	}
	
    /**
	 * Fetched the user ActivityStream from the User's DB
	 * 
	 * @param user
	 * @return
	 */
	public List<ActivityEntry> getFeed(String userId) {
		List<ActivityEntry> result = new ArrayList<ActivityEntry>();
		String hash = Hashes.hash(userId + "-addendum");
		System.out.println("Getting Addendum feed: " + hash + " (user id: " + userId + ")");
		JSONObject record = dht.getRecord(hash);
		if (record != null) {
			try {
				String latestASHash = FileSharing.getHashFromMagnetURI(record.getString("uri"));

				File dbFile = new File(CACHE_FOLDER + File.separator + latestASHash + ".json");
				if (!dbFile.exists() && record.has("prev")) {
					latestASHash = FileSharing.getHashFromMagnetURI(record.getString("prev"));
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
	
}
