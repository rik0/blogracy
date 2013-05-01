package net.blogracy.controller;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import net.blogracy.config.Configurations;

public class SalmonDbController {

	static final String CACHE_FOLDER = Configurations.getPathConfig().getCachedFilesDirectoryPath();

	private static final SalmonDbController THE_INSTANCE = new SalmonDbController();

	// Database structure:
	// userId
	// contentId
	// content (JSONObject)
	private HashMap<String, HashMap<String, JSONObject>> records = new HashMap<String, HashMap<String, JSONObject>>();

	private File recordsFile;

	public static SalmonDbController getSingleton() {
		return THE_INSTANCE;
	}

	public SalmonDbController() {
		try {
			recordsFile = new File(CACHE_FOLDER + File.separator + "salmonDb.json");
			if (recordsFile.exists()) {
				JSONArray recordList = new JSONArray(new JSONTokener(new FileReader(recordsFile)));
				for (int i = 0; i < recordList.length(); ++i) {
					JSONObject record = recordList.getJSONObject(i);
					String userId = record.getString("userId");
					JSONArray contents = record.getJSONArray("contents");
					HashMap<String, JSONObject> contentsList = new HashMap<String, JSONObject>();
					for (int j = 0; j < contents.length(); ++j) {
						JSONObject content = contents.getJSONObject(j);
						String contentId = content.getString("contentId");
						JSONObject actualContent = content.getJSONObject("content");
						contentsList.put(contentId, actualContent);
					}
					records.put(userId, contentsList);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public JSONArray getUserAllContent(String userId) {
		try {
			if (records.containsKey(userId)) {
				Map<String, JSONObject> contents = records.get(userId);
				JSONArray data = new JSONArray();
				for (Map.Entry<String, JSONObject> entry : contents.entrySet()) {
					JSONObject obj = new JSONObject();
					obj.put("contentId", entry.getKey());
					obj.put("content", entry.getValue());
					data.put(obj);
				}
				return data;
			} else
				return null;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void addUserContent(String userId, String contentId, JSONObject contentData) {
		try {
			if (records.containsKey(userId)) {
				Map<String, JSONObject> contentsDataMainObject = records.get(userId);
				contentsDataMainObject.put(contentId, contentData);
			} else {
				HashMap<String, JSONObject> contentDataMainObject = new HashMap<String, JSONObject>();
				contentDataMainObject.put(contentId, contentData);
				records.put(userId, contentDataMainObject);
			}
			saveRecordsToDb();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void removeUserContent(String userId, String contentId) {
		if (records.containsKey(userId)) {
			Map<String, JSONObject> contentsDataMainObject = records.get(userId);
			if (contentsDataMainObject.containsKey(contentId)) {
				contentsDataMainObject.remove(contentId);
				saveRecordsToDb();
			}
		}
	}

	private void saveRecordsToDb() {
		try {
			JSONArray recordList = new JSONArray();
			Iterator<Map.Entry<String, HashMap<String, JSONObject>>> entries = records.entrySet().iterator();
			while (entries.hasNext()) {
				JSONObject record = new JSONObject();

				Map.Entry<String, HashMap<String, JSONObject>> entry = entries.next();
				record.put("userId", entry.getKey());
				JSONArray contents = new JSONArray();

				Iterator<Map.Entry<String, JSONObject>> entryData = entry.getValue().entrySet().iterator();
				while (entryData.hasNext()) {
					Map.Entry<String, JSONObject> data = entryData.next();
					JSONObject obj = new JSONObject();
					obj.put("contentId", data.getKey());
					obj.put("content", data.getValue());
					contents.put(obj);
				}
				record.put("contents", contents);
				recordList.put(record);
			}

			FileWriter writer = new FileWriter(recordsFile);
			recordList.write(writer);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
