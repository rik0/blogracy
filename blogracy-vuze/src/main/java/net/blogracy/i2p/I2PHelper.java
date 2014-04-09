package net.blogracy.i2p;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.logging.SimpleFormatter;
import java.util.Scanner;

import javax.xml.bind.DatatypeConverter;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import org.gudy.azureus2.core3.config.COConfigurationManager;
import org.gudy.azureus2.core3.util.Base32;
import org.gudy.azureus2.core3.util.ByteFormatter;
import org.gudy.azureus2.plugins.PluginInterface;
import org.gudy.azureus2.plugins.torrent.Torrent;
import org.gudy.azureus2.plugins.torrent.TorrentException;


public class I2PHelper {

    public static void main(String[] args) throws Exception {
        String tunnel = getTunnel("AzureusData");
        System.out.println("tunnel=" + tunnel);
        String dest = getDestination(tunnel);
        System.out.println("dest=" + dest);        
    }

    public static String getTunnel(String name) throws IOException {
        String html = null;
        try {
            Scanner scanner = new Scanner(new URL("http://127.0.0.1:7657/i2ptunnel/").openStream());
            scanner.useDelimiter("\0");
            html = scanner.next();

        } catch (MalformedURLException e) { }
        int pos1 = html.indexOf(">" + name + "<");
        int pos2 = html.lastIndexOf("tunnel=", pos1) + 7;
        int pos3 = html.indexOf('"', pos2);
        String tunnel = html.substring(pos2, pos3);
        return tunnel;
    }
    
    public static String getDestination(String tunnel) throws IOException {
        String html = null;
        try {
            Scanner scanner = new Scanner(new URL("http://127.0.0.1:7657/i2ptunnel/edit?tunnel=" + tunnel).openStream());
            scanner.useDelimiter("\0");
            html = scanner.next();
        } catch (MalformedURLException e) { }
        int pos1 = html.indexOf("id=\"localDestination\"");
        int pos2 = html.indexOf('>', pos1) + 1;
        int pos3 = html.indexOf('<', pos2);
        String dest = html.substring(pos2, pos3);
        return dest;
    }
    
    public static boolean isEnabled() {
		return COConfigurationManager.getBooleanParameter("Plugin.azneti2p.enabled");
    }
    
    public static void store(String key, String value) throws IOException {
	    String torrentStore = COConfigurationManager.getStringParameter("Plugin.blogracy.torrent_store");
        URL torrentURL = null;
        try {
    	    torrentURL = new URL(torrentStore + "/upfile/filemap?key=" + key + "&value=" + value);
		} catch (MalformedURLException e) { e.printStackTrace(); }
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 4444));
	    InputStream is = torrentURL.openConnection(proxy).getInputStream();
	    String newValue = (new Scanner(is)).nextLine();
    }

    public static String lookup(String key) throws IOException {
		String torrentStore = COConfigurationManager.getStringParameter("Plugin.blogracy.torrent_store");
	    URL torrentURL = null;
	    try {
	        torrentURL = new URL(torrentStore + "/upfile/filemap?key=" + key);
		} catch (MalformedURLException e) { e.printStackTrace(); }
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 4444));
	    InputStream is = torrentURL.openConnection(proxy).getInputStream();
	    String value = (new Scanner(is)).nextLine();
	    return value;
	}

    public static Torrent createTorrent(File file, PluginInterface vuze) throws IOException, TorrentException {
        URL tracker = null;
	    try {
	        tracker = new URL(COConfigurationManager.getStringParameter("Plugin.blogracy.tracker"));
		} catch (MalformedURLException e) { e.printStackTrace(); }
        Torrent torrent = vuze.getTorrentManager().createFromDataFile(file, tracker);
		torrent.setComplete(file.getParentFile());

        String torrentHash = DatatypeConverter.printHexBinary(torrent.getHash());
        //String torrentHash = Base32.encode(torrent.getHash());
		File torrentFile = new File (file.getParentFile(), torrentHash + ".torrent");
		torrent.writeToFile(torrentFile);
		String torrentStore = COConfigurationManager.getStringParameter("Plugin.blogracy.torrent_store");
		upload(torrentFile, torrentStore + "/upfile/fileupload", "127.0.0.1", 4444);
		return torrent;
	}

    public static Torrent getTorrent(String hash, PluginInterface vuze) throws IOException, TorrentException {
		String torrentStore = COConfigurationManager.getStringParameter("Plugin.blogracy.torrent_store");
	    URL torrentURL = null;
	    try {
    	    torrentURL = new URL(torrentStore + "/" + hash + ".torrent");
		} catch (MalformedURLException e) { e.printStackTrace(); }
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 4444));
	    InputStream is = torrentURL.openConnection(proxy).getInputStream();
	    Torrent torrent = vuze.getTorrentManager().createFromBEncodedInputStream(is);
	    return torrent;
    }

    public static void upload(File file, String url, String proxyAddr, int proxyPort) throws IOException {
	    HttpEntity reqEntity = MultipartEntityBuilder.create()
            .addBinaryBody("upfile", file)
            .addTextBody("torrent_category", "Uncategorized")
            .addTextBody("torrent_include_hashes", "IncludeHashes")
            .addTextBody("torrent_passive", "Passive")
            .addTextBody("torrent_announce_protocol", "HTTP")
            .addTextBody("torrent_hash", "")
            .addTextBody("torrent_force_start", "ForceStart")
            .build();
	    CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(reqEntity);
        if (proxyPort != 0) {
            RequestConfig config = RequestConfig.custom()
                .setProxy(new HttpHost(proxyAddr, proxyPort))
                .build();
            httpPost.setConfig(config);
        }                
        CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
    }    
}
