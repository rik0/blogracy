package it.unipr.aotlab.userRss;


import it.unipr.aotlab.userRss.errors.BlogracyError;
import it.unipr.aotlab.userRss.errors.InvalidPluginStateException;
import it.unipr.aotlab.userRss.util.FileUtils;
import it.unipr.aotlab.userRss.util.HTMLUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.gudy.azureus2.core3.util.FileUtil;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.*;
import java.net.URL;
import java.util.Formatter;
import java.util.Locale;

/**
 * Created by IntelliJ IDEA.
 * User: enrico
 * Date: 10/23/11
 * Time: 6:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class View {
    static View theView = null;
    private Browser browser;
    private static final String MAIN_PAGE = "view.html";
    private Text text;


    public View(Composite parent) {
        buildUI(parent);
    }

    private void buildUI(Composite parent) {
        System.out.println("Building data!");
        buildBrowser(parent);
    }

    private void buildBrowser(Composite parent) {
        browser = new Browser(parent, SWT.NULL);
        browser.setJavascriptEnabled(true);
        browser.setText(getPage(), true);
    }

    static boolean createView(Composite composite) {
        if (theView != null) {
            return false;
        } else {
            theView = new View(composite);
            return true;
        }

    }

    static View getView() {
        return theView;
    }

    static boolean shouldCreateView() {
        return (theView == null);
    }

    static void destroyView() {
        theView = null;
    }

    void changeLanguage() {
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
