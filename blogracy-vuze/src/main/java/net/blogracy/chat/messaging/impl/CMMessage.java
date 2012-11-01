/*
 * Created on 28 fevr. 2005
 * Created by Olivier Chalouhi
 * 
 * Copyright (C) 2004 Aelitis SARL, All rights Reserved
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

package net.blogracy.chat.messaging.impl;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import net.blogracy.chat.ChatManager;

import org.gudy.azureus2.plugins.messaging.Message;
import org.gudy.azureus2.plugins.messaging.MessageException;


public class CMMessage implements ChatMessage{
  
  private String description;
  private ByteBuffer buffer;
  
  //A unique message ID, computed as :
  //hash(time,sender,text);
  private int  messageID;
  
  //The sender's nick name
  private final String  senderNick;
  //The sender's peer ID
  private final byte[]  senderID;
  
  //The number of hops the message has been through
  private final int hops;
  
  //The message itself
  private final String text;
  
  public CMMessage(int id, String senderNick, byte[] senderID, int hops, String text) {
    this(senderNick,senderID,hops,text,false);
    this.messageID = id;
    generateBuffer();
  }
  
  public CMMessage(String senderNick, byte[] senderID, int hops, String text) {
    this(senderNick,senderID,hops,text,true);
  }
  
  private CMMessage(String senderNick, byte[] senderID, int hops, String text, boolean generateBuffer) {
    
	this.senderNick = senderNick;
    this.senderID = senderID;
    this.hops = hops;
    this.text = text;
    
    String hash = senderID + "," + System.currentTimeMillis() + "," + text;
    
    this.messageID = hash.hashCode();
       
    if(generateBuffer) generateBuffer();
  }
    
    @SuppressWarnings("unchecked")
	private void generateBuffer() {
      
    this.description = getID()+ " from " + senderNick + " : " + text +  " (id: " + messageID + ", hops:" + hops + ")";
    @SuppressWarnings("rawtypes")
	Map mMessage = new HashMap();
    mMessage.put("id",new Long(messageID));
    mMessage.put("s",senderID);
    mMessage.put("n",senderNick);
    mMessage.put("h",new Long(hops));
    mMessage.put("t",text);
    
    byte[] bMessage = new byte[0];
    
    try {
      bMessage = ChatManager.bEncode(mMessage);      
    } catch(Exception exception) {
      exception.printStackTrace();
    }    
    buffer = ByteBuffer.allocate(bMessage.length);
    buffer.put(bMessage);
    buffer.flip();
  }
  
  
  public int getMessageID() {return messageID;}
  public byte[] getSenderID() {return senderID;}
  public String getSenderNick() {return senderNick;}
  public int getNbHops() {return hops;}
  public String getText() {return text;}
  
  public String getID() {return ChatMessage.ID_CHAT_MESSAGE;}
  public byte getVersion() {return ChatMessage.CHAT_DEFAULT_VERSION;}
  public int getType() {return Message.TYPE_PROTOCOL_PAYLOAD;}  
  public String getDescription() {return description;} 
  public ByteBuffer[] getPayload() {return new ByteBuffer[] { buffer };}  
  public void destroy() {/*nothing*/}
  
  
  public Message create(ByteBuffer data) throws MessageException {
    if(data == null) {
      throw new MessageException( "[" +getID() + ":" +getVersion()+ "] decode error: data == null" );
    }
    
    if(data.remaining() < 13) {/*nothing*/}
    int size = data.remaining();

    byte[] bMessage = new byte[size];
    data.get(bMessage);
    
    try {
      //mMessage.put("id",new Long(messageID));
      //mMessage.put("s",senderID);
      //mMessage.put("n",senderNick);
      //mMessage.put("h",new Long(hops));
      //mMessage.put("t",text);
    	
      @SuppressWarnings("rawtypes")
	  Map mMessage = ChatManager.bDecode(bMessage);
      int messageID = ((Long)mMessage.get("id")).intValue();
      byte[] senderID = (byte[])mMessage.get("s");
      String senderNick = new String((byte[])mMessage.get("n"));
      int hops = ((Long)mMessage.get("h")).intValue();
      String text = new String((byte[])mMessage.get("t"));
      
      return new CMMessage(messageID,senderNick,senderID,hops,text);  
    } 
    
    catch(Exception e) {
      throw new MessageException( "[" +getID() + ":" +getVersion()+ "] decode error: " + e );
    }

        
  }
  
}

