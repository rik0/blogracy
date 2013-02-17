package net.blogracy.messaging.peer;

import java.util.List;

import org.gudy.azureus2.plugins.download.Download;

import net.blogracy.messaging.impl.BlogracyDataMessage;

public interface PeerController {

	public void initialize(List<BlogracyDataMessage> listOfMessageTypes);

	public void startPeerProcessing();

	/****************************************
	 * 
	 *       Listener Handling
	 * 
	 ****************************************/

	public void addMessageListener(BlogracyDataMessageListener listener);

	public void removeMessageListener(BlogracyDataMessageListener listener);
	
	public void sendMessage(Download download,byte[] peerID, String senderUserId, BlogracyDataMessage message);

}