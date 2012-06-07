package net.blogracy.web;

import java.io.File;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.blogracy.controller.FileSharing;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.shindig.social.opensocial.model.MediaItem;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ImageGalleryUploader extends HttpServlet {

	private final String SERVLET_RELATIVE_URL = "ImageGalleryUploader";
	private final String CACHE_RELATIVE_URL = "cache/";

	/***
	 * Retrieves the images that belong to the album
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		// Obtaining the current userId and albumId
		String userId = request.getParameter("userId");
		String albumId = request.getParameter("albumId");

		if (userId != null && albumId != null) {
			// Getting the media items from user's record db
			List<MediaItem> list = FileSharing.getSingleton()
					.getMediaItemsWithCachedImages(userId, albumId);
			response.setContentType("application/json");

			List<String> imageUrlList = new ArrayList<String>();
			for (MediaItem item : list) {
				imageUrlList.add(item.getUrl());
			}

			String jsonAnswer = toJSON(imageUrlList, albumId, userId);
			response.getWriter().write(jsonAnswer);
		}
	}

	/***
	 * It's used for:
	 * - creating a new album (it requires "user" and "galleryName" as parameters)
	 * - uploading images to the album (it requires "user" and "albumId" as parameters alongside the actual files, request should be multipart content)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		if (!ServletFileUpload.isMultipartContent(request))
			createNewPhotoAlbum(request);
		else
			addUploadedFilesToPhotoAlbum(request, response);
	}


	/**
	 * A certain mediaId is removed from an album.
	 * It requires "mediaId", "albumId" and "userId" as parameters
	 */
	protected void doDelete(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String mediaId = request.getParameter("mediaId");
		String albumId = request.getParameter("albumId");
		String userId = request.getParameter("userId");

		try {
			FileSharing.getSingleton().deletePhotoFromAlbum(userId, albumId,
					mediaId);
			response.setStatus(HttpServletResponse.SC_OK);
		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	
	
	private void addUploadedFilesToPhotoAlbum(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		ServletFileUpload uploadHandler = new ServletFileUpload(
				new DiskFileItemFactory());

		// The userId and albumId are sent along with the request
		String userHash = null;
		String albumId = null;
		Map<FileItem, FileDTO> cachedFiles = new HashMap<FileItem, FileDTO>();
		Map<File,String> fileAndMimeTypeMap = new HashMap<File, String>();
		
		try {
			@SuppressWarnings("unchecked")
			List<FileItem> items = uploadHandler.parseRequest(request);
			for (FileItem item : items) {
				if (item.isFormField()) {
					String fieldName = item.getFieldName();
					if (fieldName.equalsIgnoreCase("user"))
						userHash = item.getString();
					else if (fieldName.equalsIgnoreCase("albumId"))
						albumId = item.getString();

				} else {
					File f = new File(item.getName());
					item.write(f);
					cachedFiles.put(item, new FileDTO(f));
					fileAndMimeTypeMap.put(f, item.getContentType());
					item.delete();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	
		List<String> hashes = FileSharing.getSingleton().addMediaItemsToAlbum(userHash, albumId, fileAndMimeTypeMap);
		
		int i = 0;
		for (Entry<FileItem, FileDTO> entry  : cachedFiles.entrySet()){
			entry.getValue().setHash(hashes.get(i));
			++i;
		}

		// Send a Json message to the imageGalleryUploader control
		// containing the file's data
		sendImageUploaderJsonReply(cachedFiles, response, albumId, userHash);
	}
	
	private void createNewPhotoAlbum(HttpServletRequest request) {
		// Obtaining the current user
		String user = request.getParameter("user");

		try {
			String galleryName = request.getParameter("galleryname");

			if (galleryName != null) {
				// Gallery creation
				FileSharing.getSingleton().createPhotoAlbum(user, galleryName);

			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.out.println(e.getStackTrace());
		}
	}

	private void sendImageUploaderJsonReply(Map<FileItem, FileDTO> cachedFiles,
			HttpServletResponse response, String albumId, String userId)
			throws IOException {

		response.setContentType("application/json");

		for (Entry<FileItem, FileDTO> entry : cachedFiles.entrySet()) {
			List<String> stringEntry = new ArrayList<String>();
			stringEntry.add(CACHE_RELATIVE_URL + entry.getValue().getHash());
			String jsonAnswer = toJSON(stringEntry, albumId, userId);
			response.getWriter().write(jsonAnswer);
		}
	}

	private String toJSON(Collection<String> fileNames, String albumId,
			String userId) throws IOException {
		JSONArray array = new JSONArray();

		try {
			for (String fileName : fileNames) {
				JSONObject json = new JSONObject();
				File currentFile = new File(fileName);
				System.out.println(fileName);

				json.put("name", "");
				json.put("size", String.valueOf(currentFile.length()));
				json.put("url", fileName);
				json.put("thumbnail_url", fileName);
				json.put("delete_url", String.format(SERVLET_RELATIVE_URL
						+ "?mediaId=%s&albumId=%s&userId=%s",
						fileName.substring(CACHE_RELATIVE_URL.length()), albumId,
						userId));
				json.put("delete_type", "DELETE");
				array.put(json);
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return array.toString();
	}
	
	
	/***
	 * Auxiliary Data Transfer Object which keeps the File and its bittorrent hash string information
	 *
	 */
	public class FileDTO
	{
		public FileDTO(File associatedFile)
		{
			this.file = associatedFile;
		}
		
		public File getFile() {
			return file;
		}

		public String getHash() {
			return hash;
		}
		
		public void setHash(String hash) {
			this.hash = hash;
		}
		protected File file;
		protected String hash;
		
		public Entry<File,String> toEntry()
		{
			return new AbstractMap.SimpleEntry<File,String>(this.file, this.hash);
		}
	}
}
