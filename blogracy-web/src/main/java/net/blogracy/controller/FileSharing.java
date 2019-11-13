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
import java.io.IOException;
import java.security.InvalidParameterException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import net.blogracy.util.FileUtils;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.codec.binary.Base32;
import org.apache.shindig.protocol.conversion.BeanConverter;
import org.apache.shindig.protocol.conversion.BeanJsonConverter;
import org.apache.shindig.social.core.model.ActivityEntryImpl;
import org.apache.shindig.social.core.model.ActivityObjectImpl;
import org.apache.shindig.social.core.model.AlbumImpl;
import org.apache.shindig.social.core.model.MediaItemImpl;
import org.apache.shindig.social.opensocial.model.ActivityEntry;
import org.apache.shindig.social.opensocial.model.ActivityObject;
import org.apache.shindig.social.opensocial.model.Album;
import org.apache.shindig.social.opensocial.model.MediaItem;
import org.apache.shindig.social.opensocial.model.MediaItem.Type;
import org.json.JSONArray;
import org.json.JSONException;
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
    private Connection downloadConnection;
    private Connection seedConnection;
    private Session downloadSession;
    private Session seedSession;
    private Destination seedQueue;
    private Destination downloadQueue;
    private MessageProducer downloadProducer;
    private MessageProducer seedProducer;

    static final DateFormat ISO_DATE_FORMAT = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss'Z'");
    static final String CACHE_FOLDER = Configurations.getPathConfig()
            .getCachedFilesDirectoryPath();

    private static final FileSharing theInstance = new FileSharing();

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

    public static String hash(File file) {
        String result = null;
        try {
            MessageDigest digester = MessageDigest.getInstance("SHA-1");
            Base32 encoder = new Base32();
            byte[] digest = digester.digest(FileUtils.getBytesFromFile(file));
            result = encoder.encodeAsString(digest);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public FileSharing() {
        ISO_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            connectionFactory = new ActiveMQConnectionFactory(
                    ActiveMQConnection.DEFAULT_BROKER_URL);
            downloadConnection = connectionFactory.createConnection();
            downloadConnection.start();
            seedConnection = connectionFactory.createConnection();
            seedConnection.start();

            downloadSession = downloadConnection.createSession(false,
                    Session.AUTO_ACKNOWLEDGE);
            downloadProducer = downloadSession.createProducer(null);
            downloadProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            downloadQueue = downloadSession.createQueue("download");

            seedSession = seedConnection.createSession(false,
                    Session.AUTO_ACKNOWLEDGE);
            seedProducer = seedSession.createProducer(null);
            seedProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            seedQueue = seedSession.createQueue("seed");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String seed(File file) {
        String uri = null;
        try {
            Destination tempDest = seedSession.createTemporaryQueue();
            MessageConsumer responseConsumer = seedSession
                    .createConsumer(tempDest);

            JSONObject requestObj = new JSONObject();
            requestObj.put("file", file.getAbsolutePath());

            TextMessage request = seedSession.createTextMessage();
            request.setText(requestObj.toString());
            request.setJMSReplyTo(tempDest);
            seedProducer.send(seedQueue, request);

            TextMessage response = (TextMessage) responseConsumer.receive();
            String msgText = ((TextMessage) response).getText();
            JSONObject responseObj = new JSONObject(msgText);
            uri = responseObj.getString("uri");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return uri;
    }

    public static String getHashFromMagnetURI(String uri) {
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
        downloadByHash(hash, null, null);
    }

    public void download(final String uri, final String ext) {
        String hash = getHashFromMagnetURI(uri);
        downloadByHash(hash, ext, null);
    }

    public void downloadByHash(final String hash) {
        downloadByHash(hash, null, null);
    }

    public void downloadByHash(final String hash, final String ext,
            final MessageListener listener) {
        try {
            Destination tempDest = downloadSession.createTemporaryQueue();
            MessageConsumer responseConsumer = downloadSession
                    .createConsumer(tempDest);
            if (listener != null) {
                responseConsumer.setMessageListener(listener);
            } else {
                responseConsumer.setMessageListener(new MessageListener() {
                    @Override
                    public void onMessage(Message response) {
                    }
                });
            }

            String file = CACHE_FOLDER + File.separator + hash;
            if (ext != null)
                file += ext;
            JSONObject sharedFile = new JSONObject();
            sharedFile.put("uri", "magnet:?xt=urn:btih:" + hash);
            sharedFile.put("file", file);

            TextMessage message = downloadSession.createTextMessage();
            message.setText(sharedFile.toString());
            message.setJMSReplyTo(tempDest);
            downloadProducer.send(downloadQueue, message);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
