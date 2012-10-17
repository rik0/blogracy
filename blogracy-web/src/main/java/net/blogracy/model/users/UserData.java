package net.blogracy.model.users;

import java.util.List;

import net.blogracy.errors.PhotoAlbumDuplicated;

import org.apache.shindig.social.opensocial.model.ActivityEntry;
import org.apache.shindig.social.opensocial.model.Album;
import org.apache.shindig.social.opensocial.model.MediaItem;

public interface UserData {

	public User getUser();

	public List<ActivityEntry> getActivityStream();

	public List<Album> getAlbums();

	public List<MediaItem> getMediaItems();


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
	 * Removes a mediaItem from a given album
	 * @param mediaId
	 * @param albumId
	 * @param publishedDate
	 */
	public void removeMediaItem(final String mediaId, final String albumId,
			final String publishedDate);

}
