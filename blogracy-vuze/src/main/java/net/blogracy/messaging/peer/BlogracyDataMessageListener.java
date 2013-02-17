package net.blogracy.messaging.peer;

import net.blogracy.messaging.impl.BlogracyDataMessage;

import org.gudy.azureus2.plugins.download.Download;

public interface BlogracyDataMessageListener {
	
	  public void blogracyDataMessageReceived(Download download,byte[] sender,String nick, String content);
	
	/* are these really necessary? */
	  public void downloadAdded(Download download);
	  
	  public void downloadRemoved(Download download);
	  
	  public void downloadActive(Download download);
	  
	  public void downloadInactive(Download download);
	


}
