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

import it.unipr.aotlab.blogracy.config.Configurations;
import it.unipr.aotlab.blogracy.errors.ServerConfigurationError;
import it.unipr.aotlab.blogracy.errors.URLMappingError;
import it.unipr.aotlab.blogracy.logging.Logger;
import it.unipr.aotlab.blogracy.web.misc.HttpResponseCode;
import it.unipr.aotlab.blogracy.web.resolvers.ErrorPageResolver;
import it.unipr.aotlab.blogracy.web.resolvers.RequestResolver;
import it.unipr.aotlab.blogracy.web.url.URLMapper;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogChute;
import org.gudy.azureus2.core3.config.COConfigurationManager;
import org.gudy.azureus2.core3.config.impl.ConfigurationDefaults;
import org.gudy.azureus2.plugins.PluginException;
import org.gudy.azureus2.plugins.PluginInterface;
import org.gudy.azureus2.plugins.ddb.DistributedDatabase;
import org.gudy.azureus2.plugins.ddb.DistributedDatabaseEvent;
import org.gudy.azureus2.plugins.ddb.DistributedDatabaseException;
import org.gudy.azureus2.plugins.ddb.DistributedDatabaseListener;
import org.gudy.azureus2.plugins.download.Download;
import org.gudy.azureus2.plugins.download.DownloadException;
import org.gudy.azureus2.plugins.logging.LoggerChannel;
import org.gudy.azureus2.plugins.torrent.Torrent;
import org.gudy.azureus2.plugins.torrent.TorrentException;
import org.gudy.azureus2.plugins.tracker.web.TrackerWebPageRequest;
import org.gudy.azureus2.plugins.tracker.web.TrackerWebPageResponse;
import org.gudy.azureus2.plugins.ui.config.ConfigSection;
import org.gudy.azureus2.plugins.ui.config.HyperlinkParameter;
import org.gudy.azureus2.plugins.ui.model.BasicPluginConfigModel;
import org.gudy.azureus2.plugins.utils.resourcedownloader.ResourceDownloader;
import org.gudy.azureus2.plugins.utils.resourcedownloader.ResourceDownloaderException;
import org.gudy.azureus2.ui.webplugin.WebPlugin;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;


public class Blogracy extends WebPlugin {

    private URLMapper mapper = new URLMapper();

    private HyperlinkParameter testParam;
    static private Blogracy singleton;

    static private final String URL_CONFIG_NAME = "blogracyUrl.config";

    static class Accesses {
        static String ALL = "all";
        static String LOCAL = "local";

        static String wantsLocal(boolean local) {
            return local ? Accesses.LOCAL : Accesses.ALL;
        }
    }

    static private PluginInterface plugin;

    // Misc Keys
    private static final String MESSAGES_BLOGRACY_URL_KEY = "blogracy.url";

    // blogracy.internal. keys
    private static final String INTERNAL_CONFIG_ACCESS_KEY = "blogracy.internal.config.access";
    private static final String INTERNAL_CONFIG_PORT_KEY = "blogracy.internal.config.port";
    private static final String INTERNAL_TEST_URL_KEY = "blogracy.internal.test.url";
    private static final String INTERNAL_MIGRATED_KEY = "blogracy.internal.migrated";

    // blogracy default keys
    private static final String PLUGIN_DEFAULT_DEVICE_BLOGRACY_PORT = "Plugin.default.device.blogracy.port";
    private static final String PLUGIN_DEFAULT_DEVICE_BLOGRACY_LOCALONLY = "Plugin.default.device.blogracy.localonly";
    private static final String PLUGIN_DEFAULT_DEVICE_BLOGRACY_ENABLE = "Plugin.default.device.blogracy.enable";

    private static Properties defaults = new Properties();

    public static final String DSNS_PLUGIN_CHANNEL_NAME = "DSNS";

    final static int DEFAULT_PORT = 32674;
    final static String DEFAULT_ACCESS = Accesses.ALL;

    private static boolean loaded;


