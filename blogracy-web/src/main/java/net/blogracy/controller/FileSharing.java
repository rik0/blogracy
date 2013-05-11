/*
 * Copyright (c)  2011 Enrico Franchi, Michele Tomaiuolo and University of Parma.
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
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

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
import net.blogracy.errors.PhotoAlbumDuplicated;
import net.blogracy.model.hashes.Hashes;
import net.blogracy.model.users.User;
import net.blogracy.model.users.UserData;
import net.blogracy.model.users.UserDataImpl;
import net.blogracy.model.users.Users;
import net.blogracy.util.FileExtensionConverter;
import net.blogracy.util.FileUtils;
import net.blogracy.util.JsonWebSignature;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.shindig.protocol.conversion.BeanConverter;
import org.apache.shindig.protocol.conversion.BeanJsonConverter;
import org.apache.shindig.social.opensocial.model.ActivityEntry;
import org.apache.shindig.social.opensocial.model.Album;
import org.apache.shindig.social.opensocial.model.MediaItem;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Module;
import com.google.inject.name.Names;

/**
 * Generic functions to manipulate feeds are defined in this class.
 */
public class FileSharing {

	private ConnectionFactory connectionFactory;
	private Connection downloadConnection;
	private Connection seedConnection;
	private Session downloadSession;
	private Session seedSession;
	private Destination seedQueue;
	private Destination downloadQueue;
	private MessageProducer downloadProducer;
	private MessageProducer seedProducer;

	static final DateFormat ISO_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	static final String CACHE_FOLDER = Configurations.getPathConfig().getCachedFilesDirectoryPath();

	private static final FileSharing theInstance = new FileSharing();
	private static final ActivitiesController activitiesController = ActivitiesController.getSingleton();

	private static BeanJsonConverter CONVERTER = new BeanJsonConverter(Guice.createInjector(new Module() {
		@Override
		public void configure(Binder b) {
			b.bind(BeanConverter.class).annotatedWith(Names.named("shindig.bean.converter.json")).to(BeanJsonConverter.class);
		}
	}));

	public static FileSharing getSingleton() {
		return theInstance;
	}

	public static String hash(String text) {
		String result = null;
		result = Hashes.hash(text);

		return result;
	}

