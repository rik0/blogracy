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

import it.unipr.aotlab.blogracy.errors.ServerConfigurationError;
import it.unipr.aotlab.blogracy.errors.URLMappingError;
import it.unipr.aotlab.blogracy.logging.Logger;
import it.unipr.aotlab.blogracy.web.resolvers.ErrorPageResolver;
import it.unipr.aotlab.blogracy.web.resolvers.RequestResolver;
import it.unipr.aotlab.blogracy.web.url.URLMapper;
import org.apache.velocity.app.Velocity;
import org.gudy.azureus2.core3.config.COConfigurationManager;
import org.gudy.azureus2.core3.config.impl.ConfigurationDefaults;
import org.gudy.azureus2.core3.util.SystemProperties;
import org.gudy.azureus2.plugins.PluginException;
import org.gudy.azureus2.plugins.PluginInterface;
import org.gudy.azureus2.plugins.tracker.web.TrackerWebPageRequest;
import org.gudy.azureus2.plugins.tracker.web.TrackerWebPageResponse;
import org.gudy.azureus2.plugins.ui.config.ConfigSection;
import org.gudy.azureus2.plugins.ui.config.HyperlinkParameter;
import org.gudy.azureus2.plugins.ui.model.BasicPluginConfigModel;
import org.gudy.azureus2.ui.webplugin.WebPlugin;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Properties;


public class Blogracy extends WebPlugin {

    private URLMapper mapper = new URLMapper();

    private static final String BLOGRACY = "blogracy";
    private HyperlinkParameter test_param;
    static private Blogracy singleton;


    static class Accesses {
        static String ALL = "all";
        static String LOCAL = "local";
    }

    static private PluginInterface plugin;

    // Velocity configuration keys
    private static final String VELOCITY_RESOURCE_LOADER_PATH_KEY = "file.resource.loader.path";

    // Misc Keys
    private static final String MESSAGES_BLOGRACY_URL_KEY = "blogracy.url";
    private static final String PLUGIN_NAME_KEY = "blogracy.name";

    // blogracy.internal. keys
    private static final String CONFIG_ACCESS_KEY = "blogracy.internal.config.access";
    private static final String CONFIG_PORT_KEY = "blogracy.internal.config.port";
    private static final String DEVICE_ACCESS_KEY = "blogracy.internal.config.access";
    private static final String INTERNAL_URL_KEY = "blogracy.internal.test.url";
    private static final String DID_MIGRATE_KEY = "blogracy.internal.migrated";

    // blogracy default keys
    private static final String DEVICE_PORT_KEY = "Plugin.default.device.blogracy.port";
    private static final String DEVICE_LOCALONLY_KEY = "Plugin.default.device.blogracy.localonly";
    private static final String DEVICE_BLOGRACY_ENABLE_KEY = "Plugin.default.device.blogracy.enable";


    private static Properties defaults = new Properties();

    public static final String DSNS_PLUGIN_CHANNEL_NAME = "DSNS";

    final static int DEFAULT_PORT = 32674;
    final static String DEFAULT_ACCESS = Accesses.ALL;

    private static boolean loaded;


    static {
        ConfigurationDefaults cd = ConfigurationDefaults.getInstance();
        cd.addParameter(DID_MIGRATE_KEY, Boolean.TRUE);
        cd.addParameter(DEVICE_LOCALONLY_KEY, Boolean.TRUE);
        cd.addParameter(DEVICE_BLOGRACY_ENABLE_KEY, Boolean.TRUE);
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

        if (singletonShouldReturnImmediately()) return;
        File root_dir = createRootDirectoryIfMissingAndGetPath();

        if (COConfigurationManager.getBooleanParameter(DID_MIGRATE_KEY)) {
            configureIfMigratedKey(root_dir);
        } else {
            configureIfNotMigrateKey(root_dir);
        }

    }

    private static void configureIfNotMigrateKey(final File root_dir) {
        final Integer blogracy_port = COConfigurationManager.getIntParameter(DEVICE_PORT_KEY, DEFAULT_PORT);
        final String blogracy_access;
        if (blogracy_port != DEFAULT_PORT) {
            COConfigurationManager.setParameter(CONFIG_PORT_KEY, blogracy_port);
        }

        boolean local = COConfigurationManager.getBooleanParameter(DEVICE_LOCALONLY_KEY);
        blogracy_access = local ? Accesses.LOCAL : Accesses.ALL;
        if (!blogracy_access.equals(DEFAULT_ACCESS)) {
            COConfigurationManager.setParameter(DEVICE_ACCESS_KEY, blogracy_access);
        }
        COConfigurationManager.setParameter(DID_MIGRATE_KEY, Boolean.TRUE);
        final boolean blogracyEnable = COConfigurationManager.getBooleanParameter(DEVICE_BLOGRACY_ENABLE_KEY);
        setDefaultsProperties(blogracy_port, blogracy_access, root_dir, blogracyEnable);
    }

