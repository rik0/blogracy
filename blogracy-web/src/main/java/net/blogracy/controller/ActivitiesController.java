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

import java.io.BufferedReader;
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

import net.blogracy.Attribute;
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

import cpabe.Cpabe;

/**
 * Generic functions to manipulate feeds are defined in this class.
 */
public class ActivitiesController extends SecurityManager {

    static final DateFormat ISO_DATE_FORMAT = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss'Z'");
    static final String CACHE_FOLDER = Configurations.getPathConfig()
            .getCachedFilesDirectoryPath();

    static String PVRFILE = Configurations.getPathConfig().getCachedFilesDirectoryPath()+File.separator + "pvrfile";
    static String DECFILE = Configurations.getPathConfig().getCachedFilesDirectoryPath()+File.separator + "decfile";
    
    private static final FileSharing sharing = FileSharing.getSingleton();
    private static final DistributedHashTable dht = DistributedHashTable
            .getSingleton();
    private static final ActivitiesController theInstance = new ActivitiesController();
    private static final Attribute attribute = Attribute.getSingleton();

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
                        ActivityEntry entry = (ActivityEntry) CONVERTER
                        		.convertToObject(item, ActivityEntry.class);

                        // Check if the message is encrypted with cpabe
                        if( item.get("cipher-scheme").toString().equalsIgnoreCase("cpabe") ) {
                        	
                        	File cipherInfoFile = new File( CACHE_FOLDER + File.separator + 
                        								sharing.getHashFromMagnetURI(item.getString("chiper-scheme-info")) + ".json" );
                        	
                        	String attributes = attribute.getAttribute(cipherInfoFile,
                        							Configurations.getUserConfig().getUser().getHash().toString());
                        	
                        	JSONObject jsonObj = FileUtils.getJSONFromFile(cipherInfoFile);
                        	String uriPub = jsonObj.getString("uri-pubkey");
                        	String uriMsk = jsonObj.getString("uri-mskkey");
                        	
                        	// CP-ABE: Decifro il messaggio con cpabe
                        	Cpabe cpabe = new Cpabe();
                        	cpabe.keygen(CACHE_FOLDER + File.separator + sharing.getHashFromMagnetURI(uriPub),
                        				 PVRFILE,
                        				 CACHE_FOLDER + File.separator + sharing.getHashFromMagnetURI(uriMsk),
                        				 attributes);
                        	
                        	String pathFileToDec = CACHE_FOLDER + File.separator + 
                        								sharing.getHashFromMagnetURI( item.get("url").toString() )
                        									+ ".cpabe";
                        	
                        	// Avoid the 'System.exit()' in the cpabe Library
                        	SecurityManager previousSM = System.getSecurityManager();
                            final SecurityManager SM = new SecurityManager() {
                            	@Override
                            	public void checkPermission(final Permission permission) {
                            		if( permission.getName() != null && permission.getName().startsWith("exitVM") )
                            			throw new SecurityException();
                            	}
                            };
                        	System.setSecurityManager(SM);
                            
                        	try {
                        		cpabe.dec(CACHE_FOLDER + File.separator + sharing.getHashFromMagnetURI(uriPub),
                        				  PVRFILE,
                        				  pathFileToDec,
                        				  DECFILE);
                        		File dec = new File(DECFILE);
                        		FileReader r = new FileReader(dec);
                        		BufferedReader br = new BufferedReader(r);
                        		String temp = null;
                        		String fileText = "";
                        		while( (temp = br.readLine()) != null) {
                        			fileText += temp;
                        		}
                        		br.close();
                        		r.close();
                        		dec.delete();
                        		entry.setContent(fileText);
                        		result.add(entry);
                        	} catch (SecurityException e){ 
                        		System.out.println("DECIPHER INFO | Unable to decipher a message.");
                        	} finally {
                        		System.setSecurityManager(previousSM);
                        	}
                    	} else { // Messaggio in chiaro
                    		result.add(entry);
                    	}
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
    	addFeedEntry(id, text, attachment, null, null);
    }

    public void addFeedEntry(String id, String text, File attachment, String policy, String uriCipherInfo) {
    	try {
            String hash = sharing.hash(text);
                        
            File textFile = new File(CACHE_FOLDER + File.separator + hash + ".txt");

            FileWriter w = new FileWriter(textFile);
            w.write(text);
            w.close();
            
            String Uri, cipherText = "";
            
            if( policy != null ) {
            	// cp-abe: Encription
            	System.out.println("CPABE | Encripting File :: Start");
            	Cpabe cpabe = new Cpabe();
            	JSONObject jObj = FileUtils.getJSONFromFile(new File(CACHE_FOLDER + File.separator
            															+ sharing.getHashFromMagnetURI(uriCipherInfo) + ".json"));

            	cpabe.enc(CACHE_FOLDER + File.separator + sharing.getHashFromMagnetURI(jObj.get("uri-pubkey").toString()),
            			policy,	// Policy
            			CACHE_FOLDER + File.separator + hash + ".txt",	// Input File
            			CACHE_FOLDER + File.separator + hash + ".cpabe");	// Output File
            	System.out.println("CPABE | Encripting File :: Finish");
            
            	File cipherFile = new File(CACHE_FOLDER + File.separator + hash + ".cpabe");
            	FileReader r = new FileReader(cipherFile);
            	BufferedReader br = new BufferedReader(r);
            	String temp = null;

            	while( (temp = br.readLine()) != null) {
            		cipherText += temp;
            	}
            	br.close();
            	r.close();
            
            	textFile.delete();
            
            	Uri = sharing.seed(cipherFile);
            } else {
            	Uri = sharing.seed(textFile);
            }
            
            String attachmentUri = null;
            if (attachment != null) {
                attachmentUri = sharing.seed(attachment);
            }

            final List<ActivityEntry> feed = getFeed(id);
            final ActivityEntry entry = new ActivityEntryImpl();
            entry.setVerb("post");
            entry.setUrl(Uri);
            entry.setPublished(ISO_DATE_FORMAT.format(new Date()));
            
            if( policy != null ) {
            	entry.put("cipher-scheme", new String("cpabe"));
            	entry.setContent(cipherText);
            	entry.put("chiper-scheme-info", uriCipherInfo);
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

        FileWriter writer = new FileWriter(feedFile);
        db.write(writer);
        writer.close();

        String feedUri = sharing.seed(feedFile);
        return feedUri;
    }
}
