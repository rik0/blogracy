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

package it.unipr.aotlab.blogracy.view;

import it.unipr.aotlab.blogracy.errors.InvalidPluginStateException;
import it.unipr.aotlab.blogracy.logging.Logger;
import org.eclipse.swt.widgets.Composite;
import org.gudy.azureus2.ui.swt.plugins.UISWTViewEvent;
import org.gudy.azureus2.ui.swt.plugins.UISWTViewEventListener;


public class ViewListener implements UISWTViewEventListener {

    @Override
    public boolean eventOccurred(UISWTViewEvent uiswtViewEvent) {
        switch (uiswtViewEvent.getType()) {
            case UISWTViewEvent.TYPE_CREATE:
                return ViewFactory.shouldCreateView();
            case UISWTViewEvent.TYPE_DESTROY:
                ViewFactory.destroyView();
                return true;
            case UISWTViewEvent.TYPE_INITIALIZE:
                Composite composite = (Composite)uiswtViewEvent.getData();
                return ViewFactory.createView(composite);
            case UISWTViewEvent.TYPE_LANGUAGEUPDATE:
                try {
                    View theView = ViewFactory.getView();
                    theView.changeLanguage();
                    return true;
                } catch (InvalidPluginStateException e) {
                    Logger.error(e.getMessage());
                    return false;
                }
            case UISWTViewEvent.TYPE_REFRESH:
                return true;
        }

        return true;
    }
}
