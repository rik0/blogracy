/*
 * Copyright (c)  2011 Enrico Franchi, Michele Tomaiuolo and University of Parma.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package net.blogracy.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.SignatureException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TimeZone;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import net.blogracy.config.Configurations;
import net.blogracy.util.JsonWebSignature;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Generic functions to manipulate feeds are defined in this class.
 */
public class DistributedHashTable {

	 class DownloadListener implements FileSharingDownloadListener {
	        private String id;
	        private String hash;
	        private String version;
	        private JSONObject record;
	        private long start;
	        private long sent;

	        DownloadListener(String id, String hash, String version,
	                JSONObject record) {
	            this.id = id;
	            this.hash = hash;
	            this.version = version;
	            this.record = record;
	            this.start = System.currentTimeMillis();
	            try {
	                this.sent = ISO_DATE_FORMAT.parse(version).getTime();
	            } catch (ParseException e) {
	                e.printStackTrace();
	            }
	            log.info("download-req " + id + " " + hash + " " + version);
	        }

			@Override
			public void onFileDownloaded(String fileFullPath) {
				String now = ISO_DATE_FORMAT.format(new java.util.Date());
	            long delay = System.currentTimeMillis() - start;
	            long size = -1;
	            long received = -1;
	            try {
	                received = ISO_DATE_FORMAT.parse(now).getTime() - sent;
	                File file = new File(fileFullPath);
	                size = file.length();
	            } catch (Exception e) {
	                e.printStackTrace();
	            }
	            log.info("download-ans " + id + " " + hash + " " + version + " "
	                    + now + " " + delay + " " + received + " " + size);
	            putRecord(record);
	        }
	    }

	
	class LookupListener implements MessageListener {
		private String id;
		private long start;

		LookupListener(String id) {
			this.id = id;
			this.start = System.currentTimeMillis();
			log.info("lookup-req " + id);
		}

		@Override
		public void onMessage(Message response) {
			try {
				long delay = System.currentTimeMillis() - start; 
				String msgText = ((TextMessage) response).getText();
				JSONObject keyValue = new JSONObject(msgText);
				final String value = keyValue.getString("value");
				final String id = keyValue.getString("id");
				PublicKey signerKey = JsonWebSignature.getSignerKey(value);
                final JSONObject record = new JSONObject(JsonWebSignature.verify(
                        value, signerKey));
				
				String version = record.getString("version");
                String uri = record.getString("uri");
                String hash = FileSharing.getHashFromMagnetURI(uri);
                String now = ISO_DATE_FORMAT.format(new java.util.Date());
                log.info("lookup-ans " + id + " " + hash + " " + version + " " + now + " " + delay);

				
				JSONObject currentRecord = getRecord(id);
				if (currentRecord == null || currentRecord.getString("version").compareTo(version) < 0) {		
					 FileSharing.getSingleton().downloadByHash(hash, ".json",
	                            new DownloadListener(id, hash, version, record));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private ConnectionFactory connectionFactory;
	private Connection connection;
	private Session session;
	private Destination lookupQueue;
	private Destination storeQueue;
	private Destination downloadQueue;
	private MessageProducer producer;
	private MessageConsumer consumer;

	static final DateFormat ISO_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			 
	
	static final String CACHE_FOLDER = Configurations.getPathConfig().getCachedFilesDirectoryPath();

	private HashMap<String, JSONObject> records = new HashMap<String, JSONObject>();
	private Logger log;

	private static final DistributedHashTable THE_INSTANCE = new DistributedHashTable();

	public static DistributedHashTable getSingleton() {
		return THE_INSTANCE;
	}

	public static File recordsFile = new File(CACHE_FOLDER + File.separator + "records.json");
	
	public DistributedHashTable() {
		ISO_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
		try {
			log = Logger.getLogger("net.blogracy.controller.dht");
			log.addHandler(new FileHandler("dht.log"));
			log.getHandlers()[0].setFormatter(new SimpleFormatter()); 
			

			if (recordsFile.exists()) {
				JSONArray recordList = new JSONArray(new JSONTokener(new FileReader(recordsFile)));
				for (int i = 0; i < recordList.length(); ++i) {
					JSONObject record = recordList.getJSONObject(i);
					records.put(record.getString("id"), record);
				}
			}

			connectionFactory = new ActiveMQConnectionFactory(ActiveMQConnection.DEFAULT_BROKER_URL);
			connection = connectionFactory.createConnection();
			connection.start();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			producer = session.createProducer(null);
			producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
			lookupQueue = session.createQueue("lookup");
			storeQueue = session.createQueue("store");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void lookup(final String id) {
		try {
			Destination tempDest = session.createTemporaryQueue();
			MessageConsumer responseConsumer = session.createConsumer(tempDest);
			responseConsumer.setMessageListener(new LookupListener(id));

			JSONObject record = new JSONObject();
			record.put("id", id);

			TextMessage message = session.createTextMessage();
			message.setText(record.toString());
			message.setJMSReplyTo(tempDest);
			producer.send(lookupQueue, message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void store(final String id, final String uri, final String version) {
		try {
			JSONObject record = new JSONObject();
			record.put("id", id);
			record.put("uri", uri);
			record.put("version", version);
			// put "magic" public-key; e.g.
			// RSA.modulus(n).exponent(e)
			// record.put("signature", user); // TODO

			KeyPair keyPair = Configurations.getUserConfig().getUserKeyPair();
			String value = JsonWebSignature.sign(record.toString(), keyPair);

			JSONObject keyValue = new JSONObject();
			keyValue.put("key", id);
			keyValue.put("value", value);
			TextMessage message = session.createTextMessage();
			message.setText(keyValue.toString());
			producer.send(storeQueue, message);
			putRecord(record);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public JSONObject getRecord(final String user) {
		return records.get(user);
	}

	public void putRecord(JSONObject record) {
		try {
			String id = record.getString("id");
			JSONObject old = records.get(id);
			if (old == null || record.getString("version").compareTo(old.getString("version")) > 0) {
				records.put(id, record);
			}
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
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
