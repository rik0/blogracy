package net.blogracy.controller;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import net.blogracy.config.Configurations;
import net.blogracy.errors.BlogracyItemNotFound;
import net.blogracy.model.hashes.Hashes;
import net.blogracy.model.users.User;
import net.blogracy.model.users.UserData;
import net.blogracy.model.users.Users;

import org.apache.shindig.social.opensocial.model.ActivityEntry;
import org.json.JSONException;

public class CommentsController {

	static final DateFormat ISO_DATE_FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss");

	private static final CommentsController THE_INSTANCE = new CommentsController();

	public static CommentsController getInstance() {
		return THE_INSTANCE;
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
			final String objectId, final String publishedDate)
			throws BlogracyItemNotFound {
		UserData data = FileSharing.getUserData(commentedUserId);

		User commentingUser = null;
		if (commentedUserId.equals(commentingUserId))
			commentingUser = data.getUser();
		else {
			// The right user should be searched in the user's friends
			commentingUser = Configurations.getUserConfig().getFriend(
					commentingUserId);

			// This shouldn't happen... anyway a new user with the requested
			// userHash is built (maybe it should throw an exception
			if (commentingUser == null)
				commentingUser = Users.newUser(Hashes
						.fromString(commentingUserId));
		}
		data.addComment(commentingUser, text, objectId, publishedDate);

		String dbUri;
		try {
			dbUri = FileSharing.getSingleton().seedUserData(data);
			DistributedHashTable.getSingleton().store(commentedUserId, dbUri,
					publishedDate);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
