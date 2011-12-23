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
import org.gudy.azureus2.plugins.tracker.web.TrackerWebPageRequest;
import org.gudy.azureus2.plugins.tracker.web.TrackerWebPageResponse;

public interface RequestResolver {
    /**
     * Takes an input request from the webserver and a response object, processes the request and writes output to the response.
     *
     * Ah, writing servlet containers from scratch! How fun is that!
     *
     * @param request the object representing the request for the server
     * @param response the object representing the response for the server
     * @throws URLMappingError if blip happens
     */
    void resolve(TrackerWebPageRequest request, TrackerWebPageResponse response)
            throws URLMappingError;

    /**
     * Returns the kind of request we got.
     * @return the {@link HTTPRequestType} expressing the kind of request we are dealing with.
     */
    HTTPRequestType getRequestHTTPRequestType();

    /**
     * Returns if we are dealing with an AJAX call.
     * @param request the request to process.
     * @return true if the call comes from an XMLHTTPRequest object.
     */
    boolean isAJAXRequest(final TrackerWebPageRequest request);
}
