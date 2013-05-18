package net.blogracy.model.users;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.apache.shindig.social.opensocial.model.ActivityEntry;

public class UserAddendumDataImpl implements UserAddendumData {

	private User user;
	private List<ActivityEntry> activityStream;

	public UserAddendumDataImpl(User user) {
		this.user = user;
	}

	@Override
	public void addAddendumEntry(ActivityEntry entry) {
		if (!this.activityStream.contains(entry))
			this.activityStream.add(0, entry);
	}

	@Override
	public List<ActivityEntry> getCommentsByObjectId(final String objectId) {
		List<ActivityEntry> comments = new ArrayList<ActivityEntry>();

		for (ActivityEntry entry : this.getActivityStream()) {
			if (entry.getTarget() != null && entry.getTarget().getId() != null && entry.getTarget().getId().equals(objectId) && entry.getObject() != null && entry.getObject().getObjectType() != null
					&& entry.getObject().getObjectType().equals("comment") && entry.getVerb() != null && entry.getVerb().equals("post")) {
				comments.add(entry);
			}
		}

		java.util.Collections.sort(comments, new Comparator<ActivityEntry>() {
			public int compare(ActivityEntry o1, ActivityEntry o2) {
				return o1.getPublished().compareTo(o2.getPublished());
			}
		});

		return comments;
	}

	@Override
	public User getUser() {
		return this.user;
	}

	@Override
	public List<ActivityEntry> getActivityStream() {
		return this.activityStream;
	}

	
	public void setActivityStream(Collection<ActivityEntry> activityStream) {
		this.activityStream = new ArrayList<ActivityEntry>(activityStream);
	}
}
