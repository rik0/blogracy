package net.blogracy.messaging.impl;

public class BlogracyContentAccepted extends BlogracyDataMessageBase {

	public BlogracyContentAccepted(String senderUserId, byte[] senderID,
			int hops, String content) {
		super(senderUserId, senderID, hops, content);
	}

	
	public static final String ID = "ID_BLOGRACYMESSAGE_BlogracyContentAccepted";
	
	@Override
	public String getID() {
		return ID;
	}
	
}
