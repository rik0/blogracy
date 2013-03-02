/*
 * Created by Andrea Vida
 * University of Parma (Italy)
 * 
 */
package net.blogracy.controller;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

public class ChatController {

	private ConnectionFactory connectionFactory;
	private Connection connection;
	private static Session session;
	private static Destination topic;
	private static MessageProducer producer;
	private String TOPIC_NAME = "CHAT.DEMO";

	private static String localUser, remoteUser;

	private static final ChatController THE_INSTANCE = new ChatController();

	public static ChatController getSingleton() {
		return THE_INSTANCE;
	}

	public ChatController() {
		try {
			connectionFactory = new ActiveMQConnectionFactory(ActiveMQConnection.DEFAULT_BROKER_URL);
			connection = connectionFactory.createConnection();
			connection.start();
			
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			topic = session.createTopic(TOPIC_NAME);
			producer = session.createProducer(topic);
			producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
		} catch (Exception e) {
			System.out.println("JMS error: creating the text listener");
		}
	}

	public static void setLocalUser(String local) {
		if (! local.equals(localUser)) {
			localUser = local;
			createChannel(localUser);
		}
	}

	public static void createChannel(String channel) {
		System.out.println("Creating chat channel: " + channel);
		TextMessage msg;
		try {
			msg = session.createTextMessage();
			msg.setText("<message type=\"join\" from=\"" + localUser + "\" channel=\"" + channel + "\"/>");
			producer.send(topic, msg);
		} catch (JMSException e) {
			System.out.println("JMS error: sending messages");
		}
	}

	public static void setRemoteUser(String remote) {
		remoteUser = remote;
		System.out.println("Remote user hash: " + remoteUser);     
	}

	public static String getRemoteUser() {
		return remoteUser;
	}

	public static void chatting() {
		if (! remoteUser.equals(localUser)) {
			createChannel(remoteUser);
		}
	}

	public static void privateChatting() {
		if (! remoteUser.equals(localUser)) {
			createChannel(localUser + remoteUser);
		}
	}

	public static void privateChatting2() {
		if (! remoteUser.equals(localUser)) {
			createChannel(remoteUser + localUser);
		}
	}
}
