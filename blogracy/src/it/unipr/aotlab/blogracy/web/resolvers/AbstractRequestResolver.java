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

import it.unipr.aotlab.blogracy.Blogracy;
import it.unipr.aotlab.blogracy.errors.URLMappingError;
import org.apache.velocity.Template;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.gudy.azureus2.plugins.tracker.web.TrackerWebPageRequest;
import org.gudy.azureus2.plugins.tracker.web.TrackerWebPageResponse;

import java.io.File;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.util.Map;

/**
 * User: enrico
 * Package: it.unipr.aotlab.blogracy.web
 * Date: 11/3/11
 * Time: 10:37 AM
 */
abstract public class AbstractRequestResolver implements RequestResolver {
    private File TEMPLATES_ROOT_DIRECTORY = Blogracy.getTemplateDirectory();

    private Status requestStatus = Status.INVALID;

    protected void setHTMLResponse(TrackerWebPageResponse response) {
        response.setContentType("text/html");
    }

    protected OutputStreamWriter outputWriter(final TrackerWebPageResponse response) {
        return new OutputStreamWriter(response.getOutputStream());
    }

    static protected enum Status {
        GET, POST, PUT, DELETE, INVALID
    }

    final protected Status getRequestStatus() {
        return requestStatus;
    }

    @Override
    final public void resolve(final TrackerWebPageRequest request, final TrackerWebPageResponse response)
            throws URLMappingError {
        processHeaders(request);
        response.setContentType(getViewType());
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
            requestStatus = Status.GET;
        } else if (status.startsWith("POST")) {
            requestStatus = Status.POST;
        } else if (status.startsWith("PUT")) {
            requestStatus = Status.PUT;
        } else if (status.startsWith("DELETE")) {
            requestStatus = Status.DELETE;
        }

    }

    protected void delete(final TrackerWebPageRequest request, final TrackerWebPageResponse response) throws URLMappingError {
        throw new URLMappingError(
                HttpURLConnection.HTTP_BAD_METHOD,
                "Command DELETE not supported for current resource."
        );
    }

    protected void put(final TrackerWebPageRequest request, final TrackerWebPageResponse response) throws URLMappingError {
        throw new URLMappingError(
                HttpURLConnection.HTTP_BAD_METHOD,
                "Command PUT not supported for current resource."
        );
    }

    protected void post(final TrackerWebPageRequest request, final TrackerWebPageResponse response) throws URLMappingError {
        throw new URLMappingError(
                HttpURLConnection.HTTP_BAD_METHOD,
                "Command POST not supported for current resource."
        );
    }

    protected void get(final TrackerWebPageRequest request, final TrackerWebPageResponse response) throws URLMappingError {
        throw new URLMappingError(
                HttpURLConnection.HTTP_BAD_METHOD,
                "Command GET not supported for current resource."
        );
    }

    protected Template loadTemplate()
            throws ParseErrorException, ResourceNotFoundException {
        Status currentStatus = getRequestStatus();
        String htmlViewName = getViewName();
        String templateName;
        if (hasSpecialTemplateForStatus(currentStatus, htmlViewName)) {
            templateName = buildTemplateName(currentStatus, htmlViewName);
        } else {
            templateName = htmlViewName;
        }
        return Velocity.getTemplate(templateName);
    }

    protected String buildTemplateName(Status currentStatus, String htmlViewName) {
        return currentStatus.toString() + "/" + htmlViewName;
    }

    private boolean hasSpecialTemplateForStatus(final Status currentStatus, final String htmlViewName) {
        File specialTemplateDirectory = new File(TEMPLATES_ROOT_DIRECTORY, currentStatus.toString());
        if (specialTemplateDirectory.exists()) {
            File templateFile = new File(specialTemplateDirectory, htmlViewName);
            return templateFile.exists();
        } else {
            return false;
        }
    }

    /**
     * Implements this method to specify the name of the view to use.
     * <p/>
     * A file with that name must exist either in a subdirectory named COMMAND
     * (one among GET, POST, etc.) or directly in the templates directory.
     * <p/>
     * In the former case it will be chosen only when the request is a GET, POST, etc
     * respectively. A file directly in the templates directory is used, if exists,
     * as a fallback for requests without a specific file.
     *
     * @return the name of the file.
     */
    abstract protected String getViewName();

    /**
     * Return the kind of file this resolver is going to send back
     * <p/>
     * E.g., text/html
     *
     * @return the MIME type of the file to be sent back.
     */
    abstract protected String getViewType();

}
