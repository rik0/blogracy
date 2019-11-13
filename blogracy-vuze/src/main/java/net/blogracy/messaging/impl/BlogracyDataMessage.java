package net.blogracy.messaging.impl;
import org.gudy.azureus2.plugins.messaging.Message;

public interface BlogracyDataMessage extends BlogracyMessage, Message {

	/***
	 * The sender's Blogracy userId
	 * @return
	 */
	public String getSenderUserId();
	

	
	/***
	 * the local peerID advertised to the download swarm.
	 * @return
	 */
	public byte[] getSenderPeerID();
	
	
	/***
	 * Gets current number of Hops
	 * @return
	 */
	public int getNbHops();
	
	
	/***
	 * Sets the number of hops this message has been through
	 * @param hops
	 */
	public void setNbHops(int hops);
	
	/***
	 * Unique id of this specific message
	 * @return
	 */
	public long getMessageID();
	
	/***
	 * Gets the content string of the message
	 * @return
	 */
	public String getContent();
	
	/***
	 * Clones the current message
	 * @return
	 */
	public BlogracyDataMessage copy();
}
