/*
 * Created on Feb 24, 2005
 * Created by Alon Rohter
 * Copyright (C) 2004-2005 Aelitis, All Rights Reserved.
 *
 * Furtherly modified by Andrea Vida, University of Parma (Italy).
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */
package net.blogracy.chat.peer.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.gudy.azureus2.plugins.PluginInterface;
import org.gudy.azureus2.plugins.download.*;
import org.gudy.azureus2.plugins.messaging.*;
import org.gudy.azureus2.plugins.network.IncomingMessageQueueListener;
import org.gudy.azureus2.plugins.peers.*;

import net.blogracy.chat.ChatManager;
import net.blogracy.chat.messaging.MessageListener;
import net.blogracy.chat.peer.PeerController;
import net.blogracy.chat.messaging.impl.CMMessage;
import net.blogracy.chat.messaging.impl.CMNoRoute;
import net.blogracy.chat.messaging.impl.CMRoute;
import net.blogracy.chat.messaging.impl.ChatMessage;

import net.blogracy.chat.web.Bridge;

@SuppressWarnings("rawtypes")
public class PeerControllerImpl implements PeerController {
  
  private final int NB_MAX_HOPS = 10;
  
  private final ChatManager chat_plugin;
  
  private Map downloadsToLastMessages;
  private static final int NB_LAST_MESSAGES = 512;
  
  // maps download -> list of routers
  private Map  downloadsToRouters;
  private static final int NB_ROUTERS_PER_TORRENT = 5;
  
  // maps download -> list of peers to route to
  private Map  downloadsToRoutePeer;
  
  // maps download -> list of peers
  private Map downloadsToPeers;
  
  // Messages Listeners
  private List listeners;
  
  //Ignores
  private List nickIgnores;
  private List idIgnores;
  
  //Bridge
  private Bridge[] bridge;
  private static final int MAX_CHANNELS_NUMBER = 70;
  private int bridgeCounter = 0;
  
  
  //private String username;
  
  public PeerControllerImpl(ChatManager plugin, String username) { 
    this.chat_plugin = plugin;
    //this.username = username;
    downloadsToLastMessages = new HashMap();
    downloadsToRouters = new HashMap();
    downloadsToRoutePeer = new HashMap();
    downloadsToPeers = new HashMap();
    listeners = new ArrayList();
    nickIgnores = new LinkedList();
    idIgnores   = new LinkedList();
    bridge = new Bridge[MAX_CHANNELS_NUMBER];
  }
   
  public void initialize() {
    try {
      chat_plugin.getPluginInterface().getMessageManager().registerMessageType( new CMMessage( "",new byte[20],-1, "" ) );
      chat_plugin.getPluginInterface().getMessageManager().registerMessageType( new CMNoRoute() );
      chat_plugin.getPluginInterface().getMessageManager().registerMessageType( new CMRoute() );
    }
    catch( MessageException e ) {   e.printStackTrace();  }
  }
  
  public void addBridge(Download download, String channelName){
	  if (bridgeCounter<MAX_CHANNELS_NUMBER){
		  bridge[bridgeCounter] = new Bridge(chat_plugin,download,channelName);
		  bridgeCounter++;
	  }
	  else System.out.println("Max channels number reached");
    }
  
  public void removeBridge(String channelName){
	  int index = findBridgebyChannel(channelName);
	  bridge[index].finalize();
  }
  
  public void startPeerProcessing() {
    PluginInterface pi = chat_plugin.getPluginInterface();
    
    pi.getDownloadManager().addListener(new DownloadManagerListener() {
    	
    	@SuppressWarnings("unchecked")
    	public void downloadAdded( Download dwnld ) {       
    		downloadsToLastMessages.put(dwnld, new LinkedList());
    		downloadsToRoutePeer.put(dwnld, new LinkedList());
    		downloadsToRouters.put(dwnld,new ArrayList());
    		downloadsToPeers.put(dwnld,new ArrayList());
        
    		notifyListenersOfDownloadAdded(dwnld);
    		notifyListenersOfDownloadInactive(dwnld);
    	}
      
    	public void downloadRemoved( Download download ) {
    		notifyListenersOfDownloadRemoved(download);
        
    		downloadsToLastMessages.remove(download);
    		downloadsToPeers.remove(download);
    		downloadsToRoutePeer.remove(download);        
    		downloadsToRouters.remove(download);
      }
    });

    pi.getMessageManager().locateCompatiblePeers(pi,new CMMessage("",new byte[20],0,""),new MessageManagerListener() {
        public void compatiblePeerFound(Download download,Peer peer,Message message) {        
        	messagingPeerFound(download,peer);
        }
      
        public void peerRemoved(Download download, Peer peer) {
            notifyOfPeerRemoval(download,peer);
        }
    });
  }
  
