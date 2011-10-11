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
import org.gudy.azureus2.plugins.ui.UIInstance;
import org.gudy.azureus2.plugins.ui.UIManagerListener;
import org.gudy.azureus2.ui.swt.plugins.UISWTInstance;


public class UserRSS implements Plugin {

    private PluginInterface pluginInterface;
    private static final String VIEWID = "User RSS - Tesi Laurea";
    public static final String CATEGORY_FEED_DESC = "cat";
    private View cView = null;
    private UISWTInstance swtInstance = null;


    @Override
    public void initialize(PluginInterface plgnInterface) throws PluginException {
        pluginInterface = plgnInterface;
        cView = new View(this, pluginInterface);

        this.pluginInterface.getUIManager().addUIListener(new UIManagerListener() {
            public void UIAttached(UIInstance instance) {
                if (instance instanceof UISWTInstance) {
                    swtInstance = ((UISWTInstance) instance);

                    if (cView != null) {
                        //add a menu item
                        swtInstance.addView(UISWTInstance.VIEW_MAIN, VIEWID, cView);
                        //start the plugin when vuze start
                        swtInstance.openMainView(VIEWID, cView, null);
                    }
                }
            }

            public void UIDetached(UIInstance instance) {
                if (instance instanceof UISWTInstance)
                    instance = null;
            }
        });

    }
}

