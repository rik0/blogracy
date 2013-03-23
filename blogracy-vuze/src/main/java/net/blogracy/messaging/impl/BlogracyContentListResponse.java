package net.blogracy.messaging.impl;

import java.nio.ByteBuffer;
import java.util.Map;

import net.blogracy.messaging.MessagingManager;

import org.gudy.azureus2.plugins.messaging.Message;
import org.gudy.azureus2.plugins.messaging.MessageException;

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
	
	
	/* (non-Javadoc)
	 * @see net.blogracy.messaging.impl.BlogracyDataMessageBase#create(java.nio.ByteBuffer)
	 */
	@Override
	public Message create(ByteBuffer data) throws MessageException {
		if(data == null) {
			throw new MessageException( "[" +getID() + ":" +getVersion()+ "] decode error: data == null" );
		}

		if(data.remaining() < 13) {/*nothing*/}
		int size = data.remaining();

		byte[] bMessage = new byte[size];
		data.get(bMessage);

		try {

			@SuppressWarnings("rawtypes")
			Map mMessage = MessagingManager.bDecode(bMessage);
			int messageID = ((Long)mMessage.get("id")).intValue();
			byte[] senderID = (byte[])mMessage.get("s");
			String uid = new String((byte[])mMessage.get("uid"));
			int hops = ((Long)mMessage.get("h")).intValue();
			String content = new String((byte[])mMessage.get("t"));
			// TODO 
			String destinationUserId = null;

			BlogracyContentListResponse message = new BlogracyContentListResponse(uid, senderID, hops, destinationUserId, content);
			message.setMessageID(messageID);
			return message;
		} 

		catch(Exception e) {
			throw new MessageException( "[" +getID() + ":" +getVersion()+ "] decode error: " + e );
		}
	}




	/* (non-Javadoc)
	 * @see net.blogracy.messaging.impl.BlogracyDataMessageBase#copy()
	 */
	@Override
	public BlogracyDataMessageBase copy() {
		BlogracyContentListResponse message = new BlogracyContentListResponse(getSenderUserId(), getSenderPeerID(), getNbHops(), this.destinationUserId, getContent());
		message.setMessageID(this.getMessageID());
		return message;
	}
	

}
