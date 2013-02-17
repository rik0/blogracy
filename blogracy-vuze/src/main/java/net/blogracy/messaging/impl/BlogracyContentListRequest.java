package net.blogracy.messaging.impl;

public class BlogracyContentListRequest extends BlogracyDataMessageBase {

	public static final String ID = "ID_BLOGRACYMESSAGE_BlogracyContentListRequest";
	
	
	public BlogracyContentListRequest( String senderUserId,
			byte[] senderID, int hops, String content) {
		super(senderUserId, senderID, hops, content);
	}


	@Override
	public String getID() {
		return ID;
	}
	

	

}
