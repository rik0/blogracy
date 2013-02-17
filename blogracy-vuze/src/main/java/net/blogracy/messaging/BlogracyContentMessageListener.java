package net.blogracy.messaging;

import net.blogracy.messaging.impl.BlogracyContent;
import net.blogracy.messaging.impl.BlogracyContentAccepted;
import net.blogracy.messaging.impl.BlogracyContentListRequest;
import net.blogracy.messaging.impl.BlogracyContentListResponse;
import net.blogracy.messaging.impl.BlogracyContentRejected;

public interface BlogracyContentMessageListener {
	public void blogracyContentReceived(BlogracyContent message);
	public void blogracyContentListRequestReceived(BlogracyContentListRequest message);
	public void blogracyContentListResponseReceived(BlogracyContentListResponse message);
	public void blogracyContentAcceptedReceived(BlogracyContentAccepted message);
	public void blogracyContentRejectedReceived(BlogracyContentRejected message);
	
}
