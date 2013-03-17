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
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

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
import org.gudy.azureus2.plugins.download.DownloadListener;
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
	class DownloadCompletedListener implements DownloadListener {
		private TextMessage request;

		DownloadCompletedListener(TextMessage request) {
			this.request = request;
		}

		@Override
		public void positionChanged(Download download, int oldPosition,
				int newPosition) {
			// Don't care much about this event at the moment...
		}

		@Override
		public void stateChanged(Download download, int old_state, int new_state) {
			// if the download is seeding, it has been completed
			if (download.isComplete()) {
				try {
					if (request.getJMSReplyTo() != null) {
						JSONObject record = new JSONObject(request.getText());
						TextMessage response = session.createTextMessage();
						response.setText(record.toString());
						response.setJMSCorrelationID(request
								.getJMSCorrelationID());
						producer.send(request.getJMSReplyTo(), response);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (JMSException e) {
					e.printStackTrace();
				}
			}

		}

	}

	private PluginInterface plugin;

	private Session session;
	private Destination queue;
	private MessageProducer producer;
	private MessageConsumer consumer;

	public DownloadService(Connection connection, PluginInterface plugin) {
		this.plugin = plugin;
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
	public void onMessage(Message request) {
		try {
			String text = ((TextMessage) request).getText();
			Logger.info("download service:" + text + ";");
			JSONObject entry = new JSONObject(text);
			try {
				URL magnetUri = new URL(entry.getString("uri"));
				File file = new File(entry.getString("file"));
				File folder = file.getParentFile();

				Download download = null;
				ResourceDownloader rdl = plugin.getUtilities()
						.getResourceDownloaderFactory().create(magnetUri);
				InputStream is = rdl.download();
				Torrent torrent = plugin.getTorrentManager()
						.createFromBEncodedInputStream(is);
				DownloadCompletedListener listener = new DownloadCompletedListener(
						(TextMessage) request);
				download = plugin.getDownloadManager().addDownload(
						torrent, null, folder);
				download.addListener(listener);

				if (download != null && file != null)
					download.renameDownload(file.getName());
				
				// Force signaling completion if already completed.
				if (download.isComplete())
					listener.stateChanged(download, Download.ST_SEEDING,
							Download.ST_SEEDING);

				Logger.info(magnetUri + " added to download list");
			} catch (ResourceDownloaderException e) {
				Logger.error("Torrent download error: download service: "
						+ text);
			} catch (TorrentException e) {
				Logger.error("Torrent error: download service: " + text);
			} catch (DownloadException e) {
				Logger.error("File download error: download service: " + text);
			} catch (MalformedURLException e) {
				Logger.error("Malformed URL error: download service: " + text);
			}
		} catch (JMSException e) {
			Logger.error("JMS error: download service");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

}
