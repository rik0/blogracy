package net.blogracy.web;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;

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
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;

import net.blogracy.config.Configurations;
import net.blogracy.model.feeds.Feeds;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.codec.binary.Base32;

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

public class FileUpload extends HttpServlet {

	private String cacheFolder;

	private ConnectionFactory connectionFactory;
	private Connection connection;
	private Session session;
	private Destination seedQueue;
	private Destination storeQueue;
	private MessageProducer producer;
	private MessageConsumer consumer;


	public static String hash(String text) throws NoSuchAlgorithmException {
		MessageDigest digester = MessageDigest.getInstance("SHA-1");
		Base32 encoder = new Base32();
		byte[] digest = digester.digest(text.getBytes());
		String result = encoder.encodeAsString(digest);
		return result;
	}

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		try {
			cacheFolder = Configurations.getPathConfig().getCachedFilesDirectoryPath(); 
			connectionFactory = new ActiveMQConnectionFactory(ActiveMQConnection.DEFAULT_BROKER_URL);
			connection = connectionFactory.createConnection();
			connection.start();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			producer = session.createProducer(null);
			producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
			seedQueue = session.createQueue("seed");
			storeQueue = session.createQueue("store");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void addFeedEntry(String user, String uri, String text, String attachment) {
		try {
			final SyndFeed feed = Feeds.getFeed(user);

			SyndEntry entry = new SyndEntryImpl();
			entry.setTitle("No Title");
			entry.setLink(uri);
			entry.setPublishedDate(new Date());
			SyndContent description = new SyndContentImpl();
			description.setType("text/plain");
			description.setValue(text);
			entry.setDescription(description);
			/*
        	SyndLink link = new SyndLinkImpl();
        	link.setHref(uri.toString());
        	link.setTitle(uri.toString());
        	ArrayList links = new ArrayList();
        	links.add(link);
        	entry.setLinks(links);
			 */
			if (attachment != null) {
				SyndEnclosure enclosure = new SyndEnclosureImpl();
				enclosure.setUrl(attachment);
				ArrayList enclosures = new ArrayList();
				enclosures.add(enclosure);
				entry.setEnclosures(enclosures);
			}

			feed.getEntries().add(0, entry);
			final File feedFile = new File(cacheFolder + File.separator + user + ".rss");
			new SyndFeedOutput().output(feed, new PrintWriter(feedFile));
			// TODO: sign feedFile

			// URL feedUri = shareFile(feedFile);
			try {
				Destination tempDest = session.createTemporaryQueue();
				MessageConsumer responseConsumer = session.createConsumer(tempDest);
				responseConsumer.setMessageListener(new MessageListener() {
					@Override
					public void onMessage(Message response) {
						try {
							final String user = hash("mic");
							String msgText = ((TextMessage) response).getText();
							// store msgText @ user
							TextMessage message = session.createTextMessage();
							message.setText(user + "=" + msgText);
							producer.send(storeQueue, message);                          

							// create a copy of latest feed, named after its author
							new SyndFeedOutput().output(feed, new PrintWriter(feedFile));
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});

				TextMessage message = session.createTextMessage();
				message.setText(feedFile.getAbsolutePath());
				message.setJMSReplyTo(tempDest);
				producer.send(seedQueue, message);
			} catch (Exception e) {
				e.printStackTrace();
			}

		} catch (Exception e) { e.printStackTrace(); }
	}

	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		StringBuffer buff = new StringBuffer();

		File file = (File) req.getAttribute("userfile");
		File textFile = null;

		if (file == null || ! file.exists()) {
			buff.append("File does not exist");
		} else if( file.isDirectory()) {
			buff.append("File is a directory");
		} else {
			File outputFile = new File(cacheFolder + File.separator + req.getParameter("userfile"));
			file.renameTo(outputFile);
			file = outputFile;
			buff.append("File successfully uploaded");
		}

		final String text = req.getParameter("usertext").trim();
		if (text.length() > 0) {
			try {
				String hash = hash(text);
				textFile = new File(cacheFolder + File.separator + hash + ".txt");

				java.io.FileWriter w = new java.io.FileWriter(textFile);
				w.write(text);
				w.close();		        
			} catch (Exception e) {
				e.printStackTrace();
			}
			//torrentMagnetURI = shareFile(new File(fullFileName));
		}

		try {
			Destination tempDest = session.createTemporaryQueue();
			MessageConsumer responseConsumer = session.createConsumer(tempDest);
			responseConsumer.setMessageListener(new MessageListener() {
				@Override
				public void onMessage(Message response) {
					try {
						final String user = hash("mic");
						String uri = null; 
						String attachment = null; 
						String msgText = ((TextMessage) response).getText();
						String[] lines = msgText.split("\\r?\\n");
						if (lines.length > 0) uri = lines[0];
						if (lines.length > 1) attachment = lines[1];
						addFeedEntry(user, uri, text, attachment);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

			StringBuffer files = new StringBuffer();
			if (textFile != null) files.append(textFile.getAbsolutePath());
			files.append('\n');
			if (file != null) files.append(file.getAbsolutePath());
			TextMessage message = session.createTextMessage();
			message.setText(files.toString());
			message.setJMSReplyTo(tempDest);
			producer.send(seedQueue, message);
		} catch (Exception e) {
			e.printStackTrace();
		}

		PrintWriter outp = resp.getWriter(); 
		outp.write("<html>");
		outp.write("<head><title>FileUpload page</title></head>");
		outp.write("<body>");
		outp.write("<h2>" + buff.toString() + "</h2>");
		outp.write("</body>");
		outp.write("</html>");
	}

}
