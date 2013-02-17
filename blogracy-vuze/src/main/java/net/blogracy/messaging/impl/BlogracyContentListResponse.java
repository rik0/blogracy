package net.blogracy.messaging.impl;

public class BlogracyContentListResponse extends BlogracyDataMessageBase {

	
	public static final String ID = "ID_BLOGRACYMESSAGE_BlogracyContentListResponse";
	private String destinationUserId;

	public String getDestinationUserId() {
		return destinationUserId;
	}


	public BlogracyContentListResponse(String senderUserId, byte[] senderID,
			int hops, String destinationUserId, String content) {
		
		super(senderUserId, senderID, hops, content);
		this.destinationUserId = destinationUserId;
	}
	
	@Override
	public String getID() {
		return ID;
	}
	
	

}
