/*
 * Created on 28 fevr. 2005
 * Created by Olivier Chalouhi
 * 
 * Copyright (C) 2004 Aelitis SARL, All rights Reserved
 * 
 * Furtherly modified by Andrea Vida, University of Parma (Italy).
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details ( see the LICENSE file ).
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * AELITIS, SARL au capital de 30,000 euros,
 * 8 Allee Lenotre, La Grille Royale, 78600 Le Mesnil le Roi, France.
 */
package net.blogracy.chat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.gudy.azureus2.plugins.Plugin;
import org.gudy.azureus2.plugins.PluginInterface;
import org.gudy.azureus2.plugins.config.ConfigParameter;
import org.gudy.azureus2.plugins.config.ConfigParameterListener;
import org.gudy.azureus2.plugins.download.Download;
import org.gudy.azureus2.plugins.torrent.Torrent;
import org.gudy.azureus2.plugins.utils.Formatters;
import org.gudy.azureus2.plugins.utils.LocaleUtilities;

import net.blogracy.chat.peer.impl.PeerControllerImpl;
import net.blogracy.chat.peer.PeerController;
import net.blogracy.chat.messaging.MessageListener;
import net.blogracy.logging.Logger;

public class ChatManager implements Plugin, MessageListener, ConfigParameterListener{
	
	private PluginInterface plugin_interface;  
	private PeerController  controller;
	
	private ArrayList listeners; 
	private ArrayList listenersDownload;
	  
	private String nick;
	private boolean active;
	  	  
	private String resTorrent = "channel.torrent";
	public Torrent genericTorrent;
	private static Formatters formatters = null;
	  
	public void initialize(PluginInterface pi) {
	    plugin_interface  = pi;
	    formatters = plugin_interface.getUtilities().getFormatters();
	    genericTorrent = loadTorrent(resTorrent);
	    	    
	    nick = plugin_interface.getPluginconfig().getPluginStringParameter("nick","Guest" + (int) (Math.random() * 100000));
	    active = plugin_interface.getPluginconfig().getPluginBooleanParameter("enable",true);

	    if(active) {    
	      listeners = new ArrayList();
	      listenersDownload = new ArrayList();
	      controller = new PeerControllerImpl(this,nick);    
	      controller.addMessageListener(this);
	      controller.initialize();
	      controller.startPeerProcessing();
	    }
	  }
	
	public PluginInterface getPluginInterface() {
		  return plugin_interface;
	  }
	  
	  public LocaleUtilities getLocaleUtils() {
		  return plugin_interface.getUtilities().getLocaleUtilities();
	  }
	  
	  @SuppressWarnings("unchecked")
	  public void addMessageListener(MessageListener listener, Download download) {
	    synchronized (listeners) {
	      listeners.add(listener);
	      listenersDownload.add(download);
	    }

	    listener.downloadAdded(download);
	    if(controller.isDownloadActive(download)) {
	      listener.downloadActive(download);
	    } else {
	      listener.downloadInactive(download);
	    }
	  }
	  
	  public void removeMessageListener(MessageListener listener) {
	    synchronized (listeners) {
	    	int idx = listeners.indexOf(listener);
	    	if (idx >= 0) {
	        listenersDownload.remove(idx);
	        listeners.remove(listener);
	    	}
	    }	    
	  }
	  
	  public void downloadAdded(Download download) {
	    synchronized (listeners) {
	    	for (int i = 0; i < listenersDownload.size(); i++) {
	    		Download lDownload = (Download) listenersDownload.get(i);
	    		if (download.equals(lDownload)) {
	      		MessageListener listener = (MessageListener) listeners.get(i);
	      		listener.downloadAdded(download);
	    		}
	    	}
	    }    
	  }
	  
	  public void downloadRemoved(Download download) {
	    synchronized (listeners) {
	    	for (int i = 0; i < listenersDownload.size(); i++) {
	    		Download lDownload = (Download) listenersDownload.get(i);
	    		if (download.equals(lDownload)) {
	      		MessageListener listener = (MessageListener) listeners.get(i);
	      		listener.downloadRemoved(download);
	    		}
	    	}
	    }    
	  }
	  
	  public void downloadActive(Download download) {   
	    synchronized (listeners) {
	    	for (int i = 0; i < listenersDownload.size(); i++) {
	    		Download lDownload = (Download) listenersDownload.get(i);
	    		if (download.equals(lDownload)) {
	      		MessageListener listener = (MessageListener) listeners.get(i);
	      		listener.downloadActive(download);
	    		}
	    	}
	    }    
	  }
	  
	  public void downloadInactive(Download download) {   
	    synchronized (listeners) {
	    	for (int i = 0; i < listenersDownload.size(); i++) {
	    		Download lDownload = (Download) listenersDownload.get(i);
	    		if (download.equals(lDownload)) {
	      		MessageListener listener = (MessageListener) listeners.get(i);
	      		listener.downloadInactive(download);
	    		}
	    	}
	    }    
	  }
	  
