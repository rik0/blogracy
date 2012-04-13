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

package it.unipr.aotlab.blogracy.services;

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

import org.gudy.azureus2.plugins.PluginInterface;
import org.gudy.azureus2.plugins.ddb.DistributedDatabase;
import org.gudy.azureus2.plugins.ddb.DistributedDatabaseException;
import org.gudy.azureus2.plugins.ddb.DistributedDatabaseKey;
import org.gudy.azureus2.plugins.ddb.DistributedDatabaseValue;

import it.unipr.aotlab.blogracy.logging.Logger;

/**
 * User: mic
 * Package: it.unipr.aotlab.blogracy.services
 * Date: 10/27/11
 * Time: 12:36 PM
 */

/**
 * ...
 */
public class StoreService implements MessageListener {
	
	private PluginInterface plugin;
	
    private Session session;
    private Destination queue;
    private MessageProducer producer;
    private MessageConsumer consumer;

    public StoreService(Connection connection, PluginInterface plugin) {
    	this.plugin = plugin;
    	try {
	        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
	        producer = session.createProducer(null);
	        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);	        
			queue = session.createQueue("store");
	        consumer = session.createConsumer(queue);
	        consumer.setMessageListener(this);
    	} catch (JMSException e) {
    		Logger.error("JMS error: creating store service");
    	}
    }


	@Override
	public void onMessage(Message request) {
		try {
			String text = ((TextMessage)request).getText();
			try {
				final String[] kv = ((TextMessage)request).getText().split("=", 2);
				
	            DistributedDatabase ddb = plugin.getDistributedDatabase();
	            DistributedDatabaseKey key = ddb.createKey(kv[0].trim());
	            DistributedDatabaseValue value = ddb.createValue(kv[1].trim());
	
	            ddb.write(null, key, new DistributedDatabaseValue[] {value});
	        } catch (JMSException e) {
	        	Logger.error("JMS error: store service: " + text);
	        } catch (DistributedDatabaseException e) {
	        	Logger.error("DDB error: store service: " + text);
	        }
		} catch (JMSException e) {
            Logger.error("JMS error: store service");
		}
	}

}
