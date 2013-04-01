package net.blogracy.messaging.impl;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import net.blogracy.messaging.MessagingManager;

import org.gudy.azureus2.plugins.messaging.Message;
import org.gudy.azureus2.plugins.messaging.MessageException;

public class BlogracyDataMessageBase implements BlogracyDataMessage {

	public static final String ID_BLOGRACY_MESSAGE = "ID_BLOGRACYMESSAGE";

	private String description;
	private ByteBuffer buffer;

	// A unique message ID, computed as :
	// hash(time,sender,text);
	private long messageID;

	// The sender's blogracy user Id
	private final String senderUserId;
	// The sender's peer ID
	private final byte[] senderID;

	// The number of hops the message has been through
	private int hops;

	// The message itself
	private final String content;

	BlogracyDataMessageBase(long id, String senderUserId, byte[] senderID, int hops, String content) {
		this(senderUserId, senderID, hops, content, false);
		this.messageID = id;
		generateBuffer();
	}

	public BlogracyDataMessageBase(String senderUserId, byte[] senderID,  int hops, String content) {
		this(senderUserId, senderID, hops, content, true);
	}

	private BlogracyDataMessageBase(String senderUserId, byte[] senderID, int hops, String content, boolean generateBuffer) {

		this.senderUserId = senderUserId;
		this.senderID = senderID;
		this.hops = hops;
		this.content = content;

		String hash = senderID + "," + System.currentTimeMillis() + "," + content;

		this.messageID = hash.hashCode();

		if (generateBuffer)
			generateBuffer();
	}

	@SuppressWarnings("unchecked")
	private void generateBuffer() {

		this.description = getID() + " from " + senderUserId + " : " + content + " (id: " + messageID + ", hops:" + hops + ")";
		@SuppressWarnings("rawtypes")
		Map mMessage = new HashMap();
		mMessage.put("id", new Long(messageID));
		mMessage.put("s", senderID);
		mMessage.put("uid", senderUserId);
		mMessage.put("h", new Long(hops));
		mMessage.put("t", content);

		byte[] bMessage = new byte[0];

		try {
			bMessage = MessagingManager.bEncode(mMessage);
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		buffer = ByteBuffer.allocate(bMessage.length);
		buffer.put(bMessage);
		buffer.flip();
	}

	public long getMessageID() {
		return messageID;
	}

	public byte[] getSenderPeerID() {
		return senderID;
	}

	public String getSenderUserId() {
		return senderUserId;
	}


	public int getNbHops() {
		return hops;
	}

	public void setNbHops(int nbHops) {
		hops = nbHops;
	}

	public String getContent() {
		return content;
	}

	public String getID() {
		return BlogracyDataMessageBase.ID_BLOGRACY_MESSAGE;
	}

	public byte getVersion() {
		return BlogracyMessage.BLOGRACYMESSAGE_DEFAULT_VERSION;
	}

	public int getType() {
		return Message.TYPE_PROTOCOL_PAYLOAD;
	}

	public String getDescription() {
		return description;
	}

	public ByteBuffer[] getPayload() {
		return new ByteBuffer[] { buffer };
	}

	public void destroy() {/* nothing */
	}

	protected void setMessageID(long id) {
		this.messageID = id;
	}

	public Message create(ByteBuffer data) throws MessageException {
		if (data == null) {
			throw new MessageException("[" + getID() + ":" + getVersion() + "] decode error: data == null");
		}

		if (data.remaining() < 13) {/* nothing */
		}
		int size = data.remaining();

		byte[] bMessage = new byte[size];
		data.get(bMessage);

		try {

			@SuppressWarnings("rawtypes")
			Map mMessage = MessagingManager.bDecode(bMessage);
			int messageID = ((Long) mMessage.get("id")).intValue();
			byte[] senderID = (byte[]) mMessage.get("s");
			String uid = new String((byte[]) mMessage.get("uid"));
			int hops = ((Long) mMessage.get("h")).intValue();
			String content = new String((byte[]) mMessage.get("t"));

			return new BlogracyDataMessageBase(messageID, uid, senderID,  hops, content);
		}

		catch (Exception e) {
			throw new MessageException("[" + getID() + ":" + getVersion() + "] decode error: " + e);
		}
	}

	public BlogracyDataMessageBase copy() {
		BlogracyDataMessageBase msg = new BlogracyDataMessageBase(messageID, this.getSenderUserId(), this.getSenderPeerID(), this.getNbHops(), this.getContent());
		return msg;
	}
}
