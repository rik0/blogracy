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
                download = plugin.getDownloadManager().addDownload(torrent,
                        null, folder);
                if (download != null && file != null)
                    download.renameDownload(file.getName());
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
