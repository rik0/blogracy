/*
 * Created on 28 feb. 2005
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
package com.aelitis.azureus.plugins.chat.peer.impl.messaging;

import org.gudy.azureus2.plugins.messaging.Message;

public interface ChatMessage extends Message {
  
  public static final String ID_CHAT_MESSAGE      = "CHAT_MESSAGE";
  public static final String ID_CHAT_NO_ROUTE     = "CHAT_NO_ROUTE";
  public static final String ID_CHAT_ROUTE        = "CHAT_ROUTE";
  
  public static final byte CHAT_DEFAULT_VERSION = (byte)2;
}
