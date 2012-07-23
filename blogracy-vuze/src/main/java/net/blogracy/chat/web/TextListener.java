/*
 * Created by Andrea Vida
 * University of Parma (Italy)
 */
package net.blogracy.chat.web;

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

import org.xml.sax.InputSource;
import org.gudy.azureus2.plugins.download.Download;
import org.w3c.dom.*;
import java.io.*;

import net.blogracy.chat.ChatManager;


public class TextListener implements MessageListener{

	 private ChatManager plugin;
	 private Download download;
	
	 private Session session;
	 private Destination topic;
	 private MessageProducer producer;
	 private MessageConsumer consumer;
	 
	 private String topicName = "CHAT.DEMO";
	 private String channelName = "";
	 private String lastMessageSend = "";
	 private String lastMessageReceived = "";
	
	public TextListener(Connection connection, ChatManager plugin, Download download, String channelName){
	        this.plugin = plugin;
	        this.download = download;
	        this.channelName = channelName;
	        try {
	            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
	            topic = session.createTopic(topicName);
	            producer = session.createProducer(topic);
	            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
	            consumer = session.createConsumer(topic);
	            consumer.setMessageListener(this);
	        } catch (JMSException e) {
	        	System.out.println("JMS error: creating the text listener");
	        }
	    }
	
	@Override
	public void onMessage(Message message) {
		if (message instanceof TextMessage) {
			try {
				String text = ((TextMessage) message).getText();
				if (floodControl(text)) {/*System.out.println("Flooding");*/}
				else {
					parseXML(text);
					lastMessageSend = text;
					//System.out.println("Outgoing Message: " + text);
				}
			} catch (JMSException e) {
				System.out.println("JMS error: reading messages");
			}
		}
		else System.out.println("This is not a TextMessage");
	}
	
	public void msgfromVuze(String text,String nick,String type){
		String XMLmessage = createXML(text,nick,type);
		//System.out.println("Incoming Message: " + XMLmessage);
		lastMessageReceived = XMLmessage;
		
		TextMessage message;
		try {
			message = session.createTextMessage();
			message.setText(XMLmessage);
	        producer.send(topic, message);
		} catch (JMSException e) {
			System.out.println("JMS error: sending messages");
		}
	}
	
	private void parseXML(String XMLstring){
		try {
	        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	        DocumentBuilder db = dbf.newDocumentBuilder();
	        InputSource is = new InputSource();
	        is.setCharacterStream(new StringReader(XMLstring));
	        Document doc = db.parse(is);
	        
	        NodeList nodes = doc.getElementsByTagName("message");
	        Element element = (Element) nodes.item(0);
	        String type = element.getAttribute("type");
	        String nick = element.getAttribute("from");
	        String channel = element.getAttribute("channel");
	        String text = element.getTextContent();
	        if (channel.equals(channelName)) msgforVuze(type,nick,text);
	    }
	    catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	private String createXML(String text,String nick,String type){
		try {
	        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	        DocumentBuilder db = dbf.newDocumentBuilder();
	        Document doc = db.newDocument();
	        
	        Element element = doc.createElement("message");
	        element.setAttribute("type",type);
	        element.setAttribute("from",nick);
	        element.setAttribute("channel",channelName);
	        if(type.equals("chat")) element.setTextContent(text);
	        
	        Source source = new DOMSource(element);
            StringWriter stringWriter = new StringWriter();
            Result result = new StreamResult(stringWriter);
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            transformer.transform(source, result);
            String XMLstring1 = stringWriter.getBuffer().toString();
            String XMLstring2 = XMLstring1.substring(38);
            return XMLstring2;
	    }
	    catch (Exception e) {
	        e.printStackTrace();
	        return "nothing";
	    }
	}
	
	private void msgforVuze(String type,String nick,String text){
		if(type.equals("chat")) plugin.sendMessage(download, text);
		
		if(type.equals("join")) {
			plugin.setNick(nick);
		}
		
		if(type.equals("leave")){
			plugin.sendMessage(download, "has left the channel");
			if (nick.equals(plugin.getNick()))plugin.closeBridge(channelName);
		}
		
	}
	
	private boolean floodControl(String text){
		if (text.equals(lastMessageReceived)) return true;
		if (text.equals(lastMessageSend)) return true;
		return false;
	}

}
