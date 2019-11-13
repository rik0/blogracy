package net.blogracy.mocks.jms;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;

public class MockJmsMessageProducer implements  MessageProducer {

	
	private Message lastMessageSent = null; 
	
	/**
	 * @return the lastMessageSent
	 */
	public Message getLastMessageSent() {
		return lastMessageSent;
	}

	@Override
	public void close() throws JMSException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getDeliveryMode() throws JMSException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Destination getDestination() throws JMSException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean getDisableMessageID() throws JMSException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean getDisableMessageTimestamp() throws JMSException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getPriority() throws JMSException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getTimeToLive() throws JMSException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void send(Message arg0) throws JMSException {
		this.lastMessageSent = arg0;
	}

	@Override
	public void send(Destination arg0, Message arg1) throws JMSException {
		this.lastMessageSent = arg1;
		
	}

	@Override
	public void send(Message arg0, int arg1, int arg2, long arg3) throws JMSException {
		this.lastMessageSent = arg0;
		
	}

	@Override
	public void send(Destination arg0, Message arg1, int arg2, int arg3, long arg4) throws JMSException {
		this.lastMessageSent = arg1;
	}

	@Override
	public void setDeliveryMode(int arg0) throws JMSException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDisableMessageID(boolean arg0) throws JMSException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDisableMessageTimestamp(boolean arg0) throws JMSException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPriority(int arg0) throws JMSException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setTimeToLive(long arg0) throws JMSException {
		// TODO Auto-generated method stub
		
	}

}
