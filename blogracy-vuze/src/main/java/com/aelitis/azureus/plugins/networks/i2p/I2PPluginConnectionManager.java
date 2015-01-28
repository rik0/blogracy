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
import java.lang.reflect.*;
import java.util.Properties;
import java.util.StringTokenizer;

import org.gudy.azureus2.core3.util.AEMonitor;
import org.gudy.azureus2.core3.util.AESemaphore;
import org.gudy.azureus2.core3.util.AEThread;
import org.gudy.azureus2.core3.util.Debug;
import org.gudy.azureus2.plugins.logging.LoggerChannel;
import org.gudy.azureus2.plugins.ui.config.BooleanParameter;
import org.gudy.azureus2.plugins.ui.config.IntParameter;
import org.gudy.azureus2.plugins.ui.config.StringParameter;

import com.aelitis.azureus.core.proxy.AEProxyException;
import com.aelitis.azureus.core.proxy.socks.*;

/**
 * @author parg
 *
 */

public class 
I2PPluginConnectionManager
	implements AESocksProxyPlugableConnectionFactory
{
	private ClassLoader		class_loader;
	
	private LoggerChannel		log;
	
	private volatile  Object			socket_manager;
	
	private Class				i2p_Destination;
	private Method			i2p_Destination_fromBase64;
	
	private Class 			i2p_I2PSocketManagerFactory;
	private Method			i2p_I2PSocketManager_getSession;
	private Method			i2p_I2PSocketManager_ping;
	private Method			i2p_I2PSocketManager_connect;
	
	private Method			i2p_I2PSession_getMyDestination;
	
	private Method			i2p_I2PSocket_close;
	private Method			i2p_I2PSocket_getInputStream;
	private Method			i2p_I2PSocket_getOutputStream;
	
	//private Method			i2p_I2PInputStream_read;
	//private Method			i2p_I2POutputStream_write;
	
	private StringParameter		router_host;
	private IntParameter		router_port;
	private StringParameter		router_options;
	// private	BooleanParameter	proxy_i2p_only;
	
	private BooleanParameter	trace;
	
	private AESemaphore			sem			= new AESemaphore( "I2PInit" );
	private AEMonitor			this_mon	= new AEMonitor( "I2PPluginConnectionManager" );
	
	protected
	I2PPluginConnectionManager(
		ClassLoader		_class_loader,
		LoggerChannel	_log )
	{
		class_loader	= _class_loader;
		log				= _log;
	}
	
	protected void
	initialise(
		StringParameter		_router_host,
		IntParameter		_router_port,
		StringParameter		_router_options,
		BooleanParameter	_trace )
		// BooleanParameter	_proxy_i2p_only )
	{
		router_host		= _router_host;
		router_port		= _router_port;
		router_options	= _router_options;
		trace			= _trace;
		
		// proxy_i2p_only	= _proxy_i2p_only;
		
		
		try{
			log.log( "Initialising I2P Socket Manager" );
								
			Class i2p_I2PContext = class_loader.loadClass( "net.i2p.I2PAppContext" );

			i2p_I2PContext.newInstance();
			
			i2p_Destination = class_loader.loadClass( "net.i2p.data.Destination" );

			i2p_Destination_fromBase64 = i2p_Destination.getMethod( "fromBase64", new Class[]{ String.class });
			
			Class i2p_I2PSession = class_loader.loadClass( "net.i2p.client.I2PSession" );
			
			i2p_I2PSession_getMyDestination = i2p_I2PSession.getMethod( "getMyDestination", new Class[0]);
			
			i2p_I2PSocketManagerFactory = class_loader.loadClass( "net.i2p.client.streaming.I2PSocketManagerFactory" );
			
			Class i2p_I2PSocketManager = class_loader.loadClass( "net.i2p.client.streaming.I2PSocketManager" );
					
			i2p_I2PSocketManager_getSession = i2p_I2PSocketManager.getMethod( "getSession", new Class[0]);
			i2p_I2PSocketManager_ping		= i2p_I2PSocketManager.getMethod( "ping", new Class[]{ i2p_Destination, long.class });
			i2p_I2PSocketManager_connect 	= i2p_I2PSocketManager.getMethod( "connect", new Class[]{ i2p_Destination });
			
			Class i2p_I2PSocket = class_loader.loadClass( "net.i2p.client.streaming.I2PSocket" );

			i2p_I2PSocket_close			 	= i2p_I2PSocket.getMethod( "close", new Class[0] );
			i2p_I2PSocket_getInputStream 	= i2p_I2PSocket.getMethod( "getInputStream", new Class[0] );
			i2p_I2PSocket_getOutputStream	= i2p_I2PSocket.getMethod( "getOutputStream", new Class[0] );
			
			/*
			Class i2p_I2PInputStream = class_loader.loadClass( "net.i2p.client.streaming.MessageInputStream" );

			i2p_I2PInputStream_read	= i2p_I2PInputStream.getMethod( "read", new Class[]{ byte[].class });
			
			Class i2p_I2POutputStream = class_loader.loadClass( "net.i2p.client.streaming.MessageOutputStream" );

			i2p_I2POutputStream_write	= i2p_I2POutputStream.getMethod( "write", new Class[]{ byte[].class, int.class, int.class });
			*/
			
			connectToRouter();
			
		}catch( Throwable e ){
			
			log.logAlert( "I2P Router initialisation failed", e );
		}
	}
	
	protected void
	connectToRouter()
	{
		Thread t = 
			new AEThread( "I2P Initialiser" )
				{		
					public void
					runSupport()
					{
						boolean	error_reported = false;
						
						while( socket_manager == null ){
				
							long	last_create_time = System.currentTimeMillis();
				
							try{
																
								Properties opts = new Properties();
								
								StringTokenizer tok = new StringTokenizer( router_options.getValue());
								
								while (tok.hasMoreTokens()) {
									
									String pair = tok.nextToken();
									
									int eq = pair.indexOf('=');
									
									if ( (eq <= 0) || (eq >= pair.length()) ){
								
										continue;
									}
									
									String key = pair.substring(0, eq);
									
									String val = pair.substring(eq+1);
									
									opts.setProperty(key, val);
								}
								
								socket_manager = 
									i2p_I2PSocketManagerFactory.getMethod( 
											"createManager", 
											new Class[] { String.class, int.class, Properties.class }).invoke( 
													null, new Object[] { router_host.getValue(), new Integer(router_port.getValue()), opts });
								 									
						        if ( socket_manager != null ){
						        			        				        	
									log.logAlertRepeatable( 
											LoggerChannel.LT_INFORMATION,
											"I2P Router connection succeeded" );
					
									Object	session = i2p_I2PSocketManager_getSession.invoke( socket_manager, new Object[0] );
										
									final Object	my_dest = i2p_I2PSession_getMyDestination.invoke( session, new Object[0] );

									final Object	this_sm = socket_manager;
									
									AEThread	pinger = 
										new AEThread( "I2P Pinger" )
											{
												public void
												runSupport()
												{		
													int	consecutive_fails = 0;
													
													while( socket_manager == this_sm ){
														
														try{
															Thread.sleep(60000);
															
															long	start = System.currentTimeMillis();
															
															boolean	res = ((Boolean)i2p_I2PSocketManager_ping.invoke( 
																				this_sm, 
																				new Object[]{ my_dest, new Integer( 10000 )})).booleanValue();
			
															log.log( "I2P router ping response = " + (res?"OK":"Failed") +", elapsed = " + (System.currentTimeMillis() - start ));
															
															if ( res){
																
																consecutive_fails	= 0;
															}else{
																
																consecutive_fails++;
																
																if ( consecutive_fails == 3 ){
																	
																	routerFailure( this_sm );
																}
															}
														}catch( Throwable e ){
																
															log.log( e );
																
															routerFailure( this_sm );
														}
													}
												}
											};
											
									pinger.setDaemon( true );
									
									pinger.start();
									
									sem.releaseForever();
									
						        	break;
						        }
						        
							}catch( Throwable e ){
								
								log.log( "I2P Router connection failed", e );
							}
							
					        String	msg = "I2P Router connection failed, check that the I2P router is running";
					        
					        if ( error_reported ){
					        	
					        	log.log( msg );
					        	
					        }else{
					        	
					        	error_reported	= true;
					        	
					        	log.logAlert( LoggerChannel.LT_ERROR, "I2P Router connection failed, check that the I2P router is running" );
					        }
					        
				        	long	sleep = 30000 - ( System.currentTimeMillis() - last_create_time );
					        	
					        if ( sleep > 0 ){
					        
					        	try{
					        		Thread.sleep( sleep );
					        		
					        	}catch( Throwable e ){
					        		
					        		e.printStackTrace();
					        	}
					        }
						}
					}
				};
		
		t.setDaemon( true );
		
		t.start();
	}
	
	protected void
	routerFailure(
		Object	failed_socket_manager )
	{
		try{
			this_mon.enter();
		
			if ( socket_manager == failed_socket_manager ){
			
				log.logAlertRepeatable( LoggerChannel.LT_ERROR, "I2P Router connection failed" );
		
				socket_manager	= null;
				
				sem = new AESemaphore( "I2PInit" );
				
				connectToRouter();
			}
		}finally{
			
			this_mon.exit();
		}
	}
	
	protected void
	closedown()
	{
		
	}

	protected void
	log(
		String		str )
	{
		log.log( str );
	}
	
	protected void
	trace(
		String		str )
	{
		if ( trace.getValue()){
			
			log.log( str );
		}
	}
	
	protected boolean
	proxyI2POnly()
	{
		return( false ); // proxy_i2p_only.getValue());
	}
	
	public AESocksProxyPlugableConnection
	create(
		AESocksProxyConnection	connection )
	
		throws AEProxyException
	{
		return( new I2PPluginConnection( this, connection ) );
	}
	
	protected Object
	i2pSocketManager_connect(
		String	address )
	
		throws IOException
	{
		sem.reserve();
		
		Object	current_socket_manager	= socket_manager;
		
		if ( current_socket_manager == null || i2p_Destination == null ){
			
			throw( new IOException( "I2P network unavailable" ));
		}
		
		try{
			Object remote_dest = i2p_Destination.newInstance();
	        
			i2p_Destination_fromBase64.invoke( remote_dest, new Object[]{ address });
			
			Object res = i2p_I2PSocketManager_connect.invoke( current_socket_manager, new Object[]{ remote_dest });
						
			return( res );
			
		}catch( Throwable e ){
			
			if ( Debug.getNestedExceptionMessage(e).toLowerCase().indexOf( "session is closed" ) != -1 ){
				
				routerFailure( current_socket_manager );
			}
			
			log.log(e);
			
			throw( new IOException( e.getMessage()));
		}
	}
	
	protected void
	i2pSocket_close(
		Object		socket )
	
		throws IOException
	{
		try{
			i2p_I2PSocket_close.invoke( socket, new Object[0]);
			
		}catch( Throwable e ){
			
			if ( e instanceof IOException ){
				
				throw((IOException)e);
			}
			
			log.log(e);
			
			throw( new IOException( e.getMessage()));
		}
	}
	
	protected InputStream
	i2pSocket_getInputStream(
		Object		socket )
	
		throws IOException
	{
		try{
			return((InputStream)i2p_I2PSocket_getInputStream.invoke( socket, new Object[0]));
			
		}catch( Throwable e ){
			
			if ( e instanceof IOException ){
				
				throw((IOException)e);
			}
			
			log.log(e);
			
			throw( new IOException( e.getMessage()));
		}
	}
	
	protected OutputStream
	i2pSocket_getOutputStream(
		Object		socket )
	
		throws IOException
	{
		try{
			return((OutputStream)i2p_I2PSocket_getOutputStream.invoke( socket, new Object[0]));
			
		}catch( Throwable e ){
			
			if ( e instanceof IOException ){
				
				throw((IOException)e);
			}
			
			log.log(e);
			
			throw( new IOException( e.getMessage()));
		}
	}
	
	/*
	protected int
	i2pInputStream_read(
		Object		input_stream,
		byte[]		buffer )
	
		throws IOException
	{
		try{
			Integer res = (Integer)i2p_I2PInputStream_read.invoke( input_stream, new Object[]{buffer});
			
			return( res.intValue());
			
		}catch( Throwable e ){
			
			if ( e instanceof IOException ){
				
				throw((IOException)e);
			}
			
			log.log(e);
			
			throw( new IOException( e.getMessage()));
		}
	}
	
	protected void
	i2pOutputStream_write(
		Object		output_stream,
		byte[]		buffer,
		int			start,
		int			len )
	
		throws IOException
	{
		try{
			i2p_I2POutputStream_write.invoke( output_stream, new Object[]{buffer, new Integer(start), new Integer(len)});
			
		}catch( Throwable e ){
			
			if ( e instanceof IOException ){
				
				throw((IOException)e);
			}
			
			log.log(e);
			
			throw( new IOException( e.getMessage()));
		}
	}
	*/
}
