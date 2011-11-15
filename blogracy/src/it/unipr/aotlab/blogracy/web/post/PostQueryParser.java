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

package it.unipr.aotlab.blogracy.web.post;

import it.unipr.aotlab.blogracy.errors.URLMappingError;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import static java.lang.System.getProperty;

/**
 * User: enrico
 * Package: it.unipr.aotlab.blogracy.web.post
 * Date: 11/15/11
 * Time: 11:00 AM
 */
public class PostQueryParser {
    public PostQuery parse(final InputStream inputStream, final Map<String, String> headers) throws IOException, URLMappingError, URISyntaxException {
        final String encoding = "ISO-8859-1"; // only valid encoding according to standard
        final PostQueryImpl postQuery = new PostQueryImpl(encoding);
        final int size = findContentLength(headers);
        final byte[] buffer = new byte[size];
        final int readBytes = inputStream.read(buffer);
        final URI uri = new URI("/main?" + new String(buffer));
        assert readBytes == size;

        // TODO: parse by hand. Their parser looks badly broken for POST data.
        final List<NameValuePair> params = URLEncodedUtils.parse(uri, getProperty("encoding"));
        for(NameValuePair pair : params) {
            postQuery.put(pair.getName(), pair.getValue().getBytes(encoding));
        }
        return postQuery;
    }

    private int findContentLength(final Map<String, String> headers) throws URLMappingError {
        final int size;
        try {
            size = Integer.valueOf(headers.get("content-length"));
        } catch (NumberFormatException e) {
            throw new URLMappingError(HttpURLConnection.HTTP_INTERNAL_ERROR, e);
        }
        return size;
    }
}
