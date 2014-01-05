/*
 * Copyright (c) 2011 Enrico Franchi, Michele Tomaiuolo and University of Parma.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package net.blogracy.controller;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import net.blogracy.config.Configurations;
import net.blogracy.model.users.UserData;
import net.blogracy.util.FileUtils;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.codec.binary.Base32;
import org.apache.shindig.protocol.conversion.BeanConverter;
import org.apache.shindig.protocol.conversion.BeanJsonConverter;
import org.apache.shindig.social.core.model.ActivityEntryImpl;
import org.apache.shindig.social.core.model.ActivityObjectImpl;
import org.apache.shindig.social.core.model.AlbumImpl;
import org.apache.shindig.social.core.model.MediaItemImpl;
import org.apache.shindig.social.opensocial.model.ActivityEntry;
import org.apache.shindig.social.opensocial.model.ActivityObject;
import org.apache.shindig.social.opensocial.model.Album;
import org.apache.shindig.social.opensocial.model.MediaItem;
import org.apache.shindig.social.opensocial.model.MediaItem.Type;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Module;
import com.google.inject.name.Names;

public class ActivitiesController {

	static final DateFormat ISO_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	static final String CACHE_FOLDER = Configurations.getPathConfig().getCachedFilesDirectoryPath();

	private static final FileSharingImpl sharing = FileSharingImpl.getSingleton();
	private static final DistributedHashTable dht = DistributedHashTable.getSingleton();
	private static final ActivitiesController theInstance = new ActivitiesController();

	private static BeanJsonConverter CONVERTER = new BeanJsonConverter(Guice.createInjector(new Module() {
		@Override
		public void configure(Binder b) {
			b.bind(BeanConverter.class).annotatedWith(Names.named("shindig.bean.converter.json")).to(BeanJsonConverter.class);
		}
	}));

	public static ActivitiesController getSingleton() {
		return theInstance;
	}

	private ActivitiesController() {
		ISO_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	/**
	 * Fetched the user ActivityStream from the User's DB
	 * 
	 * @param user
	 * @return
	 */
	static public List<ActivityEntry> getFeed(String user) {
		List<ActivityEntry> result = new ArrayList<ActivityEntry>();
		System.out.println("Getting feed: " + user);
		JSONObject record = dht.getRecord(user);
		if (record != null) {
			try {
				String latestHash = FileSharingImpl.getHashFromMagnetURI(record.getString("uri"));

				File dbFile = new File(CACHE_FOLDER + File.separator + latestHash + ".json");
				if (!dbFile.exists() && record.has("prev")) {
					latestHash = FileSharingImpl.getHashFromMagnetURI(record.getString("prev"));
					dbFile = new File(CACHE_FOLDER + File.separator + latestHash + ".json");
				}
				if (dbFile.exists()) {
					System.out.println("Getting feed: " + dbFile.getAbsolutePath());
					JSONObject db = new JSONObject(new JSONTokener(new FileReader(dbFile)));

					JSONArray items = db.getJSONArray("items");
					for (int i = 0; i < items.length(); ++i) {
						JSONObject item = items.getJSONObject(i);
						ActivityEntry entry = (ActivityEntry) CONVERTER.convertToObject(item, ActivityEntry.class);
						result.add(entry);
					}
					System.out.println("Feed loaded");
				} else {
					System.out.println("Feed not found");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * Adds a new message / attachment to the user's ActivityStream (verb
	 * "POST")
	 * 
	 * @param userId
	 * @param text
	 * @param attachment
	 */
	public void addFeedEntry(String userId, String text, File attachment) {
		try {
			String hash = sharing.hash(text);
			File textFile = new File(CACHE_FOLDER + File.separator + hash + ".txt");

			FileWriter w = new FileWriter(textFile);
			w.write(text);
			w.close();

			String textUri = sharing.seed(textFile);
			String attachmentUri = null;
			if (attachment != null) {
				attachmentUri = sharing.seed(attachment);
			}

			final String publishDate = ISO_DATE_FORMAT.format(new Date());

			UserData userData = sharing.getUserData(userId);
			userData.addFeedEntry(text, textUri, attachmentUri, publishDate);

			String dbUri = this.sharing.seedUserData(userData);

			DistributedHashTable.getSingleton().store(userId, userId, dbUri, publishDate);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * public String seedActivityStream(String userId, final List<ActivityEntry>
	 * feed) throws JSONException, IOException { final File feedFile = new
	 * File(CACHE_FOLDER + File.separator + userId + ".json");
	 * 
	 * JSONArray items = new JSONArray(); for (int i = 0; i < feed.size(); ++i)
	 * { JSONObject item = new JSONObject(feed.get(i)); items.put(item); }
	 * JSONObject db = new JSONObject();
	 * 
	 * db.put("items", items);
	 * 
	 * FileWriter writer = new FileWriter(feedFile); db.write(writer);
	 * writer.close();
	 * 
	 * String feedUri = sharing.seed(feedFile); return feedUri; }
	 */

}
