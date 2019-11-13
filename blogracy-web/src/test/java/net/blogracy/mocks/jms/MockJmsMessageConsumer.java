package net.blogracy.mocks.jms;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;

public class MockJmsMessageConsumer implements MessageConsumer {

	@Override
	public void close() throws JMSException {
		// TODO Auto-generated method stub

	}

	@Override
	public MessageListener getMessageListener() throws JMSException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getMessageSelector() throws JMSException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Message receive() throws JMSException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Message receive(long arg0) throws JMSException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Message receiveNoWait() throws JMSException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setMessageListener(MessageListener arg0) throws JMSException {
		// TODO Auto-generated method stub

	}

}
