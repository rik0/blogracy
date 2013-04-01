package net.blogracy.model.users;

import java.util.List;

import net.blogracy.errors.BlogracyItemNotFound;
import net.blogracy.errors.PhotoAlbumDuplicated;

import org.apache.shindig.social.opensocial.model.ActivityEntry;
import org.apache.shindig.social.opensocial.model.Album;
import org.apache.shindig.social.opensocial.model.MediaItem;

public interface UserData {

	/**
	 * Adds a new comment in the user's ActivityStream
	 * 
	 * @param commentingUser
	 *            the user who is actually commenting
	 * @param commentText
	 *            the comment text string
	 * @param commentedObjectId
	 *            the object which is being commented (located via the item's id)
	 * @param publishDate
	 *            Date / time of publication
	 */
	public void addComment(final User commentingUser, final String commentText,
			final String commentedObjectId, final String publishDate)
			throws BlogracyItemNotFound;

	
	/**
	 * Creates an ActivityEntry that represents a comment in the system 
	 * @param commentingUser   the user who is actually commenting
	 * @param commentText  the comment text string 
	 * @param commentedObjectId the object which is being commented (located via the item's id) 
	 * @param publishDate  Date / time of publication
	 * @param commentId the comment's id (useful for faster approval / rejection)
	 * @return
	 */
	ActivityEntry createComment(final User commentingUser, final String commentText, final String commentedObjectId, final String publishDate) throws BlogracyItemNotFound;
	
	
	
	/**
	 * Adds a new message / attachment entry to the user ActivityStream
	 * 
	 * @param text
	 *            the text of the message
	 * @param textUri
	 *            the text of the message in the form of a published magnet link
	 *            uri
	 * @param attachmentUri
	 *            the attachment's magnet link (null if none)
	 * @param publishDate
	 *            Date / time of publication
	 */
	public void addFeedEntry(final String text, final String textUri,
			final String attachmentUri, final String publishDate);

	
	/**
	 * Add a mediaItem to an existing album
	 * 
	 * @param albumHash
	 * @param photoUrl
	 * @param photoUrlHash
	 * @param mimeType
	 * @param publishedDate
	 *            Date / time of publication
	 */
	public void addMediaItemToAlbum(final String albumHash,
			final String photoUrl, final String photoUrlHash,
			final String mimeType, final String publishedDate);

	/**
	 * Creates a new Photo Album, updated albums and ActivityStream accordingly.
	 * 
	 * @param photoAlbumTitle
	 *            The title of the new album
	 * @param publishedDate
	 *            Date / time of publication
	 * @return the album hash value
	 * @throws PhotoAlbumDuplicated
	 *             if album is already present
	 */
	public String createPhotoAlbum(final String photoAlbumTitle,
			final String publishedDate) throws PhotoAlbumDuplicated;
	
	public List<ActivityEntry> getActivityStream();

	public List<Album> getAlbums();

	/**
	 * Gets all the comments associated to an object
	 * @param objectId the object identifier
	 * @return a (ascending sorted) list of comments
	 */
	public List<ActivityEntry> getCommentsByObjectId(final String objectId);

	public List<MediaItem> getMediaItems();

	public User getUser();

	/**
	 * Removes a mediaItem from a given album
	 * 
	 * @param mediaId
	 * @param albumId
	 * @param publishedDate
	 */
	public void removeMediaItem(final String mediaId, final String albumId,
			final String publishedDate);

	/***
	 * Retrieves the Base64-encoded public key
	 * @return
	 */
	public String getUserPublicKey();
	
}
