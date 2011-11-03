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
import org.gudy.azureus2.plugins.tracker.web.TrackerWebPageRequest;
import org.gudy.azureus2.plugins.tracker.web.TrackerWebPageResponse;

/**
 * User: enrico
 * Package: it.unipr.aotlab.blogracy.web
 * Date: 11/3/11
 * Time: 10:37 AM
 */
public class AbstractRequestResolver implements RequestResolver {
    @Override
    public void resolve(final TrackerWebPageRequest request, final TrackerWebPageResponse response) throws Exception {
        if (isGET(request)) {
            get(request, response);
        } else if (isPOST(request)) {
            post(request, response);
        } else if (isPUT(request)) {
            put(request, response);
        } else if (isDELETE(request)) {
            delete(request, response);
        }
    }

    protected void delete(final TrackerWebPageRequest request, final TrackerWebPageResponse response) throws NotImplementedHTTPRequest {
        throw new NotImplementedHTTPRequest();
    }

    protected void put(final TrackerWebPageRequest request, final TrackerWebPageResponse response) throws NotImplementedHTTPRequest {
        throw new NotImplementedHTTPRequest();
    }

    protected void post(final TrackerWebPageRequest request, final TrackerWebPageResponse response) throws NotImplementedHTTPRequest {
        throw new NotImplementedHTTPRequest();
    }

    protected void get(final TrackerWebPageRequest request, final TrackerWebPageResponse response) throws NotImplementedHTTPRequest {
        throw new NotImplementedHTTPRequest();
    }

    private boolean isDELETE(final TrackerWebPageRequest request) {
        return false;
    }


    private boolean isPUT(final TrackerWebPageRequest request) {
        return false;
    }

    private boolean isPOST(final TrackerWebPageRequest request) {
        return false;
    }

    private boolean isGET(final TrackerWebPageRequest request) {
        return false;
    }
}
