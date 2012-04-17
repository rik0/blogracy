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

package net.blogracy.controller;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import net.blogracy.config.Configurations;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Generic functions to manipulate feeds are defined in this class.
 */
public class DistributedHashTable {

    private ConnectionFactory connectionFactory;
    private Connection connection;
    private Session session;
    private Destination lookupQueue;
    private Destination storeQueue;
    private Destination downloadQueue;
    private MessageProducer producer;
    private MessageConsumer consumer;

    static final ObjectMapper MAPPER = new ObjectMapper();
    static final String CACHE_FOLDER = Configurations.getPathConfig()
            .getCachedFilesDirectoryPath();

    private HashMap<String, ObjectNode> records = new HashMap<String, ObjectNode>();

    private static final DistributedHashTable theInstance = new DistributedHashTable();

    public static DistributedHashTable getSingleton() {
        return theInstance;
    }

    public DistributedHashTable() {
        try {
            File recordsFile = new File(CACHE_FOLDER + File.separator
                    + "records.json");
            if (recordsFile.exists()) {
                ArrayNode recordList = (ArrayNode) MAPPER.readTree(recordsFile);
                for (int i = 0; i < recordList.size(); ++i) {
                    ObjectNode record = (ObjectNode) recordList.get(i);
                    records.put(record.get("id").textValue(), record);
                }
            }

            connectionFactory = new ActiveMQConnectionFactory(
                    ActiveMQConnection.DEFAULT_BROKER_URL);
            connection = connectionFactory.createConnection();
            connection.start();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            producer = session.createProducer(null);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            lookupQueue = session.createQueue("lookup");
            storeQueue = session.createQueue("store");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void lookup(final String id) {
        try {
            Destination tempDest = session.createTemporaryQueue();
            MessageConsumer responseConsumer = session.createConsumer(tempDest);
            responseConsumer.setMessageListener(new MessageListener() {
                @Override
                public void onMessage(Message response) {
                    try {
                        String msgText = ((TextMessage) response).getText();
                        ObjectNode record = (ObjectNode) MAPPER
                                .readTree(msgText);
                        ObjectNode currentRecord = getRecord(id);
                        if (currentRecord == null
                                || currentRecord.get("version").longValue() < record
                                        .get("version").longValue()) {
                            putRecord(record);
                            String uri = record.get("uri").textValue();
                            FileSharing.getSingleton().download(uri);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            ObjectNode record = MAPPER.createObjectNode();
            record.put("id", id);

            TextMessage message = session.createTextMessage();
            message.setText(MAPPER.writeValueAsString(record));
            message.setJMSReplyTo(tempDest);
            producer.send(lookupQueue, message);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void store(final String id, final String uri, final long version) {
        try {
            ObjectNode record = MAPPER.createObjectNode();
            record.put("id", id);
            record.put("uri", uri);
            record.put("version", version);
            // put "magic" public-key; e.g.
            // RSA.modulus(n).exponent(e)
            // record.put("signature", user); // TODO

            TextMessage message = session.createTextMessage();
            message.setText(MAPPER.writeValueAsString(record));
            producer.send(storeQueue, message);
            putRecord(record);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ObjectNode getRecord(String user) {
        return records.get(user);
    }

    public void putRecord(ObjectNode record) {
        records.put(record.get("id").textValue(), record);
        ArrayNode recordList = (ArrayNode) MAPPER.createArrayNode();
        Iterator<ObjectNode> entries = records.values().iterator();
        while (entries.hasNext()) {
            ObjectNode entry = entries.next();
            recordList.add(entry);
        }
        File recordsFile = new File(CACHE_FOLDER + File.separator
                + "records.json");
        try {
            MAPPER.writeValue(recordsFile, recordList);
        } catch (JsonGenerationException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
