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

package it.unipr.aotlab.blogracy.web.resolvers.staticfiles;

import it.unipr.aotlab.blogracy.errors.ServerConfigurationError;
import it.unipr.aotlab.blogracy.errors.URLMappingError;
import it.unipr.aotlab.blogracy.mime.MimeFinder;
import org.gudy.azureus2.plugins.tracker.web.TrackerWebPageRequest;
import org.gudy.azureus2.plugins.tracker.web.TrackerWebPageResponse;

import java.io.File;
import java.net.HttpURLConnection;

/**
 * User: enrico
 * Package: it.unipr.aotlab.blogracy.web.resolvers
 * Date: 11/11/11
 * Time: 5:02 PM
 */
public class StaticFileResolvers {
    static public StaticFileResolver getNullStaticFileResolver() {
        return new DummyStaticFileResolver();
    }

    static public StaticFileResolver getStaticFileResolver(File filepath)
            throws ServerConfigurationError {
        return new StaticFileResolverImpl(filepath);
    }

    static public StaticFileResolver getStaticFileResolver(File filepath, MimeFinder mimeFinder)
            throws ServerConfigurationError {
        return new StaticFileResolverImpl(filepath, mimeFinder);
    }

    private static class DummyStaticFileResolver implements StaticFileResolver {
        @Override
        public boolean couldResolve(final String url) {
            return false;
        }

        @Override
        public void resolve(final TrackerWebPageRequest request, final TrackerWebPageResponse response)
                throws URLMappingError {
            throw new URLMappingError(HttpURLConnection.HTTP_NOT_FOUND, "Static files not available.");
        }
    }
}
