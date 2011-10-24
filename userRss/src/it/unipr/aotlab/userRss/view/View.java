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
package it.unipr.aotlab.userRss.view;


import it.unipr.aotlab.userRss.errors.BlogracyError;
import it.unipr.aotlab.userRss.errors.InvalidPluginStateException;
import it.unipr.aotlab.userRss.util.FileUtils;
import it.unipr.aotlab.userRss.util.HTMLUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.*;
import java.util.Locale;

public class View {
    static View theView = null;
    private Browser browser;
    private static final String MAIN_PAGE = "view.html";

    protected View(Composite parent) {
        buildUI(parent);
    }

    private void buildUI(Composite parent) {
        buildBrowser(parent);
    }

    private void buildBrowser(Composite parent) {
        browser = new Browser(parent, SWT.NULL);
        browser.setJavascriptEnabled(true);
        browser.setText(getPage(), true);
    }

    public static boolean createView(Composite composite) {
        if (theView != null) {
            return false;
        } else {
            theView = new View(composite);
            return true;
        }

    }

    public static View getView() throws InvalidPluginStateException {
        if(theView == null) {
            throw new InvalidPluginStateException("View not instantiated.");
        } else {
            return theView;
        }
    }

    public static boolean shouldCreateView() {
        return (theView == null);
    }

    public static void destroyView() {
        theView = null;
    }

    public void changeLanguage() {
        Locale lang = Locale.getDefault();
        throw new NotImplementedException();
    }

    public String getPage() {
        try {
            return getLocalFileContent(MAIN_PAGE);

        } catch (FileNotFoundException e) {
            return HTMLUtil.errorString(e);
        } catch (IOException e) {
            return HTMLUtil.errorString(e);
        } catch (BlogracyError e) {
            return HTMLUtil.errorString(e);

        }

    }

    public String getLocalFileContent(String fileName) throws IOException, BlogracyError {
        Class<View> cl = View.class;
        InputStream is = cl.getResourceAsStream(fileName);

        if (is == null) {
            throw new BlogracyError("Could not find " + fileName + " file");
        } else {
            StringWriter stringWriter = new StringWriter(512);
            FileUtils.copyCompletely(new InputStreamReader(is), stringWriter);
            return stringWriter.toString();
        }


    }


}
