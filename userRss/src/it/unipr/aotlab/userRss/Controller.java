/*
 * Copyright (c)  2011 Alan Nonnato, Enrico Franchi, Michele Tomaiuolo and University of Parma.
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
package it.unipr.aotlab.userRss;

import org.gudy.azureus2.core3.util.ByteFormatter;
import org.gudy.azureus2.core3.util.SystemTime;
import org.gudy.azureus2.core3.util.TorrentUtils;
import org.gudy.azureus2.plugins.PluginInterface;
import org.gudy.azureus2.plugins.ddb.DistributedDatabase;
import org.gudy.azureus2.plugins.ddb.DistributedDatabaseException;
import org.gudy.azureus2.plugins.ddb.DistributedDatabaseKey;
import org.gudy.azureus2.plugins.ddb.DistributedDatabaseValue;
import org.gudy.azureus2.plugins.download.Download;
import org.gudy.azureus2.plugins.download.DownloadException;
import org.gudy.azureus2.plugins.download.DownloadManager;
import org.gudy.azureus2.plugins.torrent.Torrent;
import org.gudy.azureus2.plugins.torrent.TorrentAttribute;
import org.gudy.azureus2.plugins.torrent.TorrentException;
import org.gudy.azureus2.plugins.utils.resourcedownloader.ResourceDownloader;
import org.gudy.azureus2.plugins.utils.resourcedownloader.ResourceDownloaderAdapter;
import org.gudy.azureus2.pluginsimpl.local.PluginCoreUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

public class Controller {
    static PluginInterface pluginInterface = null;
    private static SortedMap<String, Rss> userContentMap = new TreeMap<String, Rss>();
    private OldView cView;
    static Model cModel;
    private static DistributedDatabase ddb;
    private UserRSS plugin = null;
    private TorrentAttribute ta_publish_feed_content;
    private File temp_data_dir;
    private File friends_feed_dir;
    private DistributedDatabaseKey key = null;
    private TorrentAttribute ta_category;
    private String allStr;


    /**
     * Constructor
     *
     * @param view          the view
     * @param model         the model
     * @param plgnInterface
     * @param plgn
     * @param s_allStr
     */
    public Controller(OldView view, Model model, UserRSS plgn, PluginInterface plgnInterface, String s_allStr) {
        cView = view;
        cModel = model;
        plugin = plgn;
        pluginInterface = plgnInterface;
        ddb = pluginInterface.getDistributedDatabase();
        allStr = s_allStr;
        cModel.setNameUserRss(allStr);

        ta_publish_feed_content = pluginInterface.getTorrentManager().getPluginAttribute("publish_feed_content");

        //if doesn't exist, make the directory
        temp_data_dir = new File(pluginInterface.getPluginDirectoryName(), "my_feed");
        if (!temp_data_dir.isDirectory())
            temp_data_dir.mkdirs();

        /*temp_data_dir = new File( my_feed_dir, "tmp" );
          if(!temp_data_dir.isDirectory())
              temp_data_dir.mkdirs();*/


        friends_feed_dir = new File(pluginInterface.getPluginDirectoryName(), "friends_dir");
        if (!friends_feed_dir.isDirectory())
            friends_feed_dir.mkdirs();

        cModel.setStatus("okStatus");
        //add a download listener
        DownloadEventListener myListener = new DownloadEventListener(pluginInterface);
        pluginInterface.getDownloadManager().getGlobalDownloadEventNotifier().addListener(myListener);

        ta_category = pluginInterface.getTorrentManager().getAttribute(TorrentAttribute.TA_CATEGORY);
    }

    /**
     * If i pressed UPDATE, make the rss a save it
     */
    public void updateRss() {
        //if the userContentMap have something inside
        if (userContentMap.size() > 0) {
            writeXmlOnFile("Content", "", cModel.getUserId());

        } else {
            cModel.setStatus("oneItemRequiredMessage");
        }
    }

    /**
     * Check the content file and call the content publisher
     *
     * @param feed_name     name of file
     * @param feed_location dir of file
     * @param hash          hash code of the content-child file
     * @param type          Content//Feed
     * @return
     */

    protected Torrent publishContent(String feed_name, String feed_location, String hash, String type) {
        try {
            File test_file = new File(feed_location);

            if (test_file.exists() && test_file.isDirectory()) {

                // System.out.println("Feed location '" + feed_location + "' must not be a directory");

                return (null);
            }
        } catch (Throwable e) {
        }


        // now obtain the actual feed content and create the content torrent

        FileOutputStream fos = null;

        try {
            DownloadDetails details = downloadResource(feed_location);

            InputStream is = details.getInputStream();

            try {

                String suffix = ".rss";

                return (publishStuff(type, ta_publish_feed_content, cModel.getUserId() + "_userRssPluginCategory", feed_name, is, details.getContentType(), suffix));
            } finally {

                try {
                    is.close();

                } catch (Throwable e) {

                    // System.out.println(e);
                }
            }

        } catch (Throwable e) {

            // System.out.println("Failed to download feed from '" + feed_location + "'");

            return (null);

        } finally {

            if (fos != null) {

                try {
                    fos.close();
                } catch (Throwable e) {
                }
            }
        }
    }

    DownloadDetails downloadResource(String resource)

            throws Exception {
        // System.out.println("Download of " + resource + " starts");

        ResourceDownloader rd;

        String lc = resource.toLowerCase();

        if (lc.startsWith("http:") || lc.startsWith("https:") || lc.startsWith("magnet:")) {

            rd = pluginInterface.getUtilities().getResourceDownloaderFactory().create(new URL(resource));

        } else {

            rd = pluginInterface.getUtilities().getResourceDownloaderFactory().create(new File(resource));

        }

        rd.addListener(
                new ResourceDownloaderAdapter() {
                    public void
                    reportActivity(
                            ResourceDownloader downloader,
                            String activity) {
                        // System.out.println(activity);
                    }
                });


        InputStream is = rd.download();

        DownloadDetails result = new DownloadDetails(is, (String) rd.getProperty(ResourceDownloader.PR_STRING_CONTENT_TYPE));

        // System.out.println("Download of " + resource + " completed");

        return (result);
    }

    /**
     * Publish the content, and put it on seeding
     *
     * @param type              Content//Feed
     * @param torrent_attribute ---
     * @param category          ----
     * @param feed_name         name of file
     * @param is                Input stream
     * @param content_type      ------
     * @param suffix            suffix of the name
     * @return the torrent
     */
    Torrent publishStuff(
            String type,
            TorrentAttribute torrent_attribute,
            String category,
            String feed_name,
            InputStream is,
            String content_type,
            String suffix) {


        String log_str = "'" + feed_name + "/" + suffix + "/" + type + "'";
        FileOutputStream fos = null;
        try {
            //open file and make the torrent
            File file_tmp = new File(temp_data_dir, feed_name + suffix);
            Torrent t = pluginInterface.getTorrentManager().createFromDataFile(file_tmp, new URL("dht:"));
            t = pluginInterface.getTorrentManager().createFromDataFile(file_tmp, new URL("dht:"));
            t.setAnnounceURL(new URL("dht://" + ByteFormatter.encodeString(t.getHash()) + ".dht/announce"));
            DownloadManager download_manager = pluginInterface.getDownloadManager();

            //check if i have the same torrent on download manager (name based)
            Download old_content = download_manager.getDownload(t);

            Torrent old_torrent = old_content == null ? null : old_content.getTorrent();

            if (old_torrent != null) {

                // System.out.println("Torrent already running for " + log_str + " (identical torrent)");

                file_tmp.delete();

                return (old_torrent);
            }

            // check if i have the same torrent on download manager (hash pieces based)
            old_content = getPublishContent(feed_name);
            old_torrent = old_content == null ? null : old_content.getTorrent();

            if (old_torrent != null) {

                byte[][] old_pieces = old_content.getTorrent().getPieces();
                byte[][] new_pieces = t.getPieces();

                boolean same = true;

                if (old_pieces.length == new_pieces.length) {

                    for (int i = 0; i < old_pieces.length; i++) {

                        if (!Arrays.equals(old_pieces[i], new_pieces[i])) {

                            same = false;

                            break;
                        }
                    }
                } else {
                    same = false;
                }

                //if i checked the same file, don't do anithings
                if (same) {

                    // System.out.println("Torrent already running for " + log_str + " (identical pieces)");

                    file_tmp.delete();

                    return (old_torrent);
                }
            }

            //if exist the torrent with the same name, remove it
            Download[] downloads = download_manager.getDownloads();

            for (int i = 0; i < downloads.length; i++) {
                Download dl = downloads[i];

                String attr = dl.getAttribute(torrent_attribute);
                if (attr != null && attr.equals(feed_name)) {
                    final Download f_existing = dl;
                    removeDownload(f_existing, temp_data_dir, log_str + " (delayed removal of old publish content)");
                }
            }

            //start to sharing
            TorrentUtils.setFlag(PluginCoreUtils.unwrap(t), TorrentUtils.TORRENT_FLAG_LOW_NOISE, true);
            t.setComplete(temp_data_dir);
            File file_act = new File(temp_data_dir, feed_name + suffix);
            File torrent_file = new File(file_act.toString() + ".torrent");
            t.writeToFile(torrent_file);

            //share the file
            Download d = download_manager.addDownload(t, torrent_file, temp_data_dir);
            d.setFlag(Download.FLAG_DISABLE_AUTO_FILE_MOVE, true);
            d.setFlag(Download.FLAG_LOW_NOISE, true);
            d.setForceStart(true);
            d.setAttribute(torrent_attribute, feed_name);
            d.setAttribute(ta_category, cModel.getUserId() + "_userRssPluginCategory");

            // System.out.println("-------------------->Ho condiviso questa categoria  " + d.getAttribute(ta_category));
            return (t);

        } catch (Throwable e) {

            // System.out.println(e);

            return (null);

        } finally {

            if (fos != null) {

                try {
                    fos.close();

                } catch (Throwable e) {

                    // System.out.println(e);
                }
            }
        }
    }

    /**
     * Remove a existing download
     *
     * @param download    name of the download
     * @param torrent_dir dir of the torrent
     * @patam log_str            strings for debug
     */
    protected void removeDownload(Download download, File torrent_dir, String log_str) {
        try {
            try {
                download.stop();

            } catch (Throwable e) {
            }
            //take the file
            File original_torrent = new File(torrent_dir, download.getTorrent().getName() + ".torrent");
            //remove the download
            download.remove(true, true);
            original_torrent.delete();

            // System.out.println("Removed torrent '" + download.getName() + "' for " + log_str);

        } catch (Throwable e) {

            // System.out.println("Failed to remove existing torrent '" + download.getName() + "' for " + log_str);
        }
    }

    /**
     * get a feedName and return the linked download
     *
     * @param feed_name feed name
     * @return download        the feed name linked download
     */
    protected Download getPublishContent(String feed_name) {
        Download[] downloads = pluginInterface.getDownloadManager().getDownloads();
        long latest_date = 0;
        Download result = null;

        for (int i = 0; i < downloads.length; i++) {
            Download download = downloads[i];
            if (download.getTorrent() == null) {
                continue;
            }

            String attr = download.getAttribute(ta_publish_feed_content);
            if (attr != null && attr.equals(feed_name)) {
                long date = download.getTorrent().getCreationDate();
                if (date > latest_date) {
                    result = download;
                }
            }
        }
        return (result);
    }


    /**
     * get tag value, used the xml parser
     *
     * @param sTag     the tag
     * @param eElement the element
     * @return nValue.getNodeValue()    the content of tag
     */
    private static String getTagValue(String sTag, Element eElement) {
        NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();
        Node nValue = (Node) nlList.item(0);

        return nValue.getNodeValue();
    }


    /**
     * write the xml Rss Feed Text
     *
     * @param type Content//Feed
     * @param hash the hash code of the Child-Content
     * @param id   Userid
     */
    private void writeXmlOnFile(String type, String hash, String id) {
        String fileName;
        //se sto scrivendo contenuti e non il Feed principale
        if ((type.equals("")) || (hash.equals(""))) {
            Date date = new Date(SystemTime.getCurrentTime());
            SimpleDateFormat temp = new SimpleDateFormat("ddMMyyyy_HHmmss");
            String date_str = temp.format(date);
            fileName = id + "_" + date_str;
        } else {
            fileName = id + "_feed";
        }

        //if the file doesn't exists, make it
        if (!(new File(temp_data_dir + "\\" + fileName + ".rss").exists())) {
            makeNewXml(temp_data_dir + "\\" + fileName + ".rss");
        }
        //add content to the file
        makeXml(temp_data_dir + "\\" + fileName + ".rss", hash, id);

        // System.out.println("Create The Rss (xml) file " + temp_data_dir + "\\" + fileName + ".rss  " + hash + id);
        //make the torrent
        Torrent content_torrent = publishContent(fileName, temp_data_dir + "\\" + fileName + ".rss", hash, type);

        DistributedDatabaseValue value = null;
        //if i'm making the content, now make the principal Rss
        if (type.equals("Content")) {

            writeXmlOnFile("Feed", ByteFormatter.encodeString(content_torrent.getHash()), id);

            // System.out.println("====>" + ByteFormatter.encodeString(content_torrent.getHash()));
        }
        //if i'm making the principal Rss, publish the user id on the DDB
        if (type.equals("Feed")) {
            try {
                //write key value (idUser-Principal rss hash)
                key = ddb.createKey(cModel.getUserId().getBytes());
                value = ddb.createValue(ByteFormatter.encodeString(content_torrent.getHash()));
                ddb.write(new DdbListenerWrite(), key, value);
                cModel.setStatus("fileCreatedAddKeyDdbMessage");
            } catch (DistributedDatabaseException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * search in the friends_dir if there are all the file
     * otherwise, download it
     *
     * @param friend the friend
     */


    private void addDownlodRss(String friend) {
        try {
            //go in the friends dir and make an arrayFile
            File dir = new File(friends_feed_dir + "\\" + friend);
            String a[] = dir.list(); //creo un array di stringhe e lo riempio con la lista dei files presenti nella directory
            boolean found;

            DistributedDatabaseKey key = null;
            key = ddb.createKey(friend.getBytes());
            ddb.read(new DdbListenerRead(), key, 2 * 60 * 1000, DistributedDatabase.OP_EXHAUSTIVE_READ);

            //pluginInterface.getDownloadManager().addDownload(new URL("magnet:?xt=urn:btih:"+getTagValue("description", eElement)),true);

            //open the Principal Rss file
            File fXmlFile = new File(dir + "\\" + friend + "_feed.rss");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("item");
            //for each item(hash code) in the principal file...
            for (int temp = 0; temp < nList.getLength(); temp++) {

                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    found = false;
                    //... match the file hash
                    for (int kw = 0; kw < a.length; kw++) {
                        // System.out.println("rss :" + getTagValue("description", eElement));
                        if (!(a[kw].endsWith(".torrent")) && (!a[kw].equals(cModel.getUserId() + ".rss"))) {
                            //make the file torrent
                            Torrent t;
                            t = pluginInterface.getTorrentManager().createFromDataFile(new File(dir + "\\" + a[kw]), new URL("dht:"));
                            // System.out.println("file :" + ByteFormatter.encodeString(t.getHash()));
                            if (getTagValue("description", eElement).equals(ByteFormatter.encodeString(t.getHash()))) {
                                // System.out.println("---------------------------------trovato");
                                found = true;
                                // System.out.println("I found the corrispondance file hash :" + getTagValue("description", eElement) + "<=OK");
                                break;
                            }

                        }
                    }//end for
                    //if i didn't found the file, put it in download
                    if (found == false) {
                        // System.out.println("---------------------------------da scaricare");
                        pluginInterface.getDownloadManager().addDownload(new URL("magnet:?xt=urn:btih:" + getTagValue("description", eElement)), true);
                    }//end if: non ho trovato il file corrispondente al torrent4
                }//end if: se ho un elemento valido
            }//end for: scorro tutti i nodi del file
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (TorrentException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            // System.out.println("ERRORE-file non trovato--->Riscarica il Main File dell'amico " + friend);
        } catch (DownloadException e) {
            e.printStackTrace();
        } catch (DistributedDatabaseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    /**
     * create  a RSS map with the rss-file in the friand's directory
     *
     * @param type type of ordering
     * @user user the searched user
     */
    public void viewRSS(int type, String user) {
        try {
            cModel.setUpdateContentFlag(true);
            String rssKey = "";
            int idRss = 0;
            if (user == "") {
                user = cModel.getNameUserRss();
            }
            if (type == 0) {
                type = cModel.getOrderingType();
            } else {
                cModel.setOrderingType(type);

            }
            //created a sortedmap for order my rss by date(timestamp)
            SortedMap<String, Rss> map = new TreeMap<String, Rss>();
            SortedMap<String, String> friendsMap = new TreeMap<String, String>();
            File nameFile;
            String strLine = "";

            long timestamp = 0;
            int nOfRssMessage = 0;
            int cont = 0;

            //open the "friends"'s file, read it and put the friends in a map
            FileInputStream fstream;
            fstream = new FileInputStream("friends.txt");
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            while ((strLine = br.readLine()) != null) {
                friendsMap.put(strLine, strLine);
            }

            cView.friendsCmb.removeAll();
            cView.deleteFriendsCmb.removeAll();
            cView.friendsCmb.add(allStr);
            Iterator<String> friendsIterator = friendsMap.keySet().iterator();
            String title, text, date, author, typeObj = null, link, timestampStr, prefix = "";

            //for each friend
            while (friendsIterator.hasNext()) {
                Object key = friendsIterator.next();
                strLine = friendsMap.get(key);
                idRss++;

                addDownlodRss(strLine);

                if (strLine != null) {
                    cView.friendsCmb.add(strLine);
                    cView.deleteFriendsCmb.add(strLine);
                    File d = new File(friends_feed_dir + "\\" + strLine);
                    //make an array with him file
                    if (!d.isDirectory())
                        d.mkdirs();

                    String a[] = d.list();

                    //loop the file checking only the content
                    for (int kw = 0; kw < a.length; kw++) {
                        if (!(a[kw].endsWith(".torrent")) && (!a[kw].equals(strLine + "_feed.rss"))) {
                            idRss++;
                            switch (type) {
                                case 1:
                                    prefix = strLine;
                                    break;
                                case 2:
                                    prefix = "";
                                    break;
                            }

                            nameFile = new File(friends_feed_dir + "\\" + strLine + "\\" + a[kw]);
                            if (nameFile.isFile()) {
                                // System.out.println("I'm opening the content " + a[kw]);
                                //if the file exists, and i select all/current friend
                                if ((nameFile.exists()) && ((user.equals(allStr))) || (user.equals(strLine))) {
                                    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                                    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                                    Document doc = dBuilder.parse(nameFile);
                                    doc.getDocumentElement().normalize();

                                    NodeList nList = doc.getElementsByTagName("item");
                                    //for each item in this file
                                    for (int temp = nList.getLength() - 1; temp >= 0; temp--) {

                                        Node nNode = nList.item(temp);
                                        if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                                            //get the params
                                            Element eElement = (Element) nNode;

                                            author = getTagValue("author", eElement);
                                            title = getTagValue("title", eElement);
                                            text = getTagValue("description", eElement);
                                            date = getTagValue("pubDate", eElement);
                                            typeObj = getTagValue("type", eElement);
                                            link = getTagValue("link", eElement);
                                            timestampStr = getTagValue("timestamp", eElement);
                                            timestamp = Long.parseLong(timestampStr);


                                            if (typeObj.equals("img")) {
                                                pluginInterface.getDownloadManager().addDownload(new URL(link), true);
                                            }
                                            cont = 0;

                                            // correct the timestamp  sovrapposition
                                            if (map.containsKey(prefix + (Long.MAX_VALUE - timestamp)) == true) {
                                                while (map.containsKey(prefix + (Long.MAX_VALUE - timestamp))) {
                                                    timestamp = Long.parseLong(timestampStr);
                                                    cont++;
                                                    timestamp += cont;
                                                }
                                            } else {
                                                nOfRssMessage++;
                                                idRss++;
                                            }

                                            //add the rss to the map
                                            Rss newRss = new Rss(timestamp, author, text, title, date, typeObj, link, idRss);
                                            //Revert the timestamp, and create the key
                                            timestamp = Long.MAX_VALUE - timestamp;
                                            rssKey = prefix + timestamp;
                                            //put all in the map
                                            map.put(rssKey, newRss);
                                        }//end if se il nodo letto ha senso
                                    }//ciclo for che scorre i nodi
                                }//enf if "� un file"
                            }
                        }//end if file corretto
                    }//end for dei file
                }
            }


            in.close();
            cModel.setNOfRss(nOfRssMessage);
            cModel.setNameUserRss(user);
            cModel.setRssTableGrid(map);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * update the existing rss whit the new entry
     * if Hash!="", i'm making the Feed otherwise trhe Content
     *
     * @param filename name of th efile to modify
     * @param hash     the child-content hash
     * @param id       user id
     * @return newXml the complete string of XML
     */
    private void makeXml(String filename, String hash, String id) {
        try {
            Rss tempRss = null;
            String descriptionStr = "", titleStr = "", contentType = "text", contentLink = "attribute";
            int timestampInt = getUnixTime();
            int nItem = 1;
            //repeat only 1 time if i'm upgrading the Principal rss
            //else repeat for each item in the map
            if (hash.equals("")) {
                nItem = userContentMap.size();
            }

            for (int k = 1; k <= nItem; k++) {

                if (hash.equals("")) {
                    //if i'm updating a content get all the stat from the map
                    nItem = userContentMap.size();
                    tempRss = userContentMap.get(k + "");
                    descriptionStr = tempRss.getText();
                    if (tempRss.getText().equals("")) {
                        descriptionStr = "";
                    }
                    titleStr = tempRss.getTitle();
                    contentType = tempRss.getRssType();
                    contentLink = tempRss.getLink();
                    if (!contentType.equals("text")) {
                        //if i'm not  inserting a text
                        //create the torrent from the file
                        //and share it
                        int ultimoSlash = titleStr.lastIndexOf("\\");
                        String path = titleStr;
                        String dir = titleStr.substring(0, ultimoSlash);

                        titleStr = titleStr.substring(ultimoSlash + 1);
                        Torrent t = pluginInterface.getTorrentManager().createFromDataFile(new File(path), new URL("dht:"));
                        t.setAnnounceURL(new URL("dht://" + ByteFormatter.encodeString(t.getHash()) + ".dht/announce"));
                        contentLink = "magnet:?xt=urn:btih:" + ByteFormatter.encodeString(t.getHash());
                        t.setComplete(new File(dir));
                        File torrent_file = new File(path + ".torrent");
                        t.writeToFile(torrent_file);

                        Download d = pluginInterface.getDownloadManager().addDownload(t, torrent_file, new File(dir));
                        d.setAttribute(ta_category, cModel.getUserId() + "_userRssPluginCategory");
                        d.setFlag(Download.FLAG_DISABLE_AUTO_FILE_MOVE, true);
                        d.setFlag(Download.FLAG_LOW_NOISE, true);
                        d.setForceStart(true);

                        File f = new File(titleStr + ".torrent");
                        f.delete();
                    }
                } else {
                    descriptionStr = hash;
                    titleStr = "hash-code";
                    contentLink = "magnet:?xt=urn:btih:" + hash;
                }
                //create the content
                // System.out.println("I'm updating the rss");

                DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                Document doc = docBuilder.parse(filename);

                Node channel = doc.getElementsByTagName("channel").item(0);
                // item elements
                Element item = doc.createElement("item");
                channel.appendChild(item);

                // author elements
                Element author = doc.createElement("author");
                author.appendChild(doc.createTextNode(id));
                item.appendChild(author);

                // title elements
                Element title = doc.createElement("title");
                title.appendChild(doc.createTextNode(titleStr));
                item.appendChild(title);

                // description elements
                Element description = doc.createElement("description");
                description.appendChild(doc.createTextNode(descriptionStr));
                item.appendChild(description);

                // type elements
                Element type = doc.createElement("type");
                type.appendChild(doc.createTextNode(contentType));
                item.appendChild(type);

                // typeAttr elements
                Element link = doc.createElement("link");
                link.appendChild(doc.createTextNode(contentLink));
                item.appendChild(link);

                // pubDate elements
                Element pubDate = doc.createElement("pubDate");
                pubDate.appendChild(doc.createTextNode(getTimestamp().toString()));
                item.appendChild(pubDate);

                // timestamp elements
                Element timestamp = doc.createElement("timestamp");
                timestamp.appendChild(doc.createTextNode(timestampInt + ""));
                item.appendChild(timestamp);

                // write the content into xml file
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                DOMSource source = new DOMSource(doc);
                StreamResult result = new StreamResult(new File(filename));
                transformer.transform(source, result);
                result = null;
            }
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        } catch (TorrentException e) {
            e.printStackTrace();
        } catch (DownloadException e) {
            e.printStackTrace();
        }

    }


    /**
     * Return a timestamp
     *
     * @return currentTimestamp the timestamp dd/mm/yyyy  hh.mm.ss.ms
     */
    private Timestamp getTimestamp() {
        Calendar calendar = Calendar.getInstance();
        Timestamp currentTimestamp = new java.sql.Timestamp(calendar.getTime().getTime());
        return currentTimestamp;
    }

    /**
     * return the UNIX time in seconds
     *
     * @return the UNIX time
     */
    private int getUnixTime() {
        return (int) (System.currentTimeMillis());

    }

    /**
     * Make an empty XML
     *
     * @param fileName the new file name
     */
    private void makeNewXml(String fileName) {

        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder;
            docBuilder = docFactory.newDocumentBuilder();
            // root elements
            Document doc = docBuilder.newDocument();
            Element rssElement = doc.createElement("rss");
            rssElement.setAttribute("version", "1.0");
            doc.appendChild(rssElement);

            //make the channel element with sub-node
            Element channel = doc.createElement("channel");
            rssElement.appendChild(channel);

            Element title = doc.createElement("title");
            title.setTextContent(cModel.getChannelTitle());
            channel.appendChild(title);

            Element link = doc.createElement("link");
            link.setTextContent(cModel.getChannelLink());
            channel.appendChild(link);

            Element description = doc.createElement("description");
            description.setTextContent(cModel.getChannelDescription());
            channel.appendChild(description);

            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(fileName));

            transformer.transform(source, result);

            // System.out.println("File created!");
            result = null;
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }


    /**
     * add/delete a new friend
     * (make the file , if doesn't exist)
     *
     * @param type       1 add, 2 delete
     * @param deleteName name to delete
     */
    public void manageFriend(int type, String deleteName) {
        if (((cView.friendIdTxt.getText() != "") && (type == 1)) || (type == 2)) {
            //if doesn't exists the file firends, make him
            if (!(new File("friends.txt").exists())) {
                try {
                    FileOutputStream file = new FileOutputStream("friends.txt");
                    PrintStream Output = new PrintStream(file);
                    Output.print(cView.friendIdTxt.getText());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

            } else {
                try {
                    //open the friends file
                    String line, str = "";
                    FileReader reader = new FileReader("friends.txt");
                    Scanner in = new Scanner(reader);
                    while (in.hasNextLine()) {
                        line = in.nextLine();
                        //delete a friends
                        if (((type == 2) && (!(deleteName.equals(line)))) || (type == 1)) {
                            str += line + "\n";
                        }
                    }
                    //add a friends
                    if (type == 1) {
                        str += cView.friendIdTxt.getText();
                        File friendsDir = new File(friends_feed_dir, "" + cView.friendIdTxt.getText());
                        if (!friendsDir.isDirectory())
                            friendsDir.mkdirs();
                    }
                    PrintStream Output = new PrintStream("friends.txt");
                    Output.println(str);
                    viewRSS(0, allStr);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * add a textual content to the user contentMap
     *
     * @param id the id of the content
     */
    public void addContent(String id) {
        String type = "text", link = "#";
        if (userContentMap.containsKey(id + "")) {
            Rss tmpRss = userContentMap.get(id + "");
            type = tmpRss.getRssType();
            link = tmpRss.getLink();
        }
        // System.out.println(id);
        if ((cView.textTxt.getText().length() > 0) && ((cView.titleTxt.getText().length() > 0) || (Integer.parseInt(id) > 1))) {
            Rss contentRss = new Rss(Long.parseLong("0"), "author", cView.textTxt.getText(), cView.titleTxt.getText(), "", type, link, 10);
            userContentMap.put(id + "", contentRss);
            cModel.setUserContentmap(userContentMap);
            changeLblNContent(0);
            cModel.setStatus("contentAddesdSuccessfulMessage");//currentId
        } else {
            cModel.setStatus("errorAddingContentMessage");//currentId
        }
    }

    /**
     * add a File or Image to the userContent
     *
     * @param file path del file
     * @param type tipo del file (img or file)
     * @param id   id del content
     */
    public void addFileToRSS(String file, String type, String id) {
        if (((type.equals("link")) && (cView.textTxt.getText().length() != 0)) || (type.equals("img"))) {
            Rss contentRss = new Rss(Long.parseLong("0"), "author", cView.textTxt.getText(), file, "blankDate", type, file, 10);
            userContentMap.put(id + "", contentRss);
            cModel.setUserContentmap(userContentMap);
            changeLblNContent(0);
            cModel.setStatus("fileAddedSuccessfulMessage");

        } else {
            cModel.setStatus("descriptionAddingFileRequiredMessage");
        }
    }

    /**
     * delete the id content
     *
     * @param id the id of the content
     */
    public void deleteContent(String id) {
        if (userContentMap.containsKey(id + "")) {
            userContentMap.remove(id + "");
            changeLblNContent(0);
            cModel.setStatus("itemCorrectlyDeleteMessage");
        } else {
            cModel.setStatus("impossibileDeleteItemMessage");
        }
    }

    /**
     * change the N content label
     *
     * @param i the number of item
     */
    public void changeLblNContent(int i) {
        cModel.setUpdateContentFlag(false);
        String text = "", title = "";
        int val = Integer.parseInt(cView.lblNContent.getText());
        if (userContentMap.containsKey("1")) {
            if (((val + i) > 0) && ((val + i) < cModel.getMaxNOfItem())) {
                val = val + i;
                boolean b = userContentMap.containsKey(val + "");

                if (b) {
                    Rss tmpRss = userContentMap.get(val + "");
                    text = tmpRss.getText();
                    title = tmpRss.getTitle();
                }

                cModel.setTextTxtText(text);
                cModel.setTitoloTxt(title);
                cModel.setUserNContent("" + val, b);
            }
        }

        if (val > 1) {
            cModel.setTitoloTxtEnabled(false);
        } else {
            cModel.setTitoloTxtEnabled(true);
        }
    }


    //------------------------------------------------------------------------------------------
    //------------------------------------------------------------------------------------------
    //------------------------------------------------------------------------------------------
    //------------------------------------------------------------------------------------------
    //------------------------------------------------------------------------------------------
    //------------------------------------------------------------------------------------------
    //-------------------------S----------------------------------------------------------------
    //--------------------------C---------------------------------------------------------------
    //---------------------------A--------------------------------------------------------------
    //----------------------------R-------------------------------------------------------------
    //-----------------------------T------------------------------------------------------------
    //------------------------------I-----------------------------------------------------------
    //------------------------------------------------------------------------------------------
    //------------------------------------------------------------------------------------------
    //------------------------------------------------------------------------------------------
    //------------------------------------------------------------------------------------------
    //------------------------------------------------------------------------------------------
    //------------------------------------------------------------------------------------------


    /**
     * TEST WRITE
     *
     * @param key1 the key to write on dbb
     */
    public void testWrite(String key1) {
        // System.out.println("----- controller     inizio write");
        /*


                      ta_publish_feed_desc		= pluginInterface.getTorrentManager().getPluginAttribute( "publish_feed_desc" );
                      ta_publish_feed_content		= pluginInterface.getTorrentManager().getPluginAttribute( "publish_feed_content" );

                      publish_data_dir = new File( pluginInterface.getPluginDirectoryName(), "RSS" );
                      publish_data_dir.mkdirs();

                      temp_data_dir = new File( pluginInterface.getPluginDirectoryName(), "tmp" );
                      temp_data_dir.mkdirs();

                      String feed_name="kkk";
                      String feed_location="c:\\wamp\\www\\rss\\"+feed_name+".rss";

                      publishes_in_progress.put( feed_name, new Boolean( true ));*/


        //addPublishRecord( feed_name,feed_location  );


        //	publishDescription( feed_name  );

        // sign the content torrent hash

        //Torrent	content_torrent	= publishContent( feed_name, feed_location );


        /*String magnetURI = null;
                      DistributedDatabaseValue coupleValue = null;
                      try
                      {
                          magnetURI = "magnet:?xt=urn:btih:"+SHA1("userRss"+fileName);
                      } catch (NoSuchAlgorithmException e1){
                          // TODO Auto-generated catch block
                          e1.printStackTrace();
                      } catch (UnsupportedEncodingException e1){
                          // TODO Auto-generated catch block
                          e1.printStackTrace();
                      }
                      // System.out.println( step + ") chiave: " + fileKey + " - fileName: "+fileName+ " - MagnetLink: " + magnetURI +"  --  step:"+step);

          */
        //Preparo l'oggetto che dovr� salvare sul DDB
        /*

                            String user="alan";


                            try
                            {
                                File publish_data_dir;
                                publish_data_dir = new File( my_feed_dir, "RSS" );
                                if(!publish_data_dir.isDirectory())
                                    publish_data_dir.mkdirs();
                                Torrent torrent = pluginInterface.getTorrentManager().createFromBEncodedFile(new File(publish_data_dir+"\\"+user+".feed.rss.torrent"));

                                URL torrentMagnet=torrent.getAnnounceURL();
                                String torrentHash=ByteFormatter.encodeString(torrent.getHash());
                                DistributedDatabaseKey ddbKeyUser=ddb.createKey(key1);
                                DistributedDatabaseKey ddbKeyHash=ddb.createKey(torrentHash);
                                DistributedDatabaseValue ddbValueHash=ddb.createValue(torrentHash);
                                DistributedDatabaseValue ddbValueTorrent=null;

                                // System.out.println("1q"+torrentMagnet);
                                // System.out.println("1q"+torrentHash);
                                // System.out.println("1q"+torrent.getHash());

                                // System.out.println("1"+ddbKeyHash.hashCode());
                                // System.out.println("2"+ddbKeyHash.toString());
                                //// System.out.println("3"+ByteFormatter.encodeString(ddbKeyHash));
                                // System.out.println("4"+ddbKeyHash);
                                // System.out.println("5"+ddbKeyHash);
                                // System.out.println("6"+ddbValueHash.hashCode());
                                // System.out.println("7"+ddbValueHash.toString());
                                //// System.out.println("8"+ByteFormatter.encodeString(ddbValueHash));
                                // System.out.println("9"+ddbValueHash);
                                // System.out.println("10"+ddbValueHash);


                                Object object2Serailize = (Object)new DdbFile(torrentHash,publish_data_dir+"\\"+user+".feed.rss.torrent", torrentMagnet);
                                ByteArrayOutputStream byteArrayObjectOutputStream = new ByteArrayOutputStream();
                                ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayObjectOutputStream);
                                objectOutputStream.writeObject(object2Serailize);
                                byte[] serializedObject = byteArrayObjectOutputStream.toByteArray();
                                objectOutputStream.close();
                                ddbValueTorrent= ddb.createValue(serializedObject);

                                // System.out.println(ddbValueTorrent);



                                ddb.write(new DdbListenerWrite(), ddbKeyUser, ddbValueHash);
                                ddb.write(new DdbListenerWrite(), ddbKeyHash, ddbValueTorrent);
                            }
                            catch (TorrentException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            } catch (DistributedDatabaseException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
        */

        // System.out.println("----- controller     fine write");
        /*NON CANCELLARE*/


        /*TOTorrentProgressListener list = new TOTorrentProgressListener()
                      {
                          public void
                          reportProgress(
                              int		p )
                          {
                              // System.out.println( "" + p );
                          }
                          public void
                          reportCurrentTask(
                              String	task_description )
                          {
                              // System.out.println( "task = " + task_description );
                          }
                      };
                      */


        //TOTorrent t;


        //TOTorrentCreator c = null;
        /*try {
                                  c = TOTorrentFactory.createFromFileOrDirWithFixedPieceLength(
                                          new File("c:\\wamp\\www\\rss\\asd.rss"),
                                          new URL( "http://79.45.217.166:6969/announce" ),
                                          1024*10 );
                              } catch (MalformedURLException e) {
                                  // TODO Auto-generated catch block
                                  e.printStackTrace();
                              } catch (TOTorrentException e) {
                                  // TODO Auto-generated catch block
                                  e.printStackTrace();
                              }

                              c.addListener( list );*/

        //Torrent t1 = torrentManager.createFromBEncodedInputStream(is);
        //Download d = pluginInterface.getDownloadManager().addDownload(t.);


        /*
                      try {

                          Torrent torrent = pluginInterface.getTorrentManager().createFromDataFile(new File("c:/wamp/www/rss/asd.rss"), null);//(new File("c:/wamp/www/rss/asd.rss"));//.createFromBEncodedFile(new File("c:/wamp/www/rss/asd.rss.torrent"));


                          //String fileName = torrent.getName();
                          File blankTorrentFile = new File( "c:/wamp/www/rss/", "speedTestTorrent.torrent" );
                          torrent.writeToFile(blankTorrentFile);
                      } //TorrentUtils.setFlag( tot, TorrentUtils.TORRENT_FLAG_LOW_NOISE, true );
           catch (TorrentException e) {
                          // TODO Auto-generated catch block
                          e.printStackTrace();
                      }*/


        /*String	sid = "test" ;

                      File	dir = new File("c:/wamp/www/rss/");

                      dir = new File( dir, "temp" );

                      if ( !dir.exists()){

                          if ( !dir.mkdirs()){

                              throw( new IOException( "Failed to create dir '" + dir + "'" ));
                          }
                      }
                      int version=1;
                      final File	torrent_file 	= new File( dir, sid + "_" + version + ".torrent" );


                      PluginInterface pi = pluginInterface;

                      final DownloadManager dm = pi.getDownloadManager();

                      Download download = dm.getDownload( torrent.getHash());

                      if ( download == null ){

                          //log( "Adding download for subscription '" + new String(torrent.getName()) + "'" );

                          //boolean is_update = getSubscriptionFromSID( 123 ) != null;

                          PlatformTorrentUtils.setContentTitle(torrent, "UpdateDownload" + " for subscription '" + name + "'" );

                              // PlatformTorrentUtils.setContentThumbnail(torrent, thumbnail);

                          TorrentUtils.setFlag( torrent, TorrentUtils.TORRENT_FLAG_LOW_NOISE, true );

                          Torrent t = new TorrentImpl( torrent );

                          t.setDefaultEncoding();

                          t.writeToFile( torrent_file );*/
        //download = dm.addDownload( t, torrent_file, data_file );

        //download.setFlag( Download.FLAG_DISABLE_AUTO_FILE_MOVE, true );

        //download.setBooleanAttribute( ta_subs_download, true );

        //Map rd = listener.getRecoveryData();


        /**
         * pluginInterface.getDownloadManager().addDownload(torrent_file)
         *
         *
         ResourceDownloader rdl = pluginInterface.getUtilities().getResourceDownloaderFactory().create(url);
         InputStream is = rdl.download();
         Torrent t = torrentManager.createFromBEncodedInputStream(is);
         Download d = pluginInterface.getDownloadManager().addDownload(t);
         *
         *
         */


        //// System.out.println((i + 1) + ") Name: " + torrentName + " - MagnetLink: " + magnetURI.toString());


        //Serializzo l'oggetto che descriver� il torrent per poter essere salvato nel DDB
        /*try {
                              Object obj = (Object)new TorrentDescriptor(torrentName, magnetURI);
                              ByteArrayOutputStream baos = new ByteArrayOutputStream();
                              ObjectOutputStream oos = new ObjectOutputStream(baos);
                              oos.writeObject(obj);
                              byte[] serializedObject = baos.toByteArray();
                              oos.close();
                              //// System.out.println("SERIALIZZATO! --> " + obj.toString());

                              //Carico il vettore che conterr� i torrent appena serializzati
                              valueList= ddb.createValue(serializedObject);
                          }
                          catch (IOException e) {
                              //plugin.getLogger().logAlertRepeatable(LoggerChannel.LT_ERROR, "Error serializing DDB entry!");
                          }
                          catch (DistributedDatabaseException e) {
                              //plugin.getLogger().logAlertRepeatable(LoggerChannel.LT_ERROR, "Error generating value vector! [" + list.get(i).getFileName() + "]");
                          }


                      //Finito il for ora posso scrivere la entry < categoria, vettore_elementi > nel DDB
                      try {
                          //DistributedDatabaseKey key = ddb.createKey(category.getBytes());
                          //ddb.write(new DdbListenerWrite(), key, valueList);
                      }
                      catch (DistributedDatabaseException e) {
                          //plugin.getLogger().logAlertRepeatable(LoggerChannel.LT_ERROR, "Error generating key or writing to the DDB!");
                      }}*/

    }


    /**
     * read the DHT. the DdbListenerRead  throw the events
     *
     * @param key the key-identificator in the DDB
     */
    public void testRead(String key) {
        String newPath;
        TorrentAttribute ta_category = Controller.pluginInterface.getTorrentManager().getAttribute(TorrentAttribute.TA_CATEGORY);
        //String category= download.getAttribute(ta_category);
        //// System.out.println("categoria "+category);
        String name = "alan_feed.rss";
        String path = "C:\\Users\\Phobos\\AppData\\Roaming\\Azureus\\Documents\\Vuze Downloads\\alan_feed.rss";
        //// System.out.println(newState);

        // System.out.println("old path" + path);
        //check the file
        File myFile = new File(path);
        //extract the friends name
        String[] namePart = name.split("_");
        newPath = Controller.pluginInterface.getPluginDirectoryName() + "\\friends_dir\\" + namePart[0];


        //if the file doesn't end with .rss, it's a file, so put it in friend_file dir
        if (!name.endsWith(".rss")) {
            newPath += "_file";
        }

        //check if dir exist, if doesn't exist, i make it
        File tmpDir = new File(newPath);
        if (!tmpDir.exists()) {
            tmpDir.mkdir();
        }
        newPath += "\\" + name;

        // System.out.println("new path" + newPath);
        //move the original file in the correct Dir
        myFile.renameTo(new File(newPath));/*
			    	
		DistributedDatabaseValue valueList;
					try {
				    	File file=new File("C:\\Users\\Phobos\\workspace\\Tesi_laurea\\plugins\\userrss\\friends_dir\\alan_file\\VUZE.jpg");
						Object obj = (Object)file;
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						ObjectOutputStream oos;
						oos = new ObjectOutputStream(baos);
						oos.writeObject(obj);
						byte[] serializedObject = baos.toByteArray();
						oos.close();
						
						valueList = ddb.createValue(serializedObject);
						DistributedDatabaseKey key = ddb.createKey("alanTEST11".getBytes());
						ddb.write(new DdbListenerWrite(), key, valueList);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (DistributedDatabaseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}*/

        /*TEST LETTURA XML
                       try {
                               DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                              DocumentBuilder docBuilder = docFactory.newDocumentBuilder();


                              // root elements
                              Document doc = docBuilder.newDocument();
                              Element rssElement = doc.createElement("rss");
                              doc.appendChild(rssElement);


                              Element rootElement = doc.createElement("channel");
                              rssElement.appendChild(rootElement);



                              // staff elements
                              Element staff = doc.createElement("item");
                              rootElement.appendChild(staff);

                              // set attribute to staff element
                              //Attr attr = doc.createAttribute("id");
                              //attr.setValue("1");
                              //staff.setAttributeNode(attr);

                              // shorten way
                              // staff.setAttribute("id", "1");

                              // firstname elements
                              Element author = doc.createElement("author");
                              author.appendChild(doc.createTextNode("autore"));
                              staff.appendChild(author);

                              // lastname elements
                              Element title = doc.createElement("title");
                              title.appendChild(doc.createTextNode("titolo"));
                              staff.appendChild(title);

                              // nickname elements
                              Element description = doc.createElement("description");
                              description.appendChild(doc.createTextNode("text"));
                              staff.appendChild(description);

                              // salary elements
                              Element type = doc.createElement("type");
                              type.appendChild(doc.createTextNode("type"));
                              staff.appendChild(type);

                              // salary elements
                              Element typeAttr = doc.createElement("typeAttr");
                              typeAttr.appendChild(doc.createTextNode("typeAttr"));
                              staff.appendChild(typeAttr);

                              // salary elements
                              Element pubDate = doc.createElement("pubDate");
                              pubDate.appendChild(doc.createTextNode("date"));
                              staff.appendChild(pubDate);

                              // salary elements
                              Element timestamp = doc.createElement("timestamp");
                              timestamp.appendChild(doc.createTextNode("timestamp"));
                              staff.appendChild(timestamp);



                              // write the content into xml file
                              TransformerFactory transformerFactory = TransformerFactory.newInstance();
                              Transformer transformer = transformerFactory.newTransformer();
                              DOMSource source = new DOMSource(doc);
                              StreamResult result = new StreamResult(new File("C:\\file.rss"));

                              // Output to console for testing
                              // StreamResult result = new StreamResult(System.out);

                              transformer.transform(source, result);

                              // System.out.println("File saved!");
                       } catch (TransformerException e) {
                          // TODO Auto-generated catch block
                          e.printStackTrace();
                      } catch (ParserConfigurationException e) {
                          // TODO Auto-generated catch block
                          e.printStackTrace();
                      }finally{}




                       try{

                       // System.out.println("Sto iniziando a moddare");

                       String filepath = "c:\\file.rss";
                       DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                       DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                       Document doc = docBuilder.parse(filepath);

                       Node staff1 = doc.getElementsByTagName("channel").item(0);
                      // staff elements
                       Element staff = doc.createElement("item");
                       staff1.appendChild(staff);

                       // set attribute to staff element
                       //Attr attr = doc.createAttribute("id");
                       //attr.setValue("1");
                       //staff.setAttributeNode(attr);

                       // shorten way
                       // staff.setAttribute("id", "1");

                       // firstname elements
                       Element author = doc.createElement("author");
                       author.appendChild(doc.createTextNode("autore1"));
                       staff.appendChild(author);

                       // lastname elements
                       Element title = doc.createElement("title");
                       title.appendChild(doc.createTextNode("titolo1"));
                       staff.appendChild(title);

                       // nickname elements
                       Element description = doc.createElement("description");
                       description.appendChild(doc.createTextNode("text1"));
                       staff.appendChild(description);

                       // salary elements
                       Element type = doc.createElement("type");
                       type.appendChild(doc.createTextNode("type1"));
                       staff.appendChild(type);

                       // salary elements
                       Element typeAttr = doc.createElement("typeAttr");
                       typeAttr.appendChild(doc.createTextNode("typeAttr1"));
                       staff.appendChild(typeAttr);

                       // salary elements
                       Element pubDate = doc.createElement("pubDate");
                       pubDate.appendChild(doc.createTextNode("date1"));
                       staff.appendChild(pubDate);

                       // salary elements
                       Element timestamp = doc.createElement("timestamp");
                       timestamp.appendChild(doc.createTextNode("timestamp1"));
                       staff.appendChild(timestamp);



                       // write the content into xml file
                       TransformerFactory transformerFactory = TransformerFactory.newInstance();
                       Transformer transformer = transformerFactory.newTransformer();
                       DOMSource source = new DOMSource(doc);
                       StreamResult result = new StreamResult(new File("C:\\file.rss"));
                       transformer.transform(source, result);

                       } catch (TransformerConfigurationException e) {
                          // TODO Auto-generated catch block
                          e.printStackTrace();
                      } catch (SAXException e) {
                          // TODO Auto-generated catch block
                          e.printStackTrace();
                      } catch (IOException e) {
                          // TODO Auto-generated catch block
                          e.printStackTrace();
                      } catch (ParserConfigurationException e) {
                          // TODO Auto-generated catch block
                          e.printStackTrace();
                      } catch (TransformerException e) {
                          // TODO Auto-generated catch block
                          e.printStackTrace();
                      }finally{}














                               // System.out.println("Sto iniziando a leggere");

                       try {

                              File fXmlFile = new File("c:\\file.rss");
                              DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                              DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                              Document doc = dBuilder.parse(fXmlFile);
                              doc.getDocumentElement().normalize();

                              // System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
                              NodeList nList = doc.getElementsByTagName("item");
                              // System.out.println("-----------------------");

                              for (int temp = 0; temp < nList.getLength(); temp++) {

                                 Node nNode = nList.item(temp);
                                 if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                                    Element eElement = (Element) nNode;

                                    // System.out.println("author : " + getTagValue("author", eElement));
                                    // System.out.println("description : " + getTagValue("description", eElement));
                                    // System.out.println("title : " + getTagValue("title", eElement));
                                    // System.out.println("timestamp : " + getTagValue("timestamp", eElement));	    		          // System.out.println("title : " + getTagValue("title", eElement));
                                    // System.out.println("pubDate : " + getTagValue("pubDate", eElement));
                                    // System.out.println("typeAttr : " + getTagValue("typeAttr", eElement));
                                    // System.out.println("type : " + getTagValue("type", eElement));


                                 }
                              }
                            } catch (Exception e) {
                              e.printStackTrace();
                            }
                       */


    }


}