    private static void configureIfMigratedKey(final File root_dir) {
        final Integer blogracy_port = COConfigurationManager.getIntParameter(CONFIG_PORT_KEY, DEFAULT_PORT);
        final String blogracy_access = COConfigurationManager.getStringParameter(CONFIG_ACCESS_KEY, DEFAULT_ACCESS);
        final boolean blogracyEnable = COConfigurationManager.getBooleanParameter(DEVICE_BLOGRACY_ENABLE_KEY);
        setDefaultsProperties(blogracy_port, blogracy_access, root_dir, blogracyEnable);
    }

    private static boolean singletonShouldReturnImmediately() {
        synchronized (Blogracy.class) {
            if (loaded) {
                return true;
            } else {
                loaded = true;
            }
        }
        return false;
    }

    private static void setDefaultsProperties(final Integer blogracy_port, final String blogracy_access, final File root_dir, final boolean blogracyEnable) {
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
        File root_dir = getRootDirectory();
        return createDirIfMissing(root_dir);
    }

    public static File getRootDirectory() {
        File userPath = new File(SystemProperties.getUserPath());
        File pluginsDirectoryPath = new File(userPath, "plugins");
        return new File(pluginsDirectoryPath, BLOGRACY);
    }

    public static File getTemplateDirectory() {
        return new File(getRootDirectory(), "templates");
    }

    public static File getStaticFilesDirectory() {
        return new File(getRootDirectory(), "static");
    }

    private static File createDirIfMissing(File dir) {
        if (!dir.exists()) {
            boolean createdDir = dir.mkdir();
            assert (createdDir);
        }
        return dir;
    }

    public Blogracy() {
        super(defaults);
    }

    public String getURL() {
        return (getProtocol() + "://127.0.0.1:" + getPort() + "/");
    }


    @Override
    public boolean generateSupport(TrackerWebPageRequest request, TrackerWebPageResponse response)
            throws IOException {
        String url = request.getURL();
        try {
            final RequestResolver resolver = mapper.getResolver(url);
            resolver.resolve(request, response);
        } catch (URLMappingError e) {
            final ErrorPageResolver errorResolver = new ErrorPageResolver(e);
            errorResolver.resolve(request, response);
        } catch (ServerConfigurationError serverConfigurationError) {
            final ErrorPageResolver errorResolver = new ErrorPageResolver(
                    serverConfigurationError, HttpURLConnection.HTTP_FORBIDDEN
            );
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
    protected boolean
    isPluginEnabled() {
        return true;
    }

    @Override
    public void initialize(PluginInterface pluginInterface) throws PluginException {
        initializePluginInterface(pluginInterface);
        initializeLogger();
        initializeURLMapper();
        initVelocity();
        initializeSingleton();
        super.initialize(pluginInterface);
    }

    private void initVelocity() {
        Properties velocityProperties = new Properties();
        velocityProperties.setProperty(
                VELOCITY_RESOURCE_LOADER_PATH_KEY,
                getTemplateDirectory().getAbsolutePath()
        );
        Velocity.init(velocityProperties);
    }

    private void initializeSingleton() {
        singleton = this;
    }

    private void initializePluginInterface(final PluginInterface pluginInterface) {
        plugin = pluginInterface;
    }

    private void initializeLogger() {
        Logger.initialize(plugin, DSNS_PLUGIN_CHANNEL_NAME);
    }

    private void initializeURLMapper() throws PluginException {
        try {
            Object[] staticFileResolverParameters = new Object[]{getStaticFilesDirectory().getAbsolutePath()};
            mapper.configure(
                    "^/$", "it.unipr.aotlab.blogracy.web.resolvers.IndexResolver", null,
                    "^/css/(?:.*)$", "it.unipr.aotlab.blogracy.web.resolvers.StaticFileResolver", staticFileResolverParameters,
                    "^/scripts/(?:.*)$", "it.unipr.aotlab.blogracy.web.resolvers.StaticFileResolver", staticFileResolverParameters,
                    "^/followers$", "it.unipr.aotlab.blogracy.web.resolvers.Followers", null
            );
        } catch (ServerConfigurationError serverConfigurationError) {
            throw new PluginException(serverConfigurationError);
        }
    }
}

