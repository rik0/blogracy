/*
 * Created by Andrea Vida
 * University of Parma (Italy)
 */
package net.blogracy.services;

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

import javax.xml.parsers.*;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.blogracy.logging.Logger;

import org.xml.sax.InputSource;
import org.gudy.azureus2.plugins.PluginInterface;
import org.gudy.azureus2.plugins.download.Download;
import org.w3c.dom.*;

import com.aelitis.azureus.plugins.chat.ChatPlugin;

import java.io.*;
import java.util.HashMap;


public class ChatService implements javax.jms.MessageListener, com.aelitis.azureus.plugins.chat.messaging.MessageListener {
	private PluginInterface vuze;

	private Session session;
	private Destination topic;
	private MessageProducer producer;
	private MessageConsumer consumer;

	private String TOPIC_NAME = "CHAT.DEMO";
	private String lastMessageSent = "";
	private String lastMessageReceived = "";

	private String lastMsg = "";

	private HashMap<String, Download> channels = new HashMap<String, Download>();

	public ChatService(Connection connection, PluginInterface vuze) {
		this.vuze = vuze;
		try {
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			topic = session.createTopic(TOPIC_NAME);
			producer = session.createProducer(topic);
			producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
			consumer = session.createConsumer(topic);
			consumer.setMessageListener(this);
		} catch (JMSException e) {
			Logger.error("JMS error: creating the text listener");
		}
	}

	@Override
	public void onMessage(Message message) {
		if (message instanceof TextMessage) {
			try {
				String text = ((TextMessage) message).getText();
				parseXML(text);
			} catch (JMSException e) {
				Logger.error("JMS error: reading messages");
			}
		}
		else {
			Logger.error("This is not a TextMessage");
		}
	}

	@Override
	public void downloadAdded(Download download) {}

	@Override
	public void downloadRemoved(Download download) {}

	@Override
	public void downloadActive(Download download) {}

	@Override
	public void downloadInactive(Download download) {}

	@Override
	public void messageReceived(Download download, byte[] sender, String nick, String text) {
		for (String channel : channels.keySet()) {
			if (channels.get(channel) == download) {
				String type = "chat";
				if (text.equals("has left the channel")) type = "leave";
				msgFromVuze(channel, type, nick, text);
			}
		}
	}

	public void msgFromVuze(String channel, String type, String nick, String text){

		try {
			if (floodControl(channel, type, nick, text)) {
				String xml = createXML(channel, type, nick, text);
				Logger.info("Incoming Message: " + xml);
				TextMessage message = session.createTextMessage();
				message.setText(xml);
				producer.send(topic, message);
			}
		} catch (JMSException e) {
			Logger.error("JMS error: sending messages");
		}
	}

	private void parseXML(String xml){
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(xml));
			Document doc = db.parse(is);

			NodeList nodes = doc.getElementsByTagName("message");
			Element element = (Element) nodes.item(0);
			String channel = element.getAttribute("channel");
			String type = element.getAttribute("type");
			String nick = element.getAttribute("from");
			String text = element.getTextContent();

			if (floodControl(channel, type, nick, text)) {
				Logger.info("Outgoing Message: " + xml);
				msgToVuze(channel, type, nick, text);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String createXML(String channel, String type, String nick, String text){
		String xml = "";
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.newDocument();

			Element element = doc.createElement("message");
			element.setAttribute("type", type);
			element.setAttribute("from", nick);
			element.setAttribute("channel", channel);
			if (type.equals("chat")) element.setTextContent(text);

			Source source = new DOMSource(element);
			StringWriter stringWriter = new StringWriter();
			Result result = new StreamResult(stringWriter);
			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer();
			transformer.transform(source, result);
			xml = stringWriter.getBuffer().toString().substring(38);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return xml;
	}

	private void msgToVuze(String channel, String type, String nick, String text) {
		ChatPlugin plugin = (ChatPlugin) vuze.getPluginManager()
				.getPluginInterfaceByID("chat").getPlugin();

		if (type.equals("chat")) {
			Download download = channels.get(channel);
			if (download != null) {
				plugin.sendMessage(download, text);				
			}
		} else if (type.equals("join")) {
			Logger.info("Joining channel: " + channel);

			plugin.setNick(nick);
			Download download = plugin.addChannel(channel);
			channels.put(channel, download);
			plugin.addMessageListener(this, download);
			Logger.info("chat request: " + channel);

		} else if (type.equals("leave")){
			Download download = channels.get(channel);
			if (download != null) {
				plugin.sendMessage(download, "has left the channel");
			}
			if (nick.equals(plugin.getNick())) {
				// plugin.closeBridge(channelName);
			}
		}
	}

	private boolean floodControl(String channel, String type, String nick, String text) {
		String msg = channel + ":" + type + ":" + nick + ":" + text;
		if (! lastMsg.equals(msg)) {
			lastMsg = msg;
			return true;
		}
		return false;
	}
}
