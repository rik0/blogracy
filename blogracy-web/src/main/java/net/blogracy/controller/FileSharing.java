package net.blogracy.controller;

import java.io.File;
import java.io.IOException;
import java.util.List;

import net.blogracy.model.users.User;
import net.blogracy.model.users.UserAddendumData;
import net.blogracy.model.users.UserData;

import org.json.JSONException;

public interface FileSharing {

	/**
	 * Seeds a file via BitTorrent protocol
	 * 
	 * @param file
	 *            file to be seeded
	 * @return the magnet link uri of the shared file
	 */
	public String seed(File file);

	/***
	 * Adds a file to the bittorrent download queue. The file is downloaded to
	 * the CACHE_FOLDER and it will be named after the file hash
	 * 
	 * @param uri
	 *            the file's magnet-uri
	 */
	public void download(String uri);

	public void download(String uri, String ext);

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
	public void download(String uri, String ext, FileSharingDownloadListener downloadCompleteListener);

	/***
	 * Adds a file to the bittorrent download queue. The file is downloaded to
	 * the CACHE_FOLDER and it will be named after the file hash
	 * 
	 * @param hash
	 *            the file hash
	 */
	public void downloadByHash(String hash);

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
	public void downloadByHash(String hash, String ext, FileSharingDownloadListener downloadCompleteListener);

	/**
	 * Gets the user data (ActivityStream, Albums and MediaItems) from the
	 * user's db
	 * 
	 * @param userId
	 * @return
	 */
	public UserData getUserData(String userId);

	public UserAddendumData getUserAddendumData(String userId);

	public String seedUserData(UserData userData) throws JSONException, IOException;

	public String seedUserAddendumData(UserAddendumData userData) throws JSONException, IOException;
	
	public List<User> getDelegates(final String userId);

}