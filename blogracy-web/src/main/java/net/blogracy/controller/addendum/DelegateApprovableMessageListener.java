package net.blogracy.controller.addendum;

public interface DelegateApprovableMessageListener {

	public void delegateApprovableMessageReceived(String contentId, String contentRecipientUserId);

}