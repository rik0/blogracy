package net.blogracy.mocks.jms;

import java.io.Serializable;
import java.util.Enumeration;

import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.jms.StreamMessage;
import javax.jms.TemporaryQueue;
import javax.jms.TemporaryTopic;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;

public class MockJmsSession implements Session {

	private MockJmsMessageProducer lastMessageProducerCreated = null;
	private MockJmsMessageConsumer lastMessageConsumerCreated = null;

	/**
	 * @return the lastMessageProducerCreated
	 */
	public MockJmsMessageProducer getLastMessageProducerCreated() {
		return lastMessageProducerCreated;
	}

	/**
	 * @return the lastMessageConsumerCreated
	 */
	public MockJmsMessageConsumer getLastMessageConsumerCreated() {
		return lastMessageConsumerCreated;
	}

	@Override
	public void close() throws JMSException {
		// TODO Auto-generated method stub

	}

	@Override
	public void commit() throws JMSException {
		// TODO Auto-generated method stub

	}

	@Override
	public QueueBrowser createBrowser(Queue arg0) throws JMSException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public QueueBrowser createBrowser(Queue arg0, String arg1) throws JMSException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BytesMessage createBytesMessage() throws JMSException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MessageConsumer createConsumer(Destination arg0) throws JMSException {
		this.lastMessageConsumerCreated = new MockJmsMessageConsumer();
		return lastMessageConsumerCreated;
	}

	@Override
	public MessageConsumer createConsumer(Destination arg0, String arg1) throws JMSException {
		this.lastMessageConsumerCreated = new MockJmsMessageConsumer();
		return lastMessageConsumerCreated;
	}

	@Override
	public MessageConsumer createConsumer(Destination arg0, String arg1, boolean arg2) throws JMSException {
		this.lastMessageConsumerCreated = new MockJmsMessageConsumer();
		return lastMessageConsumerCreated;
	}

	@Override
	public TopicSubscriber createDurableSubscriber(Topic arg0, String arg1) throws JMSException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TopicSubscriber createDurableSubscriber(Topic arg0, String arg1, String arg2, boolean arg3) throws JMSException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MapMessage createMapMessage() throws JMSException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Message createMessage() throws JMSException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ObjectMessage createObjectMessage() throws JMSException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ObjectMessage createObjectMessage(Serializable arg0) throws JMSException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MessageProducer createProducer(Destination arg0) throws JMSException {
		this.lastMessageProducerCreated = new MockJmsMessageProducer();
		return lastMessageProducerCreated;
	}

	@Override
	public Queue createQueue(String arg0) throws JMSException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StreamMessage createStreamMessage() throws JMSException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TemporaryQueue createTemporaryQueue() throws JMSException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TemporaryTopic createTemporaryTopic() throws JMSException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TextMessage createTextMessage() throws JMSException {
		return createTextMessage("");
	}

