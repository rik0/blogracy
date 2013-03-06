package net.blogracy;

import java.util.List;

import net.blogracy.config.Configurations;
import net.blogracy.controller.ActivitiesController;
import net.blogracy.controller.ChatController;
import net.blogracy.controller.DistributedHashTable;
import net.blogracy.controller.FileSharing;
import net.blogracy.model.users.User;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

public class WebServer {

    public static void main(String[] args) throws Exception {

        String webDir = WebServer.class.getClassLoader().getResource("webapp")
                .toExternalForm();
        WebAppContext context = new WebAppContext();
        context.setResourceBase(webDir);
        // context.setDescriptor(webDir + "/WEB-INF/web.xml");
        // context.setContextPath("/");
        // context.setParentLoaderPriority(true);

        Server server = new Server(8080);
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

        int TOTAL_WAIT = 5 * 60 * 1000; // 5 minutes

        while (true) {
            ActivitiesController activities = ActivitiesController
                    .getSingleton();
            activities.addFeedEntry(id, "" + new java.util.Date(), null);

            // List<User> friends = Configurations.getUserConfig().getFriends();
            int wait = TOTAL_WAIT / friends.size();
            for (User friend : friends) {
                DistributedHashTable.getSingleton().lookup(
                        friend.getHash().toString());
                activities.getFeed(friend.getHash().toString());
                Thread.currentThread().sleep(wait);
            }
        }
    }
}