  @SuppressWarnings("unchecked")
  private void messagingPeerFound(final Download download,final Peer peer) {
    
    //Add the peer to the list of peers
    List peers = (List) downloadsToPeers.get(download);
    if(peers != null) {
        synchronized(peers) {
          if(peers.size() == 0) {notifyListenersOfDownloadActive(download);}
          peers.add(peer);  
        }
    }    
    
    //register for incoming JPC message handling
    peer.getConnection().getIncomingMessageQueue().registerListener(new IncomingMessageQueueListener() {
        public boolean messageReceived( Message message ) {

        	if( message.getID().equals( ChatMessage.ID_CHAT_MESSAGE ) ) {
        		//System.out.println( "Received [" +message.getDescription()+ "] message from peer [" +peer.getClient()+ " @" +peer.getIp()+ ":" +peer.getPort()+ "]" );
        		CMMessage msg = (CMMessage)message;          
        		processMessage(download,peer,msg);          
        		return true;
        	}
        
        	if( message.getID().equals( ChatMessage.ID_CHAT_NO_ROUTE ) ) {
        		//System.out.println( "Received [" +message.getDescription()+ "] message from peer [" +peer.getClient()+ " @" +peer.getIp()+ ":" +peer.getPort()+ "]" );
        		processNoRoute(download,peer);
        		return true;
        	} 
        
        	if( message.getID().equals( ChatMessage.ID_CHAT_ROUTE ) ) {
        		//System.out.println( "Received [" +message.getDescription()+ "] message from peer [" +peer.getClient()+ " @" +peer.getIp()+ ":" +peer.getPort()+ "]" );
        		//CMRoute route = (CMRoute)message;
        		processRoute(download,peer);
        		return true;
        	}

        	return false;
        }

        public void bytesReceived(int byte_count) {/*nothing*/}});
    
    
    //Peers start as "non routing", ie none of the 2 newly connected peers should
    //route any message to the other.
    //If not enough "routers" are used, add this peer as a router
    //and send him a message about it
    //If enough peers are routers, randomly check if we should remove the oldest one
    //and use that new one
    
    List routers = (List) downloadsToRouters.get(download);
    synchronized (routers) {
      if(routers.size() < NB_ROUTERS_PER_TORRENT) {
        routers.add(peer);
        peer.getConnection().getOutgoingMessageQueue().sendMessage(new CMRoute());
      } else {
        int acceptLevel = (int) (100 * NB_ROUTERS_PER_TORRENT / peers.size());
        if(Math.random() * 100 < acceptLevel) {
          Peer oldPeer = (Peer) routers.remove(0);
          oldPeer.getConnection().getOutgoingMessageQueue().sendMessage(new CMNoRoute());
          routers.add(peer);
        }
      }
    }
    
  }
  
  
  
  @SuppressWarnings("unchecked")
  private void notifyOfPeerRemoval( final Download download, final Peer peer ) {
    List routePeers = (List) downloadsToRoutePeer.get(download);
    if(routePeers != null) {
      synchronized(routePeers) {
        routePeers.remove(peer);  
     }
    }
    
    //Remove the peer to the list of peers
    List peers = (List) downloadsToPeers.get(download);
    if(peers != null) {
      synchronized(peers) {
        if(peers.remove(peer) && peers.size() == 0) {
          notifyListenersOfDownloadInactive(download);
        }
     }
    }

    List routers = (List) downloadsToRouters.get(download);
    if(routers.contains(peer)) {
      synchronized(routers) {
        routers.remove(peer);
      }
      //A router is dropping, we need to find a new peer to
      //route us the messages      
      synchronized(peers) {
        List peersCopy = new ArrayList(peers);
        peersCopy.removeAll(routers);
        if(peersCopy.size() > 0) {
          int random = (int) (Math.random() * peersCopy.size());
          Peer peersToAskRoute  =(Peer) peersCopy.get(random);
          peersToAskRoute.getConnection().getOutgoingMessageQueue().sendMessage(new CMRoute());
        }       
      }
    }
  }
  