	@Override
	public TextMessage createTextMessage(String arg0) throws JMSException {
		return new TextMessage() {

			@Override
			public void acknowledge() throws JMSException {
			}

			@Override
			public void clearBody() throws JMSException {
			}

			@Override
			public void clearProperties() throws JMSException {

			}

			@Override
			public boolean getBooleanProperty(String arg0) throws JMSException {
				return false;
			}

			@Override
			public byte getByteProperty(String arg0) throws JMSException {
				return 0;
			}

			@Override
			public double getDoubleProperty(String arg0) throws JMSException {
				return 0;
			}

			@Override
			public float getFloatProperty(String arg0) throws JMSException {
				return 0;
			}

			@Override
			public int getIntProperty(String arg0) throws JMSException {
				return 0;
			}

			@Override
			public String getJMSCorrelationID() throws JMSException {
				return null;
			}

			@Override
			public byte[] getJMSCorrelationIDAsBytes() throws JMSException {
				return null;
			}

			@Override
			public int getJMSDeliveryMode() throws JMSException {
				return 0;
			}

			@Override
			public Destination getJMSDestination() throws JMSException {
				return null;
			}

			@Override
			public long getJMSExpiration() throws JMSException {
				return 0;
			}

			@Override
			public String getJMSMessageID() throws JMSException {
				return null;
			}

			@Override
			public int getJMSPriority() throws JMSException {
				return 0;
			}

			@Override
			public boolean getJMSRedelivered() throws JMSException {
				return false;
			}

			@Override
			public Destination getJMSReplyTo() throws JMSException {
				return null;
			}

			@Override
			public long getJMSTimestamp() throws JMSException {
				return 0;
			}

			@Override
			public String getJMSType() throws JMSException {
				return null;
			}

			@Override
			public long getLongProperty(String arg0) throws JMSException {
				return 0;
			}

			@Override
			public Object getObjectProperty(String arg0) throws JMSException {
				return null;
			}

			@Override
			public Enumeration getPropertyNames() throws JMSException {
				return null;
			}

			@Override
			public short getShortProperty(String arg0) throws JMSException {
				return 0;
			}

			@Override
			public String getStringProperty(String arg0) throws JMSException {
				return null;
			}

			@Override
			public boolean propertyExists(String arg0) throws JMSException {
				return false;
			}

			@Override
			public void setBooleanProperty(String arg0, boolean arg1) throws JMSException {
				// TODO Auto-generated method stub

			}

			@Override
			public void setByteProperty(String arg0, byte arg1) throws JMSException {
				// TODO Auto-generated method stub

			}

			@Override
			public void setDoubleProperty(String arg0, double arg1) throws JMSException {
				// TODO Auto-generated method stub

			}

			@Override
			public void setFloatProperty(String arg0, float arg1) throws JMSException {
				// TODO Auto-generated method stub

			}

			@Override
			public void setIntProperty(String arg0, int arg1) throws JMSException {
				// TODO Auto-generated method stub

			}

			@Override
			public void setJMSCorrelationID(String arg0) throws JMSException {
				// TODO Auto-generated method stub

			}

			@Override
			public void setJMSCorrelationIDAsBytes(byte[] arg0) throws JMSException {
				// TODO Auto-generated method stub

			}

			@Override
			public void setJMSDeliveryMode(int arg0) throws JMSException {
				// TODO Auto-generated method stub

			}

			@Override
			public void setJMSDestination(Destination arg0) throws JMSException {
				// TODO Auto-generated method stub

			}

			@Override
			public void setJMSExpiration(long arg0) throws JMSException {
				// TODO Auto-generated method stub

			}

			@Override
			public void setJMSMessageID(String arg0) throws JMSException {
				// TODO Auto-generated method stub

			}

			@Override
			public void setJMSPriority(int arg0) throws JMSException {
				// TODO Auto-generated method stub

			}

			@Override
			public void setJMSRedelivered(boolean arg0) throws JMSException {
				// TODO Auto-generated method stub

			}

			@Override
			public void setJMSReplyTo(Destination arg0) throws JMSException {
				// TODO Auto-generated method stub

			}

			@Override
			public void setJMSTimestamp(long arg0) throws JMSException {
				// TODO Auto-generated method stub

			}

			@Override
			public void setJMSType(String arg0) throws JMSException {
				// TODO Auto-generated method stub

			}

			@Override
			public void setLongProperty(String arg0, long arg1) throws JMSException {
				// TODO Auto-generated method stub

			}

			@Override
			public void setObjectProperty(String arg0, Object arg1) throws JMSException {
				// TODO Auto-generated method stub

			}

			@Override
			public void setShortProperty(String arg0, short arg1) throws JMSException {
				// TODO Auto-generated method stub

			}

			@Override
			public void setStringProperty(String arg0, String arg1) throws JMSException {
				// TODO Auto-generated method stub

			}

			@Override
			public String getText() throws JMSException {
				return text;
			}

			protected String text = null;

			@Override
			public void setText(String arg0) throws JMSException {
				this.text = arg0;

			}

		};
	}

	@Override
	public Topic createTopic(String arg0) throws JMSException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getAcknowledgeMode() throws JMSException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public MessageListener getMessageListener() throws JMSException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean getTransacted() throws JMSException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void recover() throws JMSException {
		// TODO Auto-generated method stub

	}

	@Override
	public void rollback() throws JMSException {
		// TODO Auto-generated method stub

	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setMessageListener(MessageListener arg0) throws JMSException {
		// TODO Auto-generated method stub

	}

	@Override
	public void unsubscribe(String arg0) throws JMSException {
		// TODO Auto-generated method stub

	}

}
