/*
 * Copyright (c)  2011  Enrico Franchi and University of Parma.
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
package net.blogracy.logging;

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
        java.util.logging.Logger defaultLogger =
                java.util.logging.Logger.getAnonymousLogger();

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

    /**
     * Initializes the default logger so that it can be used afterwards.
     * @param pluginInterface is used to get Vuze logger
     * @param pluginChannelName is the name of the channel for this plugin
     */
    public static void initialize(PluginInterface pluginInterface,
                                  String pluginChannelName) {
        logger = pluginInterface.getLogger();
        loggerChannel = logger.getTimeStampedChannel(pluginChannelName);
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