    static {
        ConfigurationDefaults cd = ConfigurationDefaults.getInstance();
        cd.addParameter(INTERNAL_MIGRATED_KEY, Boolean.TRUE);
        cd.addParameter(PLUGIN_DEFAULT_DEVICE_BLOGRACY_LOCALONLY, Boolean.TRUE);
        cd.addParameter(PLUGIN_DEFAULT_DEVICE_BLOGRACY_ENABLE, Boolean.TRUE);
    }

    public static Blogracy getSingleton() {
        return (singleton);
    }

    @Override
    protected void initStage(int num) {
        if (num == 1) {
            BasicPluginConfigModel config = getConfigModel();
            testParam = config.addHyperlinkParameter2(INTERNAL_TEST_URL_KEY, "");
            testParam.setEnabled(isPluginEnabled());
            testParam.setLabelKey(MESSAGES_BLOGRACY_URL_KEY);
        }
    }

    /**
     * This method is effectively called when a new instance of this plugin
     * should be created.
     * <p/>
     * We found this feature not documented. Perhaps we should not rely
     * on that. We also ensure that Blogracy is a singleton.
     *
     * @param pluginInterface is the same old access point for plugins
     */
    public static void load(PluginInterface pluginInterface) {

        if (singletonShouldReturnImmediately()) return;
        File rootDir = createRootDirectoryIfMissingAndGetPath();

        if (COConfigurationManager.getBooleanParameter(INTERNAL_MIGRATED_KEY)) {
            configureIfMigratedKey(rootDir);
        } else {
            configureIfNotMigrateKey(rootDir);
        }

    }

    private static void configureIfNotMigrateKey(final File rootDir) {
        final Integer blogracyPort = COConfigurationManager.getIntParameter(
                PLUGIN_DEFAULT_DEVICE_BLOGRACY_PORT,
                DEFAULT_PORT
        );
        if (blogracyPort != DEFAULT_PORT) {
            COConfigurationManager.setParameter(
                    INTERNAL_CONFIG_PORT_KEY,
                    blogracyPort
            );
        }

        boolean local = COConfigurationManager.getBooleanParameter(
                PLUGIN_DEFAULT_DEVICE_BLOGRACY_LOCALONLY
        );
        final String blogracyAccess = Accesses.wantsLocal(local);
        if (!blogracyAccess.equals(DEFAULT_ACCESS)) {
            COConfigurationManager.setParameter(
                    INTERNAL_CONFIG_ACCESS_KEY,
                    blogracyAccess
            );
        }
        COConfigurationManager.setParameter(
                INTERNAL_MIGRATED_KEY,
                Boolean.TRUE
        );

        final boolean blogracyEnable =
                COConfigurationManager.getBooleanParameter(
                        PLUGIN_DEFAULT_DEVICE_BLOGRACY_ENABLE
                );
        setDefaultsProperties(blogracyPort, blogracyAccess, rootDir, blogracyEnable);
    }

