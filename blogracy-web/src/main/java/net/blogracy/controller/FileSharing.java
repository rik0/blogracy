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
import java.io.FileReader;
import java.io.FileWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import net.blogracy.config.Configurations;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.codec.binary.Base32;
import org.apache.shindig.protocol.conversion.BeanConverter;
import org.apache.shindig.protocol.conversion.BeanJsonConverter;
import org.apache.shindig.social.core.model.ActivityEntryImpl;
import org.apache.shindig.social.core.model.ActivityObjectImpl;
import org.apache.shindig.social.opensocial.model.ActivityEntry;
import org.apache.shindig.social.opensocial.model.ActivityObject;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Module;
import com.google.inject.name.Names;

/**
 * Generic functions to manipulate feeds are defined in this class.
 */
public class FileSharing {

    private ConnectionFactory connectionFactory;
    private Connection connection;
    private Session session;
    private Destination seedQueue;
    private Destination downloadQueue;
    private MessageProducer producer;
    private MessageConsumer consumer;

    static final DateFormat ISO_DATE_FORMAT = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss");
    static final String CACHE_FOLDER = Configurations.getPathConfig()
            .getCachedFilesDirectoryPath();

    private static final FileSharing THE_INSTANCE = new FileSharing();

    private static BeanJsonConverter CONVERTER = new BeanJsonConverter(
            Guice.createInjector(new Module() {
                @Override
                public void configure(Binder b) {
                    b.bind(BeanConverter.class)
                            .annotatedWith(
                                    Names.named("shindig.bean.converter.json"))
                            .to(BeanJsonConverter.class);
                }
            }));

    public static FileSharing getSingleton() {
        return THE_INSTANCE;
    }

    public static String hash(String text) {
        String result = null;
        try {
            MessageDigest digester = MessageDigest.getInstance("SHA-1");
            Base32 encoder = new Base32();
            byte[] digest = digester.digest(text.getBytes());
            result = encoder.encodeAsString(digest);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return result;
    }

    public FileSharing() {
        try {
            connectionFactory = new ActiveMQConnectionFactory(
                    ActiveMQConnection.DEFAULT_BROKER_URL);
            connection = connectionFactory.createConnection();
            connection.start();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            producer = session.createProducer(null);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            seedQueue = session.createQueue("seed");
            downloadQueue = session.createQueue("download");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String seed(File file) {
        String uri = null;
        try {
            Destination tempDest = session.createTemporaryQueue();
            MessageConsumer responseConsumer = session.createConsumer(tempDest);

            JSONObject requestObj = new JSONObject();
            requestObj.put("file", file.getAbsolutePath());

            TextMessage request = session.createTextMessage();
            request.setText(requestObj.toString());
            request.setJMSReplyTo(tempDest);
            producer.send(seedQueue, request);

            TextMessage response = (TextMessage) responseConsumer.receive();
            String msgText = ((TextMessage) response).getText();
            JSONObject responseObj = new JSONObject(msgText);
            uri = responseObj.getString("uri");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return uri;
    }

    static public List<ActivityEntry> getFeed(String user) {
        List<ActivityEntry> result = new ArrayList<ActivityEntry>();
        System.out.println("Getting feed: " + user);
        JSONObject record = DistributedHashTable.getSingleton().getRecord(user);
        if (record != null) {
            try {
                String latestHash = FileSharing.getHashFromMagnetURI(record
                        .getString("uri"));
                File dbFile = new File(CACHE_FOLDER + File.separator
                        + latestHash + ".json");
                System.out.println("Getting feed: " + dbFile.getAbsolutePath());
                JSONObject db = new JSONObject(new JSONTokener(new FileReader(
                        dbFile)));

                JSONArray items = db.getJSONArray("items");
                for (int i = 0; i < items.length(); ++i) {
                    JSONObject item = items.getJSONObject(i);
                    ActivityEntry entry = (ActivityEntry) CONVERTER
                            .convertToObject(item, ActivityEntry.class);
                    result.add(entry);
                }
                System.out.println("Feed loaded");
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Feed created");
            }
        }
        return result;
    }

    public void addFeedEntry(String id, String text, File attachment) {
        try {
            String hash = hash(text);
            File textFile = new File(CACHE_FOLDER + File.separator + hash
                    + ".txt");

            FileWriter w = new FileWriter(textFile);
            w.write(text);
            w.close();

            String textUri = seed(textFile);
            String attachmentUri = null;
            if (attachment != null) {
                attachmentUri = seed(attachment);
            }

            final List<ActivityEntry> feed = getFeed(id);
            final ActivityEntry entry = new ActivityEntryImpl();
            entry.setVerb("post");
            entry.setUrl(textUri);
            entry.setPublished(ISO_DATE_FORMAT.format(new Date()));
            entry.setContent(text);
            if (attachment != null) {
                ActivityObject enclosure = new ActivityObjectImpl();
                enclosure.setUrl(attachmentUri);
                entry.setObject(enclosure);
            }
            feed.add(0, entry);
            final File feedFile = new File(CACHE_FOLDER + File.separator + id
                    + ".json");

            JSONArray items = new JSONArray();
            for (int i = 0; i < feed.size(); ++i) {
                JSONObject item = new JSONObject(feed.get(i));
                items.put(item);
            }
            JSONObject db = new JSONObject();
            db.put("items", items);

            FileWriter writer = new FileWriter(feedFile);
            db.write(writer);
            writer.close();

            String feedUri = seed(feedFile);
            DistributedHashTable.getSingleton().store(id, feedUri,
                    entry.getPublished());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static String getHashFromMagnetURI(String uri) {
        String hash = null;
        int btih = uri.indexOf("xt=urn:btih:");
        if (btih >= 0) {
            hash = uri.substring(btih + "xt=urn:btih:".length());
            int amp = hash.indexOf('&');
            if (amp >= 0)
                hash = hash.substring(0, amp);
        }
        return hash;
    }

    public void download(final String uri) {
        String hash = getHashFromMagnetURI(uri);
        downloadByHash(hash);
    }

    public void downloadByHash(final String hash) {
        try {
            JSONObject sharedFile = new JSONObject();
            sharedFile.put("uri", "magnet:?xt=urn:btih:" + hash);
            sharedFile.put("file", CACHE_FOLDER + File.separator + hash);

            TextMessage message = session.createTextMessage();
            message.setText(sharedFile.toString());
            producer.send(downloadQueue, message);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
