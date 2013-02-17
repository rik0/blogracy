package net.blogracy.messaging.impl;

import java.nio.ByteBuffer;

import net.blogracy.chat.messaging.impl.CMNoRoute;
import net.blogracy.chat.messaging.impl.ChatMessage;

import org.gudy.azureus2.plugins.messaging.Message;
import org.gudy.azureus2.plugins.messaging.MessageException;

public class BMNoRoute implements BlogracyMessage {

	public static final String ID_BM_NO_ROUTE = "ID_BM_NO_ROUTE";

	private final String     description  = getID();
	private final ByteBuffer buffer       = ByteBuffer.allocate(0);

	public String getID() {return ID_BM_NO_ROUTE;}
	public byte getVersion() {return BlogracyMessage.BLOGRACYMESSAGE_DEFAULT_VERSION;}
	public int getType() {return Message.TYPE_PROTOCOL_PAYLOAD;} 
	public String getDescription() {return description;} 
	public ByteBuffer[] getPayload() {return new ByteBuffer[] { buffer };}   
	public void destroy() {/*nothing*/}

	public Message create(ByteBuffer data) throws MessageException {    
		return new BMNoRoute();
	}

}