    private static void configureIfMigratedKey(final File root_dir) {
        final Integer blogracy_port = COConfigurationManager.getIntParameter(
                INTERNAL_CONFIG_PORT_KEY,
                DEFAULT_PORT
        );
        final String blogracy_access =
                COConfigurationManager.getStringParameter(
                        INTERNAL_CONFIG_ACCESS_KEY,
                        DEFAULT_ACCESS
                );
        final boolean blogracyEnable =
                COConfigurationManager.getBooleanParameter(
                        PLUGIN_DEFAULT_DEVICE_BLOGRACY_ENABLE
                );
        setDefaultsProperties(
                blogracy_port,
                blogracy_access,
                root_dir,
                blogracyEnable
        );
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

    private static void setDefaultsProperties(
            final Integer blogracyPort,
            final String blogracyAccess,
            final File rootDir,
            final boolean blogracyEnable) {
        defaults.put(WebPlugin.PR_ENABLE, blogracyEnable);
        defaults.put(WebPlugin.PR_DISABLABLE, Boolean.TRUE);
        defaults.put(WebPlugin.PR_PORT, blogracyPort);
        defaults.put(WebPlugin.PR_ACCESS, blogracyAccess);
        defaults.put(WebPlugin.PR_ROOT_DIR, rootDir.getAbsolutePath());
        defaults.put(WebPlugin.PR_ENABLE_KEEP_ALIVE, Boolean.TRUE);
        defaults.put(WebPlugin.PR_HIDE_RESOURCE_CONFIG, Boolean.TRUE);
        defaults.put(WebPlugin.PR_PAIRING_SID, Configurations.BLOGRACY);

        defaults.put(WebPlugin.PR_CONFIG_MODEL_PARAMS,
                new String[]{
                        ConfigSection.SECTION_ROOT,
                        Configurations.BLOGRACY
                }
        );
    }

    private static File createRootDirectoryIfMissingAndGetPath() {
        File root_dir = new File(
                Configurations.getPathConfig()
                        .getRootDirectoryPath()
        );
        return createDirIfMissing(root_dir);
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
    public boolean generateSupport(
            TrackerWebPageRequest request,
            TrackerWebPageResponse response)
            throws IOException {
        String url = request.getURL();
        try {
            final RequestResolver resolver = mapper.getResolver(url);
            resolver.resolve(request, response);
        } catch (URLMappingError urlMappingError) {
            final ErrorPageResolver errorPageResolver = new ErrorPageResolver(
                    urlMappingError
            );
            Logger.error(urlMappingError.getMessage());
            urlMappingError.printStackTrace();
            errorPageResolver.resolve(request, response);
        } catch (ServerConfigurationError serverConfigurationError) {
            final ErrorPageResolver errorPageResolver = new ErrorPageResolver(
                    serverConfigurationError,
                    HttpResponseCode.HTTP_INTERNAL_ERROR
            );
            Logger.error(serverConfigurationError.getMessage());
            serverConfigurationError.printStackTrace();
            errorPageResolver.resolve(request, response);
        } catch (RuntimeException genericException) {
            final ErrorPageResolver errorPageResolver = new ErrorPageResolver(
                    genericException,
                    HttpResponseCode.HTTP_INTERNAL_ERROR
            );
            Logger.error(genericException.getMessage());
            genericException.printStackTrace();
            try {
                errorPageResolver.resolve(request, response);
            } catch (RuntimeException e) {
                e.printStackTrace();
                throw e;
            }
        }
        return true;
    }


    @Override
    protected void
    setupServer() {
        super.setupServer();

        if (testParam != null) {
            testParam.setEnabled(isPluginEnabled());
            testParam.setHyperlink(getURL());
        }
    }

    @Override
    protected boolean
    isPluginEnabled() {
        return true;
    }

    @Override
    public void initialize(PluginInterface pluginInterface)
            throws PluginException {
        initializePluginInterface(pluginInterface);
        initializeLogger();
        initializeURLMapper();
        initVelocity();
        initializeSingleton();
        super.initialize(pluginInterface);
    }

    private void initVelocity() {
        Properties velocityProperties = new Properties();
        // TODO: here we are not using the deployed files!
        velocityProperties.setProperty(
                Velocity.FILE_RESOURCE_LOADER_PATH,
                Configurations.getPathConfig().getTemplatesDirectoryPath()
        );
        Velocity.setProperty(Velocity.FILE_RESOURCE_LOADER_CACHE, false);
        Velocity.setProperty(Velocity.RUNTIME_LOG_LOGSYSTEM, new LogChute() {
            @Override
            public void init(final RuntimeServices rs) throws Exception {
                // do nothing
            }

            @Override
            public void log(final int level, final String message) {
                switch (level) {
                    case DEBUG_ID:
                    case TRACE_ID:
                    case INFO_ID:
                        Logger.info(message);
                        break;
                    case WARN_ID:
                        Logger.warn(message);
                        break;
                    case ERROR_ID:
                        Logger.error(message);
                        break;
                }
            }

            @Override
            public void log(
                    final int level,
                    final String message,
                    final Throwable t) {
                final String fullMessage = message + t.getMessage();
                log(level, message);
            }

            @Override
            public boolean isLevelEnabled(final int level) {
                return true;
            }
        });
        Velocity.init(velocityProperties);
    }

    private void initializeSingleton() {
        singleton = this;
    }

    private void initializePluginInterface(
            final PluginInterface pluginInterface) {
        plugin = pluginInterface;        
    }

    private void initializeLogger() {
        Logger.initialize(plugin, DSNS_PLUGIN_CHANNEL_NAME);
    }

    private void initializeURLMapper() throws PluginException {
        try {
            ClassLoader classLoader = ClassLoader.getSystemClassLoader();
            mapper.configure(
                   new InputStreamReader(
                           classLoader.getResourceAsStream(URL_CONFIG_NAME)
                   )
            );
        } catch (ServerConfigurationError serverConfigurationError) {
            throw new PluginException(serverConfigurationError);
        }
    }
    
    // TODO: probably to move to Network and implementing classes
    public void addDownload(final String fileHash, final String downloadDirectory, final String fileName) {
    	// add magnet-uri to download manager
		try {
	    	URL magnetURI = new URL("magnet:?xt=urn:btih:" + fileHash);
            ResourceDownloader rdl = plugin.getUtilities().getResourceDownloaderFactory().create(magnetURI);
            InputStream is = rdl.download();
            Torrent torrent = plugin.getTorrentManager().createFromBEncodedInputStream(is);
        	Download download = plugin.getDownloadManager().addDownload(torrent, null, new File(downloadDirectory));
        	download.renameDownload(fileName);
            plugin.getLogger().getTimeStampedChannel("Blogracy").logAlert(LoggerChannel.LT_INFORMATION, fileHash + " added to download list!");
		} catch (MalformedURLException e1) {
			plugin.getLogger().getTimeStampedChannel("Blogracy").logAlert(LoggerChannel.LT_ERROR,"Error generating MagnetURI for: " + fileHash);
		} catch (ResourceDownloaderException e1) {
			plugin.getLogger().getTimeStampedChannel("Blogracy").logAlert(LoggerChannel.LT_ERROR, "Resource download exception for: " + fileHash);
		} catch (TorrentException e1) {
			plugin.getLogger().getTimeStampedChannel("Blogracy").logAlert(LoggerChannel.LT_ERROR, "Torrent exception for: " + fileHash);
        } catch (DownloadException e1) {
			plugin.getLogger().getTimeStampedChannel("Blogracy").logAlert(LoggerChannel.LT_ERROR, "Download exception for: " + fileHash);
		}
    }
    
    // TODO: probably to move to Network and implementing classes
    public void addIndirectDownload(final String key, final String downloadDirectory) {
    	final long TIMEOUT = 5 * 60 * 1000; // 5 mins
    	DistributedDatabase ddb = plugin.getDistributedDatabase();
		try {
			ddb.read(
	            new DistributedDatabaseListener() {
	            	public void event(DistributedDatabaseEvent event) {
	            		int	type = event.getType();
	            		if (type == DistributedDatabaseEvent.ET_OPERATION_COMPLETE) {
	            			// ...
	            		} else if (type == DistributedDatabaseEvent.ET_OPERATION_TIMEOUT) {
	            			// ...
	            		} else if (type == DistributedDatabaseEvent.ET_VALUE_READ) {
	            			try {
	            				String fileHash = (String)event.getValue().getValue(String.class);
	            				int btih = fileHash.indexOf("xt=urn:btih:");
	            				if (btih >= 0) {
	            					fileHash = fileHash.substring(btih + "xt=urn:btih:".length());
		            				int amp = fileHash.indexOf('&');
		            				if (amp >= 0) fileHash = fileHash.substring(0, amp);
	            				}
            					addDownload(fileHash, downloadDirectory, fileHash + ".rss");
	            			} catch (DistributedDatabaseException e) {
	            				plugin.getLogger().getTimeStampedChannel("Blogracy").logAlert(LoggerChannel.LT_ERROR, "Error retrieving a value from the DDB: " + key);
	            			}
	            		}
	            	}				
			    },
                ddb.createKey(key.getBytes()),
                TIMEOUT,
                DistributedDatabase.OP_EXHAUSTIVE_READ
			);
		} catch (DistributedDatabaseException e) {
			plugin.getLogger().getTimeStampedChannel("Blogracy").logAlert(LoggerChannel.LT_ERROR, "Problem reading from the DDB");
		}

    }
}

