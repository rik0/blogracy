package net.blogracy.web;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
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
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ImageGalleryUploader extends HttpServlet {

	private final String SERVLET_RELATIVE_URL = "ImageGalleryUploader";
	private final String CACHE_FOLDER = FileSharing.getCacheFolder().substring(
			FileSharing.getCacheFolder().lastIndexOf("webapp/")
					+ ("webapp/").length());
	
	
	// List of the current images in the cache directory
	// In the next step, it should fetch the gallery from the current user's
	// ActivityStream
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		// Obtaining the current user
		String user = request.getParameter("user");

		File dir = new File(FileSharing.getCacheFolder());
		File[] files = dir.listFiles(new FileFilter() {
			public boolean accept(File entry) {
				String[] okFileExtensions = new String[] { "jpg", "jpeg", "png", "gif" };
				if (entry.isDirectory())
					return false;
				for (String extension : okFileExtensions) {
					if (entry.getName().toLowerCase().endsWith(extension)) {
						return true;
					}
				}
				return false;
			}
		});
		
		List<String> fileNames = new ArrayList<String>();
		for (File f : files) {
			fileNames.add(f.getName());
		}
		
		response.setContentType("application/json");
		String jsonAnsewer = toJSON(fileNames.toArray(new String[0]), CACHE_FOLDER);
		response.getWriter().write(jsonAnsewer);
	}

	@SuppressWarnings("unchecked")
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		if (!ServletFileUpload.isMultipartContent(request)) {
			throw new IllegalArgumentException(
					"Request is not multipart, please 'multipart/form-data' enctype for your form.");
		}

		ServletFileUpload uploadHandler = new ServletFileUpload(
				new DiskFileItemFactory());

		FileSharing sharing = FileSharing.getSingleton();
		Map<FileItem, File> cachedFiles = new HashMap<FileItem, File>();
		String user =null;
		
		try {
			List<FileItem> items = uploadHandler.parseRequest(request);
			for (FileItem item : items) {
				if (item.isFormField())
				{
					// The user hash in send along with the request
					user = item.getString();
				}
				else {
					// adding file to the cache Directory
					File f = sharing.cacheFile(item.getName(),
							item.getInputStream());
					cachedFiles.put(item, f);
					item.delete();
				}
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.out.println(e.getStackTrace());
		}

		// TODO add files to the ActivityStream Image Gallery
		
		// Send a Json message to the imageGalleryUplaoder control
		// containing the file's data
		sendImageUploaderJsonReply(cachedFiles, response);
	}

	protected void doDelete(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String name = request.getParameter("name");

		File f = new File(FileSharing.getCacheFolder() + File.separator + name);
		if (f.exists())
			f.delete();

		// TODO: remove file from ActivityStream...

		response.setStatus(HttpServletResponse.SC_OK);
	}

	
	
	private void sendImageUploaderJsonReply(Map<FileItem, File> cachedFiles,
			HttpServletResponse response) throws IOException {

		response.setContentType("application/json");

		for (Entry<FileItem, File> entry : cachedFiles.entrySet()) {
			String jsonAnswer = toJSON(new String[] { entry.getValue().getName() }, CACHE_FOLDER);
			response.getWriter().write(jsonAnswer);
		}
	}

	private String toJSON(String[] fileNames, String cachefolder) throws IOException {
		StringWriter sw = new StringWriter();
		JsonFactory f = new JsonFactory();
		JsonGenerator g = f.createJsonGenerator(sw);

		g.writeStartArray();
		for (String fileName : fileNames) {
			g.writeStartObject();
			File currentFile = new File(FileSharing.getCacheFolder()
					+ File.separator + fileName);
			g.writeStringField("name", fileName);
			g.writeStringField("size", String.valueOf(currentFile.length()));
			g.writeStringField("url",
					String.format(cachefolder + "/%s", fileName));
			g.writeStringField("thumbnail_url",
					String.format(cachefolder + "/%s", fileName));
			g.writeStringField("delete_url",
					String.format(SERVLET_RELATIVE_URL + "?name=%s", fileName));
			g.writeStringField("delete_type", "DELETE");
			System.out.println(String.format(cachefolder + "/%s", fileName));
			g.writeEndObject();
		}
		g.writeEndArray();
		g.close();
		return sw.toString();
	}

}
