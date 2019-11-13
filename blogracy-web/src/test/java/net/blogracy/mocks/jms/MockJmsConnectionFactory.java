package net.blogracy.mocks.jms;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

public class MockJmsConnectionFactory implements ConnectionFactory {

	private MockJmsConnection lastConnectionCreated = null;
	
	
	
	/**
	 * @return the lastConnectionCreated
	 */
	public MockJmsConnection getLastConnectionCreated() {
		return lastConnectionCreated;
	}

	@Override
	public Connection createConnection() throws JMSException {
		this.lastConnectionCreated =new MockJmsConnection(); 
		return  lastConnectionCreated;
	}

	@Override
	public Connection createConnection(String arg0, String arg1) throws JMSException {
		this.lastConnectionCreated =new MockJmsConnection(); 
		return  lastConnectionCreated;
	}

}
