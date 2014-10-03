/*
 * Copyright (c)  2011 Enrico Franchi, Michele Tomaiuolo and University of Parma.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package net.blogracy.controller;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.Permission;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.blogracy.config.Configurations;
import net.blogracy.util.FileUtils;

import org.apache.shindig.protocol.conversion.BeanConverter;
import org.apache.shindig.protocol.conversion.BeanJsonConverter;
import org.apache.shindig.social.core.model.ActivityEntryImpl;
import org.apache.shindig.social.core.model.ActivityObjectImpl;
import org.apache.shindig.social.opensocial.model.ActivityEntry;
import org.apache.shindig.social.opensocial.model.ActivityObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Module;
import com.google.inject.name.Names;

/**
 * Generic functions to manipulate feeds are defined in this class.
 */
public class ActivitiesController extends SecurityManager {

    static final DateFormat ISO_DATE_FORMAT = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss'Z'");
    static final String CACHE_FOLDER = Configurations.getPathConfig()
            .getCachedFilesDirectoryPath();
    
    private static final FileSharing sharing = FileSharing.getSingleton();
    private static final DistributedHashTable dht = DistributedHashTable.getSingleton();
    private static final CpAbeController cpabe = CpAbeController.getSingleton();

    private static final ActivitiesController theInstance = new ActivitiesController();

    private static BeanJsonConverter CONVERTER = new BeanJsonConverter(
            Guice.createInjector(new Module() {
                @Override
                public void configure(Binder b) {
                    b.bind(BeanConverter.class)
                            .annotatedWith(
                                    Names.named("shindig.bean.converter.json"))
                            .to(BeanJsonConverter.class);
                }
            }));

    public static ActivitiesController getSingleton() {
        return theInstance;
    }

