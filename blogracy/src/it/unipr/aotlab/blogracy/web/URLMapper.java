package it.unipr.aotlab.blogracy.web;

import it.unipr.aotlab.blogracy.errors.URLMappingError;

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
 * <li>Have patterns start and end with ^ and $ such as "^profiles$</li>
 * <li>Patterns have to start with /: e.g., "^profiles$ is an error</li>
 * <li>It is irrelevant if patterns end with trailing /: e.g., "^/profiles$" and
 * "^/profiles/$" do the same thing</li>
 * </ol>
 */
public class URLMapper {
    List<Mapping> lst;

    /**
     * Returns the appropriate resolver for the required URL.
     *
     * @param url to be resolved.
     * @return the appropriate resolver. If exceptions are thrown, and {@link ErrorPageResolver} is returned.
     *         If no regex can match the specified URL, a {@link MissingPageResolver} is returned.
     */
    public RequestResolver getResolver(String url) {
        url = removeTrailingSlash(url);
        for (Mapping mapping : lst) {
            if (mapping.matches(url)) {
                try {
                    return mapping.buildResolver();
                } catch (URLMappingError urlMappingError) {
                    return new ErrorPageResolver(urlMappingError);
                }
            }
        }
        return new MissingPageResolver(url);
    }

    private String removeTrailingSlash(final String url) {
        if (url.endsWith("/")) {
            return url.substring(0, url.length() - 1);
        } else {
            return url;
        }
    }


    public URLMapper() {
    }

    /**
     * Configures the current URL Mapper
     *
     * @param strings is an array of strings with an even of elements.
     *                odd elements are interpreted as patterns, even elements are
     *                interpreted as the fully qualified names of the classes which
     *                resolve such URLs
     * @throws URLMappingError if the number of elements is not even,
     *                         if some pattern is not valid or if some classfile cannot be
     *                         found.
     */
    public void configure(String... strings) throws URLMappingError {
        if (checkStringsAreEven(strings)) {
            prepareList(strings);
            addMappings(strings);
        } else {
            throw new URLMappingError("Odd number of parameters has been inserted.");
        }
    }

    private void addMappings(String[] strings) throws URLMappingError {
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
