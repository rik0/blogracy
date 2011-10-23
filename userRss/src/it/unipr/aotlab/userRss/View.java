package it.unipr.aotlab.userRss;


import com.sun.tools.internal.xjc.Language;
import it.unipr.aotlab.userRss.errors.InvalidPluginStateException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.gudy.azureus2.plugins.logging.LoggerChannel;
import org.pf.file.FileUtil;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.*;
import java.net.URL;
import java.nio.Buffer;
import java.text.Format;
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
        try {
            UserRSS.logError("View!");
        } catch (InvalidPluginStateException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
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
        browser.setSize(600, 600);
    }

    static boolean createView(Composite composite) {
        if(theView == null) {
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
            String fileName = getMainPagePath();
            return getLocalFileContent(fileName);
        } catch (InvalidPluginStateException e) {
            return errorString(e);
        } catch (FileNotFoundException e) {
            return errorString(e);
        } catch (IOException e) {
            return errorString(e);
        }

    }

    public String getLocalFileContent(String fileName) throws IOException {
        BufferedWriter outString = new BufferedWriter(new StringWriter(512));
        BufferedReader fileReader = new BufferedReader(new FileReader(fileName));
        FileUtils.copyTextualFile(outString, fileReader);
        return outString.toString();
    }

    private String getMainPagePath() throws InvalidPluginStateException {
        ClassLoader cl = UserRSS.getCurrentClassLoader();
        URL resourceUrl = cl.getResource(MAIN_PAGE);
        return resourceUrl.getPath();
    }

    public String errorString(Exception e) {
        // TODO: make page more beautiful!
        Formatter formatter = new Formatter();
        String headerPage = "<html><head><title>Error!</title></head><body>";
        String pageTitle = "<h1>Error: %s<h1>";
        String pageMessage = "<p>%s</p>";
        String pageTrace = "<p>%s</p>";
        String pageFooter = "</body></html>";

        formatter.format(headerPage);
        formatter.format(pageTitle, e.toString());
        formatter.format(pageMessage, e.getMessage());

        PrintWriter ost = new PrintWriter(new StringWriter(512));
        e.printStackTrace(ost);
        formatter.format(pageTrace, ost.toString());
        return formatter.toString();
    }
}
