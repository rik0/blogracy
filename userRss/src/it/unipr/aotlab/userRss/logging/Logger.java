package it.unipr.aotlab.userRss.logging;

import it.unipr.aotlab.userRss.UserRSS;
import org.gudy.azureus2.plugins.PluginInterface;
import org.gudy.azureus2.plugins.logging.LoggerChannel;

/**
 * Use the static methods of this class for all the logging. It does the right thing!
 */
public class Logger {

    interface SimpleChannel {
        void info(String msg);

        void warn(String msg);

        void error(String msg);
    }

    static LoggerChannel loggerChannel = null;
    static org.gudy.azureus2.plugins.logging.Logger logger = null;
    static SimpleChannel simpleChannel = new SimpleChannel() {
        java.util.logging.Logger defaultLogger = java.util.logging.Logger.getAnonymousLogger();

        @Override
        public void info(String s) {
            defaultLogger.info(s);
        }

        @Override
        public void warn(String s) {
            defaultLogger.warning(s);
        }

        @Override
        public void error(String s) {
            defaultLogger.severe(s);
        }
    };

    public static void initialize(PluginInterface pluginInterface) {
        logger = pluginInterface.getLogger();
        loggerChannel = logger.getChannel(UserRSS.DSNS_PLUGIN_CHANNEL_NAME);
        simpleChannel = new SimpleChannel() {
            @Override
            public void info(String msg) {
                loggerChannel.logAlert(LoggerChannel.LT_INFORMATION, msg);
            }

            @Override
            public void warn(String msg) {
                loggerChannel.logAlert(LoggerChannel.LT_WARNING, msg);
            }

            @Override
            public void error(String msg) {
                loggerChannel.logAlert(LoggerChannel.LT_ERROR, msg);
            }
        };
    }

    public static void info(String msg) {
        simpleChannel.info(msg);
    }

    public static void warn(String msg) {
        simpleChannel.warn(msg);
    }

    public static void error(String msg) {
        simpleChannel.error(msg);
    }
}
