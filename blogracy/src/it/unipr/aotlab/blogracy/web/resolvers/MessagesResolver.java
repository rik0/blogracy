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
import it.unipr.aotlab.blogracy.web.post.PostQuery;
import it.unipr.aotlab.blogracy.web.post.PostQueryParser;
import org.gudy.azureus2.plugins.tracker.web.TrackerWebPageRequest;
import org.gudy.azureus2.plugins.tracker.web.TrackerWebPageResponse;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.util.Map;

public class MessagesResolver extends AbstractRequestResolver {
    @Override
    protected String getViewType() {
        return "text/json";
    }

    @Override
    protected void post(final TrackerWebPageRequest request,
                        final TrackerWebPageResponse response) throws URLMappingError {
        final InputStream inputStream = request.getInputStream();
        final Map<String, String> headers = (Map<String,String>) request.getHeaders();
        final PostQueryParser parser = new PostQueryParser();
        try {
            final PostQuery query = parser.parse(inputStream, headers);
            String message = query.getStringValue("message");

            Logger.info(message);

        } catch (IOException e) {
            throw new URLMappingError(HttpURLConnection.HTTP_INTERNAL_ERROR, e);
        } catch (URISyntaxException e) {
            throw new URLMappingError(HttpURLConnection.HTTP_INTERNAL_ERROR, e);
        }

    }
}
