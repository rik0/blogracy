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


import it.unipr.aotlab.userRss.logging.Logger;
import it.unipr.aotlab.userRss.view.ViewListener;
import org.gudy.azureus2.core3.config.COConfigurationManager;
import org.gudy.azureus2.core3.config.impl.ConfigurationDefaults;
import org.gudy.azureus2.core3.util.SystemProperties;
import org.gudy.azureus2.plugins.PluginException;
import org.gudy.azureus2.plugins.PluginInterface;
import org.gudy.azureus2.plugins.ui.UIInstance;
import org.gudy.azureus2.plugins.ui.UIManagerListener;
import org.gudy.azureus2.plugins.ui.config.ConfigSection;
import org.gudy.azureus2.plugins.ui.config.HyperlinkParameter;
import org.gudy.azureus2.plugins.ui.model.BasicPluginConfigModel;
import org.gudy.azureus2.ui.swt.plugins.UISWTInstance;
import org.gudy.azureus2.ui.webplugin.WebPlugin;

import java.io.File;
import java.util.Properties;


public class Blogracy extends WebPlugin {

    private static final String BLOGRACY = "blogracy";
    private HyperlinkParameter test_param;
    static private Blogracy singleton;

    static class Accesses {

        static String ALL = "all";
        static String LOCAL = "local";
    }

    static private PluginInterface plugin;

    private static final String CONFIG_ACCESS_KEY = "blogracy.internal.config.access";
    private static final String CONFIG_PORT_KEY = "blogracy.internal.config.port";
    private static final String DEVICE_ACCESS_KEY = "blogracy.internal.config.access";
    private static final String INTERNAL_URL_KEY = "blogracy.internal.test.url";
    private static final String DEVICE_PORT_KEY = "Plugin.default.device.blogracy.port";
    private static final String DEVICE_LOCALONLY_KEY = "Plugin.default.device.blogracy.localonly";
    private static final String DEVICE_RSS_ENABLE_KEY = "Plugin.default.device.rss.enable";
    private static final String DID_MIGRATE_KEY = "blogracy.internal.migrated";

    private static Properties defaults = new Properties();


    public static final String DSNS_PLUGIN_CHANNEL_NAME = "DSNS";

    private final static String PLUGIN_NAME = "blogracy.name";
    private static final String PLUGIN_NAME_KEY = "blogracy.name";

    final static int DEFAULT_PORT = 32674;
    final static String DEFAULT_ACCESS = Accesses.ALL;

    private ViewListener viewListener = null;
    private UISWTInstance swtInstance = null;
    private static boolean loaded;


    static {
        ConfigurationDefaults cd = ConfigurationDefaults.getInstance();
        cd.addParameter(DID_MIGRATE_KEY, Boolean.TRUE);
        cd.addParameter(DEVICE_LOCALONLY_KEY, Boolean.TRUE);
        cd.addParameter(DEVICE_RSS_ENABLE_KEY, Boolean.FALSE);
    }

    public static Blogracy getSingleton() {
        return (singleton);
    }

    @Override
    protected void initStage(int num) {
        if (num == 1) {
            BasicPluginConfigModel config = getConfigModel();
            test_param = config.addHyperlinkParameter2(INTERNAL_URL_KEY, "");
            test_param.setEnabled(isPluginEnabled());
        }
    }

    /**
     * This method is effectively called when a new instance of this plugin should be created.
     * <p/>
     * We found this feature not documented. Perhaps we should not rely on that.
     * We also ensure that Blogracy is a singleton.
     *
     * @param pluginInterface is the same old access point for plugins
     */
    public static void load(PluginInterface pluginInterface) {
        Integer blogracy_port;
        String blogracy_access;

        Properties properties = pluginInterface.getPluginProperties();

        properties.setProperty("plugin.name", PLUGIN_NAME);
        properties.setProperty("plugin.version", "0.2");

        synchronized (Blogracy.class) {
            if (loaded) {
                return;
            } else {
                loaded = true;
            }
        }

        File root_dir = new File(SystemProperties.getUserPath() + BLOGRACY);

        if (!root_dir.exists()) {

            root_dir.mkdir();
        }


        if (COConfigurationManager.getBooleanParameter(DID_MIGRATE_KEY)) {
            blogracy_port = COConfigurationManager.getIntParameter(CONFIG_PORT_KEY, DEFAULT_PORT);
            blogracy_access = COConfigurationManager.getStringParameter(CONFIG_ACCESS_KEY, DEFAULT_ACCESS);
        } else {
            blogracy_port = COConfigurationManager.getIntParameter(DEVICE_PORT_KEY, DEFAULT_PORT);
            if (blogracy_port != DEFAULT_PORT) {
                COConfigurationManager.setParameter(CONFIG_PORT_KEY, blogracy_port);
            }

            boolean local = COConfigurationManager.getBooleanParameter(DEVICE_LOCALONLY_KEY);
            blogracy_access = local ? Accesses.LOCAL : Accesses.ALL;
            if (!blogracy_access.equals(DEFAULT_ACCESS)) {
                COConfigurationManager.setParameter(DEVICE_ACCESS_KEY, blogracy_access);
            }
            COConfigurationManager.setParameter(DID_MIGRATE_KEY, Boolean.TRUE);

        }

        final boolean rssEnable = COConfigurationManager.getBooleanParameter(DEVICE_RSS_ENABLE_KEY);
        defaults.put(WebPlugin.PR_ENABLE, rssEnable);
        defaults.put(WebPlugin.PR_DISABLABLE, Boolean.TRUE);
        defaults.put(WebPlugin.PR_PORT, blogracy_port);
        defaults.put(WebPlugin.PR_ACCESS, blogracy_access);
        defaults.put(WebPlugin.PR_ROOT_DIR, root_dir.getAbsolutePath());
        defaults.put(WebPlugin.PR_ENABLE_KEEP_ALIVE, Boolean.TRUE);
        defaults.put(WebPlugin.PR_HIDE_RESOURCE_CONFIG, Boolean.TRUE);
        defaults.put(WebPlugin.PR_PAIRING_SID, BLOGRACY);

        defaults.put(WebPlugin.PR_CONFIG_MODEL_PARAMS,
                new String[]{ConfigSection.SECTION_ROOT, BLOGRACY});

    }

    public Blogracy() {
        super(defaults);
    }

    public String getURL() {
        return (getProtocol() + "://127.0.0.1:" + getPort() + "/");
    }

    @Override
    protected void
    setupServer() {
        super.setupServer();

        if (test_param != null) {
            test_param.setEnabled(isPluginEnabled());
            test_param.setHyperlink(getURL());
        }
    }

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
                        swtInstance.addView(UISWTInstance.VIEW_MAIN, PLUGIN_NAME_KEY, viewListener);
                        swtInstance.openMainView(PLUGIN_NAME_KEY, viewListener, null);
                    }
                }
            }

            public void UIDetached(UIInstance instance) {
                if (instance instanceof UISWTInstance) {
                    Logger.info("Destroyed plugin.");
                }

            }
        });

        singleton = this;

        pluginInterface.getPluginProperties().setProperty("plugin.name", PLUGIN_NAME);
        super.initialize(pluginInterface);
    }
}

