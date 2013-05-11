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

package net.blogracy.services;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.SimpleFormatter;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import net.blogracy.logging.Logger;

import org.gudy.azureus2.core3.util.Base32;
import org.gudy.azureus2.core3.util.ByteFormatter;
import org.gudy.azureus2.plugins.PluginInterface;
import org.gudy.azureus2.plugins.download.Download;
import org.gudy.azureus2.plugins.download.DownloadCompletionListener;
import org.gudy.azureus2.plugins.download.DownloadException;
import org.gudy.azureus2.plugins.torrent.Torrent;
import org.gudy.azureus2.plugins.torrent.TorrentException;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * User: mic
 * Package: it.unipr.aotlab.blogracy.services
 * Date: 10/27/11
 * Time: 12:36 PM
 */

/**
 * ...
 */
public class SeedService implements MessageListener {
	
	class CompletionListener implements DownloadCompletionListener {
		private TextMessage request;
		private long cron;
		private long started;

		CompletionListener(TextMessage request, long cron) {
			this.request = request;
			this.cron = cron;
			this.started = System.currentTimeMillis() - cron;
			try {
				log.info("seed-started " + started + " " + request.getText());
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}

		public void onCompletion(Download d) {
			try {
				long completed = System.currentTimeMillis() - cron;
				log.info("seed-completed " + completed + " " + started + " " + request.getText());
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}
	}


    private PluginInterface vuze;
    private java.util.logging.Logger log;

    private Session session;
    private Destination queue;
    private MessageProducer producer;
    private MessageConsumer consumer;

    public SeedService(Connection connection, PluginInterface vuze) {
        try {
            log = java.util.logging.Logger.getLogger("net.blogracy.services.seed");
			log.addHandler(new java.util.logging.FileHandler("seed.log"));
	        log.getHandlers()[0].setFormatter(new SimpleFormatter());
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
        

        this.vuze = vuze;
        try {
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            producer = session.createProducer(null);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            queue = session.createQueue("seed");
            consumer = session.createConsumer(queue);
            consumer.setMessageListener(this);
        } catch (JMSException e) {
            Logger.error("JMS error: creating seed service");
        }
    }

    @Override
    public void onMessage(Message request) {
        try {
            String text = ((TextMessage) request).getText();
            final long cron = System.currentTimeMillis();
			log.info("seed-requested " + text);
            Logger.info("seed service:" + text + ";");
            JSONObject entry = new JSONObject(text);
            try {
                File file = new File(entry.getString("file"));

                /*Torrent torrent = plugin.getTorrentManager()
                        .createFromDataFile(file,
                                new URL("udp://tracker.openbittorrent.com:80")); */
                Torrent torrent = vuze.getTorrentManager().createFromDataFile( file, new URL("dht:"));
    			torrent.setAnnounceURL(new URL("dht://" + ByteFormatter.encodeString(torrent.getHash()) + ".dht/announce"));
                torrent.setComplete(file.getParentFile());

                String name = Base32.encode(torrent.getHash());
                int index = file.getName().lastIndexOf('.');
                if (0 < index && index <= file.getName().length() - 2) {
                    name = name + file.getName().substring(index);
                }

                Download download = vuze.getDownloadManager().addDownload(
                        torrent, null, // torrentFile,
                        file.getParentFile());
                if (download != null) {
                    download.renameDownload(name);
                    download.addCompletionListener(new CompletionListener((TextMessage) request, cron));
                    download.setForceStart(true); 
                }
                entry.put("uri", torrent.getMagnetURI().toExternalForm());

                if (request.getJMSReplyTo() != null) {
                    TextMessage response = session.createTextMessage();
                    response.setText(entry.toString());
                    response.setJMSCorrelationID(request.getJMSCorrelationID());
                    producer.send(request.getJMSReplyTo(), response);
                }
            } catch (MalformedURLException e) {
                Logger.error("Malformed URL error: seed service " + text);
            } catch (TorrentException e) {
                Logger.error("Torrent error: seed service: " + text);
                e.printStackTrace();
            } catch (DownloadException e) {
                Logger.error("Download error: seed service: " + text);
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (JMSException e) {
            Logger.error("JMS error: seed service");
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
