package net.blogracy;

import net.blogracy.model.users.User;
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
        
        int TOTAL_WAIT = 5 * 60 * 1000; // 5 minutes
        
        while (true) {
          List<User> friends = Configurations.getUserConfig().getFriends();
          int wait = TOTAL_WAIT / friends.size();
          for (User friend : friends) {
            FileSharing.getFeed(friend.getHash().toString());
            Thread.currentThread().sleep(wait);
          }
        }
    }
}
