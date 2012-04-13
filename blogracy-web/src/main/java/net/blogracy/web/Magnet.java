package net.blogracy.web;
 
import java.io.File;
import java.io.IOException;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.blogracy.config.Configurations;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
 
public class Magnet extends HttpServlet
{

	private String cacheFolder;

	private ConnectionFactory connectionFactory;
	private Connection connection;
	private Session session;
	private Destination downloadQueue;
	private MessageProducer producer;
	private MessageConsumer consumer;


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
			downloadQueue = session.createQueue("download");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
    	String hash = request.getParameter("hash");
    	File file = new File(cacheFolder + File.separator + hash);
    	if (file.exists()) {
    		//response.sendRedirect("/cache/" + hash);
    		response.setContentType("video/mp4"); // TODO! ...
    		request.getRequestDispatcher("/cache/" + hash).forward(request, response);
    	} else {
			try {
                int dot = hash.lastIndexOf(".");
                if (dot >= 0) {
                    hash = hash.substring(0, dot);
                }

	    		TextMessage message = session.createTextMessage();
	    		message.setText("magnet:?xt=urn:btih:" + hash + " " + file.getAbsolutePath());
	    		producer.send(downloadQueue, message);
			} catch (JMSException e) {
				e.printStackTrace();
			}
    		response.sendError(503);
    	}
    }
}
