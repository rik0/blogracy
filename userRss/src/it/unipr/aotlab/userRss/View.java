package it.unipr.aotlab.userRss;


import it.unipr.aotlab.userRss.errors.InvalidPluginStateException;
import it.unipr.aotlab.userRss.util.FileUtils;
import it.unipr.aotlab.userRss.util.HTMLUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
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
    private static final String MAIN_PAGE = "viw.html";
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
        if(theView != null) {
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
            return HTMLUtil.errorString(e);
        } catch (FileNotFoundException e) {
            return HTMLUtil.errorString(e);
        } catch (IOException e) {
            return HTMLUtil.errorString(e);
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


}
