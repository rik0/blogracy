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
	private Connection connection;
	private Session seedSession;
	private Session downloadSession;
	private Destination seedQueue;
	private Destination downloadQueue;
	private MessageProducer seedProducer;
	private MessageProducer downloadProducer;
	private MessageConsumer consumer;

	static final DateFormat ISO_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	static final String CACHE_FOLDER = Configurations.getPathConfig().getCachedFilesDirectoryPath();

	private static final FileSharing THE_INSTANCE = new FileSharing();

	private static BeanJsonConverter CONVERTER = new BeanJsonConverter(Guice.createInjector(new Module() {
		@Override
		public void configure(Binder b) {
			b.bind(BeanConverter.class).annotatedWith(Names.named("shindig.bean.converter.json")).to(BeanJsonConverter.class);
		}
	}));

	public static FileSharing getSingleton() {
		return THE_INSTANCE;
	}

	public static String hash(String text) {
		String result = null;
		try {
			result = Hashes.hash(text);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static String hash(File file) {
		String result = null;
		try {
			result = Hashes.hash(file);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	public FileSharing() {
		ISO_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
		try {
			connectionFactory = new ActiveMQConnectionFactory(ActiveMQConnection.DEFAULT_BROKER_URL);
			connection = connectionFactory.createConnection();
			connection.start();
			seedSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			downloadSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			seedProducer = seedSession.createProducer(null);
			seedProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
			seedQueue = seedSession.createQueue("seed");
			downloadProducer = downloadSession.createProducer(null);
			downloadProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
			downloadQueue = downloadSession.createQueue("download");
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

	/**
	 * Fetched the user ActivityStream from the User's DB
	 * 
	 * @param user
	 * @return
	 */
	static public List<ActivityEntry> getFeed(String user) {
		List<ActivityEntry> result = new ArrayList<ActivityEntry>();
		System.out.println("Getting feed: " + user);
		JSONObject record = DistributedHashTable.getSingleton().getRecord(user);
		if (record != null) {
			try {
				String latestHash = FileSharing.getHashFromMagnetURI(record.getString("uri"));

				File dbFile = new File(CACHE_FOLDER + File.separator + latestHash + ".json");
				if (!dbFile.exists() && record.has("prev")) {
					latestHash = FileSharing.getHashFromMagnetURI(record.getString("prev"));
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
		userData.setActivityStream(getFeed(userId));
		userData.setAlbums(getAlbums(userId));
		userData.setMediaItems(getMediaItems(userId));
		userData.setUserPublicKey(getPublicKey(userId));
		return userData;
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
			String hash = hash(text);
			File textFile = new File(CACHE_FOLDER + File.separator + hash + ".txt");

			FileWriter w = new FileWriter(textFile);
			w.write(text);
			w.close();

			String textUri = seed(textFile);
			String attachmentUri = null;
			if (attachment != null) {
				attachmentUri = seed(attachment);
			}

			final String publishDate = ISO_DATE_FORMAT.format(new Date());

			UserData userData = getUserData(userId);
			userData.addFeedEntry(text, textUri, attachmentUri, publishDate);

			String dbUri = this.seedUserData(userData);

			DistributedHashTable.getSingleton().store(userId, dbUri, publishDate);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create a new Album for the user. Adds the album to the user's recordDB
	 * entry Adds the action to the ActivityStream (verb: create)
	 * 
	 * @param userId
	 * @param photoAlbumName
	 * @throws PhotoAlbumDuplicated
	 */
	public synchronized String createPhotoAlbum(String userId, String photoAlbumTitle) throws PhotoAlbumDuplicated {
		if (userId == null)
			throw new InvalidParameterException("userId cannot be null");

		if (photoAlbumTitle == null || photoAlbumTitle.isEmpty())
			return null;

		UserData userData = getUserData(userId);

		for (Album existingAlbum : userData.getAlbums())
			if (existingAlbum.getTitle().equals(photoAlbumTitle))
				throw new PhotoAlbumDuplicated(photoAlbumTitle);

		String albumHash = null;
		try {
			final String publishedDate = ISO_DATE_FORMAT.format(new Date());

			albumHash = userData.createPhotoAlbum(photoAlbumTitle, publishedDate);
			String dbUri = this.seedUserData(userData);

			DistributedHashTable.getSingleton().store(userId, dbUri, publishedDate);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return albumHash;
	}

	/**
	 * Gets the photo albums from the user's 'mediaUri' file given the userId.
	 * 
	 * @param userId
	 */
	public static List<Album> getAlbums(String userId) {
		if (userId == null)
			throw new InvalidParameterException("userId cannot be null");

		List<Album> albums = new ArrayList<Album>();
		try {
			JSONObject recordDb = DistributedHashTable.getSingleton().getRecord(userId);

			if (recordDb == null)
				return albums;

			String latestHash = FileSharing.getHashFromMagnetURI(recordDb.getString("uri"));

			if (latestHash == null)
				return albums;

			File dbFile = new File(CACHE_FOLDER + File.separator + latestHash + ".json");
			System.out.println("Getting album data: " + dbFile.getAbsolutePath());
			JSONObject db = new JSONObject(new JSONTokener(new FileReader(dbFile)));

			JSONArray albumArray = db.optJSONArray("albums");

			if (albumArray != null) {
				for (int i = 0; i < albumArray.length(); ++i) {
					JSONObject singleAlbumObject = albumArray.getJSONObject(i);
					Album entry = (Album) CONVERTER.convertToObject(singleAlbumObject, Album.class);
					albums.add(entry);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return albums;
	}

	/**
	 * Get the images from user's 'mediaUri' file given the userId and the
	 * associated albumId
	 * 
	 * @param userId
	 * @param albumId
	 * @return
	 */
	public static List<MediaItem> getMediaItems(String userId, String albumId) {
		if (userId == null)
			throw new InvalidParameterException("userId cannot be null");

		if (albumId == null)
			throw new InvalidParameterException("albumId cannot be null");

		List<MediaItem> mediaItems = new ArrayList<MediaItem>();

		try {
			JSONObject recordDb = DistributedHashTable.getSingleton().getRecord(userId);

			if (recordDb == null)
				return mediaItems;

			String latestHash = FileSharing.getHashFromMagnetURI(recordDb.getString("uri"));

			if (latestHash == null)
				return mediaItems;

			File dbFile = new File(CACHE_FOLDER + File.separator + latestHash + ".json");

			System.out.println("Getting media data: " + dbFile.getAbsolutePath());
			JSONObject db = new JSONObject(new JSONTokener(new FileReader(dbFile)));

			JSONArray mediaItemsArray = db.optJSONArray("mediaItems");

			if (mediaItemsArray != null) {
				for (int i = 0; i < mediaItemsArray.length(); ++i) {
					JSONObject singleAlbumObject = mediaItemsArray.getJSONObject(i);
					MediaItem entry = (MediaItem) CONVERTER.convertToObject(singleAlbumObject, MediaItem.class);

					if (entry.getAlbumId().equals(albumId))
						mediaItems.add(entry);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return mediaItems;
	}

	/**
	 * Gets the images from user's 'mediaUri' file given the userId and the
	 * associated albumId. Attempts to download the images from DHT. If
	 * successful, set's the URL with the cached image link
	 * 
	 * @param userId
	 * @param albumId
	 * @return
	 */
	public static List<MediaItem> getMediaItemsWithCachedImages(String userId, String albumId) {

		if (userId == null)
			throw new InvalidParameterException("userId cannot be null");

		if (albumId == null)
			throw new InvalidParameterException("albumId cannot be null");

		List<MediaItem> mediaItems = getMediaItems(userId, albumId);

		for (MediaItem item : mediaItems) {
			String itemMagneUri = item.getUrl();
			download(itemMagneUri);
			String extension = FileExtensionConverter.toDefaultExtension(item.getMimeType());
			if (extension != null && !extension.isEmpty())
				extension = "." + extension;
			File f = new File("cache/" + getHashFromMagnetURI(itemMagneUri) + extension);
			if (f.exists())
				item.setUrl("cache/" + getHashFromMagnetURI(itemMagneUri) + extension);
			else
				item.setUrl("cache/" + getHashFromMagnetURI(itemMagneUri));
		}

		return mediaItems;
	}

	/**
	 * Gets the image from user's 'mediaUri' file given the userId, the
	 * associated albumId, and media id. Attempts to download the images from
	 * DHT. If successful, set's the URL with the cached image link
	 * 
	 * @param userId
	 * @param albumId
	 * @param mediaItemId
	 * @return MediaItem data if found, null otherwise
	 */
	public static MediaItem getMediaItemWithCachedImage(String userId, String albumId, String mediaItemId) {

		if (userId == null)
			throw new InvalidParameterException("userId cannot be null");

		if (albumId == null)
			throw new InvalidParameterException("albumId cannot be null");

		List<MediaItem> mediaItems = getMediaItems(userId, albumId);

		for (MediaItem item : mediaItems) {
			if (item.getId().equals(mediaItemId)) {
				String itemMagneUri = item.getUrl();
				download(itemMagneUri);
				String extension = FileExtensionConverter.toDefaultExtension(item.getMimeType());
				if (extension != null && !extension.isEmpty())
					extension = "." + extension;
				File f = new File("cache/" + getHashFromMagnetURI(itemMagneUri) + extension);
				if (f.exists())
					item.setUrl("cache/" + getHashFromMagnetURI(itemMagneUri) + extension);
				else
					item.setUrl("cache/" + getHashFromMagnetURI(itemMagneUri));
				return item;
			}
		}
		return null;
	}

	/***
	 * Add multiple MediaItems to an album. It updates the user's 'mediaUri'
	 * file and notifies the action in the user's Activity Stream (verb: add)
	 * 
	 * @param userId
	 * @param albumId
	 * @param photos
	 * @return
	 */
	public synchronized List<String> addMediaItemsToAlbum(String userId, String albumId, Map<File, String> photos) {
		if (photos == null)
			return null;

		if (userId == null)
			throw new InvalidParameterException("userId cannot be null");

		if (albumId == null)
			throw new InvalidParameterException("albumId cannot be null");

		UserData userData = getUserData(userId);

		Album album = null;
		for (Album a : userData.getAlbums()) {
			if (a.getId().equals(albumId)) {
				album = a;
				break;
			}
		}

		if (album == null)
			throw new InvalidParameterException("AlbumId " + albumId + " does not match to a valid album for the user " + userId);

		List<String> hashList = new ArrayList<String>();

		final String publishedDate = ISO_DATE_FORMAT.format(new Date());

		try {

			for (Entry<File, String> mapEntry : photos.entrySet()) {
				File photo = mapEntry.getKey();
				String mimeType = mapEntry.getValue();
				String extension = FileExtensionConverter.toDefaultExtension(mimeType);
				if (extension != null && !extension.isEmpty())
					extension = "." + extension;
				String fileHash = hash(photo);

				final File photoCachedFile = new File(CACHE_FOLDER + File.separator + fileHash + extension);

				FileUtils.copyFile(photo, photoCachedFile);
				photo.delete();

				final String fileUrl = this.seed(photoCachedFile);

				userData.addMediaItemToAlbum(albumId, fileUrl, getHashFromMagnetURI(fileUrl), mimeType, publishedDate);

				hashList.add(getHashFromMagnetURI(fileUrl));
			}

			String dbUri = this.seedUserData(userData);

			DistributedHashTable.getSingleton().store(userId, dbUri, publishedDate);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return hashList;
	}

	/***
	 * Adds a single MediaItem file to an Album. It updates the user's
	 * 'mediaUri' file and notifies the action in the user's Activity Stream
	 * (verb: remove)
	 * 
	 * @param userId
	 * @param albumId
	 * @param photo
	 * @param mimeType
	 * @return
	 */
	public synchronized String addMediaItemToAlbum(String userId, String albumId, File photo, String mimeType) {
		Map<File, String> map = new HashMap<File, String>();
		map.put(photo, mimeType);
		List<String> hashes = this.addMediaItemsToAlbum(userId, albumId, map);

		return (hashes != null && !hashes.isEmpty()) ? hashes.get(0) : null;
	}

	/***
	 * A Media Item is removed from an album
	 * 
	 * @param userId
	 * @param albumId
	 * @param mediaId
	 */
	public synchronized void deletePhotoFromAlbum(String userId, String albumId, String mediaId) {
		if (mediaId == null)
			throw new InvalidParameterException("mediaId cannot be null");

		if (userId == null)
			throw new InvalidParameterException("userId cannot be null");

		if (albumId == null)
			throw new InvalidParameterException("albumId cannot be null");

		UserData userData = getUserData(userId);

		Album album = null;
		for (Album a : userData.getAlbums()) {
			if (a.getId().equals(albumId)) {
				album = a;
				break;
			}
		}

		if (album == null)
			throw new InvalidParameterException("AlbumId " + albumId + " does not correspond to a valid album for the user " + userId);

		final String publishedDate = ISO_DATE_FORMAT.format(new Date());

		try {

			userData.removeMediaItem(mediaId, albumId, publishedDate);
			String dbUri = this.seedUserData(userData);

			DistributedHashTable.getSingleton().store(userId, dbUri, publishedDate);

		} catch (Exception e) {
			e.printStackTrace();

		}
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

	static String getHashFromMagnetURI(String uri) {
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
	public static void download(final String uri) {
		download(uri, null, null);
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
	public static void download(final String uri, final String ext, final FileSharingDownloadListener downloadCompleteListener) {
		String hash = getHashFromMagnetURI(uri);
		THE_INSTANCE.downloadByHash(hash, ext, downloadCompleteListener);
	}

	/***
	 * Adds a file to the bittorrent download queue. The file is downloaded to
	 * the CACHE_FOLDER and it will be named after the file hash
	 * 
	 * @param hash
	 *            the file hash
	 */
	public void downloadByHash(final String hash) {
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
							String fileFullPath = keyValue.getString("fileFullPath");

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

	/***
	 * Gets all the mediaItems for the user. This shall not be used by
	 * UserInterface, use getMediaItems(userId, albumId) instead.
	 * 
	 * @param userId
	 * @return list of the user's MediaItem
	 */
	protected static List<MediaItem> getMediaItems(String userId) {
		if (userId == null)
			throw new InvalidParameterException("userId cannot be null");

		List<MediaItem> mediaItems = new ArrayList<MediaItem>();

		try {
			JSONObject recordDb = DistributedHashTable.getSingleton().getRecord(userId);

			if (recordDb == null)
				return mediaItems;

			String latestHash = FileSharing.getHashFromMagnetURI(recordDb.getString("uri"));

			if (latestHash == null)
				return mediaItems;

			File dbFile = new File(CACHE_FOLDER + File.separator + latestHash + ".json");

			System.out.println("Getting media data: " + dbFile.getAbsolutePath());
			JSONObject db = new JSONObject(new JSONTokener(new FileReader(dbFile)));

			JSONArray mediaItemsArray = db.optJSONArray("mediaItems");

			if (mediaItemsArray != null) {
				for (int i = 0; i < mediaItemsArray.length(); ++i) {
					JSONObject singleAlbumObject = mediaItemsArray.getJSONObject(i);
					MediaItem entry = (MediaItem) CONVERTER.convertToObject(singleAlbumObject, MediaItem.class);
					mediaItems.add(entry);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return mediaItems;
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
