package it.unipr.aotlab.userRss.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Formatter;

/**
 * Created by IntelliJ IDEA.
 * User: enrico
 * Date: 10/23/11
 * Time: 11:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class HTMLUtil {
    static public String errorString(Exception e) {
        // TODO: make page more beautiful!
        Formatter formatter = new Formatter();
        String headerPage = "<html><head><title>Error!</title></head><body>";
        String pageTitle = "<h1>Error: %s</h1>";
        String pageMessage = "<p>%s</p>";
        String pageTrace = "<pre>%s</pre>";
        String pageFooter = "</body></html>";

        formatter.format(headerPage);
        formatter.format(pageTitle, e.toString());
        formatter.format(pageMessage, e.getMessage());

        StringWriter stringWriter = new StringWriter(512);
        PrintWriter ost = new PrintWriter(stringWriter);
        e.printStackTrace(ost);
        formatter.format(pageTrace, stringWriter.toString());
        formatter.format(pageFooter);

        return formatter.toString();
    }
}
