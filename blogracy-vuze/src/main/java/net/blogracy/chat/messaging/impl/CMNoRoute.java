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
import org.gudy.azureus2.plugins.messaging.Message;
import org.gudy.azureus2.plugins.messaging.MessageException;

public class CMNoRoute implements ChatMessage {
  
  private final String     description  = getID();
  private final ByteBuffer buffer       = ByteBuffer.allocate(0);
  
  public String getID() {return ChatMessage.ID_CHAT_NO_ROUTE;}
  public byte getVersion() {return ChatMessage.CHAT_DEFAULT_VERSION;}
  public int getType() {return Message.TYPE_PROTOCOL_PAYLOAD;} 
  public String getDescription() {return description;} 
  public ByteBuffer[] getPayload() {return new ByteBuffer[] { buffer };}   
  public void destroy() {/*nothing*/}
  
  public Message create(ByteBuffer data) throws MessageException {    
    return new CMNoRoute();
  }
}
