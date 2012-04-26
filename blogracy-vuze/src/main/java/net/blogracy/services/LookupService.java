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

import net.blogracy.logging.Logger;

import org.gudy.azureus2.plugins.PluginInterface;
import org.gudy.azureus2.plugins.ddb.DistributedDatabase;
import org.gudy.azureus2.plugins.ddb.DistributedDatabaseEvent;
import org.gudy.azureus2.plugins.ddb.DistributedDatabaseException;
import org.gudy.azureus2.plugins.ddb.DistributedDatabaseListener;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * User: mic
 * Package: net.blogracy.services
 * Date: 10/27/11
 * Time: 12:36 PM
 */

/**
 * ...
 */
public class LookupService implements MessageListener {

    class DhtListener implements DistributedDatabaseListener {
        private TextMessage request;

        DhtListener(TextMessage request) {
            this.request = request;
        }

        public void event(DistributedDatabaseEvent event) {
            int type = event.getType();
            if (type == DistributedDatabaseEvent.ET_OPERATION_COMPLETE) {
                // ...
            } else if (type == DistributedDatabaseEvent.ET_OPERATION_TIMEOUT) {
                // ...
            } else if (type == DistributedDatabaseEvent.ET_VALUE_READ) {
                String value = null;
                try {
                    value = (String) event.getValue().getValue(String.class);
                    JSONObject record = new JSONObject(request.getText());
                    record.put("value", value);
                    TextMessage response = session.createTextMessage();
                    response.setText(record.toString());
                    response.setJMSCorrelationID(request.getJMSCorrelationID());
                    producer.send(request.getJMSReplyTo(), response);
                } catch (JMSException e) {
                    Logger.error("JMS error: lookup " + value);
                } catch (DistributedDatabaseException e) {
                    Logger.error("DDB error: lookup " + value);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private PluginInterface plugin;

    private Session session;
    private Destination queue;
    private MessageProducer producer;
    private MessageConsumer consumer;

    public LookupService(Connection connection, PluginInterface plugin) {
        this.plugin = plugin;
        try {
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            producer = session.createProducer(null);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            queue = session.createQueue("lookup");
            consumer = session.createConsumer(queue);
            consumer.setMessageListener(this);
        } catch (JMSException e) {
            Logger.error("JMS error: creating lookup service");
        }
    }

    @Override
    public void onMessage(final Message request) {
        String text = null;
        try {
            TextMessage textRequest = (TextMessage) request;
            text = textRequest.getText();
            Logger.info("lookup service:" + text + ";");
            JSONObject record = new JSONObject(text);

            final long TIMEOUT = 5 * 60 * 1000; // 5 mins
            DistributedDatabase ddb = plugin.getDistributedDatabase();
            ddb.read(new DhtListener(textRequest),
                    ddb.createKey(record.getString("key")), TIMEOUT,
                    DistributedDatabase.OP_EXHAUSTIVE_READ);
        } catch (DistributedDatabaseException e) {
            Logger.error("DDB error: lookup service: " + text);
        } catch (JMSException e) {
            Logger.error("JMS error: lookup service: " + text);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
