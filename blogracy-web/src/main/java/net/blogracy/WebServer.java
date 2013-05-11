package net.blogracy;

import net.blogracy.model.users.User;
import net.blogracy.controller.ActivitiesController;
import net.blogracy.controller.ChatController;
import net.blogracy.controller.CommentsController;
import net.blogracy.controller.DistributedHashTable;
import net.blogracy.controller.FileSharing;
import net.blogracy.config.Configurations;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import java.util.logging.Logger;

public class WebServer {

	public static final String LOREM_IPSUM = "Lorem ipsum dolor sit amet, " + "consectetur adipisicing elit, sed do eiusmod tempor " + "incididunt ut labore et dolore magna aliqua.";
	static final DateFormat ISO_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

	static final int TOTAL_WAIT = 2 * 60 * 1000; // 2 minutes

	public static void main(String[] args) throws Exception {

		ISO_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));

		int randomWait = (int) (TOTAL_WAIT * Math.random());
		Logger log = Logger.getLogger("net.blogracy.webserver");
		log.info("Web server: waiting for " + (randomWait / 1000) + " secs before starting");
		Thread.currentThread().sleep(randomWait);

		Server server = new Server(8181);

		WebAppContext context = new WebAppContext();
		context.setDescriptor("target/webapp/WEB-INF/web.xml");
		context.setResourceBase("target/webapp");
		context.setContextPath("/");
		context.setParentLoaderPriority(true);

		server.setHandler(context);

		server.start();
		// server.join();

		List<User> friends = Configurations.getUserConfig().getFriends();
        for (User friend : friends) {
            String hash = friend.getHash().toString();
            ChatController.getSingleton().joinChannel(hash);
        }
        String id = Configurations.getUserConfig().getUser().getHash()
                .toString();
        ChatController.getSingleton().joinChannel(id);


		CommentsController.getInstance().initializeConnection();

		while (true) {
			ActivitiesController activities = ActivitiesController.getSingleton();
			String now = ISO_DATE_FORMAT.format(new java.util.Date());
			activities.addFeedEntry(id, now + " " + LOREM_IPSUM, null);
			CommentsController.getInstance().getContentList(id);

			// List<User> friends = Configurations.getUserConfig().getFriends();
			randomWait = (int) (TOTAL_WAIT * (0.8 + 0.4 * Math.random()));
			int wait = randomWait / friends.size();
			for (User friend : friends) {
				DistributedHashTable.getSingleton().lookup(friend.getHash().toString());
				activities.getFeed(friend.getHash().toString());
				Thread.currentThread().sleep(wait);
			}
		}
	}
}
