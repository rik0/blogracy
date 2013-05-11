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
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.FileHandler;
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

import org.gudy.azureus2.plugins.PluginInterface;
import org.gudy.azureus2.plugins.download.Download;
import org.gudy.azureus2.plugins.download.DownloadCompletionListener;
import org.gudy.azureus2.plugins.download.DownloadException;
import org.gudy.azureus2.plugins.torrent.Torrent;
import org.gudy.azureus2.plugins.torrent.TorrentException;
import org.gudy.azureus2.plugins.utils.resourcedownloader.ResourceDownloader;
import org.gudy.azureus2.plugins.utils.resourcedownloader.ResourceDownloaderException;
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
public class DownloadService implements MessageListener {

	class CompletionListener implements DownloadCompletionListener {
		private TextMessage request;
		private long cron;
		private long started;

		CompletionListener(TextMessage request, long cron) {
			this.request = request;
			this.cron = cron;
			this.started = System.currentTimeMillis() - cron;
			try {
				long delay = System.currentTimeMillis() - cron;
				log.info("download-started " + delay + " " + request.getText());
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}

		public void onCompletion(Download d) {
			try {
				long completed = System.currentTimeMillis() - cron;
				log.info("download-completed " + completed + " " + started + " " + request.getText());
			} catch (JMSException e) {
				e.printStackTrace();
			}
			try {
				TextMessage response = session.createTextMessage();
				response.setText(request.getText());
				response.setJMSCorrelationID(request.getJMSCorrelationID());
				producer.send(request.getJMSReplyTo(), response);
			} catch (JMSException e) {
				Logger.error("JMS error: download completion");
			}
		}
	}

	private PluginInterface vuze;
	private java.util.logging.Logger log;

	private Session session;
	private Destination queue;
	private MessageProducer producer;
	private MessageConsumer consumer;

	public DownloadService(Connection connection, PluginInterface vuze) {
		try {
			log = java.util.logging.Logger.getLogger("net.blogracy.services.download");
			log.addHandler(new java.util.logging.FileHandler("download.log"));
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
			queue = session.createQueue("download");
			consumer = session.createConsumer(queue);
			consumer.setMessageListener(this);
		} catch (JMSException e) {
			Logger.error("JMS error: creating download service");
		}

	}

	@Override
	public void onMessage(final Message request) {
		(new Thread() {
			public void run() {
				try {
					String text = ((TextMessage) request).getText();
					final long cron = System.currentTimeMillis();
					log.info("download-requested " + text);
					Logger.info("download service:" + text + ";");
					final JSONObject entry = new JSONObject(text);
					try {
						URL magnetUri = new URL(entry.getString("uri"));
						File file = new File(entry.getString("file"));
						File folder = file.getParentFile();

						Download download = null;
						ResourceDownloader rdl = vuze.getUtilities().getResourceDownloaderFactory().create(magnetUri);
						InputStream is = rdl.download();
						Torrent torrent = vuze.getTorrentManager().createFromBEncodedInputStream(is);

						download = vuze.getDownloadManager().addDownload(torrent, null, folder);

						if (download != null && file != null) {
							download.renameDownload(file.getName());
							download.addCompletionListener(new CompletionListener((TextMessage) request, cron));
							download.setForceStart(true);
							Logger.info(magnetUri + " added to download list");
						} else {
							Logger.info(magnetUri + " *not* added to download list");
						}
					} catch (ResourceDownloaderException e) {
						Logger.error("Torrent download error: download service: " + text + " " + e.getMessage());
					} catch (TorrentException e) {
						Logger.error("Torrent error: download service: " + text + " " + e.getMessage());
					} catch (DownloadException e) {
						Logger.error("File download error: download service: " + text + " " + e.getMessage());
					} catch (MalformedURLException e) {
						Logger.error("Malformed URL error: download service: " + text + " " + e.getMessage());
					}
				} catch (JMSException e) {
					Logger.error("JMS error: download service" + " " + e.getMessage());
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

}