    public ActivitiesController() {
        ISO_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
    
    static public List<ActivityEntry> getFeed(String user) {
    	return getFeed(user, false);
    }
    
    static public List<ActivityEntry> getFeed(String user, boolean addNewEntry) {
        List<ActivityEntry> result = new ArrayList<ActivityEntry>();
        
        System.out.println("Getting feed: " + user);
        JSONObject record = dht.getRecord(user);
        
        if (record != null) {
            try {
                String latestHash = FileSharing.getHashFromMagnetURI(record
                        .getString("uri"));
                File dbFile = new File(CACHE_FOLDER + File.separator + latestHash + ".json");
                if (!dbFile.exists() && record.has("prev")) {
                    latestHash = FileSharing.getHashFromMagnetURI(record
                            .getString("prev"));
                    dbFile = new File(CACHE_FOLDER + File.separator
                            + latestHash + ".json");
                }
                if (dbFile.exists()) {
                    System.out.println("Getting feed: " + dbFile.getAbsolutePath());
                    JSONObject db = new JSONObject(new JSONTokener(new FileReader(dbFile)));

                    JSONArray items = db.getJSONArray("items");
                    for (int i = 0; i < items.length(); ++i) {
                        JSONObject item = items.getJSONObject(i);
                        ActivityEntry entry = (ActivityEntry) CONVERTER.convertToObject(item, ActivityEntry.class);

                        tryToDecipher(result, entry, item, addNewEntry, db);
                    }
                    System.out.println("Feed loaded");
                } else {
                    System.out.println("Feed not found");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public void addFeedEntry(String id, String text, File attachment) {
    	addFeedEntry(id, text, attachment, "");
    }

    public void addFeedEntry(String id, String text, File attachment, String policy) {
    	try {
            String hash = sharing.hash(text);
        	
            File textFile = new File(CACHE_FOLDER + File.separator + hash + ".txt");

            FileWriter w = new FileWriter(textFile);
            w.write(text);
            w.close();

            String Uri = "", cipherText = "";
            
            if( policy.isEmpty() ) {
            	Uri = sharing.seed(textFile);
             } else {
            	// CPABE: Encription
            	System.out.println("CPABE | Encripting File :: Start");
            	
            	String uriCipherInfo = cpabe.getUriCipherInfo();
            	JSONObject cipherInfoJSON = FileUtils.getJSONFromFile(new File(CACHE_FOLDER + File.separator
            										+ sharing.getHashFromMagnetURI(uriCipherInfo) + ".json"));

            	File publicKeyFile = new File(cpabe.getPubPath());
    			FileWriter writer = new FileWriter(publicKeyFile);
    	        writer.write( cipherInfoJSON.get("pubkey").toString() );
    			writer.close();
            	
            	cpabe.encryptMessage(cpabe.getPubPath(),
            						 policy,	// Policy
            						 textFile.getAbsolutePath(),	// Input File
            						 CACHE_FOLDER + File.separator + hash + ".cpabe");	// Output File
            	
            	publicKeyFile.delete();
            	textFile.delete();
            	
            	System.out.println("CPABE | Encripting File :: Finish");
            	
            	File cipherFile = new File(CACHE_FOLDER + File.separator + hash + ".cpabe");
            	cipherText = FileUtils.getContentFromFile(cipherFile);
            
            	Uri = sharing.seed(cipherFile);
            }
            
            String attachmentUri = null;
            if (attachment != null) {
                attachmentUri = sharing.seed(attachment);
            }

            final List<ActivityEntry> feed = getFeed(id, true);
            final ActivityEntry entry = new ActivityEntryImpl();
            entry.setVerb("post");
            entry.setUrl(Uri);
            entry.setPublished(ISO_DATE_FORMAT.format(new Date()));
            
            if( !policy.isEmpty() ) {
            	entry.put("cipher-scheme", new String("cpabe"));
            	entry.setContent(cipherText);
            } else {
            	entry.put("cipher-scheme", new String("none"));
            	entry.setContent(text);
            }
            
            if (attachment != null) {
                ActivityObject enclosure = new ActivityObjectImpl();
                enclosure.setUrl(attachmentUri);
                entry.setObject(enclosure);
            }
            feed.add(0, entry);
            String feedUri = seedActivityStream(id, feed);
            DistributedHashTable.getSingleton().store(id, feedUri,
                    entry.getPublished());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public String seedActivityStream(String userId,
            final List<ActivityEntry> feed) throws JSONException, IOException {
        final File feedFile = new File(CACHE_FOLDER + File.separator + userId
                + ".json");

        JSONArray items = new JSONArray();
        for (int i = 0; i < feed.size(); ++i) {
            JSONObject item = new JSONObject(feed.get(i));
            items.put(item);
        }
        JSONObject db = new JSONObject();

        db.put("items", items);
        
        String cpabeSchemeInfoPath = CACHE_FOLDER + File.separator + 
        								sharing.getHashFromMagnetURI(cpabe.getSingleton().getUriCipherInfo()) + ".json";
        if( new File(cpabeSchemeInfoPath).exists() )
        	db.put("cpabe-scheme-info", FileUtils.getJSONFromFile(new File(cpabeSchemeInfoPath)));
        
        FileWriter writer = new FileWriter(feedFile);
        db.write(writer);
        writer.close();

        String feedUri = sharing.seed(feedFile);
        return feedUri;
    }
    
    public static void tryToDecipher(List<ActivityEntry> result, ActivityEntry entry, 
    		JSONObject item, boolean addNewEntry, JSONObject db) throws Exception {
    	// Check if the message is encrypted with cpabe and the getFeed(..) function is called for
        // showing the entries in the Web interface
        if( item.get("cipher-scheme").toString().equalsIgnoreCase("cpabe") && !addNewEntry ) {
        	// Get file with info on the cipher-scheme
        	JSONObject cipherSchemeInfo = db.getJSONObject("cpabe-scheme-info");

        	// Get the private key for the local user and retrieve its absolute path
        	File privateKeyFile = cpabe.getPrivateKeyFile(cipherSchemeInfo);
        	String privateKeyFilePath = privateKeyFile.getAbsolutePath();
        	
        	File publicKeyFile = new File(cpabe.getPubPath());
        	FileWriter writer = new FileWriter(publicKeyFile);
	        writer.write( cipherSchemeInfo.getString("pubkey").toString() );
	        writer.close();
        	
        	// Get the path of the file to decipher
        	String pathFileToDec = CACHE_FOLDER + File.separator + 
        	    sharing.getHashFromMagnetURI( item.get("url").toString() ) + ".cpabe";
        	
        	// Avoid the 'System.exit()' in the CP-ABE Library
        	SecurityManager previousSM = System.getSecurityManager();
                    final SecurityManager SM = new SecurityManager() {
                        @Override
                        public void checkPermission(final Permission permission) {
            		    if( permission.getName() != null && permission.getName().startsWith("exitVM") )
                                throw new SecurityException();
            	        }
                    };
        	System.setSecurityManager(SM);
            
        	// Try to decipher the message
        	// Abbiamo separato l'istruzione result.add(entry) in due parti, sia qui
        	// che nel ramo dell'else, poiché se un utente cerca di recuperare 
        	// un messaggio cifrato, ma non riesce a decifrarlo (perché non possiede
        	// i giusti attributi) esso non dovrà aggiungerlo al vettore result
        	// altrimenti visualizzerà nello stream sull'interfaccia web un messaggio cifrato.
        	// Quindi questo accade poiché se cpabe.decryptMessage(..) non va a buon fine 
        	// l'istruzioni successive non vengono eseguite.
        	try {
        	    cpabe.decryptMessage(publicKeyFile.getAbsolutePath(),
        				  		privateKeyFilePath,
        				  		pathFileToDec,
        				  		cpabe.getDecPath());
        	    publicKeyFile.delete();
        	    privateKeyFile.delete();
        		
                //System.out.println(" CP-ABE | Decrypted Message!!!");
        		
        	    File decFile = new File(cpabe.getDecPath());
        	    String fileText = FileUtils.getContentFromFile(decFile);
        	    decFile.delete();
        		
        	    entry.setContent(fileText);
        	    result.add(entry);
        	} catch (SecurityException e){ 
                    //System.out.println(" CP-ABE | Unable to decipher the message.");
        	} finally {
        	    System.setSecurityManager(previousSM);
        	}
    	} else {
    		result.add(entry);
    	}
    }
}