	public static String hash(File file) {
		String result = null;
		try {
			result = Hashes.hash(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	private FileSharing() {
		ISO_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
		try {
			connectionFactory = new ActiveMQConnectionFactory(ActiveMQConnection.DEFAULT_BROKER_URL);
			downloadConnection = connectionFactory.createConnection();
			downloadConnection.start();
			seedConnection = connectionFactory.createConnection();
			seedConnection.start();

			downloadSession = downloadConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			downloadProducer = downloadSession.createProducer(null);
			downloadProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
			downloadQueue = downloadSession.createQueue("download");

			seedSession = seedConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			seedProducer = seedSession.createProducer(null);
			seedProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
			seedQueue = seedSession.createQueue("seed");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Seeds a file via BitTorrent protocol
	 * 
	 * @param file
	 *            file to be seeded
	 * @return the magnet link uri of the shared file
	 */
	public String seed(File file) {
		String uri = null;
		try {
			Destination tempDest = seedSession.createTemporaryQueue();
			MessageConsumer responseConsumer = seedSession.createConsumer(tempDest);

			JSONObject requestObj = new JSONObject();
			requestObj.put("file", file.getAbsolutePath());

			TextMessage request = seedSession.createTextMessage();
			request.setText(requestObj.toString());
			request.setJMSReplyTo(tempDest);
			seedProducer.send(seedQueue, request);

			TextMessage response = (TextMessage) responseConsumer.receive();
			String msgText = ((TextMessage) response).getText();
			JSONObject responseObj = new JSONObject(msgText);
			uri = responseObj.getString("uri");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return uri;
	}

	public static String getHashFromMagnetURI(String uri) {
		String hash = null;
		int btih = uri.indexOf("xt=urn:btih:");
		if (btih >= 0) {
			hash = uri.substring(btih + "xt=urn:btih:".length());
			int amp = hash.indexOf('&');
			if (amp >= 0)
				hash = hash.substring(0, amp);
		}
		return hash;
	}

	/***
	 * Adds a file to the bittorrent download queue. The file is downloaded to
	 * the CACHE_FOLDER and it will be named after the file hash
	 * 
	 * @param uri
	 *            the file's magnet-uri
	 */
	public  void download(final String uri) {
		String hash = getHashFromMagnetURI(uri);
		downloadByHash(hash, null, null);
	}

	public void download(final String uri, final String ext) {
		String hash = getHashFromMagnetURI(uri);
		downloadByHash(hash, ext, null);
	}

	/***
	 * Adds a file to the bittorrent download queue. The file is downloaded to
	 * the CACHE_FOLDER and it will be named after the file hash. When the
	 * download completes FileSharingDownloadListener.onFileDownloaded(String
	 * filePath) method is called.
	 * 
	 * @param uri
	 *            the file's magnet-uri
	 * @param donwloadCompleteListener
	 *            a listener for the completion event of this download
	 */
	public void download(final String uri, final String ext, final FileSharingDownloadListener downloadCompleteListener) {
		String hash = getHashFromMagnetURI(uri);
		downloadByHash(hash, ext, downloadCompleteListener);
	}

	/***
	 * Adds a file to the bittorrent download queue. The file is downloaded to
	 * the CACHE_FOLDER and it will be named after the file hash
	 * 
	 * @param hash
	 *            the file hash
	 */
	public  void downloadByHash(final String hash) {
		downloadByHash(hash, null, null);
	}

	/***
	 * Adds a file to the bittorrent download queue. The file is downloaded to
	 * the CACHE_FOLDER and it will be named after the file hash. When the
	 * download completes FileSharingDownloadListener.onFileDownloaded(String
	 * filePath) method is called.
	 * 
	 * @param hash
	 *            the file hash
	 * @param donwloadCompleteListener
	 *            a listener for the completion event of this download
	 */
	public void downloadByHash(final String hash, final String ext, final FileSharingDownloadListener downloadCompleteListener) {
		try {
			Destination tempDest = downloadSession.createTemporaryQueue();
			MessageConsumer responseConsumer = downloadSession.createConsumer(tempDest);
			if (downloadCompleteListener != null) {
				responseConsumer.setMessageListener(new MessageListener() {
					@Override
					public void onMessage(Message response) {
						try {
							String msgText = ((TextMessage) response).getText();
							JSONObject keyValue = new JSONObject(msgText);
							String fileFullPath = keyValue.getString("file");

							if (downloadCompleteListener != null)
								downloadCompleteListener.onFileDownloaded(fileFullPath);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
			} else {
				responseConsumer.setMessageListener(new MessageListener() {
					@Override
					public void onMessage(Message response) {
					}
				});
			}

			String file = CACHE_FOLDER + File.separator + hash;
			if (ext != null)
				file += ext;
			JSONObject sharedFile = new JSONObject();
			sharedFile.put("uri", "magnet:?xt=urn:btih:" + hash);
			sharedFile.put("file", file);

			TextMessage message = downloadSession.createTextMessage();
			message.setText(sharedFile.toString());
			if (tempDest != null)
				message.setJMSReplyTo(tempDest);
			downloadProducer.send(downloadQueue, message);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Gets the user data (ActivityStream, Albums and MediaItems) from the
	 * user's db
	 * 
	 * @param userId
	 * @return
	 */
	public static UserData getUserData(String userId) {
		if (userId == null)
			throw new InvalidParameterException("userId cannot be null");

		User user = null;

		if (userId.equals(Configurations.getUserConfig().getUser().getHash().toString()))
			user = Configurations.getUserConfig().getUser();
		else {
			// The right user should be searched in the user's friends
			user = Configurations.getUserConfig().getFriend(userId);

			// This shouldn't happen in current implementation, but anyway a new
			// user with the requested userHash is built
			if (user == null)
				user = Users.newUser(Hashes.fromString(userId));
		}

		UserDataImpl userData = new UserDataImpl(user);
		userData.setActivityStream(ActivitiesController.getFeed(userId));
		userData.setAlbums(MediaController.getAlbums(userId));
		userData.setMediaItems(MediaController.getMediaItems(userId));
		userData.setUserPublicKey(getPublicKey(userId));
		return userData;
	}

	public String seedUserData(final UserData userData) throws JSONException, IOException {
		final File feedFile = new File(CACHE_FOLDER + File.separator + userData.getUser().getHash().toString() + ".json");

		List<ActivityEntry> feed = userData.getActivityStream();

		JSONArray items = new JSONArray();
		for (int i = 0; i < feed.size(); ++i) {
			JSONObject item = new JSONObject(feed.get(i));
			items.put(item);
		}

		List<Album> albums = userData.getAlbums();
		JSONArray albumsData = new JSONArray();
		for (int i = 0; i < albums.size(); ++i) {
			JSONObject item = new JSONObject(CONVERTER.convertToString(albums.get(i)));
			albumsData.put(item);
		}

		List<MediaItem> mediaItems = userData.getMediaItems();
		JSONArray mediaItemsData = new JSONArray();
		for (int i = 0; i < mediaItems.size(); ++i) {
			JSONObject item = new JSONObject(CONVERTER.convertToString(mediaItems.get(i)));
			mediaItemsData.put(item);
		}

		JSONObject db = new JSONObject();

		db.put("albums", albumsData);
		db.put("mediaItems", mediaItemsData);
		db.put("items", items);
		db.put("publicKey", userData.getUserPublicKey());

		FileWriter writer = new FileWriter(feedFile);
		db.write(writer);
		writer.close();

		String feedUri = seed(feedFile);
		return feedUri;
	}

	public static String getPublicKey(String userId) {
		if (userId == null)
			throw new InvalidParameterException("userId cannot be null");

		if (userId.equals(Configurations.getUserConfig().getUser().getHash().toString()))
			return JsonWebSignature.getPublicKeyString(Configurations.getUserConfig().getUserKeyPair().getPublic());

		String pKey = null;
		try {
			JSONObject recordDb = DistributedHashTable.getSingleton().getRecord(userId);

			if (recordDb == null)
				return pKey;

			String latestHash = FileSharing.getHashFromMagnetURI(recordDb.getString("uri"));

			if (latestHash == null)
				return pKey;

			File dbFile = new File(CACHE_FOLDER + File.separator + latestHash + ".json");

			System.out.println("Getting user publicKey data: " + dbFile.getAbsolutePath());
			JSONObject db = new JSONObject(new JSONTokener(new FileReader(dbFile)));

			if (db.has("publicKey"))
				pKey = db.getString("publicKey");
			else
				pKey = null;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return pKey;

	}

}
