package net.blogracy.controller;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.UUID;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import net.blogracy.mocks.jms.*;
import net.blogracy.model.hashes.Hashes;
import net.blogracy.model.users.User;
import net.blogracy.model.users.Users;

import org.apache.tools.ant.taskdefs.condition.IsTrue;
import org.hamcrest.core.IsEqual;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class CommentsControllerImplTest {
	
	public CommentsControllerImpl commentsController = null;
	public MockJmsMessageProducer messageProducer = null;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		MockJmsConnectionFactory factory = new MockJmsConnectionFactory();
		commentsController = new CommentsControllerImpl(factory, null);
		MockJmsConnection connection = factory.getLastConnectionCreated();
		MockJmsSession session = connection.getLastSessionCreated();
		messageProducer = session.getLastMessageProducerCreated();
	}

	@After
	public void tearDown() throws Exception {
	}
	
	
	@Test
	public void testConnectToFriendsMessage_Valid() {
		User u1 = Users.newUser("LOCAL", Hashes.newHash("LOCAL"));
		ArrayList<User> friends = new ArrayList<User>();
		friends.add(Users.newUser("U2", Hashes.newHash("U2")));
		friends.add(Users.newUser("U3", Hashes.newHash("U3")));
		commentsController.connectToFriends(u1, friends);
		TextMessage message = (TextMessage)messageProducer.getLastMessageSent();
		JSONObject jsonContent = null;
		try {
			jsonContent = new JSONObject(message.getText());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertEquals(jsonContent.optString("request"), "connectToFriends");
		
	}
}
