package net.blogracy.messaging.peer.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.blogracy.messaging.impl.BMNoRoute;
import net.blogracy.messaging.impl.BMRoute;
import net.blogracy.messaging.impl.BlogracyDataMessage;
import net.blogracy.messaging.impl.BlogracyDataMessageBase;
import net.blogracy.messaging.peer.BlogracyDataMessageListener;
import net.blogracy.messaging.peer.PeerController;

import org.gudy.azureus2.plugins.PluginInterface;
import org.gudy.azureus2.plugins.download.Download;
import org.gudy.azureus2.plugins.download.DownloadManagerListener;
import org.gudy.azureus2.plugins.messaging.Message;
import org.gudy.azureus2.plugins.messaging.MessageException;
import org.gudy.azureus2.plugins.messaging.MessageManagerListener;
import org.gudy.azureus2.plugins.network.IncomingMessageQueueListener;
import org.gudy.azureus2.plugins.peers.Peer;

/***
 * Based on:
 * 
 * Created on Feb 24, 2005 Created by Alon Rohter Copyright (C) 2004-2005
 * Aelitis, All Rights Reserved.
 * 
 * Furtherly modified by Andrea Vida, University of Parma (Italy). Furtherly
 * modified by Vittorio Sozzi, University of Parma (Italy).
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * 
 */
public class PeerControllerImpl implements PeerController {

	private final int NB_MAX_HOPS = 10;

	public final PluginInterface plugin;

	/**
	 * Repository of received messages, maps download to a list of message IDs
	 */
	private Map<Download, List<Long>> downloadsToLastMessages;

	/**
	 * Max number of archived message for each download
	 */
	private static final int MAX_LAST_MESSAGES = 512;

	/**
	 * Repository of Routers Peers, maps download to a list of Routing peers
	 */
	private Map<Download, List<Peer>> downloadsToRouters;

	/**
	 * Max number of routing peers for each download
	 */
	private static final int MAX_ROUTERS_PER_TORRENT = 5;

	/**
	 * Repository of connected peers to route to, maps download to a list of
	 * connected peers which messages should be routed to. It is unlimited by
	 * default.
	 */
	private Map<Download, List<Peer>> downloadsToRoutePeer;

	/**
	 * Global repository of connected peers by download
	 */
	private Map<Download, List<Peer>> downloadsToPeers;

	/***
	 * External message listeners
	 */
	private List<BlogracyDataMessageListener> listeners;

	private List<BlogracyDataMessage> listOfRegisteredMessageTypes = new ArrayList<BlogracyDataMessage>();

	public PeerControllerImpl(PluginInterface plugin) {
		this.plugin = plugin;
		downloadsToLastMessages = new HashMap<Download, List<Long>>();
		downloadsToRouters = new HashMap<Download, List<Peer>>();
		downloadsToRoutePeer = new HashMap<Download, List<Peer>>();
		downloadsToPeers = new HashMap<Download, List<Peer>>();
		listeners = new ArrayList<BlogracyDataMessageListener>();
	}