  @SuppressWarnings("unchecked")
  private void processMessage(Download download,Peer peer,CMMessage message) { 
 
    //  1. Test if the message has already been processed
    int messageID = message.getMessageID();    
    List lastMessages = (List) downloadsToLastMessages.get(download);
    
    int bridgeIndex = findBridgebyDownload(download);
    
    synchronized(lastMessages) {
      if(lastMessages.contains(new Integer(messageID))) {
        //Do nothing, duplicate
      } else {
        //Add it to the queue of messages received
        lastMessages.add(0,new Integer(messageID));
        //If the queue is too long, drop the last item
        if(lastMessages.size() > NB_LAST_MESSAGES) lastMessages.remove(lastMessages.size() - 1);
        
        
        //New message
        byte[] peerID = message.getSenderID();
        String nick   = message.getSenderNick();
        
        //Check out that it's not an ignored peer
        if(nickIgnores.contains(nick)) {
          nickIgnores.remove(nick);
          idIgnores.add(peerID);
        }
        
        boolean ignore = false;
        
        Iterator iterIgnore = idIgnores.iterator();
        while(iterIgnore.hasNext()) {
          byte[] id = (byte[]) iterIgnore.next();
          if(compareIDs(peerID,id)) {
            ignore = true;
          }
        }
        
        if(ignore) return;
        
        //Check if Nick doesn't override with our nick
        if(!compareIDs(download.getDownloadPeerId(),peerID) && nick.equals(chat_plugin.getNick())) {
          sendMessage(download,download.getDownloadPeerId(),"System","/me : Multiple peers are using the nick " + nick);
          if (bridgeIndex >= 0) bridge[bridgeIndex].sysMsg("Multiple peers are using the nick " + nick);
          else System.out.println("error finding bridge by download");
        }
        
        String text = message.getText();
        notifyListenersOfMessageReceived(download,peerID,nick,text);
        if (bridgeIndex >= 0) bridge[bridgeIndex].inMsg(text,nick);
        else System.out.println("error finding bridge by download");
        
        //Dispatch the message
        List routePeers = (List) downloadsToRoutePeer.get(download);
        int nbHops    = message.getNbHops() + 1;     
        if(nbHops < NB_MAX_HOPS) {
          synchronized(routePeers) {        
            Iterator iter = routePeers.iterator();
            while(iter.hasNext()) {
              Peer peerToRoute = (Peer) iter.next();
              //Don't send it to the sending peer
              byte[] peerToRouteID = peerToRoute.getId();
              if(peerToRoute != peer && ! compareIDs(peerID,peerToRouteID)) {
                CMMessage msg = new CMMessage(messageID,nick,peerID,nbHops,text);
                peerToRoute.getConnection().getOutgoingMessageQueue().sendMessage(msg);
              }
            }
          }
        }
      }
    }
  }

  private synchronized void processNoRoute(Download download,Peer peer) {
    List routePeers = (List) downloadsToRoutePeer.get(download);
    synchronized (routePeers) {
      routePeers.remove(peer);
    }
  }
  
  @SuppressWarnings("unchecked")
  private synchronized void processRoute(Download download,Peer peer) {
    List routePeers = (List) downloadsToRoutePeer.get(download);
    synchronized (routePeers) {
      if(!routePeers.contains(peer)) {
        routePeers.add(peer);
      }
    }
  }
  
  @SuppressWarnings("unchecked")
  public void addMessageListener(MessageListener listener) {
    synchronized (listeners) {
      listeners.add(listener);
    }    
  }
  
  public void removeMessageListener(MessageListener listener) {
    synchronized (listeners) {
      listeners.remove(listener);  
    }    
  }
  
  private void notifyListenersOfMessageReceived(Download download,byte[] peerID, String nick, String text) {
    synchronized (listeners) {
      for(Iterator iter = listeners.iterator() ; iter.hasNext() ; ) {
        MessageListener listener = (MessageListener) iter.next();
        listener.messageReceived(download,peerID,nick,text);
      }
    }
  }
  
