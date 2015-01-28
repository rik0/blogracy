/*
 * Created on 09-Dec-2004
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
import java.net.URL;
import java.net.URLClassLoader;
import java.text.SimpleDateFormat;
import java.util.*;


import org.gudy.azureus2.core3.util.Debug;
import org.gudy.azureus2.plugins.*;
import org.gudy.azureus2.plugins.logging.LoggerChannel;
import org.gudy.azureus2.plugins.logging.LoggerChannelListener;

import org.gudy.azureus2.plugins.ui.UIManager;
import org.gudy.azureus2.plugins.ui.config.*;
import org.gudy.azureus2.plugins.ui.model.BasicPluginConfigModel;
import org.gudy.azureus2.plugins.ui.model.BasicPluginViewModel;
import org.gudy.azureus2.plugins.utils.LocaleUtilities;

import com.aelitis.azureus.core.proxy.socks.AESocksProxyFactory;
import com.aelitis.azureus.plugins.upnp.UPnPMapping;
import com.aelitis.azureus.plugins.upnp.UPnPPlugin;


/**
 * @author parg
 *
 */
public class I2PPlugin 
	implements Plugin
{
	private static final int	PROXY_CON_TIMEOUT		= 120*1000;
	private static final int	PROXY_READ_TIMEOUT		= 120*1000;
	
	protected PluginInterface	plugin_interface;
	
	public static final String[]	I2P_JARS = { "i2p.jar", "streaming.jar", "mstreaming.jar", "jbigi.jar" };
	
	public static final String 	CONFIG_ENABLE		= "enable";
	public static final boolean CONFIG_ENABLE_DEFAULT	= true;
	
	public static final String 	CONFIG_PROXY_PORT			= "proxy_port";
	public static final int 	CONFIG_PROXY_PORT_DEFAULT	= 0;
	
	//public static final String 	CONFIG_PROXY_I2P_ONLY			= "proxy_i2p_only";
	//public static final boolean CONFIG_PROXY_I2P_ONLY_DEFAULT	= false;

	public static final String 	CONFIG_I2P_LOCATION				= "i2p_location";
	public static 		String 	CONFIG_I2P_LOCATION_DEFAULT		= "/home/mic/i2p";
	
	public static final String 	CONFIG_UPNP_ENABLE			= "upnp_enable";
	public static final boolean CONFIG_UPNP_ENABLE_DEFAULT	= false;
	
	public static final String 	CONFIG_UPNP_LEAVE_MAPPING			= "upnp_persistent_mapping";
	public static final boolean CONFIG_UPNP_LEAVE_MAPPING_DEFAULT	= true;
	
	public static final String 	CONFIG_UPNP_PORT			= "upnp_port";
	public static final int		CONFIG_UPNP_PORT_DEFAULT	= 8887;

	public static final String 	CONFIG_I2P_ROUTER_HOST			= "i2p_router_host";
	public static final String 	CONFIG_I2P_ROUTER_HOST_DEFAULT	= "localhost";

	public static final String 	CONFIG_I2P_ROUTER_PORT			= "i2p_router_port";
	public static final int 	CONFIG_I2P_ROUTER_PORT_DEFAULT	= 7654;

	public static final String 	CONFIG_I2P_ROUTER_OPTIONS			= "i2p_router_options";
	public static final String 	CONFIG_I2P_ROUTER_OPTIONS_DEFAULT	= "";

	public static final String 	CONFIG_TRACE					= "trace";
	public static final boolean CONFIG_TRACE_DEFAULT			= false;

	protected LoggerChannel			log;
	protected LoggerChannelListener	log_temp;
	protected List					logs	= new ArrayList();
	
	protected BooleanParameter 		enable;
	protected IntParameter			proxy_port;
	//protected BooleanParameter 		proxy_i2p_only;
		
	protected BooleanParameter 		upnp_enable;
	protected IntParameter			upnp_port;
	protected BooleanParameter		upnp_persist;

	protected StringParameter		i2p_router_host;
	protected IntParameter			i2p_router_port;
	protected StringParameter		i2p_router_options;
	
	protected BooleanParameter		trace;

	public void 
	load(
		PluginInterface _plugin_interface )
	{		
		plugin_interface	= _plugin_interface;
		
		log	= plugin_interface.getLogger().getChannel( "I2P Network Plugin");
		
		if ( plugin_interface.getUtilities().isWindows()){
			
			String	win_def = "C:\\Program Files\\i2p";
			
			if ( new File(win_def).exists()){
				
				CONFIG_I2P_LOCATION_DEFAULT	= win_def;
			}
		}
		
		log_temp = 			
			new LoggerChannelListener()
			{
				public void
				messageLogged(
					int		type,
					String	content )
				{
					logs.add( content );
				}
				
				public void
				messageLogged(
					String		str,
					Throwable	error )
				{
					if ( str.length() > 0 ){
						logs.add( str );
					}
					http://rt.com/news/right-nationalists-storm-ukraine-701/
					logs.add( Debug.getNestedExceptionMessage( error ));
				}
			};
			
		log.addListener( log_temp );
		
		
		UIManager	ui_manager = plugin_interface.getUIManager();

		BasicPluginConfigModel config_model = ui_manager.createBasicPluginConfigModel( "plugins", "i2pnet.name");
						
		enable 		= config_model.addBooleanParameter2( CONFIG_ENABLE, "i2pnet.enable", CONFIG_ENABLE_DEFAULT );
		
		LabelParameter lab1 = config_model.addLabelParameter2( "i2pnet.proxy_port.info" );
		
		proxy_port 	= config_model.addIntParameter2( CONFIG_PROXY_PORT, "i2pnet.proxy_port", CONFIG_PROXY_PORT_DEFAULT );
		
		/*
		 * this doesn't work as Java will revert to not using the proxy if it fails. GRRRRRRRRRRR
		 * 
		proxy_i2p_only 	= config_model.addBooleanParameter2( CONFIG_PROXY_I2P_ONLY, "i2pnet.proxy_i2p_only", CONFIG_PROXY_I2P_ONLY_DEFAULT );

		LabelParameter lab2 = config_model.addLabelParameter2( "i2pnet.proxy_i2p_only.info" );
		
		config_model.createGroup(
				"i2pnet.i2p_only_group",
				new Parameter[]{ proxy_i2p_only, lab2 });
		*/
		
		DirectoryParameter	location = config_model.addDirectoryParameter2( CONFIG_I2P_LOCATION, "i2pnet.i2p_location", CONFIG_I2P_LOCATION_DEFAULT );
		
		upnp_enable 	= config_model.addBooleanParameter2( CONFIG_UPNP_ENABLE, "i2pnet.upnp_enable", CONFIG_UPNP_ENABLE_DEFAULT );
		
		upnp_persist 	= config_model.addBooleanParameter2( CONFIG_UPNP_LEAVE_MAPPING, "i2pnet.upnp_persist", CONFIG_UPNP_LEAVE_MAPPING_DEFAULT );
		
		upnp_port 		= config_model.addIntParameter2( CONFIG_UPNP_PORT, "i2pnet.upnp_port", CONFIG_UPNP_PORT_DEFAULT );

		config_model.createGroup(
			"i2pnet.upnp_group",
			new Parameter[]{ upnp_enable, upnp_persist, upnp_port });
		
			// I2P Router stuff
		
		LabelParameter	i2p_lab = config_model.addLabelParameter2( "i2pnet.i2p_options.info" );
		
		i2p_router_host 		= config_model.addStringParameter2( CONFIG_I2P_ROUTER_HOST, "i2pnet.i2p_router_host", CONFIG_I2P_ROUTER_HOST_DEFAULT );
		
		i2p_router_port 		= config_model.addIntParameter2( CONFIG_I2P_ROUTER_PORT, "i2pnet.i2p_router_port", CONFIG_I2P_ROUTER_PORT_DEFAULT );

		i2p_router_options 		= config_model.addStringParameter2( CONFIG_I2P_ROUTER_OPTIONS, "i2pnet.i2p_router_options", CONFIG_I2P_ROUTER_OPTIONS_DEFAULT );

		config_model.createGroup(
				"i2pnet.i2p_router_group",
				new Parameter[]{ i2p_lab, i2p_router_host, i2p_router_port, i2p_router_options });

			
		trace 	= config_model.addBooleanParameter2( CONFIG_TRACE, "i2pnet.trace_enable", CONFIG_TRACE_DEFAULT );

		enable.addEnabledOnSelection( proxy_port );
		// enable.addEnabledOnSelection( proxy_i2p_only );
		enable.addEnabledOnSelection( lab1 );
		// enable.addEnabledOnSelection( lab2 );
		enable.addEnabledOnSelection( location );
		enable.addEnabledOnSelection( upnp_enable );
		
		ParameterListener	pl = 
			new ParameterListener()
			{
				public void
				parameterChanged(
					Parameter	param  )
				{
					upnp_port.setEnabled( enable.getValue() && upnp_enable.getValue());
					upnp_persist.setEnabled( enable.getValue() && upnp_enable.getValue());
				}
			};
		
		enable.addListener( pl );
		upnp_enable.addListener( pl );
		
		pl.parameterChanged(null);
		
		enable.addEnabledOnSelection( i2p_lab );
		enable.addEnabledOnSelection( i2p_router_host );
		enable.addEnabledOnSelection( i2p_router_port );
		enable.addEnabledOnSelection( i2p_router_options );
		enable.addEnabledOnSelection( trace );

		if ( enable.getValue()){
			
			boolean	bad 	= false;
			boolean	alert	= false;
			
			try{
				String	loc = location.getValue();
				
				if ( loc.length() == 0 || !new File(loc).exists()){
					
					bad		= true;
					alert	= true;
					
					throw( new Exception( "I2P install location not defined, plugin initialisation failed." ));				
				}
					
				File	lib = new File( loc, "lib" );
				
		   		URL[]	jars = new URL[I2P_JARS.length];
		   	
		   		for (int i=0;i<jars.length;i++){
		   			
		   			File	jar = new File(lib,I2P_JARS[i] );
		   			
		   			if ( !jar.exists()){
		   				
		   				bad	= true;
		   				
		   				throw( new Exception( "I2P jar file '" + jar + "' not found" ));
		   			}
		   			
		   			jars[i] = jar.toURL();
		   		}
		   		
		   		ClassLoader	class_loader = getClass().getClassLoader();
    				    		
	    		if ( class_loader instanceof URLClassLoader ){
	    			
	    			URL[]	old = ((URLClassLoader)class_loader).getURLs();
	  
	    			URL[]	new_urls = new URL[old.length+jars.length];
	    			
	    			System.arraycopy( old, 0, new_urls, 0, old.length );
	    			
	    			System.arraycopy( jars, 0, new_urls, old.length, jars.length );
	    			
	    			class_loader = new URLClassLoader(
	    								new_urls,
	    								class_loader );
	    		}else{
	    			  		
	    			class_loader = new URLClassLoader(jars,class_loader);
	    		}
				
				int	port = proxy_port.getValue();
				
				if ( port == 0 ){
					
					bad	= true;
					
					throw( new Exception( "I2P poxy port not defined, can't initialise" ));
				}
				
				final I2PPluginConnectionManager	con_man = new I2PPluginConnectionManager( class_loader, log );
				
				AESocksProxyFactory.create(	port, PROXY_CON_TIMEOUT, PROXY_READ_TIMEOUT, con_man );
				
				log.log( "Established network proxy on port " + port );
				
				plugin_interface.addListener(
					new PluginListener()
					{
						public void
						initializationComplete()
						{
							con_man.initialise(
								i2p_router_host,
								i2p_router_port,
								i2p_router_options,
								trace );
								// proxy_i2p_only );
						}
						
						public void
						closedownInitiated()
						{
							con_man.closedown();
						}
						
						public void
						closedownComplete()
						{
							
						}
					});
	
			}catch( Throwable e ){
				
				if ( bad ){
					
					if ( alert ){
						
						log.logAlert( LoggerChannel.LT_ERROR, e.getMessage());
						
					}else{
						
						log.log( e.getMessage());
					}
				}else{
				
					log.log(e);
				}
			}
		}
	}
	
	public void 
	initialize(
		PluginInterface _plugin_interface )
	{	
		LocaleUtilities loc_utils = plugin_interface.getUtilities().getLocaleUtilities();
		
		UIManager	ui_manager = plugin_interface.getUIManager();
		
		final BasicPluginViewModel	view_model = 
			ui_manager.createBasicPluginViewModel( loc_utils.getLocalisedMessageText( "i2pnet.name" ));

		view_model.getActivity().setVisible( false );
		view_model.getProgress().setVisible( false );
		
		log.removeListener( log_temp );
		
		log.addListener(
				new LoggerChannelListener()
				{
					public void
					messageLogged(
						int		type,
						String	content )
					{
						view_model.getLogArea().appendText( getTimeStamp() + content + "\n" );
					}
					
					public void
					messageLogged(
						String		str,
						Throwable	error )
					{
						if ( str.length() > 0 ){
							view_model.getLogArea().appendText( getTimeStamp() + str + "\n" );
						}
						view_model.getLogArea().appendText( getTimeStamp() + Debug.getNestedExceptionMessage(error) + "\n" );
					}
					
					protected String
					getTimeStamp()
					{
						return( "[" + new SimpleDateFormat( "HH:mm:ss" ).format(new Date()) + "] " );
					}
				});
		
		for (int i=0;i<logs.size();i++){
			
			log.log((String)logs.get(i));
		}
		
		logs.clear();
		
		view_model.getStatus().setText( enable.getValue()?"Enabled":"Disabled");
		
		enable.addListener(
			new ParameterListener()
			{
				public void
				parameterChanged(
					Parameter		p )
				{					
					view_model.getStatus().setText( enable.getValue()?"Enabled":"Disabled");
				}
			});	
		
		
		if ( enable.getValue()){
			
			if ( plugin_interface.getPluginconfig().getBooleanParameter(
					PluginConfig.CORE_PARAM_BOOLEAN_SOCKS_PROXY_NO_INWARD_CONNECTION )){
				
				log.logAlert( 
						LoggerChannel.LT_ERROR,
						"I2P Plugin requires that the 'inform tracker of limitation' setting for 'Connection' is deselected - please amend it" );
				
			}
			
			plugin_interface.addListener(
					new PluginListener()
					{
						public void
						initializationComplete()
						{
							PluginInterface pi_upnp = plugin_interface.getPluginManager().getPluginInterfaceByClass( UPnPPlugin.class );
							
							if ( pi_upnp == null ){
								
								log.log( "No UPnP plugin available, not attempting port mapping");
								
							}else{
								
								if ( upnp_enable.getValue()){
									
									UPnPMapping	mapping = 
										((UPnPPlugin)pi_upnp.getPlugin()).addMapping( 
											plugin_interface.getPluginName(), 
											true, upnp_port.getValue(), 
											true );
									
									if ( upnp_persist.getValue()){
										
										mapping.setPersistent( UPnPMapping.PT_PERSISTENT );
									}
									
								}else{
									
									log.log( "UPnP disabled for the plugin, not attempting port mapping");
									
								}
							}
						}
						
						public void
						closedownInitiated()
						{
						}
						
						public void
						closedownComplete()
						{	
						}
					});
		}
	}
}
