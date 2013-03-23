package net.blogracy.messaging;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.blogracy.logging.Logger;
import net.blogracy.messaging.impl.BlogracyContent;
import net.blogracy.messaging.impl.BlogracyContentAccepted;
import net.blogracy.messaging.impl.BlogracyContentListRequest;
import net.blogracy.messaging.impl.BlogracyContentListResponse;
import net.blogracy.messaging.impl.BlogracyContentRejected;
import net.blogracy.messaging.impl.BlogracyDataMessage;
import net.blogracy.messaging.impl.MessagingUtils;
import net.blogracy.messaging.peer.BlogracyDataMessageListener;
import net.blogracy.messaging.peer.PeerController;
import net.blogracy.messaging.peer.impl.PeerControllerImpl;

import org.gudy.azureus2.plugins.PluginInterface;
import org.gudy.azureus2.plugins.download.Download;
import org.gudy.azureus2.plugins.torrent.Torrent;
import org.gudy.azureus2.plugins.utils.Formatters;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MessagingManager implements BlogracyDataMessageListener {

	// private static String localUserHash;
	private PluginInterface pluginInterface;
	public Torrent genericTorrent;
	private static Formatters formatters;

	private final static String RES_TORRENT = "channel.torrent";
	private final static String SWARMCHANNELS_DIR = "swarmChannels";

	protected PeerController peerController;

	private Map<String, Download> swarmList = new HashMap<String, Download>();

	private static URL ANNOUNCE_URL;

	protected List<BlogracyContentMessageListener> listeners = new ArrayList<BlogracyContentMessageListener>();

	public MessagingManager(PluginInterface pi) {
		try {
			ANNOUNCE_URL = new URL("dht://chat.dht/announce");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		// localUserHash = currentUserHash;
		pluginInterface = pi;

		formatters = pluginInterface.getUtilities().getFormatters();
		genericTorrent = loadTorrent(RES_TORRENT);

		peerController = new PeerControllerImpl(pluginInterface);
		peerController.addMessageListener(this);
		List<BlogracyDataMessage> listOfHandledMessages = new ArrayList<BlogracyDataMessage>();
		listOfHandledMessages.add(new BlogracyContentAccepted("", new byte[20],
				-1, ""));
		listOfHandledMessages.add(new BlogracyContentRejected("", new byte[20],
				-1, ""));
		listOfHandledMessages.add(new BlogracyContentListRequest("",
				new byte[20], -1, ""));
		listOfHandledMessages.add(new BlogracyContentListResponse("",
				new byte[20], -1, "", ""));
		listOfHandledMessages.add(new BlogracyContent("",
				new byte[20], -1, ""));
		peerController.initialize(listOfHandledMessages);
		peerController.startPeerProcessing();
	}

	public void addListener(BlogracyContentMessageListener listener) {
		synchronized (listeners) {
			if (!listeners.contains(listener))
				listeners.add(listener);
		}
	}

	private Torrent loadTorrent(String res) {
		ClassLoader cl = this.getClass().getClassLoader();
		InputStream is = cl.getResourceAsStream(res);
		if (is != null) {
			try {
				return pluginInterface.getTorrentManager()
						.createFromBEncodedInputStream(is);
			} catch (Exception e) {
				Logger.info("System: The channel torrent is impossible to create!");
				return null;
			}
		}
		Logger.info("System: The channel torrent created is null");
		return null;
	}

	@SuppressWarnings("rawtypes")
	protected Torrent getSwarmTorrent(String userId) {
		if (userId == null || userId.isEmpty())
			return null;

		try {
			Map genericMap = genericTorrent.writeToMap();
			Map info = (Map) genericMap.get("info");
			String channelName = userId + "-BLOGRACY-FRIENDSWARM";
			info.put("name", channelName.getBytes());
			info.put("name.utf8", channelName.getBytes("UTF-8"));
			genericMap.put("info", info);
			byte[] channelTorrent = pluginInterface.getUtilities()
					.getFormatters().bEncode(genericMap);
			Torrent result = pluginInterface.getTorrentManager()
					.createFromBEncodedData(channelTorrent);
			result.setComment(channelName);
			result.setAnnounceURL(ANNOUNCE_URL);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public Download getSwarm(String userId) {
		if (userId == null || userId.isEmpty())
			return null;
		synchronized (swarmList) {
			if (swarmList.containsKey(userId))
				return swarmList.get(userId);
			else
				return null;
		}
	}

	public Download addSwarm(String userId) {
		Torrent torrent = getSwarmTorrent(userId);
		String savePath = pluginInterface.getPluginDirectoryName();
		try {
			File saveDir = new File(savePath, SWARMCHANNELS_DIR
					+ File.separator);
			saveDir.mkdir();
			Download dl = null;
			synchronized (swarmList) {
				dl = pluginInterface.getDownloadManager().addDownload(torrent,
						null, saveDir);
				dl.setForceStart(true);

				swarmList.put(userId, dl);
			}

			File dest = new File(savePath, SWARMCHANNELS_DIR + File.separator
					+ userId);
			File src = new File(savePath, SWARMCHANNELS_DIR + File.separator
					+ "channel");
			MessagingUtils.copyFile(src, dest);
			return dl;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void sendContentAccepted(String userId, String contentId) {
		Download userIdSwarm = this.getSwarm(userId);

		if (userIdSwarm == null)
			return;
		byte[] peerID = userIdSwarm.getDownloadPeerId();
		if (peerID != null) {
			JSONObject jsonContent = new JSONObject();
			try {
				jsonContent.put("contentId", contentId);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			BlogracyContentAccepted message = new BlogracyContentAccepted(
					userId, peerID, 0, jsonContent.toString());

			peerController.sendMessage(userIdSwarm, peerID, userId, message);
		} else {
			System.out
					.println("System: Torrent isn't running, message can't be delivered");
		}

	}

	public void sendContentRejected(String userId, String contentId) {
		Download userIdSwarm = this.getSwarm(userId);

		if (userIdSwarm == null)
			return;
		byte[] peerID = userIdSwarm.getDownloadPeerId();
		if (peerID != null) {
			JSONObject jsonContent = new JSONObject();
			try {
				jsonContent.put("contentId", contentId);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			BlogracyContentRejected message = new BlogracyContentRejected(
					userId, peerID, 0, jsonContent.toString());

			peerController.sendMessage(userIdSwarm, peerID, userId, message);
		} else {
			System.out
					.println("System: Torrent isn't running, message can't be delivered");
		}
	}

	public void sendContentListRequest(String userId) {
		Download userIdSwarm = this.getSwarm(userId);

		if (userIdSwarm == null)
			return;

		byte[] peerID = userIdSwarm.getDownloadPeerId();
		if (peerID != null) {
			String content = "";
			BlogracyContentListRequest message = new BlogracyContentListRequest(
					userId, peerID, 0, content);
			peerController.sendMessage(userIdSwarm, peerID, userId, message);
		} else {
			System.out
					.println("System: Torrent isn't running, message can't be delivered");
		}
	}
	
	public void sendContentListResponse(String senderUserId, String queriedUserId, JSONArray contentData)
	{
		Download queriedUserIdSwarm = this.getSwarm(queriedUserId);
		
		if (queriedUserIdSwarm == null)
			return;

		byte[] peerID = queriedUserIdSwarm.getDownloadPeerId();
		if (peerID != null) {
			JSONObject jsonContent = new JSONObject();
			try {
				jsonContent.put("contentUserId", queriedUserId);
				jsonContent.put("content", contentData);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			BlogracyContentListResponse message = new BlogracyContentListResponse(
					senderUserId, peerID, 0, queriedUserId, jsonContent.toString());
			peerController.sendMessage(queriedUserIdSwarm, peerID, senderUserId, message);
		} else {
			System.out
					.println("System: Torrent isn't running, message can't be delivered");
		}
	}

	public void sendContentMessage(String userId, String destinationUserId,
			String contentData) {
		Download destinationSwarm = this.getSwarm(destinationUserId);

		if (destinationSwarm == null)
			return;

		byte[] peerID = destinationSwarm.getDownloadPeerId();
		if (peerID != null) {
			String content = contentData;
			BlogracyContent message = new BlogracyContent(
					userId, peerID, 0, content);
			peerController.sendMessage(destinationSwarm, peerID, userId,
					message);
		} else {
			System.out
					.println("System: Torrent isn't running, message can't be delivered");
		}
	}

	public static byte[] bEncode(Map map) {
		if (formatters == null) {
			return new byte[0];
		}
		try {
			return formatters.bEncode(map);
		} catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public static Map bDecode(byte[] bytes) {
		if (formatters == null) {
			return new HashMap();
		}
		try {
			return formatters.bDecode(bytes);
		} catch (IOException e) {
			e.printStackTrace();
			return new HashMap();
		}
	}

	/*****************************************************
	 * 
	 * BlogracyDataMessageListener members
	 * 
	 * 
	 *****************************************************/
	@Override
	public void blogracyDataMessageReceived(Download download, byte[] sender,
			String nick, String content) {
		// TODO Auto-generated method stub

	}

	@Override
	public void downloadAdded(Download download) {
	}

	@Override
	public void downloadRemoved(Download download) {

	}

	@Override
	public void downloadActive(Download download) {
	}

	@Override
	public void downloadInactive(Download download) {
	}

}
