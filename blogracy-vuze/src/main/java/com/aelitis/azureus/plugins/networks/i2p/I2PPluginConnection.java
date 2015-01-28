/*
 * Created on 13-Dec-2004
 * Created by Paul Gardner
 * Copyright (C) 2004 Aelitis, All Rights Reserved.
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

package com.aelitis.azureus.plugins.networks.i2p;

import java.io.*;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.gudy.azureus2.core3.util.AEMonitor;
import org.gudy.azureus2.core3.util.AERunnable;
import org.gudy.azureus2.core3.util.AESemaphore;
import org.gudy.azureus2.core3.util.AEThread;
import org.gudy.azureus2.core3.util.Debug;
import org.gudy.azureus2.core3.util.ThreadPool;

import com.aelitis.azureus.core.proxy.*;
import com.aelitis.azureus.core.proxy.socks.*;

/**
 * @author parg
 *
 */

public class 
I2PPluginConnection
	implements AESocksProxyPlugableConnection
{
	private static final boolean TRACE = true;
	
		// try to buffer at least a whole block
	
	public static final int RELAY_BUFFER_SIZE	= 64*1024 + 256;
	
	protected I2PPluginConnectionManager	con_man;
	protected Object						socket;
	protected boolean						socket_closed;
	
	protected AESocksProxyConnection		proxy_connection;
	
	protected proxyStateRelayData			relay_state;
	
	protected AEMonitor			this_mon	= new AEMonitor( "I2PPluginConnection" );

	protected
	I2PPluginConnection(
		I2PPluginConnectionManager		_con_man,
		AESocksProxyConnection			_proxy_connection )
	{
		con_man				= _con_man;
		proxy_connection	= _proxy_connection;
		
		proxy_connection.disableDNSLookups();
	}
	
	public String
	getName()
	{
		return( "I2PPluginConnection" );
	}
	
	public InetAddress
	getLocalAddress()
	{
		try{
			return( InetAddress.getLocalHost() );
			
		}catch( Throwable e ){
			
			Debug.printStackTrace(e);
			
			return( null );
		}
	}
	
	public int
	getLocalPort()
	{
		return( -1 );
	}

	public void
	connect(
		AESocksProxyAddress		_address )
		
		throws IOException
	{
		if ( TRACE ){
			
			con_man.log( "connect request to " + _address.getUnresolvedAddress() + "/" + _address.getAddress() + "/" + _address.getPort());
		}
		
		if ( _address.getAddress() != null ){
		
			if ( con_man.proxyI2POnly()){
				
				if ( TRACE ){
					
					con_man.log( "    NOT delegating resolved, non-I2P delegation disabled" );
				}
				
				throw( new IOException( "delegation to non-I2P locations disabled" ));
			}
			
			if ( TRACE ){
				
				con_man.log( "    delegating resolved" );
			}
			
			AESocksProxyPlugableConnection	delegate = proxy_connection.getProxy().getDefaultPlugableConnection( proxy_connection );
			
			proxy_connection.setDelegate( delegate );
			
			delegate.connect( _address );
			
		}else{ 
			
			final String	externalised_address = AEProxyFactory.getAddressMapper().externalise(_address.getUnresolvedAddress());
		
			if ( !externalised_address.toLowerCase().endsWith(".i2p")){
				
				if ( con_man.proxyI2POnly()){
					
					if ( TRACE ){
						
						con_man.log( "    NOT delegating unresolved, non-I2P delegation disabled" );
					}
					
					throw( new IOException( "delegation to non-I2P locations disabled" ));
				}
				
				if ( TRACE ){
					
					con_man.log( "    delegating unresolved" );
				}
				
				AESocksProxyPlugableConnection	delegate = proxy_connection.getProxy().getDefaultPlugableConnection( proxy_connection );
				
				proxy_connection.enableDNSLookups();
				
				proxy_connection.setDelegate( delegate );
				
				delegate.connect( _address );

			}else{

				AEThread	connect_thread = 
					new AEThread( "I2PConnect")
					{
						public void
						runSupport()
						{
							if ( TRACE ){
								
								con_man.log( "    delegating to I2P" );
							}
							
							try{
								
									// remove the .i2p
								
								String new_externalised_address = externalised_address.substring( 0, externalised_address.length() - 4 );
								
						        socket = con_man.i2pSocketManager_connect( new_externalised_address );
						       	
						        proxy_connection.connected();
						        
						        
							}catch( Throwable e ){
								
								try{
									proxy_connection.close();
									
								}catch( Throwable f ){
									
									f.printStackTrace();
								}
								
								e.printStackTrace();
								
								con_man.log( "I2PSocket creation fails: " + Debug.getNestedExceptionMessage(e) );
							}
						}
					};
					
				connect_thread.setDaemon( true );
				
				connect_thread.start();
			}
		}
	}
	
	public void
	relayData()
	
		throws IOException
	{
		try{
			this_mon.enter();
		
			if ( socket_closed ){
			
				throw( new IOException( "I2PPluginConnection::relayData: socket already closed"));
			}
		
			relay_state = new proxyStateRelayData( proxy_connection.getConnection());
			
		}finally{
			
			this_mon.exit();
		}
	}
	
	public void
	close()
	
		throws IOException
	{
		try{
			this_mon.enter();
		
			if ( socket != null && !socket_closed ){
				
				socket_closed	= true;
			
				if ( relay_state != null ){
					
					relay_state.close();
				}
				
				final Object	f_socket	= socket;
				
				socket	= null;
				
				AEThread t = 
					new AEThread( "I2P SocketCloser" )
					{
						public void
						runSupport()
						{
							try{
								con_man.i2pSocket_close( f_socket );
								
							}catch( Throwable e ){
								
							}
						}
					};
					
				t.setDaemon(true);
				
				t.start();
			}
		}finally{
			
			this_mon.exit();
		}
	}
	
	protected class
	proxyStateRelayData
		implements AEProxyState
	{
		protected AEProxyConnection		connection;
		protected ByteBuffer			source_buffer;
		protected ByteBuffer			target_buffer;
		
		protected SocketChannel			source_channel;
		
		protected InputStream			input_stream;
		protected OutputStream			output_stream;
		
		protected long					outward_bytes	= 0;
		protected long					inward_bytes	= 0;
		
		protected AESemaphore			write_sem = new AESemaphore( "I2PSocket write sem" );
		
		protected ThreadPool			async_pool = new ThreadPool( "I2PSocket async", 2 );
		
		protected
		proxyStateRelayData(
			AEProxyConnection	_connection )
		
			throws IOException
		{		
			connection	= _connection;
			
			source_channel	= connection.getSourceChannel();
			
			source_buffer	= ByteBuffer.allocate( RELAY_BUFFER_SIZE );
			
			connection.setReadState( this );
			
			connection.setWriteState( this );
			
			connection.requestReadSelect( source_channel );
						
			connection.setConnected();
			
			input_stream 	= con_man.i2pSocket_getInputStream( socket );
			output_stream 	= con_man.i2pSocket_getOutputStream( socket );

			async_pool.run(
				new AERunnable()
				{
					public void
					runSupport()
					{
						byte[]	buffer = new byte[RELAY_BUFFER_SIZE];
						
						
						while( !connection.isClosed()){
						
							try{
								con_man.trace( "I2PCon: " + getStateName() + " : read Starts <- I2P " );

								long	start = System.currentTimeMillis();
								
								int	len = input_stream.read( buffer );
								
								if ( len <= 0 ){
									
									break;
								}
															
								con_man.trace( "I2PCon: " + getStateName() + " : read Done <- I2P - " + len + ", elapsed = " + ( System.currentTimeMillis() - start ));
								
								if ( target_buffer != null ){
									
									Debug.out("I2PluginConnection: target buffer should be null" );
								}
								
								target_buffer = ByteBuffer.wrap( buffer, 0, len );
								
								read();
								
							}catch( Throwable e ){
								
								if ( 	e instanceof IOException &&
										e.getMessage() != null &&
										e.getMessage().startsWith( "Already closed" )){
								
										// ignore this one
									
								}else{
										
									Debug.printStackTrace(e);
								}
								
								break;
							}
						}
						
						if ( !proxy_connection.isClosed()){
							
							try{
								proxy_connection.close();
								
							}catch( IOException e ){
								
								Debug.printStackTrace(e);
							}
						}
					}
				});
		}
		
		protected void
		close()
		{						
			con_man.trace( "I2PCon: " + getStateName() + " close" );
			
			write_sem.releaseForever();
		}
		
		protected void
		read()
		
			throws IOException
		{
				// data from I2P
			
			connection.setTimeStamp();
		
			int written = source_channel.write( target_buffer );
				
			con_man.trace( "I2PCon: " + getStateName() + " : write -> AZ - " + written );
			
			inward_bytes += written;
			
			if ( target_buffer.hasRemaining()){
			
				connection.requestWriteSelect( source_channel );
				
				write_sem.reserve();
			}
			
			target_buffer	= null;
		}
		
		public boolean
		read(
			SocketChannel 		sc )
		
			throws IOException
		{
			if ( source_buffer.position() != 0 ){
				
				Debug.out( "I2PluginConnection: source buffer position invalid" );
			}
			
				// data read from source
			
			connection.setTimeStamp();
															
			final int	len = sc.read( source_buffer );
	
			if ( len == 0 ){
				
				return( false );
			}
			
			if ( len == -1 ){
				
				throw( new IOException( "read channel shutdown" ));
				
			}else{
				
				if ( source_buffer.position() > 0 ){
					
					connection.cancelReadSelect( source_channel );
					
					con_man.trace( "I2PCon: " + getStateName() + " : read <- AZ - " + len );
					
						// offload the write to separate thread as can't afford to block the
						// proxy
				
					async_pool.run(
						new AERunnable()
						{
							public void
							runSupport()
							{
								try{					
									source_buffer.flip();
									
									long	start = System.currentTimeMillis();
									
									con_man.trace( "I2PCon: " + getStateName() + " : write Starts -> I2P - " + len );
									
									output_stream.write( source_buffer.array(), 0, len );
					
									source_buffer.position( 0 );
									
									source_buffer.limit( source_buffer.capacity());
									
									// output_stream.flush();
									
									con_man.trace( "I2PCon: " + getStateName() + " : write done -> I2P - " + len + ", elapsed = " + ( System.currentTimeMillis() - start ));
									
									outward_bytes += len;
									
									connection.requestReadSelect( source_channel );								

								}catch( Throwable e ){
									
									connection.failed( e );
								}
							}
						});			
				}
			}
			
			return( true );
		}
		
		public boolean
		write(
			SocketChannel 		sc )
		
			throws IOException
		{
			
			try{
				int written = source_channel.write( target_buffer );
					
				inward_bytes += written;
					
				con_man.trace( "I2PCon: " + getStateName() + " write -> AZ: " + written );
				
				if ( target_buffer.hasRemaining()){
									
					connection.requestWriteSelect( source_channel );
					
				}else{
					
					write_sem.release();
				}
				
				return( written > 0 );
				
			}catch( Throwable e ){
				
				write_sem.release();
				
				if (e instanceof IOException ){
					
					throw((IOException)e);
				}
				
				throw( new IOException( "write fails: " + Debug.getNestedExceptionMessage(e)));
			}
		}
		
		public boolean
		connect(
			SocketChannel	sc )
		
			throws IOException
		{
			throw( new IOException( "unexpected connect" ));
		}
		
		public String
		getStateName()
		{
			String	state = this.getClass().getName();
			
			int	pos = state.indexOf( "$");
			
			state = state.substring(pos+1);
			
			return( state  +" [out=" + outward_bytes +",in=" + inward_bytes +"] " + (source_buffer==null?"":source_buffer.toString()) + " / " + target_buffer );
		}
	}
}
