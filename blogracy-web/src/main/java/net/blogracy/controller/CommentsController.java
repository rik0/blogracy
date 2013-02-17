package net.blogracy.controller;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import net.blogracy.config.Configurations;
import net.blogracy.errors.BlogracyItemNotFound;
import net.blogracy.model.hashes.Hashes;
import net.blogracy.model.users.User;
import net.blogracy.model.users.UserData;
import net.blogracy.model.users.UserDataImpl;
import net.blogracy.model.users.Users;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.shindig.social.opensocial.model.ActivityEntry;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CommentsController implements MessageListener {

	private ConnectionFactory connectionFactory;
	private Connection connection;
	private Session session;
	private Destination salmonContentQueue;
	private MessageProducer producer;
	private MessageConsumer consumer;
	private Boolean isInitialized = false;

	static final DateFormat ISO_DATE_FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss");

	private static final CommentsController THE_INSTANCE = new CommentsController();

	public static CommentsController getInstance() {
		return THE_INSTANCE;
	}

	protected CommentsController() {
		connectionFactory = new ActiveMQConnectionFactory(
				ActiveMQConnection.DEFAULT_BROKER_URL);
		try {
			connection = connectionFactory.createConnection();
			connection.start();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			producer = session.createProducer(null);
			producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
			salmonContentQueue = session.createQueue("salmonContentService");
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void initializeConnection() {
		if (!isInitialized) {
			User localUser = Configurations.getUserConfig().getUser();
			synchronized (isInitialized) {
				if (!isInitialized) {
					connectToFriends(localUser.getHash().toString());
					isInitialized = true;
				}
			}
		}
	}

	public List<ActivityEntry> getComments(final String userId,
			final String objectId) {
		UserData data = FileSharing.getUserData(userId);

		return data.getCommentsByObjectId(objectId);
	}

	public void addComment(final String commentedUserId,
			final String commentingUserId, final String text,
			final String objectId) throws BlogracyItemNotFound {
		this.addComment(commentedUserId, commentingUserId, text, objectId,
				ISO_DATE_FORMAT.format(new Date()));
	}

	public void addComment(final String commentedUserId,
			final String commentingUserId, final String text,
			final String commentedObjectId, final String publishedDate)
			throws BlogracyItemNotFound {

		User commentingUser = null;
		if (commentedUserId.equals(commentingUserId)) {
			UserData data = FileSharing.getUserData(commentedUserId);
			commentingUser = data.getUser();
			data.addComment(commentingUser, text, commentedObjectId,
					publishedDate);
			try {
				String dbUri = FileSharing.getSingleton().seedUserData(data);
				DistributedHashTable.getSingleton().store(commentedUserId,
						dbUri, publishedDate);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			commentingUser = Configurations.getUserConfig().getUser();

			// This shouldn't happen... anyway a new user with the requested
			// userHash is built (maybe it should throw an exception
			if (commentingUser == null)
				commentingUser = Users.newUser(Hashes
						.fromString(commentingUserId));

			try {
				JSONObject requestObj = new JSONObject();
				requestObj.put("request", "content");
				requestObj.put("currentUserId", commentingUserId);
				requestObj.put("destinationUserId", commentedUserId);
				requestObj
						.put("contentData", UserDataImpl.createComment(
								commentingUser, text, commentedObjectId,
								publishedDate));

				TextMessage request = session.createTextMessage();
				request.setText(requestObj.toString());
				producer.send(salmonContentQueue, request);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	public void connectToFriends(String userId) {
		List<User> friendList = Configurations.getUserConfig().getFriends();
		try {
			JSONObject requestObj = new JSONObject();
			requestObj.put("request", "connectToFriends");
			requestObj.put("currentUserId", userId);
			JSONArray friends = new JSONArray();
			for (User friend : friendList) {
				friends.put(friend.getHash().toString());
			}
			requestObj.put("friendsList", friends);
			TextMessage request = session.createTextMessage();
			request.setText(requestObj.toString());
			producer.send(salmonContentQueue, request);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void getContentList(String userId) {
		try {
			JSONObject requestObj = new JSONObject();
			requestObj.put("request", "contentList");
			requestObj.put("currentUserId", userId);
			TextMessage request = session.createTextMessage();
			request.setText(requestObj.toString());
			producer.send(salmonContentQueue, request);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void acceptContent(String userId, String contentId) {
		try {
			JSONObject requestObj = new JSONObject();
			requestObj.put("request", "contentAccepted");
			requestObj.put("currentUserId", userId);
			requestObj.put("contentId", contentId);
			TextMessage request = session.createTextMessage();
			request.setText(requestObj.toString());
			producer.send(salmonContentQueue, request);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void rejectContent(String userId, String contentId) {
		try {
			JSONObject requestObj = new JSONObject();
			requestObj.put("request", "contentRejected");
			requestObj.put("currentUserId", userId);
			requestObj.put("contentId", contentId);
			TextMessage request = session.createTextMessage();
			request.setText(requestObj.toString());
			producer.send(salmonContentQueue, request);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendContentListResponse(String queryUserId,
			JSONObject contentData) {
		try {
			JSONObject requestObj = new JSONObject();
			requestObj.put("response", "contentListQueryResponse");
			requestObj.put("queryUserId", queryUserId);
			requestObj.put("contentData", contentData);
			TextMessage request = session.createTextMessage();
			request.setText(requestObj.toString());
			producer.send(salmonContentQueue, request);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onMessage(Message request) {
		TextMessage textRequest = (TextMessage) request;
		String text;
		try {
			text = textRequest.getText();

			JSONObject record = new JSONObject(text);
			if (record.has("request"))
				handleRequest(record);
		} catch (JMSException e1) {
			e1.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void handleRequest(JSONObject record) throws JSONException {
		if (!record.has("request"))
			return;

		String requestType = record.getString("request");

		if (requestType.equalsIgnoreCase("contentListQuery")) {
			if (!record.has("queryUserId"))
				return;

			String queryUserId = record.getString("queryUserId");

			JSONObject content = SalmonDbController.getSingleton()
					.getUserContent(queryUserId);

			if (content == null)
				return;

			sendContentListResponse(queryUserId, content);
		} else if (requestType.equalsIgnoreCase("contentAcceptedInfo")) {
			if (!record.has("contentUserId"))
				return;

			String contentUserId = record.getString("contentUserId");

			JSONObject content = SalmonDbController.getSingleton()
					.getUserContent(contentUserId);

			if (content == null)
				return;

			// Remove current Content id from list

			JSONArray array = content.getJSONArray("contentData");

			// TODO: Find a good way do remove id from content
			SalmonDbController.getSingleton().addUserContent(contentUserId,
					content);
		} else if (requestType.equalsIgnoreCase("contentRejectedInfo")) {
			if (!record.has("contentUserId"))
				return;

			String contentUserId = record.getString("contentUserId");

			JSONObject content = SalmonDbController.getSingleton()
					.getUserContent(contentUserId);

			if (content == null)
				return;

			// Remove current Content id from list

			JSONArray array = content.getJSONArray("contentData");

			// TODO: Find a good way do remove id from content
			SalmonDbController.getSingleton().addUserContent(contentUserId,
					content);
		} else if (requestType.equalsIgnoreCase("contentReceived")) {
			if (!record.has("currentUserId"))
				return;

			if (!record.has("destinationUserId"))
				return;

			String destinationUserId = record.getString("destinationUserId");
			JSONArray newContentData = record.getJSONArray("contentData");

			JSONObject content = SalmonDbController.getSingleton()
					.getUserContent(destinationUserId);

			if (content == null) {
				content = new JSONObject();
				content.put("contentData", newContentData);
			} else {
				JSONArray array = content.getJSONArray("contentData");
				for (int i = 0; i < newContentData.length(); ++i)
					array.put(newContentData.get(i));
			}

			SalmonDbController.getSingleton().addUserContent(destinationUserId,
					content);
		}
	}

}
