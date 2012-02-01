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

import it.unipr.aotlab.blogracy.config.Configurations;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.gudy.azureus2.plugins.tracker.web.TrackerWebPageRequest;
import org.gudy.azureus2.plugins.tracker.web.TrackerWebPageResponse;

import java.io.File;
import java.io.OutputStreamWriter;


/**
 * User: enrico
 * Package: it.unipr.aotlab.blogracy.web
 * Date: 11/3/11
 * Time: 10:53 AM
 */

// TODO: check if I'm necessary
public class Followers extends AbstractRequestResolver {
    private String TEMPLATES_ROOT_DIRECTORY =
            Configurations.getPathConfig().getTemplatesDirectoryPath();

    @Override
    protected void get(final TrackerWebPageRequest request, final TrackerWebPageResponse response) {
        VelocityContext context = new VelocityContext();
        Template template = loadTemplate();
        template.merge(context, new OutputStreamWriter(response.getOutputStream()));
    }

    protected String getViewName() {
        return "exp.vm";

    }

    @Override
    protected String getViewType() {
        return "text/text";
    }

    protected Template loadTemplate()
            throws ParseErrorException, ResourceNotFoundException {
        HTTPRequestType currentRequestType = getRequestHTTPRequestType();
        String htmlViewName = getViewName();
        String templateName;
        if (hasSpecialTemplateForStatus(currentRequestType, htmlViewName)) {
            templateName = buildTemplateName(currentRequestType, htmlViewName);
        } else {
            templateName = htmlViewName;
        }
        return Velocity.getTemplate(templateName);
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
}
