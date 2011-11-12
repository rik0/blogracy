package it.unipr.aotlab.blogracy.web.url;

import it.unipr.aotlab.blogracy.errors.ServerConfigurationError;
import it.unipr.aotlab.blogracy.errors.URLMappingError;
import it.unipr.aotlab.blogracy.web.resolvers.MissingPageResolver;
import it.unipr.aotlab.blogracy.web.resolvers.RequestResolver;
import it.unipr.aotlab.blogracy.web.resolvers.staticfiles.StaticFileResolver;
import it.unipr.aotlab.blogracy.web.resolvers.staticfiles.StaticFileResolvers;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * Enrico Franchi, 2011 (mc)a
 * <p/>
 * This program or module is released under the terms of the MIT license.
 * <p/>
 * User: enrico
 * Date: 11/2/11
 * Time: 11:54 AM
 */

/**
 * URLMapper provides the correct RequestResolver for the specified URL.
 * <p/>
 * URLMapper is heavily inspired by <a href="https://docs.djangoproject.com/en/dev/topics/http/urls/">Django URL dispatcher</a>.
 * <p/>
 * URLMapper is configured with a list of strings (whose semantics is specified in {@link URLMapper#configure(String...)}.
 * <p/>
 * About the individual patterns:
 * <ol>
 * <li>Have patterns start and end with ^ and $ such as "^/profiles$</li>
 * <li>Patterns have to start with /: e.g., "^profiles$ is an error</li>
 * <li>Patterns have to end without /: e.g., "^/profiles$" is ok,
 * "^/profiles/$" does not. In any case we remove the trailing slash from URLs, so it would not match</li>
 * </ol>
 */
public class URLMapper {
    List<Mapping> lst;

    private StaticFileResolver staticFilesResolver = StaticFileResolvers.getNullStaticFileResolver();

    /**
     * Returns the appropriate resolver for the required URL.
     *
     * @param url to be resolved. If the url ends with a slash, it is removed.
     * @return the appropriate resolver. If exceptions are thrown, and {@link it.unipr.aotlab.blogracy.web.resolvers.ErrorPageResolver} is returned.
     *         If no regex can match the specified URL, a {@link MissingPageResolver} is returned.
     * @throws URLMappingError          if the URL cannot be resolved.
     * @throws ServerConfigurationError if something was wrong in the configuration.
     */
    public RequestResolver getResolver(String url) throws ServerConfigurationError, URLMappingError {
        url = fixLeadingAndTrailingSlashes(url);
        return findResolver(url);

    }

    /**
     * Finds the appropriate resolver for {@param url}.
     *
     * @param url is the url to be resolved
     * @return a not null resolver
     * @throws ServerConfigurationError if something was wrong in the configurations
     * @throws URLMappingError          if the url could not be resolved
     */
    private RequestResolver findResolver(String url) throws ServerConfigurationError, URLMappingError {
        RequestResolver resolver = visitDefinedMappings(url);
        if (resolver != null) {
            return resolver;
        } else if (staticFilesResolver.couldResolve(url)) {
            return staticFilesResolver;
        }
        throw new URLMappingError(
                HttpURLConnection.HTTP_NOT_FOUND,
                "Could not find an appropriate resolver for " + url
        );
    }

    private RequestResolver visitDefinedMappings(final String url) throws ServerConfigurationError {
        // TODO this should not throw ServerConfigurationError as they should be thrown only at configuration time.
        for (Mapping mapping : lst) {
            if (mapping.matches(url)) {
                return mapping.buildResolver();
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
     * @param strings is an array of strings with an even of elements.
     *                odd elements are interpreted as patterns, even elements are
     *                interpreted as the fully qualified names of the classes which
     *                resolve such URLs
     * @throws ServerConfigurationError if the number of elements is not even,
     *                                  if some pattern is not valid or if some classfile cannot be
     *                                  found.
     */
    public void configure(String... strings) throws ServerConfigurationError {
        if (checkStringsAreEven(strings)) {
            prepareList(strings);
            addMappings(strings);
        } else {
            throw new ServerConfigurationError("Odd number of parameters has been inserted.");
        }
    }

    public void setStaticFilesDirectory(String staticRoot) throws ServerConfigurationError {
        staticFilesResolver = StaticFileResolvers.getStaticFileResolver(staticRoot);
    }

    private void addMappings(String[] strings) throws ServerConfigurationError {
        for (int nextIndex = 0; nextIndex < strings.length; nextIndex += 2) {
            lst.add(new Mapping(strings[nextIndex], strings[nextIndex + 1]));
        }
    }

    private void prepareList(String[] strings) {
        lst = new ArrayList<Mapping>(strings.length / 2);
    }

    private boolean checkStringsAreEven(String[] strings) {
        return strings.length % 2 == 0;
    }
}
