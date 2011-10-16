/*
 * Copyright (c)  2011 Alan Nonnato, Enrico Franchi, Michele Tomaiuolo and University of Parma.
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
package it.unipr.aotlab.userRss;


import org.gudy.azureus2.plugins.Plugin;
import org.gudy.azureus2.plugins.PluginException;
import org.gudy.azureus2.plugins.PluginInterface;
import org.gudy.azureus2.plugins.logging.Logger;
import org.gudy.azureus2.plugins.ui.UIInstance;
import org.gudy.azureus2.plugins.ui.UIManagerListener;
import org.gudy.azureus2.ui.swt.plugins.UISWTInstance;


public class UserRSS implements Plugin {

    private PluginInterface plugin;

    final String DSNS_PLUGIN_CHANNEL_NAME = "DSNS";
    final String PLUGIN_NAME = "blogracy.name";

    private View cView = null;
    private UISWTInstance swtInstance = null;

    public Logger getLogger() {
        return logger;
    }

    private Logger logger;


    @Override
    public void initialize(PluginInterface pluginInterface) throws PluginException {
        this.plugin = pluginInterface;
        cView = new View(this, this.plugin);

        logger = pluginInterface.getLogger();

        this.plugin.getUIManager().addUIListener(new UIManagerListener() {
            public void UIAttached(UIInstance instance) {
                if (instance instanceof UISWTInstance) {
                    swtInstance = ((UISWTInstance) instance);

                    if (cView != null) {
                        swtInstance.addView(UISWTInstance.VIEW_MAIN, PLUGIN_NAME, cView);
                        swtInstance.openMainView(PLUGIN_NAME, cView, null);
                    }
                }
            }

            public void UIDetached(UIInstance instance) {
                if (instance instanceof UISWTInstance) {
                    logger.getChannel("info").log("Unloaded plugin.");
                }

            }
        });

    }
}

