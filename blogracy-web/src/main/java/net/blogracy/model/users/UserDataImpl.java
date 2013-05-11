package net.blogracy.model.users;

import java.security.InvalidParameterException;
import java.util.UUID;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import net.blogracy.errors.BlogracyItemNotFound;
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
	private String userPublicKey;

	public UserDataImpl(User user) {
		this.user = user;
	}

	public void addComment(final User commentingUser, final String commentText, final String commentedObjectId, final String publishDate) throws BlogracyItemNotFound {
		this.addComment(commentingUser, commentText, commentedObjectId, publishDate, UUID.randomUUID().toString());
	}

	public void addComment(final User commentingUser, final String commentText, final String commentedObjectId, final String publishDate, final String commentId) throws BlogracyItemNotFound {
		final ActivityEntry comment = this.createComment(commentingUser, commentText, commentedObjectId, publishDate, commentId);
		this.activityStream.add(0, comment);
	}

	public void addComment(ActivityEntry entry) {
		// The should be some validation in order to check if:
		// it is actually a comment
		// it has all the data a comment needs

		this.activityStream.add(0, entry);
	}

	public ActivityEntry createComment(final User commentingUser, final String commentText, final String commentedObjectId, final String publishDate, final String commentId)
			throws BlogracyItemNotFound {

		List<ActivityEntry> currentUserActivities = this.getActivityStream();

		// Getting the entry in the activity stream that commentingUser is
		// actually posting a comment for...
		ActivityEntry commentedEntry = null;
		for (ActivityEntry entry : currentUserActivities) {
			if (entry.getObject() != null && entry.getObject().getId() != null && entry.getObject().getId().equals(commentedObjectId)) {
				commentedEntry = entry;
				break;
			}
		}

		// Cannot comment nothing! Probably the content to be commented has been
		// removed....
		if (commentedEntry == null)
			throw new BlogracyItemNotFound(commentedObjectId);

		final ActivityEntry comment = new ActivityEntryImpl();
		ActivityObject actor = new ActivityObjectImpl();
		actor.setObjectType("person");
		actor.setId(commentingUser.getHash().toString());
		actor.setDisplayName(commentingUser.getLocalNick());
		comment.setActor(actor);
		comment.setVerb("post");
		comment.setPublished(publishDate);
		final ActivityObject commentObject = new ActivityObjectImpl();
		commentObject.setObjectType("comment");
		commentObject.setContent(commentText);
		commentObject.setId(commentId);
		comment.setObject(commentObject);
		comment.setTarget(commentedEntry.getObject());
		return comment;
	}

	public ActivityEntry createComment(final User commentingUser, final String commentText, final String commentedObjectId, final String publishDate) throws BlogracyItemNotFound {
		return this.createComment(commentingUser, commentText, commentedObjectId, publishDate, UUID.randomUUID().toString());
	}

	public void addFeedEntry(final String text, final String textUri, final String attachmentUri, final String publishDate) {
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

	public void addMediaItemToAlbum(final String albumId, final String photoUrl, final String photoUrlHash, final String mimeType, final String publishedDate) {
		Album album = null;
		for (Album a : this.albums) {
			if (a.getId().equals(albumId)) {
				album = a;
				break;
			}
		}

		if (album == null)
			throw new InvalidParameterException("AlbumId " + albumId + " does not match to a valid album for the user " + this.user.getHash().toString());

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
		mediaItemObject.setId(photoUrlHash);
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

	public String createPhotoAlbum(final String photoAlbumTitle, final String publishedDate) throws PhotoAlbumDuplicated {
		// Checks if album already present, if it is an exception is thrown
		for (Album existingAlbum : albums)
			if (existingAlbum.getTitle().equals(photoAlbumTitle))
				throw new PhotoAlbumDuplicated(photoAlbumTitle);

		String albumHash = null;
		albumHash = Hashes.hash(this.user.getHash().toString() + photoAlbumTitle);

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
		mediaAlbumObject.setId(albumHash);
		entry.setObject(mediaAlbumObject);
		entry.setPublished(publishedDate);
		entry.setContent(photoAlbumTitle);
		this.activityStream.add(0, entry);

		return albumHash;
	}

	public List<ActivityEntry> getActivityStream() {
		return activityStream;
	}

	public List<Album> getAlbums() {
		return albums;
	}

	public List<ActivityEntry> getCommentsByObjectId(final String objectId) {
		List<ActivityEntry> comments = new ArrayList<ActivityEntry>();

		for (ActivityEntry entry : this.getActivityStream()) {
			if (entry.getTarget() != null && entry.getTarget().getId() != null && entry.getTarget().getId().equals(objectId) && entry.getObject() != null && entry.getObject().getObjectType() != null
					&& entry.getObject().getObjectType().equals("comment") && entry.getVerb() != null && entry.getVerb().equals("post")) {
				comments.add(entry);
			}
		}

		java.util.Collections.sort(comments, new Comparator<ActivityEntry>() {
			public int compare(ActivityEntry o1, ActivityEntry o2) {
				return o1.getPublished().compareTo(o2.getPublished());
			}
		});

		return comments;
	}

	public List<MediaItem> getMediaItems() {
		return mediaItems;
	}

	public User getUser() {
		return this.user;
	}

	public String getUserPublicKey() {
		return this.userPublicKey;
	}

	public void removeMediaItem(final String mediaId, final String albumId, final String publishedDate) {
		Album album = null;
		for (Album a : this.albums) {
			if (a.getId().equals(albumId)) {
				album = a;
				break;
			}
		}

		if (album == null)
			throw new InvalidParameterException("AlbumId " + albumId + " does not correspond to a valid album for the user " + this.user.getHash().toString());

		for (Iterator<MediaItem> iter = this.mediaItems.iterator(); iter.hasNext();) {
			MediaItem mediaItem = iter.next();
			if (mediaId.equals(mediaItem.getId()) && albumId.equals(mediaItem.getAlbumId()))
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

	public void setActivityStream(Collection<ActivityEntry> activityStream) {
		this.activityStream = new ArrayList<ActivityEntry>(activityStream);
	}

	public void setAlbums(Collection<Album> albums) {
		this.albums = new ArrayList<Album>(albums);
	}

	public void setMediaItems(Collection<MediaItem> mediaItems) {
		this.mediaItems = new ArrayList<MediaItem>(mediaItems);
	}

	public void setUserPublicKey(String pKey) {
		this.userPublicKey = pKey;
	}

}
