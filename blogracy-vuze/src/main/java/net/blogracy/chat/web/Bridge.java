/*
 * Created by Andrea Vida
 * University of Parma (Italy)
 */
package net.blogracy.chat.web;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.gudy.azureus2.plugins.download.Download;

import net.blogracy.chat.ChatManager;

public class Bridge{
	
	@SuppressWarnings("unused")
	private ChatManager plugin;
	private Download download;
	private String channelName;
	
	private ConnectionFactory connectionFactory;
    private Connection connection;
    
    private TextListener textListener;
	
    //create a Bridge item and set up connections with JMS
	public Bridge(ChatManager plugin,Download download,String channelName){
		this.plugin = plugin;
		this.download = download;
		this.channelName = channelName;
		
		BasicConfigurator.configure();
	    org.apache.log4j.Logger.getLogger("org.apache").setLevel(Level.INFO);
		String brokerUrl = ActiveMQConnection.DEFAULT_BROKER_URL;
		
		try {
			connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
            connection = connectionFactory.createConnection();
            connection.start();
            textListener = new TextListener(connection,plugin,download,channelName);        
        } catch (JMSException e) {
        	System.out.println("JMS error: creating JMS connections");
        }
	}
	
	public void inMsg(String message,String nick) {
		if(message.equals("has left the channel")){
			textListener.msgfromVuze(message,nick,"leave");
		}					
		else textListener.msgfromVuze(message,nick,"chat");	
	}
	
	public void sysMsg(String message) {
		System.out.println("System Message: " + message);
	}
	
	public String getChannelName(){
		return this.channelName;
	}
	
	public Download getDownload(){
		return this.download;
	}
	
	//destroy a Bridge item and close the connection
	public void finalize(){
		try {
			connection.close();
			textListener = null;
		} catch (JMSException e) {
			System.out.println("JMS error: closing JMS connection");}
	}
	
}
