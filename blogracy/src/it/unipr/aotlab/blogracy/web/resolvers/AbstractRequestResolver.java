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

import java.net.HttpURLConnection;
import java.util.Map;

/**
 * An AbstractRequestResolver abstracts the idea of resolving requests according to the
 * appropriate HTTP commands (e.g.,
 * {@link AbstractRequestResolver#get(org.gudy.azureus2.plugins.tracker.web.TrackerWebPageRequest,
 * org.gudy.azureus2.plugins.tracker.web.TrackerWebPageResponse)} for get)
 */
abstract public class AbstractRequestResolver implements RequestResolver {

    private HTTPStatus requestHTTPStatus = HTTPStatus.INVALID;


    /**
     * @return the type of the HTTP request.
     */
    @Override
    public final HTTPStatus getRequestHTTPStatus() {
        return requestHTTPStatus;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    final public void resolve(final TrackerWebPageRequest request, final TrackerWebPageResponse response)
            throws URLMappingError {
        processHeaders(request);
        response.setContentType(getViewType());
        switch (requestHTTPStatus) {
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
                throw new URLMappingError(
                        HttpURLConnection.HTTP_BAD_METHOD,
                        "Could not find out the kind of request we got."
                );
        }
    }

    private void processHeaders(final TrackerWebPageRequest request) {
        Map headers = request.getHeaders();
        String status = (String) headers.get("status");
        if (status.startsWith("GET")) {
            requestHTTPStatus = HTTPStatus.GET;
        } else if (status.startsWith("POST")) {
            requestHTTPStatus = HTTPStatus.POST;
        } else if (status.startsWith("PUT")) {
            requestHTTPStatus = HTTPStatus.PUT;
        } else if (status.startsWith("DELETE")) {
            requestHTTPStatus = HTTPStatus.DELETE;
        }

    }

    /**
     * Answers to a DELETE HTTP command
     *
     * @param request  the request for the client
     * @param response the response from the server
     * @throws URLMappingError if something goes bad
     */
    protected void delete(final TrackerWebPageRequest request, final TrackerWebPageResponse response) throws URLMappingError {
        throw new URLMappingError(
                HttpURLConnection.HTTP_BAD_METHOD,
                "Command DELETE not supported for current resource."
        );
    }

    /**
     * Answers to a PUT HTTP command
     *
     * @param request  the request for the client
     * @param response the response from the server
     * @throws URLMappingError if something goes bad
     */
    protected void put(final TrackerWebPageRequest request, final TrackerWebPageResponse response) throws URLMappingError {
        throw new URLMappingError(
                HttpURLConnection.HTTP_BAD_METHOD,
                "Command PUT not supported for current resource."
        );
    }

    /**
     * Answers to a POST HTTP command
     *
     * @param request  the request for the client
     * @param response the response from the server
     * @throws URLMappingError if something goes bad
     */
    protected void post(final TrackerWebPageRequest request, final TrackerWebPageResponse response) throws URLMappingError {
        throw new URLMappingError(
                HttpURLConnection.HTTP_BAD_METHOD,
                "Command POST not supported for current resource."
        );
    }

    /**
     * Answers to a GET HTTP command
     *
     * @param request  the request for the client
     * @param response the response from the server
     * @throws URLMappingError if something goes bad
     */
    protected void get(final TrackerWebPageRequest request, final TrackerWebPageResponse response) throws URLMappingError {
        throw new URLMappingError(
                HttpURLConnection.HTTP_BAD_METHOD,
                "Command GET not supported for current resource."
        );
    }

    /**
     * Return the kind of file this resolver is going to send back
     * <p/>
     * E.g., text/html
     *
     * @return the MIME type of the file to be sent back.
     */
    abstract protected String getViewType();

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAJAXRequest(final TrackerWebPageRequest request) {
        Map headers = request.getHeaders();
        String xRequestedWith = (String)headers.get("X-Requested-With");
        return xRequestedWith != null && xRequestedWith.equals("XMLHttpRequest");
    }
}
