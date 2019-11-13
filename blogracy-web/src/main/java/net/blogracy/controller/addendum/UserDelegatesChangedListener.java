package net.blogracy.controller.addendum;

import java.util.List;

import net.blogracy.model.users.User;

public interface UserDelegatesChangedListener {
	void onUserDelegatesChanged(String userId, List<User> newDelegates);
}
