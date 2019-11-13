package net.blogracy.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class FileExtensionConverter {

	private static Map<String, String> extensionMIMETypeMapping;
	private static String defaultMIMEType = "application/octet-stream";
	private static String defaultExtension = "";

	static {
		extensionMIMETypeMapping = new HashMap<String, String>();
		extensionMIMETypeMapping.put("txt", "text/plain");
		extensionMIMETypeMapping.put("rtf", "text/richtext");
		extensionMIMETypeMapping.put("wav", "audio/wav");
		extensionMIMETypeMapping.put("gif", "image/gif");
		extensionMIMETypeMapping.put("jpg", "image/jpeg");
		extensionMIMETypeMapping.put("png", "image/png");
		extensionMIMETypeMapping.put("tiff", "image/tiff");
		extensionMIMETypeMapping.put("bmp", "image/bmp");
		extensionMIMETypeMapping.put("avi", "video/avi");
		extensionMIMETypeMapping.put("mpeg", "video/mpeg");
		extensionMIMETypeMapping.put("mp4", "video/mp4");
		extensionMIMETypeMapping.put("ov", "video/quicktime");
		extensionMIMETypeMapping.put("pdf", "application/pdf");
		extensionMIMETypeMapping.put("doc", "application/msword");
		extensionMIMETypeMapping.put("dot", "application/msword");
		extensionMIMETypeMapping
				.put("docx",
						"application/vnd.openxmlformats-officedocument.wordprocessingml.document");
		extensionMIMETypeMapping
				.put("dotx",
						"application/vnd.openxmlformats-officedocument.wordprocessingml.template");
		extensionMIMETypeMapping.put("xls", "application/vnd.ms-excel");
		extensionMIMETypeMapping.put("xlt", "application/vnd.ms-excel");
		extensionMIMETypeMapping.put("csv", "application/vnd.ms-excel");
		extensionMIMETypeMapping
				.put("xlsx",
						"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		extensionMIMETypeMapping
				.put("xltx",
						"application/vnd.openxmlformats-officedocument.spreadsheetml.template");
		extensionMIMETypeMapping.put("ppt", "application/vnd.ms-powerpoint");
		extensionMIMETypeMapping.put("pot", "application/vnd.ms-powerpoint");
		extensionMIMETypeMapping
				.put("pptx",
						"application/vnd.openxmlformats-officedocument.presentationml.presentation");
		extensionMIMETypeMapping
				.put("potx",
						"application/vnd.openxmlformats-officedocument.presentationml.template");
	}

	public static String toMIMEType(String extension) {
		if (extension == null || extension.isEmpty()) {
			return defaultMIMEType;
		}

		String lowerExtension = extension.toLowerCase();

		if (extensionMIMETypeMapping.containsKey(lowerExtension)) {
			return extensionMIMETypeMapping.get(lowerExtension);
		} else
			return defaultMIMEType;
	}

	public static String toDefaultExtension(String mimeType) {
		if (mimeType == null || mimeType.isEmpty()) {
			return defaultExtension;
		}

		String lowerMimeType = mimeType.toLowerCase();

		for (Entry<String, String> entry : extensionMIMETypeMapping.entrySet()) {
			if (entry.getValue().toLowerCase().compareTo(lowerMimeType) == 0)
				return entry.getKey();
		}

		return defaultExtension;
	}
}
