package net.blogracy.model.users;

import java.util.List;

import org.apache.shindig.social.opensocial.model.ActivityEntry;

public interface UserAddendumData {

	
	public void addAddendumEntry(ActivityEntry entry);
	
	/**
	 * Gets all the comments associated to an object
	 * @param objectId the object identifier
	 * @return a (ascending sorted) list of comments
	 */
	public List<ActivityEntry> getCommentsByObjectId(final String objectId);
	
	public User getUser();
	
	public List<ActivityEntry> getActivityStream();
}
