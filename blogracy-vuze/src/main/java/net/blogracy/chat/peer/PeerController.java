/*
 * Created on Feb 24, 2005
 * Created by Alon Rohter
 * Copyright (C) 2004-2005 Aelitis, All Rights Reserved.
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
 * AELITIS, SARL au capital de 30,000 euros
 * 8 Allee Lenotre, La Grille Royale, 78600 Le Mesnil le Roi, France.
 *
 */
package net.blogracy.chat.peer;

import org.gudy.azureus2.plugins.download.Download;
import net.blogracy.chat.messaging.MessageListener;

public interface PeerController {
  
  //Adds a message listener. 
  public void addMessageListener(MessageListener listener);
    
  //Initialize.
  public void initialize();
  
  //Start Chat cache exchange handling.
  public void startPeerProcessing();
  
  //create the Bridge
  public void addBridge(Download download, String channelName);
  
  //remove the Bridge
  public void removeBridge(String channelName);
  
  //Sends a message to all channels
  public void sendMessage(String nick, String message);
    
  //Sends a message
  public void sendMessage(Download download,byte[] peerID, String nick, String message);
  
  //Tells if a download is active or not
  public boolean isDownloadActive(Download download);
  
  //Ignores a peer by Nick, will also find out the peerID and ignore that ID.
  public void ignore(String nick);
}