  private void notifyListenersOfDownloadAdded(Download download) {
    synchronized (listeners) {
      for(Iterator iter = listeners.iterator() ; iter.hasNext() ; ) {
        MessageListener listener = (MessageListener) iter.next();
        listener.downloadAdded(download);
      }
    }
  }
  
  private void notifyListenersOfDownloadRemoved(Download download) {
    synchronized (listeners) {
      for(Iterator iter = listeners.iterator() ; iter.hasNext() ; ) {
        MessageListener listener = (MessageListener) iter.next();
        listener.downloadRemoved(download);
      }
    }
  }
  
  private void notifyListenersOfDownloadActive(Download download) {
    synchronized (listeners) {
      for(Iterator iter = listeners.iterator() ; iter.hasNext() ; ) {
        MessageListener listener = (MessageListener) iter.next();
        listener.downloadActive(download);
      }
    }
  }
  
  private void notifyListenersOfDownloadInactive(Download download) {
    synchronized (listeners) {
      for(Iterator iter = listeners.iterator() ; iter.hasNext() ; ) {
        MessageListener listener = (MessageListener) iter.next();
        listener.downloadInactive(download);
      }
    }
  }
  
  public void sendMessage(String nick,String message) {
	  if(downloadsToPeers == null) return;
    
    synchronized (downloadsToPeers) {
      Iterator iter = downloadsToPeers.keySet().iterator();
      while(iter.hasNext()) {
        Download download = (Download) iter.next();
        sendMessage(download,download.getDownloadPeerId(),nick,message);
      }
    }    
  }
  
  private String oldNick;
  
  public void sendMessage(Download download,byte[] peerID, String nick, String message) {
	  sendMessage(download,peerID,nick,message,true);
  }
  
  public void sendMessage(Download download,byte[] peerID, String nick, String message,boolean checkForNick) {
	  int bridgeIndex = findBridgebyDownload(download);
	  
      if(checkForNick && ! nick.equals("System") && oldNick != null && ! oldNick.equals(nick)) {      
    	  sendMessage(download,peerID,"System","/me : " + oldNick + " is now known as " + nick);
    	  if(bridgeIndex >= 0) bridge[bridgeIndex].sysMsg(oldNick + " is now known as " + nick);
    	  else System.out.println("error finding bridge by download");
      }
      if(! nick.equals("System")) oldNick = nick;
    
      notifyListenersOfMessageReceived(download,download.getDownloadPeerId(),nick,message);
      List routePeers = (List) downloadsToPeers.get(download);
      if(routePeers != null) {
    	  synchronized (routePeers) {
    		  CMMessage msg = new CMMessage(nick,peerID,0,message);
    		  for(Iterator iter = routePeers.iterator(); iter.hasNext() ;) {
    			  Peer peerToSendMsg = (Peer) iter.next();
    			  CMMessage msgToSend = new CMMessage(msg.getMessageID(),nick,peerID,0,message);
    			  peerToSendMsg.getConnection().getOutgoingMessageQueue().sendMessage(msgToSend);
    		  }
    	  }
      }
  }
  
  
  private boolean compareIDs(byte[] id1, byte[] id2) {
	  if(id1 == null) return id2 == null;
	  if(id2 == null) return false;
	  if(id1.length != id2.length) return false;
	  for(int i = id1.length - 1 ; i >= 0 ; i--) {
		  if(id1[i] != id2[i]) return false;
	  }
	  return true;
  }
  
  public boolean isDownloadActive(Download download) {
	  List peers = (List) downloadsToPeers.get(download);
	  if(peers == null) return false;
	  return peers.size() > 0; 
  }
  
  @SuppressWarnings("unchecked")
  public void ignore(String nick) {
	  nickIgnores.add(nick);
  }
  
  private int findBridgebyDownload(Download download){
	  int index = -1;
	  for (int i=0; i<MAX_CHANNELS_NUMBER;i++){
		  if(bridge[i]!= null){
			  Download dwn = bridge[i].getDownload();
			  if (dwn.equals(download)) index = i;
		  }
	  }
	  return index;	  
  }
  
  private int findBridgebyChannel(String name){
	  int index = -1;
	  for (int i=0; i<MAX_CHANNELS_NUMBER;i++){
		  if(bridge[i]!= null){
			  String ch = bridge[i].getChannelName();
			  if (ch.equals(name)) index = i;
		  }
	  }
	  return index;	  
  }
}

