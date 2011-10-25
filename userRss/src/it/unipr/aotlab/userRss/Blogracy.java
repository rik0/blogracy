/*
 * Created on Oct 25, 2011
 * Created by Enrico Franchi
 *
 * Copyright 2009 Enrico Franchi, Michele Tomaiuolo and University of Parma.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; version 2 of the License only.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
 */
package it.unipr.aotlab.userRss;


import it.unipr.aotlab.userRss.errors.InvalidPluginStateException;
import it.unipr.aotlab.userRss.logging.Logger;
import it.unipr.aotlab.userRss.view.ViewListener;
import org.gudy.azureus2.plugins.PluginException;
import org.gudy.azureus2.plugins.PluginInterface;
import org.gudy.azureus2.plugins.logging.LoggerChannel;
import org.gudy.azureus2.plugins.ui.UIInstance;
import org.gudy.azureus2.plugins.ui.UIManagerListener;
import org.gudy.azureus2.ui.swt.plugins.UISWTInstance;
import org.gudy.azureus2.ui.webplugin.WebPlugin;


public class Blogracy extends WebPlugin {

    static private PluginInterface plugin;

    public static final String DSNS_PLUGIN_CHANNEL_NAME = "DSNS";
    final String PLUGIN_NAME = "blogracy.name";

    private ViewListener viewListener = null;
    private UISWTInstance swtInstance = null;

    

    @Override
    public void initialize(PluginInterface pluginInterface) throws PluginException {
        plugin = pluginInterface;
        Logger.initialize(plugin);
        viewListener = new ViewListener();

        plugin.getUIManager().addUIListener(new UIManagerListener() {
            public void UIAttached(UIInstance instance) {
                if (instance instanceof UISWTInstance) {
                    swtInstance = ((UISWTInstance) instance);

                    if (viewListener != null) {
                        swtInstance.addView(UISWTInstance.VIEW_MAIN, PLUGIN_NAME, viewListener);
                        swtInstance.openMainView(PLUGIN_NAME, viewListener, null);
                    }
                }
            }

            public void UIDetached(UIInstance instance) {
                if (instance instanceof UISWTInstance) {
                    Logger.info("Destroyed plugin.");
                }

            }
        });

    }

    static LoggerChannel getCurrentChannel() throws InvalidPluginStateException {
        if (plugin != null) {
            org.gudy.azureus2.plugins.logging.Logger logger = plugin.getLogger();
            return logger.getChannel(Blogracy.DSNS_PLUGIN_CHANNEL_NAME);
        } else {
            throw new InvalidPluginStateException();
        }
    }

    static ClassLoader getCurrentClassLoader() throws InvalidPluginStateException {
        if (plugin != null) {
            return plugin.getPluginClassLoader();
        } else {
            throw new InvalidPluginStateException();
        }
    }

    
}