	@Override
	public void initialize(List<BlogracyDataMessage> listOfMessageTypes) {
		try {

			// System messages
			plugin.getMessageManager().registerMessageType(new BMNoRoute());
			plugin.getMessageManager().registerMessageType(new BMRoute());

			plugin.getMessageManager().registerMessageType(
					new BlogracyDataMessageBase("", new byte[20], -1, ""));

			if (listOfMessageTypes != null) {
				for (BlogracyDataMessage m : listOfMessageTypes) {
					plugin.getMessageManager().registerMessageType(m);
					listOfRegisteredMessageTypes.add(m);
				}

			}

		} catch (MessageException e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.blogracy.messaging.peer.PeerController#startPeerProcessing()
	 */
	@Override
	public void startPeerProcessing() {

		plugin.getDownloadManager().addListener(new DownloadManagerListener() {

			public void downloadAdded(Download dwnld) {
				downloadsToLastMessages.put(dwnld, new LinkedList<Long>());
				downloadsToRoutePeer.put(dwnld, new LinkedList<Peer>());
				downloadsToRouters.put(dwnld, new ArrayList<Peer>());
				downloadsToPeers.put(dwnld, new ArrayList<Peer>());

				notifyListenersOfDownloadAdded(dwnld);
				notifyListenersOfDownloadInactive(dwnld);
			}

			public void downloadRemoved(Download download) {
				notifyListenersOfDownloadRemoved(download);

				downloadsToLastMessages.remove(download);
				downloadsToPeers.remove(download);
				downloadsToRoutePeer.remove(download);
				downloadsToRouters.remove(download);
			}
		});

		plugin.getMessageManager().locateCompatiblePeers(plugin,
				new BlogracyDataMessageBase("", new byte[20], 0, ""),
				new MessageManagerListener() {
					public void compatiblePeerFound(Download download,
							Peer peer, Message message) {
						PeerControllerImpl.this.compatiblePeerFound(download,
								peer);
					}

					public void peerRemoved(Download download, Peer peer) {
						compatiblePeerRemoved(download, peer);
					}
				});
	}

	private void compatiblePeerFound(final Download download, final Peer peer) {

		// Add the peer to the list of peers
		List<Peer> peers = downloadsToPeers.get(download);
		if (peers != null) {
			synchronized (peers) {
				if (peers.size() == 0) {
					notifyListenersOfDownloadActive(download);
				}
				peers.add(peer);
			}
		}

		// register for incoming JPC message handling
		peer.getConnection().getIncomingMessageQueue()
				.registerListener(new IncomingMessageQueueListener() {
					public boolean messageReceived(Message message) {

						// Handling system messages
						if (message.getID().equals(BMNoRoute.ID_BM_NO_ROUTE)) {
							processNoRoute(download, peer);
							return true;
						}

						if (message.getID().equals(BMRoute.ID_BM_ROUTE)) {
							processRoute(download, peer);
							return true;
						}

						// Handling other Data messages
						for (BlogracyDataMessage m : listOfRegisteredMessageTypes) {
							if (message.getID().equals(m.getID())) {

								BlogracyDataMessage msg = m.getClass().cast(
										message);
								// BlogracyDataMessage msg =
								// (BlogracyDataMessage)message;

								// 1. Test if the message has already been
								// processed
								if (!checkIfDuplicate(download,
										msg.getMessageID()))
									processMessage(download, peer, msg);

								return true;
							}
						}

						return false;
					}

					public void bytesReceived(int byte_count) {/* nothing */
					}
				});

		// Peers start as "non routing", ie none of the 2 newly connected peers
		// should
		// route any message to the other.
		// If not enough "routers" are used, add this peer as a router
		// and send him a message about it
		// If enough peers are routers, randomly check if we should remove the
		// oldest one
		// and use that new one

		List<Peer> routers = downloadsToRouters.get(download);
		synchronized (routers) {
			if (routers.size() < MAX_ROUTERS_PER_TORRENT) {
				routers.add(peer);
				peer.getConnection().getOutgoingMessageQueue()
						.sendMessage(new BMRoute());
			} else {
				int acceptLevel = (int) (100 * MAX_ROUTERS_PER_TORRENT / peers
						.size());
				if (Math.random() * 100 < acceptLevel) {
					Peer oldPeer = (Peer) routers.remove(0);
					oldPeer.getConnection().getOutgoingMessageQueue()
							.sendMessage(new BMNoRoute());
					routers.add(peer);
				}
			}
		}

	}

	private void compatiblePeerRemoved(final Download download, final Peer peer) {
		List<Peer> routePeers = downloadsToRoutePeer.get(download);
		if (routePeers != null) {
			synchronized (routePeers) {
				routePeers.remove(peer);
			}
		}

		// Remove the peer to the list of peers
		List<Peer> peers = downloadsToPeers.get(download);
		if (peers != null) {
			synchronized (peers) {
				if (peers.remove(peer) && peers.size() == 0) {
					notifyListenersOfDownloadInactive(download);
				}
			}
		}

		List<Peer> routers = downloadsToRouters.get(download);
		if (routers.contains(peer)) {
			synchronized (routers) {
				routers.remove(peer);
			}
			// A router is dropping, we need to find a new peer to
			// route us the messages
			synchronized (peers) {
				List<Peer> peersCopy = new ArrayList<Peer>(peers);
				peersCopy.removeAll(routers);
				if (peersCopy.size() > 0) {
					int random = (int) (Math.random() * peersCopy.size());
					Peer peersToAskRoute = peersCopy.get(random);
					peersToAskRoute.getConnection().getOutgoingMessageQueue()
							.sendMessage(new BMRoute());
				}
			}
		}
	}

	/*****************************************
	 * 
	 * Message Handlers
	 * 
	 ****************************************/

	private boolean checkIfDuplicate(Download download, long messageID) {
		List<Long> lastMessages = downloadsToLastMessages.get(download);

		boolean isDuplicate = false;
		synchronized (lastMessages) {
			if (lastMessages.contains(messageID)) {
				// Do nothing, duplicate
				isDuplicate = true;
			} else {
				// Add it to the queue of messages received
				lastMessages.add(0, messageID);
				// If the queue is too long, drop the last item
				if (lastMessages.size() > MAX_LAST_MESSAGES)
					lastMessages.remove(lastMessages.size() - 1);
				isDuplicate = false;
			}
		}
		return isDuplicate;
	}

	private void processMessage(Download download, Peer peer,
			BlogracyDataMessage message) {
		notifyListenersOfMessageReceived(download, message.getSenderPeerID(),
				message.getSenderUserId(), message.getContent());
		dispatchMessageToRouters(download, peer, message);
	}

	private void dispatchMessageToRouters(Download download, Peer peer,
			BlogracyDataMessage message) {
		// New message
		byte[] peerID = message.getSenderPeerID();

		// Dispatch the message to the routers
		List<Peer> routePeers = downloadsToRoutePeer.get(download);
		int nbHops = message.getNbHops() + 1;
		if (nbHops < NB_MAX_HOPS) {
			synchronized (routePeers) {
				Iterator<Peer> iter = routePeers.iterator();
				while (iter.hasNext()) {
					Peer peerToRoute = iter.next();
					// Don't send it to the sending peer
					byte[] peerToRouteID = peerToRoute.getId();
					if (peerToRoute != peer
							&& !comparePeerIDs(peerID, peerToRouteID)) {
						BlogracyDataMessage msg = message.copy();
						msg.setNbHops(nbHops);
						peerToRoute.getConnection().getOutgoingMessageQueue()
								.sendMessage(msg);
					}
				}
			}
		}
	}

	private synchronized void processNoRoute(Download download, Peer peer) {
		List<Peer> routePeers = downloadsToRoutePeer.get(download);
		synchronized (routePeers) {
			routePeers.remove(peer);
		}
	}

	private synchronized void processRoute(Download download, Peer peer) {
		List<Peer> routePeers = downloadsToRoutePeer.get(download);
		synchronized (routePeers) {
			if (!routePeers.contains(peer)) {
				routePeers.add(peer);
			}
		}
	}

	private boolean comparePeerIDs(byte[] id1, byte[] id2) {
		if (id1 == null)
			return id2 == null;
		if (id2 == null)
			return false;
		if (id1.length != id2.length)
			return false;
		for (int i = id1.length - 1; i >= 0; i--) {
			if (id1[i] != id2[i])
				return false;
		}
		return true;
	}

	public void sendMessage(String userId, BlogracyDataMessage message) {
		if (downloadsToPeers == null)
			return;

		synchronized (downloadsToPeers) {
			Iterator<Download> iter = downloadsToPeers.keySet().iterator();
			while (iter.hasNext()) {
				Download download = iter.next();
				sendMessage(download, download.getDownloadPeerId(), userId,
						message);
			}
		}
	}

	public void sendMessage(Download download, byte[] peerID,
			String senderUserId, BlogracyDataMessage message) {
		sendMessage(download, peerID, senderUserId, message, true);
	}

	public void sendMessage(Download download, byte[] peerID,
			String senderUserId, BlogracyDataMessage message,
			boolean checkForNick) {
		// int bridgeIndex = findBridgebyDownload(download);

		/*
		 * if(checkForNick && ! nick.equals("System") && oldNick != null && !
		 * oldNick.equals(nick)) { sendMessage(download,peerID,"System","/me : "
		 * + oldNick + " is now known as " + nick); if(bridgeIndex >= 0)
		 * bridge[bridgeIndex].sysMsg(oldNick + " is now known as " + nick);
		 * else System.out.println("error finding bridge by download"); } if(!
		 * nick.equals("System")) oldNick = nick;
		 */

		notifyListenersOfMessageReceived(download,
				download.getDownloadPeerId(), senderUserId,
				message.getContent());
		List<Peer> routePeers = downloadsToPeers.get(download);
		if (routePeers != null) {
			synchronized (routePeers) {
				for (Iterator<Peer> iter = routePeers.iterator(); iter
						.hasNext();) {
					Peer peerToSendMsg = iter.next();
					peerToSendMsg.getConnection().getOutgoingMessageQueue()
							.sendMessage(message);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.blogracy.messaging.peer.PeerController#addMessageListener(net.blogracy
	 * .messaging.BlogracyDataMessageListener)
	 */

	@Override
	public void addMessageListener(BlogracyDataMessageListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.blogracy.messaging.peer.PeerController#removeMessageListener(net.
	 * blogracy.messaging.BlogracyDataMessageListener)
	 */
	@Override
	public void removeMessageListener(BlogracyDataMessageListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	private void notifyListenersOfMessageReceived(Download download,
			byte[] peerID, String userId, String content) {
		synchronized (listeners) {
			for (Iterator<BlogracyDataMessageListener> iter = listeners
					.iterator(); iter.hasNext();) {
				BlogracyDataMessageListener listener = iter.next();
				listener.blogracyDataMessageReceived(download, peerID, userId,
						content);
			}
		}
	}

	private void notifyListenersOfDownloadAdded(Download download) {
		synchronized (listeners) {
			for (Iterator<BlogracyDataMessageListener> iter = listeners
					.iterator(); iter.hasNext();) {
				BlogracyDataMessageListener listener = iter.next();
				listener.downloadAdded(download);
			}
		}
	}

	private void notifyListenersOfDownloadRemoved(Download download) {
		synchronized (listeners) {
			for (Iterator<BlogracyDataMessageListener> iter = listeners
					.iterator(); iter.hasNext();) {
				BlogracyDataMessageListener listener = iter.next();
				listener.downloadRemoved(download);
			}
		}
	}

	private void notifyListenersOfDownloadActive(Download download) {
		synchronized (listeners) {
			for (Iterator<BlogracyDataMessageListener> iter = listeners
					.iterator(); iter.hasNext();) {
				BlogracyDataMessageListener listener = iter.next();
				listener.downloadActive(download);
			}
		}
	}

	private void notifyListenersOfDownloadInactive(Download download) {
		synchronized (listeners) {
			for (Iterator<BlogracyDataMessageListener> iter = listeners
					.iterator(); iter.hasNext();) {
				BlogracyDataMessageListener listener = iter.next();
				listener.downloadInactive(download);
			}
		}
	}
}