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
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;

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
import org.json.JSONObject;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEnclosure;
import com.sun.syndication.feed.synd.SyndEnclosureImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.SyndFeedOutput;
import com.sun.syndication.io.XmlReader;

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

    static final String CACHE_FOLDER = Configurations.getPathConfig()
            .getCachedFilesDirectoryPath();

    private static final FileSharing theInstance = new FileSharing();

    public static FileSharing getSingleton() {
        return theInstance;
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

    static public SyndFeed getFeed(String user) {
        System.out.println("Getting feed: " + user);
        SyndFeed feed = null;
        try {
            JSONObject record = DistributedHashTable.getSingleton().getRecord(
                    user);
            String latestHash = FileSharing.getHashFromMagnetURI(record
                    .getString("uri"));
            File feedFile = new File(CACHE_FOLDER + File.separator + latestHash
                    + ".rss");
            System.out.println("Getting feed: " + feedFile.getAbsolutePath());
            feed = new SyndFeedInput().build(new XmlReader(feedFile));
            System.out.println("Feed loaded");
        } catch (Exception e) {
            feed = new SyndFeedImpl();
            feed.setFeedType("rss_2.0");
            feed.setTitle(user);
            feed.setLink("http://www.blogracy.net");
            feed.setDescription("This feed has been created using ROME (Java syndication utilities");
            feed.setEntries(new ArrayList());
            System.out.println("Feed created");
        }
        return feed;
    }

    public void addFeedEntry(String id, String text, File attachment) {
        try {
            String hash = hash(text);
            File textFile = new File(CACHE_FOLDER + File.separator + hash
                    + ".txt");

            java.io.FileWriter w = new java.io.FileWriter(textFile);
            w.write(text);
            w.close();

            String textUri = seed(textFile);
            String attachmentUri = null;
            if (attachment != null) {
                attachmentUri = seed(attachment);
            }

            final SyndFeed feed = getFeed(id);
            final SyndEntry entry = new SyndEntryImpl();
            entry.setTitle("No Title");
            entry.setLink(textUri);
            entry.setPublishedDate(new Date());
            SyndContent description = new SyndContentImpl();
            description.setType("text/plain");
            description.setValue(text);
            entry.setDescription(description);
            if (attachment != null) {
                SyndEnclosure enclosure = new SyndEnclosureImpl();
                enclosure.setUrl(attachmentUri);
                ArrayList enclosures = new ArrayList();
                enclosures.add(enclosure);
                entry.setEnclosures(enclosures);
            }
            feed.getEntries().add(0, entry);
            final File feedFile = new File(CACHE_FOLDER + File.separator + id
                    + ".rss");
            new SyndFeedOutput().output(feed, new PrintWriter(feedFile));

            String feedUri = seed(feedFile);
            DistributedHashTable.getSingleton().store(id, feedUri,
                    entry.getPublishedDate().getTime());
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
