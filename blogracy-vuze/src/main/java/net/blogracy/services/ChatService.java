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

import net.blogracy.chat.ChatManager;
import net.blogracy.logging.Logger;

import org.gudy.azureus2.plugins.PluginInterface;

public class ChatService implements MessageListener {
	
	@SuppressWarnings("unused")
	private PluginInterface plugin;
	private ChatManager chatManager;

    private Session session;
    private Destination queue;
    private MessageProducer producer;
    private MessageConsumer consumer;
    
    public ChatService(Connection connection, ChatManager cm, PluginInterface plugin) {
        this.plugin = plugin;
        this.chatManager = cm;
        try {
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            producer = session.createProducer(null);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            queue = session.createQueue("chatService");
            consumer = session.createConsumer(queue);
            consumer.setMessageListener(this);
        } catch (JMSException e) {
            Logger.error("JMS error: creating chat service");
        }
    }

	@Override
	public void onMessage(Message request) {
		try {
            String channelName = ((TextMessage) request).getText();
	        chatManager.startNewChannel(channelName);
	        Logger.info("chat request by " + channelName + ";");

		}
		catch (JMSException e) {
			Logger.error("JMS error: seed service");
		}
		
	}

}
