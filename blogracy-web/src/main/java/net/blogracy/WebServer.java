package net.blogracy;

import net.blogracy.model.users.User;
import net.blogracy.controller.ChatController;
import net.blogracy.controller.DistributedHashTable;
import net.blogracy.controller.FileSharing;
import net.blogracy.config.Configurations;
import java.util.List;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

public class WebServer
{
    public static void main(String[] args) throws Exception
    {
        Server server = new Server(8080);
 
        WebAppContext context = new WebAppContext();
        context.setDescriptor("webapp/WEB-INF/web.xml");
        context.setResourceBase("webapp");
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
        String id = Configurations.getUserConfig().getUser().getHash().toString();
        ChatController.getSingleton().joinChannel(id);
        
        int TOTAL_WAIT = 5 * 60 * 1000; // 5 minutes
        
        while (true) {
            FileSharing sharing = FileSharing.getSingleton();
            sharing.addFeedEntry(id, "" + new java.util.Date(), null);
        
            // List<User> friends = Configurations.getUserConfig().getFriends();
            int wait = TOTAL_WAIT / friends.size();
            for (User friend : friends) {
                DistributedHashTable.getSingleton().lookup(friend.getHash().toString());
                FileSharing.getFeed(friend.getHash().toString());
                Thread.currentThread().sleep(wait);
            }
        }
    }
}
