package net.blogracy.controller;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import net.blogracy.config.Configurations;

public class SalmonDbController {

	static final String CACHE_FOLDER = Configurations.getPathConfig()
			.getCachedFilesDirectoryPath();

	private static final SalmonDbController THE_INSTANCE = new SalmonDbController();

	private HashMap<String, JSONObject> records = new HashMap<String, JSONObject>();

	private File recordsFile;
	
	public static SalmonDbController getSingleton() {
		return THE_INSTANCE;
	}

	public SalmonDbController() {
		try {
			recordsFile = new File(CACHE_FOLDER + File.separator
					+ "salmonDb.json");
			if (recordsFile.exists()) {
				JSONArray recordList = new JSONArray(new JSONTokener(
						new FileReader(recordsFile)));
				for (int i = 0; i < recordList.length(); ++i) {
					JSONObject record = recordList.getJSONObject(i);
					records.put(record.getString("userId"), record);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public JSONObject getUserContent(String userId) {
		if (records.containsKey(userId))
			return records.get(userId);
		else
			return null;
	}

	public void addUserContent(String userId, JSONObject contentData) {
		try {
			if (records.containsKey(userId)) {
				JSONObject contentDataMainObject =	records.get(userId);
				JSONArray array =contentDataMainObject.getJSONArray("contentData");
				array.put(contentData);
				contentDataMainObject.put("contentData", array);
				records.put(userId, contentDataMainObject);
			} else {
				JSONObject contentDataMainObject = new JSONObject();
				JSONArray array = new JSONArray();
				array.put(contentData);
				contentDataMainObject.put("contentData", array);
				records.put(userId, contentDataMainObject);
			}
			saveRecordsToDb();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void saveRecordsToDb()
	{
		JSONArray recordList = new JSONArray();
		Iterator<JSONObject> entries = records.values().iterator();
		while (entries.hasNext()) {
			JSONObject entry = entries.next();
			recordList.put(entry);
		}
		try {
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
