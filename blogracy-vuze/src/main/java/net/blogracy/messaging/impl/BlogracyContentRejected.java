package net.blogracy.messaging.impl;

public class BlogracyContentRejected extends BlogracyDataMessageBase {

	public BlogracyContentRejected(String senderUserId, byte[] senderID,
			int hops, String content) {
		super(senderUserId, senderID, hops, content);
	}

	public static final String ID = "ID_BLOGRACYMESSAGE_BlogracyContentRejected";
	
	@Override
	public String getID() {
		return ID;
	}
	
}