	  public void messageReceived(Download download,byte[] sender,String nick,String text) {
	    synchronized (listeners) {
	    	for (int i = 0; i < listenersDownload.size(); i++) {
	    		Download lDownload = (Download) listenersDownload.get(i);
	    		if (download.equals(lDownload)) {
	    			MessageListener listener = (MessageListener) listeners.get(i);
	    			listener.messageReceived(download, sender, nick, text);
	    		}
	    	}
	    }    
	  }
	  
	  public void sendMessage(Download download,String text){
	  		if (download == null) return;
	  		byte[] peerID = download.getDownloadPeerId();
	  		if(peerID != null) {
	  			controller.sendMessage(download,peerID,nick,text);      
	  		} else {
	  			System.out.println("System: Torrent isn't running, message can't be delivered");
	  		}
	  }
	  
	  public void configParameterChanged(ConfigParameter param) {
		  nick = plugin_interface.getPluginconfig().getPluginStringParameter("nick","Guest" + (int) (Math.random() * 100000));
		  if(nick.startsWith("System") || nick.startsWith("system")) {
			  nick = "Guest" + (int) (Math.random() * 100000);
		  }
	  }
	  
	  private Torrent loadTorrent(String res) {
		  ClassLoader cl = this.getClass().getClassLoader();
		  InputStream is = cl.getResourceAsStream(res);
		  if (is != null) {
			  try {
				  return plugin_interface.getTorrentManager().createFromBEncodedInputStream(is);        
			  } catch(Exception e) {
				  Logger.info("System: The channel torrent is impossible to create!");
				  return null;
			  }	        
		  }
		  Logger.info("System: The channel torrent created is null");
		  return null;
	  }

	  public String getNick() {
		  return nick;
	  }
	  
	  public void setNick(String nick) {
		  this.nick = nick;
		  plugin_interface.getPluginconfig().setPluginParameter("nick",nick);
	  }
	  
	  /*public void addIgnore(String nick) {
		  if(controller != null) controller.ignore(nick);
	  }*/
	  
	  public Torrent getChannelTorrent(String channelName) {
	    try {
	    	Map genericMap = genericTorrent.writeToMap();
	    	Map info = (Map) genericMap.get("info");
	    	info.put("name",channelName.getBytes());
	    	info.put("name.utf8",channelName.getBytes("UTF-8"));
	    	genericMap.put("info",info);	    
	    	byte[] channelTorrent = plugin_interface.getUtilities().getFormatters().bEncode(genericMap);	    
	    	Torrent result = plugin_interface.getTorrentManager().createFromBEncodedData(channelTorrent);
	    	result.setAnnounceURL(new URL("dht://chat.dht/announce"));
	    	return result;
	    } catch(Exception e) {
	    	e.printStackTrace();      
	    }
	    return null;
	  }
	  
	  public void startNewChannel(String channelName){
		  Download newChannelDL = null;
	      newChannelDL = addChannel(channelName);
	  }
	  
	  public Download addChannel(String channelName) {
		  Torrent torrent = getChannelTorrent(channelName);
		  String savePath = plugin_interface.getPluginDirectoryName();
		  try {
			  File saveDir = new File(savePath,"channels" +  File.separator );
			  saveDir.mkdir();
			  Download dl = plugin_interface.getDownloadManager().addDownload(torrent,null,saveDir);
			  dl.setForceStart(true);
			  
			  File dest = new File(savePath,"channels" +  File.separator + channelName);
			  File src = new File(savePath,"channels" +  File.separator + "channel");
			  copyFile(src,dest);
			  
			  controller.addBridge(dl,channelName);
			  return dl;
		  } catch(Exception e) {
			  e.printStackTrace();
		  }
		  return null;
	  }
	  
	  public void closeBridge(String channelName){
		  controller.removeBridge(channelName);
	  }
	  
	  public static byte[] bEncode(Map map) {
	  	if (formatters == null) {return new byte[0];}
	  	try {
				return formatters.bEncode(map);
			} catch (IOException e) {
				e.printStackTrace();
	  		return new byte[0];
			}
	  }

	  public static Map bDecode(byte[] bytes) {
	  	if (formatters == null) {return new HashMap();}
	  	try {
				return formatters.bDecode(bytes);
			} catch (IOException e) {
				e.printStackTrace();
	  		return new HashMap();
			}
	  }
	  
	  public static void copyFile(File srcFile, File dstFile) {
			if (!dstFile.exists()) {
		        try {
					dstFile.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
		    }
		    FileChannel source = null;
		    FileChannel destination = null;
		    try {
		        source = new FileInputStream(srcFile).getChannel();
		        destination = new FileOutputStream(dstFile).getChannel();
		        destination.transferFrom(source, 0, source.size());
			} catch (IOException e) {
				e.printStackTrace();
			}
		    finally {
		        if (source != null) {
		            try {
						source.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
		        }
		        if (destination != null) {
		            try {
						destination.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
		        }
		    }
	    }
}

