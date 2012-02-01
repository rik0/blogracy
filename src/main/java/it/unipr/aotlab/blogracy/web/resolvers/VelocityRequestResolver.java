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

import it.unipr.aotlab.blogracy.config.Configurations;
import it.unipr.aotlab.blogracy.errors.URLMappingError;
import it.unipr.aotlab.blogracy.web.misc.HttpResponseCode;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.gudy.azureus2.plugins.tracker.web.TrackerWebPageResponse;

import java.io.File;
import java.io.PrintStream;
import java.io.StringWriter;

/**
 * A VelocityRequestResolver should ease the creation of pages which make use of the velocity engine.
 */
public abstract class VelocityRequestResolver extends AbstractRequestResolver {
    protected VelocityContext velocityContext = new VelocityContext();
    private String TEMPLATES_ROOT_DIRECTORY =
            Configurations.getPathConfig().getTemplatesDirectoryPath();

    protected void velocityGet(final TrackerWebPageResponse response) throws URLMappingError {
        setupContext();
        Template indexTemplate = loadTemplate();
        resolveTemplate(response, indexTemplate);
    }

    /**
     * This method is called to set the appropriate values in the {@link VelocityRequestResolver#velocityContext}
     */
    protected abstract void setupContext();

    /**
     * Resolves the {@param template} using {@link VelocityRequestResolver#velocityContext}
     * and outputs it in the {@param response}
     *
     * @param response from {@link RequestResolver#resolve}
     * @param template the appropriate template (typically from {@link it.unipr.aotlab.blogracy.web.resolvers.VelocityRequestResolver#loadTemplate()}
     */
    protected void resolveTemplate(final TrackerWebPageResponse response, final Template template) {
        StringWriter writer = new StringWriter();
        template.initDocument();
        template.merge(
                velocityContext,
                writer
        );
        String text = writer.toString();
        PrintStream ps = new PrintStream(response.getOutputStream());
        ps.print(text);
    }


    protected Template loadTemplate()
            throws ParseErrorException, ResourceNotFoundException, URLMappingError {
        final HTTPRequestType currentRequestType = getRequestHTTPRequestType();
        final String htmlViewName = getViewName();
        final String templateName = getTemplateName(currentRequestType, htmlViewName);
        if (templateName == null) {
            throw new URLMappingError(
                    HttpResponseCode.HTTP_INTERNAL_ERROR,
                    "Could not resolve template name for a "
                            + currentRequestType + " and a "
                            + ((htmlViewName == null) ? "null" : htmlViewName)
                            + "view name.");
        } else {
            return Velocity.getTemplate(templateName);
        }
    }


    private String getTemplateName(final HTTPRequestType currentRequestType, final String htmlViewName) {
        final String templateName;
        if (htmlViewName != null && hasSpecialTemplateForStatus(currentRequestType, htmlViewName)) {
            templateName = buildTemplateName(currentRequestType, htmlViewName);
        } else {
            templateName = htmlViewName;
        }
        return templateName;
    }

    protected String buildTemplateName(HTTPRequestType currentRequestType, String htmlViewName) {
        return currentRequestType.toString() + "/" + htmlViewName;
    }

    private boolean hasSpecialTemplateForStatus(final HTTPRequestType currentRequestType, final String htmlViewName) {
        File specialTemplateDirectory = new File(TEMPLATES_ROOT_DIRECTORY, currentRequestType.toString());
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
}
