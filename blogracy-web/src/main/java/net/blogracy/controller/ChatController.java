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
    private static Destination hashQueue;
    private static MessageProducer producer;
    
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
            producer = session.createProducer(null);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            hashQueue = session.createQueue("chatService");
        } catch (Exception e) {
        	System.out.println("JMS error: creating the text listener");
        }
    }
    
    public static void setLocalUser(String local) {
    	if (!local.equals(localUser)){
	    	localUser = local;
	    	System.out.println("Creating chat channel for: " + localUser);
	    	TextMessage msg;
			try {
				msg = session.createTextMessage();
				msg.setText(localUser);
		        producer.send(hashQueue, msg);
			} catch (JMSException e) {
				System.out.println("JMS error: sending messages");
			}
    	}
    }
    
    public static void setRemoteUser(String remote) {
    	remoteUser = remote;
    	System.out.println("Remote user hash: " + remoteUser);     
    }
    
    public static String getRemoteUser() {
    	return remoteUser;
    }
    
    public static void chatting(){
    	if (!remoteUser.equals(localUser)){
	    	TextMessage msg;
			try {
				msg = session.createTextMessage();
				msg.setText(remoteUser);
		        producer.send(hashQueue, msg);
		        System.out.println("Connecting to channel " + remoteUser);
			} catch (JMSException e) {
				System.out.println("JMS error: sending messages");
			}
    	}
    }
    
    public static void privateChatting(){
    	if (!remoteUser.equals(localUser)){
	    	TextMessage msg;
			try {
				msg = session.createTextMessage();
				msg.setText(localUser + remoteUser);
		        producer.send(hashQueue, msg);
		        System.out.println("Creating a private channel");
			} catch (JMSException e) {
				System.out.println("JMS error: sending messages");
			}
    	}
    }
    
    public static void privateChatting2(){
    	if (!remoteUser.equals(localUser)){
	    	TextMessage msg;
			try {
				msg = session.createTextMessage();
				msg.setText(remoteUser + localUser);
		        producer.send(hashQueue, msg);
		        System.out.println("Connecting to a private channel");
			} catch (JMSException e) {
				System.out.println("JMS error: sending messages");
			}
    	}
    }
}
