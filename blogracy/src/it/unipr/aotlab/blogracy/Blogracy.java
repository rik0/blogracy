/*
 * Copyright (c)  2011 Enrico Franchi, Michele Tomaiuolo and University of Parma.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package it.unipr.aotlab.blogracy;

import it.unipr.aotlab.blogracy.errors.URLMappingError;
import it.unipr.aotlab.blogracy.logging.Logger;
import it.unipr.aotlab.blogracy.view.ViewListener;
import it.unipr.aotlab.blogracy.web.ErrorPageResolver;
import it.unipr.aotlab.blogracy.web.RequestResolver;
import it.unipr.aotlab.blogracy.web.URLMapper;
import org.gudy.azureus2.core3.config.COConfigurationManager;
import org.gudy.azureus2.core3.config.impl.ConfigurationDefaults;
import org.gudy.azureus2.core3.util.SystemProperties;
import org.gudy.azureus2.plugins.PluginException;
import org.gudy.azureus2.plugins.PluginInterface;
import org.gudy.azureus2.plugins.tracker.web.TrackerWebPageRequest;
import org.gudy.azureus2.plugins.tracker.web.TrackerWebPageResponse;
import org.gudy.azureus2.plugins.ui.UIInstance;
import org.gudy.azureus2.plugins.ui.UIManagerListener;
import org.gudy.azureus2.plugins.ui.config.ConfigSection;
import org.gudy.azureus2.plugins.ui.config.HyperlinkParameter;
import org.gudy.azureus2.plugins.ui.model.BasicPluginConfigModel;
import org.gudy.azureus2.ui.swt.plugins.UISWTInstance;
import org.gudy.azureus2.ui.webplugin.WebPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Properties;


public class Blogracy extends WebPlugin {

    private URLMapper mapper = new URLMapper();

    private static final String BLOGRACY = "blogracy";
    private HyperlinkParameter test_param;
    static private Blogracy singleton;
    private final String MESSAGES_BLOGRACY_URL_KEY = "blogracy.url";

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
    private static final String DEVICE_BLOGRACY_ENABLE_KEY = "Plugin.default.device.blogracy.enable";
    private static final String PLUGIN_NAME_KEY = "blogracy.name";

    private static final String DID_MIGRATE_KEY = "blogracy.internal.migrated";


    private static Properties defaults = new Properties();

    public static final String DSNS_PLUGIN_CHANNEL_NAME = "DSNS";

    final static int DEFAULT_PORT = 32674;
    final static String DEFAULT_ACCESS = Accesses.ALL;

    private ViewListener viewListener = null;
    private UISWTInstance swtInstance = null;
    private static boolean loaded;


    static {
        ConfigurationDefaults cd = ConfigurationDefaults.getInstance();
        cd.addParameter(DID_MIGRATE_KEY, Boolean.TRUE);
        cd.addParameter(DEVICE_LOCALONLY_KEY, Boolean.TRUE);
        cd.addParameter(DEVICE_BLOGRACY_ENABLE_KEY, Boolean.FALSE);
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
            test_param.setLabelKey(MESSAGES_BLOGRACY_URL_KEY);
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

        synchronized (Blogracy.class) {
            if (loaded) {
                return;
            } else {
                loaded = true;
            }
        }

        File root_dir = createRootDirectoryIfMissingAndGetPath();


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

        final boolean blogracyEnable = COConfigurationManager.getBooleanParameter(DEVICE_BLOGRACY_ENABLE_KEY);
        defaults.put(WebPlugin.PR_ENABLE, blogracyEnable);
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

    private static File createRootDirectoryIfMissingAndGetPath() {
        File root_dir = new File(SystemProperties.getUserPath() + BLOGRACY);

        if (!root_dir.exists()) {
            boolean createdDir = root_dir.mkdir();
            assert (createdDir);
        }
        return root_dir;
    }

    public Blogracy() {
        super(defaults);
    }

    public String getURL() {
        return (getProtocol() + "://127.0.0.1:" + getPort() + "/");
    }


    @Override
    public boolean generateSupport(TrackerWebPageRequest request, TrackerWebPageResponse response) throws IOException {
        String url = request.getURL();
        RequestResolver resolver = mapper.getResolver(url);
        try {
            resolver.resolve(request, response);
        } catch (Exception e) {
            ErrorPageResolver errorResolver = new ErrorPageResolver(e);
            errorResolver.resolve(request, response);
        }
        return true;
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
        initializePluginInterface(pluginInterface);
        initializeLoggr();
        initializeURLMapper();
        initializeViewListener();
        initializeSingleton();
        super.initialize(pluginInterface);
    }

    private void initializeSingleton() {
        singleton = this;
    }

    private void initializeViewListener() {
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
    }

    private void initializePluginInterface(final PluginInterface pluginInterface) {
        plugin = pluginInterface;
    }

    private void initializeLoggr() {
        Logger.initialize(plugin, DSNS_PLUGIN_CHANNEL_NAME);
    }

    private void initializeURLMapper() throws PluginException {
        try {
            mapper.configure(
                    "$/followers^", "it.unipr.aotlab.blogracy.web.Followers"
            );
        } catch (URLMappingError urlMappingError) {
            throw new PluginException(urlMappingError);
        }
    }
}

