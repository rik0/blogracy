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

package it.unipr.aotlab.blogracy.web;

import it.unipr.aotlab.blogracy.errors.NotImplementedHTTPRequest;
import it.unipr.aotlab.blogracy.errors.URLMappingError;
import org.gudy.azureus2.plugins.tracker.web.TrackerWebPageRequest;
import org.gudy.azureus2.plugins.tracker.web.TrackerWebPageResponse;

import java.util.Map;

/**
 * User: enrico
 * Package: it.unipr.aotlab.blogracy.web
 * Date: 11/3/11
 * Time: 10:37 AM
 */
public class AbstractRequestResolver implements RequestResolver {
    private Status requestStatus = Status.INVALID;

    static protected enum Status {
        GET, POST, PUT, DELETE, INVALID
    }

    final protected Status getRequestStatus() {
        return requestStatus;
    }

    @Override
    final public void resolve(final TrackerWebPageRequest request, final TrackerWebPageResponse response) throws Exception {
        processHeaders(request);
        switch (requestStatus) {
            case GET:
                get(request, response);
                break;
            case POST:
                post(request, response);
                break;
            case PUT:
                put(request, response);
                break;
            case DELETE:
                delete(request, response);
                break;
            case INVALID:
                throw new URLMappingError("Could not find out the kind of request we got.");
        }
    }

    private void processHeaders(final TrackerWebPageRequest request) {
        Map headers = request.getHeaders();
        String status = (String) headers.get("status");
        if (status.startsWith("GET")) {
            requestStatus = Status.GET;
        } else if (status.startsWith("POST")) {
            requestStatus = Status.POST;
        } else if (status.startsWith("PUT")) {
            requestStatus = Status.PUT;
        } else if (status.startsWith("DELETE")) {
            requestStatus = Status.DELETE;
        }

    }

    protected void delete(final TrackerWebPageRequest request, final TrackerWebPageResponse response) throws NotImplementedHTTPRequest {
        throw new NotImplementedHTTPRequest("Command DELETE not supported for current resource.");
    }

    protected void put(final TrackerWebPageRequest request, final TrackerWebPageResponse response) throws NotImplementedHTTPRequest {
        throw new NotImplementedHTTPRequest("Command PUT not supported for current resource.");
    }

    protected void post(final TrackerWebPageRequest request, final TrackerWebPageResponse response) throws NotImplementedHTTPRequest {
        throw new NotImplementedHTTPRequest("Command POST not supported for current resource.");
    }

    protected void get(final TrackerWebPageRequest request, final TrackerWebPageResponse response) throws NotImplementedHTTPRequest {
        throw new NotImplementedHTTPRequest("Command GET not supported for current resource.");
    }
}
