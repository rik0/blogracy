/*
 * Copyright (c)  2011 Enrico Franchi, Michele Tomaiuolo and University of Parma.
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

package it.unipr.aotlab.blogracy.web.resolvers;

import it.unipr.aotlab.blogracy.errors.URLMappingError;
import org.gudy.azureus2.plugins.tracker.web.TrackerWebPageRequest;
import org.gudy.azureus2.plugins.tracker.web.TrackerWebPageResponse;

import java.io.*;
import java.util.Formatter;

/**
 * User: enrico
 * Package: it.unipr.aotlab.blogracy.web
 * Date: 11/3/11
 * Time: 10:35 AM
 */
public class ErrorPageResolver implements RequestResolver {
    private Exception exception;
    private int status;

    public ErrorPageResolver(final URLMappingError e) {
        exception = e;
        status = e.getHttpErrorStatus();
    }

    public ErrorPageResolver(final Exception e, int httpErrorStatus) {
        exception = e;
        status = httpErrorStatus;
    }

    static public String htmlErrorString(Exception e) {
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

    static public String jsonErrorString(Exception e) {
        // TODO Try and use GSON.
        final String traceString = getTraceString(e);
        final String quotedMessage = quoteDoubleQuotes(e.getMessage());
        final String quotedTrace = quoteDoubleQuotes(traceString);
        final String[] splitQuotedTrace = quotedTrace.split("\n");

        final Formatter formatter = new Formatter();
        formatter.format("{\"errorMessage\": \"%s\", ", quotedMessage);
        formatter.format("\"errorTrace\": [");
        for(final String lst : splitQuotedTrace) {
            formatter.format("\"%s\"", lst);
            if(lst != splitQuotedTrace[splitQuotedTrace.length-1]) {
                formatter.format(",\n");
            } else {
                formatter.format("\n");
            }
        }
        formatter.format("]}");
        return formatter.toString();
    }

    private static String getTraceString(final Exception e) {
        StringWriter stringWriter = new StringWriter(512);
        PrintWriter ost = new PrintWriter(stringWriter);
        e.printStackTrace(ost);
        return stringWriter.toString();
    }

    private static String quoteDoubleQuotes(final String s) {
        return s.replaceAll("\"", "\\\"");
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Notice that this specific implementation does not throw anything.
     */
    @Override
    public void resolve(final TrackerWebPageRequest request, final TrackerWebPageResponse response) {
        String errorPage = prepareOutput(request, response);
        response.setReplyStatus(status);
        write(response, errorPage);
    }

    private String prepareOutput(final TrackerWebPageRequest request, final TrackerWebPageResponse response) {
        String errorPage;
        if (isAJAXRequest(request)) {
            errorPage = jsonErrorString(exception);
            response.setContentType("text/json");
        } else {
            errorPage = htmlErrorString(exception);
            response.setContentType("text/html");
        }
        return errorPage;
    }

    private void write(final TrackerWebPageResponse response, final String errorPage) {
        Writer writer = new OutputStreamWriter(response.getOutputStream());
        try {
            writer.write(errorPage);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public HTTPStatus getRequestHTTPStatus() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isAJAXRequest(final TrackerWebPageRequest request) {
        return RequestsUtilities.isAJAXRequest(request);
    }
}
