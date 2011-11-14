/*
 * Copyright (c)  2011 Enrico Franchi.
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
import it.unipr.aotlab.blogracy.logging.Logger;
import org.gudy.azureus2.plugins.tracker.web.TrackerWebPageRequest;
import org.gudy.azureus2.plugins.tracker.web.TrackerWebPageResponse;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Map;

public class MessagesResolver extends AbstractRequestResolver {
    @Override
    protected String getViewType() {
        return "text/json";
    }

    @Override
    protected void post(final TrackerWebPageRequest request,
                        final TrackerWebPageResponse response) throws URLMappingError {
        Logger.info(request.getURL());
        final InputStream inputStream = request.getInputStream();
        byte[] buffer = new byte[4028];
        try {
            System.out.println("Started reading.");
            while(inputStream.read(buffer) != -1) {
                    System.out.write(buffer);
            }
            System.out.println("Ended reading.");


            final Map<String, String> headers = request.getHeaders();
            for(Map.Entry<String, String> pair : headers.entrySet()) {
                System.out.println("<" + pair.getKey() + ": " + pair.getValue() + ">");
            }
            inputStream.close();

        } catch (IOException e) {
            throw new URLMappingError(HttpURLConnection.HTTP_INTERNAL_ERROR, e);
        }

    }
}
