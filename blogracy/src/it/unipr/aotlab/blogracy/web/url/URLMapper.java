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

package it.unipr.aotlab.blogracy.web.url;

import it.unipr.aotlab.blogracy.errors.ServerConfigurationError;
import it.unipr.aotlab.blogracy.errors.URLMappingError;
import it.unipr.aotlab.blogracy.web.resolvers.RequestResolver;
import it.unipr.aotlab.blogracy.web.resolvers.StaticFileResolver;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

public class URLMapper {
    private List<Mapping> lst;

    private StaticFileResolver staticFilesResolver = null;
    private final int ARGUMENT_LIST_MANDATORY_DIVISOR = 3;

    /**
     * Returns the appropriate resolver for the required URL.
     *
     * @param url to be resolved. If the url ends with a slash, it is removed.
     * @return the appropriate resolver.
     * @throws URLMappingError          if the URL cannot be resolved by any resolver.
     * @throws ServerConfigurationError if something was wrong in the configuration, e.g., we have a matching
     *                                  link, but we cannot instantiate the correspondingly specified class.
     */
    public RequestResolver getResolver(String url) throws ServerConfigurationError, URLMappingError {
        url = fixLeadingAndTrailingSlashes(url);
        return findResolver(url);

    }

    private RequestResolver findResolver(String url) throws ServerConfigurationError, URLMappingError {
        RequestResolver resolver = buildResolver(url);
        if (resolver != null) {
            return resolver;
        } else if (staticFilesResolver != null && staticFilesResolver.couldResolve(url)) {
            return staticFilesResolver;
        }
        throw new URLMappingError(
                HttpURLConnection.HTTP_NOT_FOUND,
                "Could not find an appropriate resolver for " + url
        );
    }

    private RequestResolver buildResolver(final String url) throws ServerConfigurationError {
        for (Mapping mapping : lst) {
            RequestResolver resolver = mapping.buildResolver(url);
            if (resolver != null) {
                return resolver;
            }
        }
        return null;
    }

    /**
     * Return a new url with the last trailing slash removed (if present) and a leading
     * slash added (if missing)
     *
     * @param url the starting url
     * @return the fixed url
     */
    private String fixLeadingAndTrailingSlashes(final String url) {
        return fixLeadingSlash(fixTrailingSlash(url));
    }

    private String fixLeadingSlash(final String url) {
        if (url.startsWith("/")) {
            return url;
        } else {
            return "/" + url;
        }
    }

    private String fixTrailingSlash(final String url) {
        if (url.endsWith("/")) {
            return url.substring(0, url.length() - 1);
        } else {
            return url;
        }
    }

    /**
     * Configures the current URL Mapper
     *
     * @param parameters is an array of parameters with a multiple of three elements.
     *                   they essentially constitute triplets where:
     *                   <ol>
     *                   <li>the first element is the regex used to specify which urls should be matched by
     *                   the triplet.</li>
     *                   <li>the second element is the fully qualified name of the class that should resolve
     *                   the url if requested. Such classes <b>must</b> implement the {@link RequestResolver}
     *                   interface</li>
     *                   <li>the third argument is an array of objects that are passed as the first parameters
     *                   to the constructor of the resolver. null is a valid value and is converted to the
     *                   empty array.</li>
     *                   </ol>
     * @throws ServerConfigurationError if the number of elements is not even,
     *                                  if some pattern is not valid or if some classfile cannot be
     *                                  found.
     */
    public void configure(Object... parameters) throws ServerConfigurationError {
        if (checkRightArgumentsNumber(parameters)) {
            prepareList(parameters);
            addMappings(parameters);
        } else {
            throw new ServerConfigurationError("Odd number of parameters has been inserted.");
        }
    }

    public void setStaticFilesDirectory(String staticRoot) throws ServerConfigurationError {
        staticFilesResolver = new StaticFileResolver(staticRoot);
    }

    private void addMappings(Object[] strings) throws ServerConfigurationError {
        for (int nextIndex = 0; nextIndex < strings.length;
             nextIndex += ARGUMENT_LIST_MANDATORY_DIVISOR) {
            try {
                String urlRegex = (String) strings[nextIndex];
                String resolverClassName = (String) strings[nextIndex + 1];
                Object[] startingParameters = (Object[]) strings[nextIndex + 2];
                addMapping(urlRegex, resolverClassName, startingParameters);
            } catch (ClassCastException e) {
                throw new ServerConfigurationError(e);
            }

        }
    }

    private void addMapping(final String urlRegex,
                            final String resolverClassName,
                            final Object[] startingParameters) throws ServerConfigurationError {
        lst.add(new Mapping(urlRegex, resolverClassName, startingParameters));
    }

    private void prepareList(Object[] strings) {
        lst = new ArrayList<Mapping>(strings.length / ARGUMENT_LIST_MANDATORY_DIVISOR);
    }

    private boolean checkRightArgumentsNumber(Object[] strings) {
        return strings.length % ARGUMENT_LIST_MANDATORY_DIVISOR == 0;
    }
}
