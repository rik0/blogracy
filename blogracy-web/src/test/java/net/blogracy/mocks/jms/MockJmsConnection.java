package net.blogracy.mocks.jms;

import javax.jms.Connection;
import javax.jms.ConnectionConsumer;
import javax.jms.ConnectionMetaData;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.ServerSessionPool;
import javax.jms.Session;
import javax.jms.Topic;

public class MockJmsConnection implements Connection {

	private MockJmsSession lastSessionCreated = null;
	
	/**
	 * @return the lastSessionCreated
	 */
	public MockJmsSession getLastSessionCreated() {
		return lastSessionCreated;
	}

	@Override
	public void close() throws JMSException {
		// TODO Auto-generated method stub

	}

	@Override
	public ConnectionConsumer createConnectionConsumer(Destination arg0, String arg1, ServerSessionPool arg2, int arg3) throws JMSException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ConnectionConsumer createDurableConnectionConsumer(Topic arg0, String arg1, String arg2, ServerSessionPool arg3, int arg4) throws JMSException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Session createSession(boolean arg0, int arg1) throws JMSException {
		this.lastSessionCreated =new MockJmsSession(); 
		return lastSessionCreated; 
	}

	@Override
	public String getClientID() throws JMSException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ExceptionListener getExceptionListener() throws JMSException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ConnectionMetaData getMetaData() throws JMSException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setClientID(String arg0) throws JMSException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setExceptionListener(ExceptionListener arg0) throws JMSException {
		// TODO Auto-generated method stub

	}

	@Override
	public void start() throws JMSException {
		// TODO Auto-generated method stub

	}

	@Override
	public void stop() throws JMSException {
		// TODO Auto-generated method stub

	}

}
