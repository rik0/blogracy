package net.blogracy.model.users;

import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;


import net.blogracy.errors.PhotoAlbumDuplicated;
import net.blogracy.model.hashes.Hashes;

import org.apache.shindig.social.core.model.ActivityEntryImpl;
import org.apache.shindig.social.core.model.ActivityObjectImpl;
import org.apache.shindig.social.core.model.AlbumImpl;
import org.apache.shindig.social.core.model.MediaItemImpl;
import org.apache.shindig.social.opensocial.model.ActivityEntry;
import org.apache.shindig.social.opensocial.model.ActivityObject;
import org.apache.shindig.social.opensocial.model.Album;
import org.apache.shindig.social.opensocial.model.MediaItem;
import org.apache.shindig.social.opensocial.model.MediaItem.Type;

public class UserDataImpl implements UserData {

	private User user;
	private List<ActivityEntry> activityStream;
	private List<Album> albums;
	private List<MediaItem> mediaItems;

	public UserDataImpl(User user) {
		this.user = user;
	}

	public User getUser() {
		return this.user;
	}

	public List<ActivityEntry> getActivityStream() {
		return activityStream;
	}

	public void setActivityStream(Collection<ActivityEntry> activityStream) {
		this.activityStream = new ArrayList<ActivityEntry>(activityStream);
	}

	public List<Album> getAlbums() {
		return albums;
	}

	public void setAlbums(Collection<Album> albums) {
		this.albums = new ArrayList<Album>(albums);
	}

	public List<MediaItem> getMediaItems() {
		return mediaItems;
	}

	public void setMediaItems(Collection<MediaItem> mediaItems) {
		this.mediaItems = new ArrayList<MediaItem>(mediaItems);
	}


	public void addFeedEntry(final String text, final String textUri,
			final String attachmentUri, final String publishDate) {
		final ActivityEntry entry = new ActivityEntryImpl();
		ActivityObject actor = new ActivityObjectImpl();
		actor.setObjectType("person");
		actor.setId(this.user.getHash().toString());
		actor.setDisplayName(this.user.getLocalNick());
		entry.setActor(actor);
		entry.setVerb("post");
		entry.setUrl(textUri);
		entry.setPublished(publishDate);
		entry.setContent(text);
		if (attachmentUri != null) {
			ActivityObject enclosure = new ActivityObjectImpl();
			enclosure.setUrl(attachmentUri);
			entry.setObject(enclosure);
		}
		this.activityStream.add(0, entry);
	}

	public String createPhotoAlbum(final String photoAlbumTitle,
			final String publishedDate) throws PhotoAlbumDuplicated {
		// Checks if album already present, if it is an exception is thrown
		for (Album existingAlbum : albums)
			if (existingAlbum.getTitle().equals(photoAlbumTitle))
				throw new PhotoAlbumDuplicated(photoAlbumTitle);

		String albumHash = null;
		try {
			albumHash = Hashes.hash(this.user.getHash().toString()
					+ photoAlbumTitle);

			Album album = new AlbumImpl();
			album.setTitle(photoAlbumTitle);
			album.setId(albumHash);
			album.setOwnerId(this.user.getHash().toString());
			List<Type> types = new ArrayList<Type>();
			types.add(Type.IMAGE);
			album.setMediaType(types);
			// Album is empty when created
			album.setMediaItemCount(0);

			// Append another album into the user's 'mediaUri' file
			this.albums.add(album);

			final ActivityEntry entry = new ActivityEntryImpl();
			ActivityObject actor = new ActivityObjectImpl();
			actor.setObjectType("person");
			actor.setId(this.user.getHash().toString());
			actor.setDisplayName(this.user.getLocalNick());
			entry.setActor(actor);
			entry.setVerb("create");
			ActivityObject mediaAlbumObject = new ActivityObjectImpl();
			mediaAlbumObject.setObjectType("collection");
			mediaAlbumObject.setContent(photoAlbumTitle);
			entry.setObject(mediaAlbumObject);
			entry.setPublished(publishedDate);
			entry.setContent(photoAlbumTitle);
			this.activityStream.add(0, entry);

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return albumHash;
	}

	public void addMediaItemToAlbum(final String albumId,
			final String photoUrl, final String photoUrlHash,
			final String mimeType, final String publishedDate) {
		Album album = null;
		for (Album a : this.albums) {
			if (a.getId().equals(albumId)) {
				album = a;
				break;
			}
		}

		if (album == null)
			throw new InvalidParameterException("AlbumId " + albumId
					+ " does not match to a valid album for the user "
					+ this.user.getHash().toString());

		final ActivityEntry entry = new ActivityEntryImpl();
		ActivityObject actor = new ActivityObjectImpl();
		actor.setObjectType("person");
		actor.setId(this.user.getHash().toString());
		actor.setDisplayName(this.user.getLocalNick());
		entry.setActor(actor);
		entry.setVerb("add");
		entry.setPublished(publishedDate);
		entry.setContent(photoUrlHash);

		ActivityObject mediaItemObject = new ActivityObjectImpl();
		mediaItemObject.setObjectType("image");
		mediaItemObject.setContent(photoUrlHash);
		mediaItemObject.setUrl(photoUrl);
		entry.setObject(mediaItemObject);

		ActivityObject mediaAlbumObject = new ActivityObjectImpl();
		mediaAlbumObject.setObjectType("collection");
		mediaAlbumObject.setContent(album.getTitle());
		mediaAlbumObject.setId(album.getId());
		entry.setTarget(mediaAlbumObject);

		this.activityStream.add(0, entry);

		MediaItem mediaItem = new MediaItemImpl();
		mediaItem.setAlbumId(albumId);
		mediaItem.setId(photoUrlHash);
		mediaItem.setUrl(photoUrl);
		mediaItem.setLastUpdated(publishedDate);
		mediaItem.setMimeType(mimeType);

		if (album.getMediaMimeType() == null)
			album.setMediaMimeType(new ArrayList<String>());

		List<String> albumMimeTypes = album.getMediaMimeType();

		if (!albumMimeTypes.contains(mimeType))
			albumMimeTypes.add(mimeType);

		this.mediaItems.add(mediaItem);

		album.setMediaItemCount(album.getMediaItemCount() + 1);
	}

	public void removeMediaItem(final String mediaId, final String albumId, final String publishedDate)
	{
		Album album = null;
		for (Album a : this.albums) {
			if (a.getId().equals(albumId)) {
				album = a;
				break;
			}
		}

		if (album == null)
			throw new InvalidParameterException("AlbumId " + albumId
					+ " does not correspond to a valid album for the user "
					+ this.user.getHash().toString());
		
		for (Iterator<MediaItem> iter = this.mediaItems.iterator(); iter
				.hasNext();) {
			MediaItem mediaItem = iter.next();
			if (mediaId.equals(mediaItem.getId())
					&& albumId.equals(mediaItem.getAlbumId()))
				iter.remove();
		}

		album.setMediaItemCount(mediaItems.size());
		
		final ActivityEntry entry = new ActivityEntryImpl();
		ActivityObject actor = new ActivityObjectImpl();
		actor.setObjectType("person");
		actor.setId(this.user.getHash().toString());
		actor.setDisplayName(this.user.getLocalNick());
		entry.setActor(actor);
		entry.setVerb("remove");
		entry.setPublished(publishedDate);
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

		this.activityStream.add(0, entry);
		
	}
}
