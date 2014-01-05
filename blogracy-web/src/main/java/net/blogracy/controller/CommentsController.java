package net.blogracy.controller;

import java.util.List;

import javax.jms.Message;

import net.blogracy.errors.BlogracyItemNotFound;

import org.apache.shindig.social.opensocial.model.ActivityEntry;
import org.json.JSONArray;
import org.json.JSONObject;

public interface CommentsController {

	public void initializeConnection();

	public List<ActivityEntry> getComments(String userId, String objectId);

	public void addComment(String commentedUserId, String commentingUserId, String text, String objectId) throws BlogracyItemNotFound;

	public void addComment(String commentedUserId, String commentingUserId, String text, String commentedObjectId, String publishedDate) throws BlogracyItemNotFound;

	public void connectToFriends(String userId);

	public void getContentList(String userId);

	public void acceptContent(String userId, String contentId);

	public void rejectContent(String userId, String contentId);

	public void sendContentListResponse(String userId, String queryUserId, JSONArray contentData);

	public void sendBullyElectionMessage(String channelUserId, String senderUserId);

	public void sendBullyAnswerMessage(String channelUserId, String senderUserId);

	public void sendBullyCoordinatorMessage(String channelUserId, String senderUserId);

	public void onMessage(Message request);

	public boolean verifyComment(JSONObject contentData, String senderUserId, String contentRecipientUserId);

}