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
import net.blogracy.errors.PhotoAlbumDuplicated;
import net.blogracy.model.users.UserData;
import net.blogracy.util.FileExtensionConverter;
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

public class MediaController {

	static final DateFormat ISO_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	static final String CACHE_FOLDER = Configurations.getPathConfig().getCachedFilesDirectoryPath();

	private static final MediaController theInstance = new MediaController();
	private static final FileSharingImpl sharing = FileSharingImpl.getSingleton();
	private static final ActivitiesController activities = ActivitiesController.getSingleton();
	private static final DistributedHashTable dht = DistributedHashTable.getSingleton();

	private static BeanJsonConverter CONVERTER = new BeanJsonConverter(Guice.createInjector(new Module() {
		@Override
		public void configure(Binder b) {
			b.bind(BeanConverter.class).annotatedWith(Names.named("shindig.bean.converter.json")).to(BeanJsonConverter.class);
		}
	}));

	public static MediaController getSingleton() {
		return theInstance;
	}

	private MediaController() {
		ISO_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
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

		UserData userData = sharing.getUserData(userId);

		for (Album existingAlbum : userData.getAlbums())
			if (existingAlbum.getTitle().equals(photoAlbumTitle))
				throw new PhotoAlbumDuplicated(photoAlbumTitle);

		String albumHash = null;
		try {
			final String publishedDate = ISO_DATE_FORMAT.format(new Date());

			albumHash = userData.createPhotoAlbum(photoAlbumTitle, publishedDate);
			String dbUri = sharing.seedUserData(userData);

			DistributedHashTable.getSingleton().store(userId,userId, dbUri, publishedDate);
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

			String latestHash = FileSharingImpl.getHashFromMagnetURI(recordDb.getString("uri"));

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

			String latestHash = FileSharingImpl.getHashFromMagnetURI(recordDb.getString("uri"));

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
			sharing.download(itemMagneUri);
			String extension = FileExtensionConverter.toDefaultExtension(item.getMimeType());
			if (extension != null && !extension.isEmpty())
				extension = "." + extension;
			File f = new File("cache/" + sharing.getHashFromMagnetURI(itemMagneUri) + extension);
			if (f.exists())
				item.setUrl("cache/" + sharing.getHashFromMagnetURI(itemMagneUri) + extension);
			else
				item.setUrl("cache/" + sharing.getHashFromMagnetURI(itemMagneUri));
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
				sharing.download(itemMagneUri);
				String extension = FileExtensionConverter.toDefaultExtension(item.getMimeType());
				if (extension != null && !extension.isEmpty())
					extension = "." + extension;
				File f = new File("cache/" + sharing.getHashFromMagnetURI(itemMagneUri) + extension);
				if (f.exists())
					item.setUrl("cache/" + sharing.getHashFromMagnetURI(itemMagneUri) + extension);
				else
					item.setUrl("cache/" + sharing.getHashFromMagnetURI(itemMagneUri));
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

		UserData userData = sharing.getUserData(userId);

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
				String fileHash = sharing.hash(photo);

				final File photoCachedFile = new File(CACHE_FOLDER + File.separator + fileHash + extension);

				FileUtils.copyFile(photo, photoCachedFile);
				photo.delete();

				final String fileUrl = sharing.seed(photoCachedFile);

				userData.addMediaItemToAlbum(albumId, fileUrl, sharing.getHashFromMagnetURI(fileUrl), mimeType, publishedDate);

				hashList.add(sharing.getHashFromMagnetURI(fileUrl));
			}

			String dbUri = sharing.seedUserData(userData);

			DistributedHashTable.getSingleton().store(userId, userId,dbUri, publishedDate);

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

		UserData userData = sharing.getUserData(userId);

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
			String dbUri = sharing.seedUserData(userData);

			DistributedHashTable.getSingleton().store(userId, userId, dbUri, publishedDate);

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

			String latestHash = FileSharingImpl.getHashFromMagnetURI(recordDb.getString("uri"));

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

}
