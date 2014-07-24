package net.blogracy;

import cpabe.Cpabe;

import java.io.File;
import java.io.FileWriter;
import java.security.KeyPair;
import java.security.PublicKey;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Logger;

import javax.security.auth.login.Configuration;

import net.blogracy.config.Configurations;
import net.blogracy.controller.ActivitiesController;
import net.blogracy.controller.ChatController;
import net.blogracy.controller.DistributedHashTable;
import net.blogracy.controller.FileSharing;
import net.blogracy.model.users.User;
import net.blogracy.util.FileUtils;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.json.JSONArray;
import org.json.JSONObject;

public class WebServer {

    public static final String LOREM_IPSUM = "Lorem ipsum dolor sit amet, "
            + "consectetur adipisicing elit, sed do eiusmod tempor "
            + "incididunt ut labore et dolore magna aliqua.";
    static final DateFormat ISO_DATE_FORMAT = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss'Z'");
    
    // CP-ABE Files
    private static final String PUBFILE = Configurations.getPathConfig().getCachedFilesDirectoryPath()+File.separator + "pubfile";
    private static final String MSKFILE = Configurations.getPathConfig().getCachedFilesDirectoryPath()+File.separator + "mskfile";

    private static final FileSharing sharing = FileSharing.getSingleton();
    private static final Attribute attribute = Attribute.getSingleton();
    
    static final int TOTAL_WAIT = 5 * 60 * 1000; // 5 minutes

    public static void main(String[] args) throws Exception {
        ISO_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));

        int randomWait = 1000 * 15;//(int) (TOTAL_WAIT * Math.random());
        Logger log = Logger.getLogger("net.blogracy.webserver");
        log.info("Web server: waiting for " + (randomWait / 1000)
                + " secs before starting");
        Thread.currentThread();
		Thread.sleep(randomWait);

        String webDir = WebServer.class.getClassLoader().getResource("webapp")
                .toExternalForm();
        WebAppContext context = new WebAppContext();
        context.setResourceBase(webDir);
        // context.setDescriptor(webDir + "/WEB-INF/web.xml");
        // context.setContextPath("/");
        // context.setParentLoaderPriority(true);

        Server server = new Server(8181);
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
        
        // CP-ABE: Initial Setup
        Cpabe cpabe = new Cpabe();
        System.out.println("CPABE ||| Start to setup");
		cpabe.setup(PUBFILE, MSKFILE);
		// CP-ABE: sharing PUBFILE, MSKFILE
    	File pub = new File(PUBFILE);
    	File msk = new File(MSKFILE);

    	JSONObject cipherProp = new JSONObject();
    	String uriPub = sharing.seed(pub);
    	String uriMsk = sharing.seed(msk);
    	cipherProp.put("uri-pubkey", uriPub);
    	cipherProp.put("uri-mskkey", uriMsk);
    	pub.delete();
    	msk.delete();
    	
    	File cipherInfo = new File(Configurations.getPathConfig().getCachedFilesDirectoryPath() 
				+ File.separator + "chiperInfo.json");
		FileWriter w = new FileWriter(cipherInfo);
		w.write(cipherProp.toString());
		w.flush();
		w.close();
		
		String uriCipherInfo = sharing.seed(cipherInfo);
		System.out.println("CPABE ||| End to setup");
		
		// CP-ABE: Attribute
		// Attributi di MIC
		attribute.setAttribute(uriCipherInfo, id, "role:doctor level:A");
		
		// Attributi degli amici di MIC
		int count = 0;
		System.out.println("MIC | Set attribute for friends");
		for(User friend : friends) {
			if( count % 2 == 0 ) attribute.setAttribute(uriCipherInfo, friend.getHash().toString(), "role:doctor");
			else attribute.setAttribute(uriCipherInfo, friend.getHash().toString(), "role:doctor");
			count++;
		}

        while (true) {
            ActivitiesController activities = ActivitiesController
                    .getSingleton();
            
            String now = ISO_DATE_FORMAT.format(new java.util.Date());
            activities.addFeedEntry(id, now + " " + LOREM_IPSUM, null);
            
            Thread.currentThread();	Thread.sleep(1000 * 5);
    		
    		// -------------------------------------------------- //
    		Thread.currentThread();	Thread.sleep(1000 * 5);
    		// -------------------------------------------------- //
    		
    		System.out.println(" DEBUG | Post a CHIPER message on my feed");
    		String policy = "role:doctor level:A 1of2";
    		now = ISO_DATE_FORMAT.format(new java.util.Date());
    		activities.addFeedEntry(id,
    								now + " [CIPHER] " + LOREM_IPSUM,
									null,
									policy,
									uriCipherInfo);
            
            randomWait = (int) (TOTAL_WAIT * (0.8 + 0.4 * Math.random()));
            int wait = randomWait / friends.size();
            wait = 1000 * 5;
            for (User friend : friends) {
            	DistributedHashTable.getSingleton().lookup(friend.getHash().toString());
                activities.getFeed(friend.getHash().toString());
                Thread.currentThread();
				Thread.sleep(wait);
            }
        }
    }
}
