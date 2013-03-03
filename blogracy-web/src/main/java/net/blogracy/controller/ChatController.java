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
	private Session session;
	private Destination topic;
	private MessageProducer producer;
	private static final String TOPIC_NAME = "CHAT.DEMO";

	private String localUser;
	private String remoteUser;

	private static final ChatController theInstance = new ChatController();

	public static ChatController getSingleton() {
		return theInstance;
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

	public void joinChannel(String channel) {
		System.out.println("Creating chat channel: " + channel);
		try {
			TextMessage msg = session.createTextMessage();
			msg.setText("<message type=\"join\" from=\"" + localUser + "\" channel=\"" + channel + "\"/>");
			producer.send(topic, msg);
		} catch (JMSException e) {
			System.out.println("JMS error: sending messages");
		}
	}

	public static String getPrivateChannel(String localUser, String remoteUser) {
		if (localUser.compareTo(remoteUser) <= 0) {
			return localUser + remoteUser;
		}
		return remoteUser + localUser;
	}
}
