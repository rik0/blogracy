package net.blogracy.messaging;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.blogracy.logging.Logger;
import net.blogracy.messaging.impl.BlogracyBullyAnswerMessage;
import net.blogracy.messaging.impl.BlogracyBullyCoordinatorMessage;
import net.blogracy.messaging.impl.BlogracyBullyElectionMessage;
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
import org.gudy.azureus2.plugins.download.DownloadListener;
import org.gudy.azureus2.plugins.torrent.Torrent;
import org.gudy.azureus2.plugins.torrent.TorrentException;
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
			Logger.error("MessagingManager | ctor | " + e.getMessage());
		}

		// localUserHash = currentUserHash;
		pluginInterface = pi;

		formatters = pluginInterface.getUtilities().getFormatters();
		genericTorrent = loadTorrent(RES_TORRENT);

		peerController = new PeerControllerImpl(pluginInterface);
		peerController.addMessageListener(this);
		List<BlogracyDataMessage> listOfHandledMessages = new ArrayList<BlogracyDataMessage>();
		listOfHandledMessages.add(new BlogracyContentAccepted("", new byte[20], "", -1, ""));
		listOfHandledMessages.add(new BlogracyContentRejected("", new byte[20], "", -1, ""));
		listOfHandledMessages.add(new BlogracyContentListRequest("", new byte[20], "", -1, ""));
		listOfHandledMessages.add(new BlogracyContentListResponse("", new byte[20], "", -1, ""));
		listOfHandledMessages.add(new BlogracyContent("", new byte[20], "", -1, ""));
		listOfHandledMessages.add(new BlogracyBullyAnswerMessage("", new byte[20], -1, ""));
		listOfHandledMessages.add(new BlogracyBullyCoordinatorMessage("", new byte[20], -1, ""));
		listOfHandledMessages.add(new BlogracyBullyElectionMessage("", new byte[20], -1, ""));
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
				return pluginInterface.getTorrentManager().createFromBEncodedInputStream(is);
			} catch (Exception e) {
				Logger.info("MessagingManager | loadTorrent |" + "System: The channel torrent is impossible to create!");
				return null;
			}
		}
		Logger.info("MessagingManager | loadTorrent |" + "System: The channel torrent created is null");
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
			byte[] channelTorrent = pluginInterface.getUtilities().getFormatters().bEncode(genericMap);
			Torrent result = pluginInterface.getTorrentManager().createFromBEncodedData(channelTorrent);
			result.setComment(channelName);
			result.setAnnounceURL(ANNOUNCE_URL);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("MessagingManager | getSwarmTorrent | " + e.getMessage());
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
			File saveDir = new File(savePath, SWARMCHANNELS_DIR + File.separator);
			saveDir.mkdir();
			Download dl = null;
			synchronized (swarmList) {
				dl = pluginInterface.getDownloadManager().addDownload(torrent, null, saveDir);
				dl.setForceStart(true);
				dl.addListener(new DownloadListener() {

					@Override
					public void stateChanged(Download download, int old_state, int new_state) {
						Logger.error("MessagingManager | download state changed  " + Download.ST_NAMES[old_state] + " -> " + Download.ST_NAMES[new_state]);
						if (new_state != Download.ST_SEEDING && new_state != Download.ST_DOWNLOADING)
							download.startDownload(true);

					}

					@Override
					public void positionChanged(Download download, int oldPosition, int newPosition) {
						// TODO Auto-generated method stub

					}
				});

				swarmList.put(userId, dl);
			}

			File dest = new File(savePath, SWARMCHANNELS_DIR + File.separator + userId);
			File src = new File(savePath, SWARMCHANNELS_DIR + File.separator + "channel");
			MessagingUtils.copyFile(src, dest);
			return dl;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void sendContentAccepted(String senderUserId, String contentRecipientUserId, String contentId) {
		Download userIdSwarm = this.getSwarm(contentRecipientUserId);

		if (userIdSwarm == null) {
			Logger.error("MessagingManager | sendContentAccepted | No swarm found for userId " + contentRecipientUserId);
			return;
		}

		byte[] peerID = userIdSwarm.getDownloadPeerId();
		if (peerID != null) {
			JSONObject jsonContent = new JSONObject();
			try {
				jsonContent.put("contentId", contentId);
			} catch (JSONException e) {
				e.printStackTrace();
				Logger.error("MessagingManager | sendContentAccepted | " + e.getMessage());
			}
			BlogracyContentAccepted message = new BlogracyContentAccepted(senderUserId, peerID, contentRecipientUserId, 0, jsonContent.toString());

			peerController.sendMessage(userIdSwarm, peerID, senderUserId, message);
		} else {
			Logger.error("MessagingManager | sendContentAccepted | " + "System: Torrent isn't running, message can't be delivered");
		}

	}

	public void sendContentRejected(String senderUserId, String contentRecipientUserId, String contentId) {
		Download userIdSwarm = this.getSwarm(contentRecipientUserId);

		if (userIdSwarm == null) {
			Logger.error("MessagingManager | sendContentRejected | No swarm found for userId " + contentRecipientUserId);
			return;
		}
		byte[] peerID = userIdSwarm.getDownloadPeerId();
		if (peerID != null) {
			JSONObject jsonContent = new JSONObject();
			try {
				jsonContent.put("contentId", contentId);
			} catch (JSONException e) {
				e.printStackTrace();
				Logger.error("MessagingManager | sendContentRejected |" + e.getMessage());
			}
			BlogracyContentRejected message = new BlogracyContentRejected(senderUserId, peerID, contentRecipientUserId, 0, jsonContent.toString());
			peerController.sendMessage(userIdSwarm, peerID, senderUserId, message);
		} else {
			Logger.error("MessagingManager | sendContentRejected |" + "System: Torrent isn't running, message can't be delivered");
		}
	}

	public void sendContentListRequest(String senderUserId, String queriedUserId) {
		Download userIdSwarm = this.getSwarm(queriedUserId);

		if (userIdSwarm == null) {
			Logger.error("MessagingManager | sendContentListRequest | No swarm found for userId " + queriedUserId);
			return;
		}

		byte[] peerID = userIdSwarm.getDownloadPeerId();
		if (peerID != null) {
			String content = "";
			BlogracyContentListRequest message = new BlogracyContentListRequest(senderUserId, peerID, queriedUserId, 0, content);
			peerController.sendMessage(userIdSwarm, peerID, senderUserId, message);
		} else {
			Logger.error("MessagingManager | sendContentListRequest | " + "System: Torrent isn't running, message can't be delivered");
		}
	}

	public void sendContentListResponse(String senderUserId, String queriedUserId, JSONArray contentData) {
		Download queriedUserIdSwarm = this.getSwarm(queriedUserId);

		if (queriedUserIdSwarm == null) {
			Logger.error("MessagingManager | sendContentListResponse | No swarm found for userId " + queriedUserIdSwarm);
			return;
		}

		byte[] peerID = queriedUserIdSwarm.getDownloadPeerId();
		if (peerID != null) {
			JSONObject jsonContent = new JSONObject();
			try {
				jsonContent.put("contentUserId", queriedUserId);
				jsonContent.put("contents", contentData);
			} catch (JSONException e) {
				e.printStackTrace();
				Logger.error("MessagingManager | sendContentListResponse | " + e.getMessage());
			}
			BlogracyContentListResponse message = new BlogracyContentListResponse(senderUserId, peerID, queriedUserId, 0, jsonContent.toString());
			peerController.sendMessage(queriedUserIdSwarm, peerID, senderUserId, message);
		} else {
			Logger.error("MessagingManager | sendContentListResponse |" + "System: Torrent isn't running, message can't be delivered");
		}
	}

	public void sendContentMessage(String senderUserId, String destinationUserId, String contentData) {
		Download destinationSwarm = this.getSwarm(destinationUserId);

		if (destinationSwarm == null) {
			Logger.error("MessagingManager | sendContentMessage | No swarm found for userId " + destinationUserId);
			return;
		}

		byte[] peerID = destinationSwarm.getDownloadPeerId();
		if (peerID != null) {
			String content = contentData;
			BlogracyContent message = new BlogracyContent(senderUserId, peerID, destinationUserId, 0, content);
			peerController.sendMessage(destinationSwarm, peerID, senderUserId, message);
		} else {
			Logger.error("MessagingManager | sendContentMessage |" + "System: Torrent isn't running, message can't be delivered");
		}
	}

	public void sendBullyCoordinatorMessage(String channelUserId, String senderUserId) {
		Download destinationSwarm = this.getSwarm(channelUserId);

		if (destinationSwarm == null) {
			Logger.error("MessagingManager | sendBullyCoordinatorMessage | No swarm found for userId " + channelUserId);
			return;
		}

		byte[] peerID = destinationSwarm.getDownloadPeerId();
		if (peerID != null) {
			BlogracyBullyCoordinatorMessage message = new BlogracyBullyCoordinatorMessage(senderUserId, peerID, 0, null);
			peerController.sendMessage(destinationSwarm, peerID, senderUserId, message);
		} else {
			Logger.error("MessagingManager | sendBullyCoordinatorMessage |" + "System: Torrent isn't running, message can't be delivered");
		}
	}

	public void sendBullyAnswerMessage(String channelUserId, String senderUserId) {
		Download destinationSwarm = this.getSwarm(channelUserId);

		if (destinationSwarm == null) {
			Logger.error("MessagingManager | sendBullyAnswerMessage | No swarm found for userId " + channelUserId);
			return;
		}

		byte[] peerID = destinationSwarm.getDownloadPeerId();
		if (peerID != null) {
			BlogracyBullyAnswerMessage message = new BlogracyBullyAnswerMessage(senderUserId, peerID, 0, null);
			peerController.sendMessage(destinationSwarm, peerID, senderUserId, message);
		} else {
			Logger.error("MessagingManager | sendBullyAnswerMessage | " + "System: Torrent isn't running, message can't be delivered");
		}
	}

	public void sendBullyElectionMessage(String channelUserId, String senderUserId) {
		Download destinationSwarm = this.getSwarm(channelUserId);

		if (destinationSwarm == null) {
			Logger.error("MessagingManager | sendBullyElectionMessage | No swarm found for userId " + channelUserId);
			return;
		}

		byte[] peerID = destinationSwarm.getDownloadPeerId();
		if (peerID != null) {
			BlogracyBullyElectionMessage message = new BlogracyBullyElectionMessage(senderUserId, peerID, 0, null);
			peerController.sendMessage(destinationSwarm, peerID, senderUserId, message);
		} else {
			Logger.error("MessagingManager | sendBullyElectionMessage | " + "System: Torrent isn't running, message can't be delivered");
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
	 * @throws UnsupportedEncodingException
	 * 
	 * 
	 *****************************************************/
	@Override
	public void blogracyDataMessageReceived(Download download, byte[] sender, String nick, BlogracyDataMessage message) {
		String channelUserId = "";
		try {
			Map genericMap = download.getTorrent().writeToMap();
			Map downloadMap = (Map) genericMap.get("info");
			channelUserId = new String((byte[]) downloadMap.get("name.utf8"), "UTF-8");
			if (channelUserId != null)
				channelUserId = channelUserId.replace("-BLOGRACY-FRIENDSWARM", "");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (TorrentException e1) {
			e1.printStackTrace();
		} catch (Exception ex) {
			Logger.error("MessagingManager | blogracyDataMessageReceived |" + ex.getMessage());
		}

		synchronized (listeners) {
			for (BlogracyContentMessageListener l : listeners) {
				if (message.getID() == BlogracyContent.ID)
					l.blogracyContentReceived((BlogracyContent) message);
				else if (message.getID() == BlogracyContentAccepted.ID)
					l.blogracyContentAcceptedReceived((BlogracyContentAccepted) message);
				else if (message.getID() == BlogracyContentRejected.ID)
					l.blogracyContentRejectedReceived((BlogracyContentRejected) message);
				else if (message.getID() == BlogracyContentListRequest.ID)
					l.blogracyContentListRequestReceived((BlogracyContentListRequest) message);
				else if (message.getID() == BlogracyContentListResponse.ID)
					l.blogracyContentListResponseReceived((BlogracyContentListResponse) message);
				else if (message.getID() == BlogracyBullyAnswerMessage.ID)
					l.blogracyBullyAnswerReceived(channelUserId, (BlogracyBullyAnswerMessage) message);
				else if (message.getID() == BlogracyBullyCoordinatorMessage.ID)
					l.blogracyBullyCoordinatorReceived(channelUserId, (BlogracyBullyCoordinatorMessage) message);
				else if (message.getID() == BlogracyBullyElectionMessage.ID)
					l.blogracyBullyElectionReceived(channelUserId, (BlogracyBullyElectionMessage) message);
			}
		}

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
