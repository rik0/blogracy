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

package it.unipr.aotlab.blogracy.util;

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
