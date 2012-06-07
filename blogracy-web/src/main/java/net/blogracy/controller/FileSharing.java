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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import net.blogracy.config.Configurations;
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

/**
 * Generic functions to manipulate feeds are defined in this class.
 */
public class FileSharing {

	private ConnectionFactory connectionFactory;
	private Connection connection;
	private Session session;
	private Destination seedQueue;
	private Destination downloadQueue;
	private MessageProducer producer;
	private MessageConsumer consumer;

	static final DateFormat ISO_DATE_FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss");
	static final String CACHE_FOLDER = Configurations.getPathConfig()
			.getCachedFilesDirectoryPath();

	private static final FileSharing THE_INSTANCE = new FileSharing();

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

	public static FileSharing getSingleton() {
		return THE_INSTANCE;
	}

	public static String hash(String text) {
		String result = null;
		try {
			MessageDigest digester = MessageDigest.getInstance("SHA-1");
			Base32 encoder = new Base32();
			byte[] digest = digester.digest(text.getBytes());
			result = encoder.encodeAsString(digest);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static String hash(File file) {
		String result = null;
		try {
			MessageDigest digester = MessageDigest.getInstance("SHA-1");
			Base32 encoder = new Base32();
			byte[] digest = digester.digest(FileUtils.getBytesFromFile(file));
			result = encoder.encodeAsString(digest);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	public FileSharing() {
		try {
			connectionFactory = new ActiveMQConnectionFactory(
					ActiveMQConnection.DEFAULT_BROKER_URL);
			connection = connectionFactory.createConnection();
			connection.start();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			producer = session.createProducer(null);
			producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
			seedQueue = session.createQueue("seed");
			downloadQueue = session.createQueue("download");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String seed(File file) {
		String uri = null;
		try {
			Destination tempDest = session.createTemporaryQueue();
			MessageConsumer responseConsumer = session.createConsumer(tempDest);

			JSONObject requestObj = new JSONObject();
			requestObj.put("file", file.getAbsolutePath());

			TextMessage request = session.createTextMessage();
			request.setText(requestObj.toString());
			request.setJMSReplyTo(tempDest);
			producer.send(seedQueue, request);

			TextMessage response = (TextMessage) responseConsumer.receive();
			String msgText = ((TextMessage) response).getText();
			JSONObject responseObj = new JSONObject(msgText);
			uri = responseObj.getString("uri");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return uri;
	}

	static public List<ActivityEntry> getFeed(String user) {
		List<ActivityEntry> result = new ArrayList<ActivityEntry>();
		System.out.println("Getting feed: " + user);
		JSONObject record = DistributedHashTable.getSingleton().getRecord(user);
		if (record != null) {
			try {
				String latestHash = FileSharing.getHashFromMagnetURI(record
						.getString("uri"));
				File dbFile = new File(CACHE_FOLDER + File.separator
						+ latestHash + ".json");
				System.out.println("Getting feed: " + dbFile.getAbsolutePath());
				JSONObject db = new JSONObject(new JSONTokener(new FileReader(
						dbFile)));

				JSONArray items = db.getJSONArray("items");
				for (int i = 0; i < items.length(); ++i) {
					JSONObject item = items.getJSONObject(i);
					ActivityEntry entry = (ActivityEntry) CONVERTER
							.convertToObject(item, ActivityEntry.class);
					result.add(entry);
				}
				System.out.println("Feed loaded");
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Feed created");
			}
		}
		return result;
	}

	public void addFeedEntry(String id, String text, File attachment) {
		try {
			String hash = hash(text);
			File textFile = new File(CACHE_FOLDER + File.separator + hash
					+ ".txt");

			FileWriter w = new FileWriter(textFile);
			w.write(text);
			w.close();

			String textUri = seed(textFile);
			String attachmentUri = null;
			if (attachment != null) {
				attachmentUri = seed(attachment);
			}

			final List<ActivityEntry> feed = getFeed(id);
			final ActivityEntry entry = new ActivityEntryImpl();
			entry.setVerb("post");
			entry.setUrl(textUri);
			entry.setPublished(ISO_DATE_FORMAT.format(new Date()));
			entry.setContent(text);
			if (attachment != null) {
				ActivityObject enclosure = new ActivityObjectImpl();
				enclosure.setUrl(attachmentUri);
				entry.setObject(enclosure);
			}
			feed.add(0, entry);
			String feedUri = seedActivityStream(id, feed);
			DistributedHashTable.getSingleton().store(id, feedUri,
					entry.getPublished());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create a new Album for the user.
	 * Adds the album to the user's recordDB entry
	 * Adds the action  to the ActivityStream (verb: create)
	 * 
	 * @param userId
	 * @param photoAlbumName
	 */
	public synchronized String createPhotoAlbum(String userId, String photoAlbumTitle) {
		if (userId == null)
			throw new InvalidParameterException("userId cannot be null");

		if (photoAlbumTitle == null || photoAlbumTitle.isEmpty())
			return null;

		String albumHash = null;
		try {
			albumHash = hash(userId + photoAlbumTitle);
			Album album = new AlbumImpl();
			album.setTitle(photoAlbumTitle);
			album.setId(albumHash);
			album.setOwnerId(userId);
			List<Type> types = new ArrayList<Type>();
			types.add(Type.IMAGE);
			album.setMediaType(types);
			// Album is empty where created
			album.setMediaItemCount(0);

			final List<ActivityEntry> feed = getFeed(userId);
			final ActivityEntry entry = new ActivityEntryImpl();
			entry.setVerb("create");
			ActivityObject mediaAlbumObject = new ActivityObjectImpl();
			mediaAlbumObject.setObjectType("collection");
			mediaAlbumObject.setContent(photoAlbumTitle);
			entry.setObject(mediaAlbumObject);

			entry.setPublished(ISO_DATE_FORMAT.format(new Date()));
			entry.setContent(photoAlbumTitle);

			feed.add(0, entry);
			String feedUri = seedActivityStream(userId, feed);

			// Append another album into the user's recordDB
			JSONObject recordDb = DistributedHashTable.getSingleton()
					.getRecord(userId);

			if (recordDb == null)
				recordDb = new JSONObject();

			JSONArray albums = recordDb.optJSONArray("albums");

			if (albums != null) {
				// Simply append new album
				albums.put(new JSONObject(CONVERTER.convertToString(album)));
			} else {
				albums = new JSONArray();
				albums.put(new JSONObject(CONVERTER.convertToString(album)));
			}

			DistributedHashTable.getSingleton().store(userId, feedUri,
					entry.getPublished(), albums,
					recordDb.optJSONArray("mediaItems"));

		} catch (Exception e) {
			e.printStackTrace();
		}

		return albumHash;
	}

	/**
	 * Get the photo albums from recordDb given the userId.
	 * 
	 * @param userId
	 */
	public List<Album> getAlbums(String userId) {
		if (userId == null)
			throw new InvalidParameterException("userId cannot be null");

		List<Album> albums = new ArrayList<Album>();
		try {
			JSONObject recordDb = DistributedHashTable.getSingleton()
					.getRecord(userId);

			if (recordDb == null)
				return albums;

			JSONArray albumArray = recordDb.optJSONArray("albums");

			if (albumArray != null) {
				for (int i = 0; i < albumArray.length(); ++i) {
					JSONObject singleAlbumObject = albumArray.getJSONObject(i);
					Album entry = (Album) CONVERTER.convertToObject(
							singleAlbumObject, Album.class);
					albums.add(entry);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return albums;
	}

	/**
	 * Get the images from recordDb given the userId and the associated albumId
	 * 
	 * @param userId
	 * @param albumId
	 * @return
	 */
	public List<MediaItem> getMediaItems(String userId, String albumId) {
		if (userId == null)
			throw new InvalidParameterException("userId cannot be null");

		if (albumId == null)
			throw new InvalidParameterException("albumId cannot be null");

		List<MediaItem> mediaItems = new ArrayList<MediaItem>();

		try {
			JSONObject recordDb = DistributedHashTable.getSingleton()
					.getRecord(userId);

			if (recordDb == null)
				return mediaItems;

			JSONArray mediaItemsArray = recordDb.optJSONArray("mediaItems");

			if (mediaItemsArray != null) {
				for (int i = 0; i < mediaItemsArray.length(); ++i) {
					JSONObject singleAlbumObject = mediaItemsArray
							.getJSONObject(i);
					MediaItem entry = (MediaItem) CONVERTER.convertToObject(
							singleAlbumObject, MediaItem.class);

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
	 * Gets the images from recordDb given the userId and the associated albumId.
	 * Attempts to download the images from DHT. If successful, set's the URL
	 * with the cached image link
	 * 
	 * @param userId
	 * @param albumId
	 * @return
	 */
	public List<MediaItem> getMediaItemsWithCachedImages(String userId,
			String albumId) {

		if (userId == null)
			throw new InvalidParameterException("userId cannot be null");

		if (albumId == null)
			throw new InvalidParameterException("albumId cannot be null");

		List<MediaItem> mediaItems = this.getMediaItems(userId, albumId);

		for (MediaItem item : mediaItems) {
			String itemMagneUri = item.getUrl();
			download(itemMagneUri);
			item.setUrl("cache/" + getHashFromMagnetURI(itemMagneUri));
		}

		return mediaItems;
	}

	/***
	 * Add multiple MediaItems to an album.
	 * It updates the user's recordDb and notifies the action in the user's Activity Stream (verb: add)
	 * @param userId
	 * @param albumId
	 * @param photos
	 * @return
	 */
	public synchronized List<String> addMediaItemsToAlbum(String userId, String albumId,
			Map<File, String> photos) {
		if (photos == null)
			return null;

		if (userId == null)
			throw new InvalidParameterException("userId cannot be null");

		if (albumId == null)
			throw new InvalidParameterException("albumId cannot be null");

		Album album = null;
		for (Album a : this.getAlbums(userId)) {
			if (a.getId().equals(albumId)) {
				album = a;
				break;
			}
		}

		if (album == null)
			throw new InvalidParameterException("AlbumId " + albumId
					+ " does not match to a valid album for the user " + userId);

		List<String> hashList = new ArrayList<String>();
		List<MediaItem> listOfMediaItems = new ArrayList<MediaItem>();

		final List<ActivityEntry> feed = getFeed(userId);
		final String publishedDate = ISO_DATE_FORMAT.format(new Date());

		try {

			for (Entry<File, String> mapEntry : photos.entrySet()) {
				File photo = mapEntry.getKey();
				String mimeType = mapEntry.getValue();
				String fileHash = hash(photo);

				final File photoCachedFile = new File(CACHE_FOLDER
						+ File.separator + fileHash);

				FileUtils.copyFile(photo, photoCachedFile);
				photo.delete();

				final String fileUrl = this.seed(photoCachedFile);

				final ActivityEntry entry = new ActivityEntryImpl();
				entry.setVerb("add");
				entry.setPublished(publishedDate);
				entry.setContent(getHashFromMagnetURI(fileUrl));

				ActivityObject mediaItemObject = new ActivityObjectImpl();
				mediaItemObject.setObjectType("image");
				mediaItemObject.setContent(getHashFromMagnetURI(fileUrl));
				mediaItemObject.setUrl(fileUrl);
				entry.setObject(mediaItemObject);

				ActivityObject mediaAlbumObject = new ActivityObjectImpl();
				mediaAlbumObject.setObjectType("collection");
				mediaAlbumObject.setContent(album.getTitle());
				mediaAlbumObject.setId(album.getId());
				entry.setTarget(mediaAlbumObject);

				feed.add(0, entry);

				MediaItem mediaItem = new MediaItemImpl();
				mediaItem.setAlbumId(albumId);
				mediaItem.setId(getHashFromMagnetURI(fileUrl));
				mediaItem.setUrl(fileUrl);
				mediaItem.setLastUpdated(publishedDate);
				mediaItem.setMimeType(mimeType);
				
				if (album.getMediaMimeType() == null)
					album.setMediaMimeType(new ArrayList<String>());
				
				List<String> albumMimeTypes = album.getMediaMimeType();
				
				if(!albumMimeTypes.contains(mimeType))
					albumMimeTypes.add(mimeType);

				listOfMediaItems.add(mediaItem);
				hashList.add(getHashFromMagnetURI(fileUrl));
			}

			album.setMediaItemCount(album.getMediaItemCount() + photos.size());

			String feedUri = seedActivityStream(userId, feed);

			// Update the album accordingly
			JSONObject recordDb = DistributedHashTable.getSingleton()
					.getRecord(userId);

			if (recordDb == null)
				recordDb = new JSONObject();

			JSONArray albums = recordDb.optJSONArray("albums");

			for (int i = 0; i < albums.length(); ++i) {
				JSONObject singleAlbumObject = albums.getJSONObject(i);
				Album entry1 = (Album) CONVERTER.convertToObject(
						singleAlbumObject, Album.class);

				if (entry1.getId().equals(albumId)) {
					albums.put(i,
							new JSONObject(CONVERTER.convertToString(album)));
					break;
				}
			}

			// Add all the newly created mediaItems
			JSONArray mediaItems = recordDb.optJSONArray("mediaItems");

			if (mediaItems == null)
				mediaItems = new JSONArray();

			for (MediaItem mediaItem : listOfMediaItems) {
				// Simply append new album
				mediaItems.put(new JSONObject(CONVERTER
						.convertToString(mediaItem)));
			}

			DistributedHashTable.getSingleton().store(userId, feedUri,
					publishedDate, albums, mediaItems);

			return hashList;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/***
	 * Adds a single MediaItem file to an Album. 
	 * It updates the user's recordDb and notifies the action in the user's Activity Stream (verb: remove)
	 * @param userId
	 * @param albumId
	 * @param photo
	 * @param mimeType
	 * @return
	 */
	public synchronized String addMediaItemToAlbum(String userId, String albumId,
			File photo, String mimeType) {
		Map<File, String> map = new HashMap<File, String>();
		map.put(photo, mimeType);
		List<String> hashes = this.addMediaItemsToAlbum(userId, albumId, map);

		return (hashes != null && !hashes.isEmpty()) ? hashes.get(0) : null;
	}

	/***
	 * A Media Item is removed from an album
	 * @param userId
	 * @param albumId
	 * @param mediaId
	 */
	public synchronized void deletePhotoFromAlbum(String userId, String albumId,
			String mediaId) {
		if (mediaId == null)
			throw new InvalidParameterException("mediaId cannot be null");

		if (userId == null)
			throw new InvalidParameterException("userId cannot be null");

		if (albumId == null)
			throw new InvalidParameterException("albumId cannot be null");

		Album album = null;
		for (Album a : this.getAlbums(userId)) {
			if (a.getId().equals(albumId)) {
				album = a;
				break;
			}
		}

		if (album == null)
			throw new InvalidParameterException("AlbumId " + albumId
					+ " does not correspond to a valid album for the user "
					+ userId);
		try {

			List<MediaItem> mediaItems = this.getMediaItems(userId, albumId);

			for (Iterator<MediaItem> iter = mediaItems.iterator(); iter
					.hasNext();) {
				MediaItem mediaItem = iter.next();
				if (mediaId.equals(mediaItem.getId())
						&& albumId.equals(mediaItem.getAlbumId()))
					iter.remove();
			}

			album.setMediaItemCount(mediaItems.size());

			final List<ActivityEntry> feed = getFeed(userId);
			final ActivityEntry entry = new ActivityEntryImpl();
			entry.setVerb("remove");
			entry.setPublished(ISO_DATE_FORMAT.format(new Date()));
			entry.setContent(mediaId);

			ActivityObject mediaItemObject = new ActivityObjectImpl();
			mediaItemObject.setObjectType("image");
			mediaItemObject.setContent(mediaId);
			entry.setObject(mediaItemObject);

			ActivityObject mediaAlbumObject = new ActivityObjectImpl();
			mediaAlbumObject.setObjectType("collection");
			mediaAlbumObject.setContent(album.getTitle());
			mediaAlbumObject.setId(album.getId());
			entry.setTarget(mediaAlbumObject);

			feed.add(0, entry);
			String feedUri = seedActivityStream(userId, feed);

			JSONObject recordDb = DistributedHashTable.getSingleton()
					.getRecord(userId);

			if (recordDb == null)
				recordDb = new JSONObject();

			JSONArray albums = recordDb.optJSONArray("albums");

			// update albums
			if (albums != null) {
				for (int i = 0; i < albums.length(); ++i) {
					JSONObject singleAlbumObject = albums.getJSONObject(i);
					Album entry1 = (Album) CONVERTER.convertToObject(
							singleAlbumObject, Album.class);

					if (entry1.getId().equals(albumId)) {
						albums.put(
								i,
								new JSONObject(CONVERTER.convertToString(album)));
						break;
					}
				}
			}

			JSONArray list = new JSONArray();
			JSONArray mediaItemsArray = recordDb.optJSONArray("mediaItems");
			if (mediaItemsArray != null) {
				for (int i = 0; i < mediaItemsArray.length(); ++i) {
					JSONObject singleMediaItemObject = mediaItemsArray
							.getJSONObject(i);
					MediaItem entry1 = (MediaItem) CONVERTER.convertToObject(
							singleMediaItemObject, MediaItem.class);
					if (!mediaId.equals(entry1.getId())
							|| !albumId.equals(entry1.getAlbumId()))
						list.put(singleMediaItemObject);
				}
			}

			DistributedHashTable.getSingleton().store(userId, feedUri,
					entry.getPublished(), albums, list);

		} catch (Exception e) {
			e.printStackTrace();

		}
	}

	private String seedActivityStream(String userId,
			final List<ActivityEntry> feed) throws JSONException, IOException {
		final File feedFile = new File(CACHE_FOLDER + File.separator + userId
				+ ".json");

		JSONArray items = new JSONArray();
		for (int i = 0; i < feed.size(); ++i) {
			JSONObject item = new JSONObject(feed.get(i));
			items.put(item);
		}
		JSONObject db = new JSONObject();

		db.put("items", items);

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

	public void download(final String uri) {
		String hash = getHashFromMagnetURI(uri);
		downloadByHash(hash);
	}

	public void downloadByHash(final String hash) {
		try {
			JSONObject sharedFile = new JSONObject();
			sharedFile.put("uri", "magnet:?xt=urn:btih:" + hash);
			sharedFile.put("file", CACHE_FOLDER + File.separator + hash);

			TextMessage message = session.createTextMessage();
			message.setText(sharedFile.toString());
			producer.send(downloadQueue, message);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
