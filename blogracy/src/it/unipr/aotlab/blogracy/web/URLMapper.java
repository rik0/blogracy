package it.unipr.aotlab.blogracy.web;

import it.unipr.aotlab.blogracy.errors.InvalidStringMapError;
import sun.misc.Regexp;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Enrico Franchi, 2011 (mc)a
 * <p/>
 * This program or module is released under the terms of the MIT license.
 * <p/>
 * User: enrico
 * Date: 11/2/11
 * Time: 11:54 AM
 */
public class URLMapper {
    static private class Mapping {
        Regexp rex;

    }

    public RequestResolver getResolver(String url) {
        throw new NotImplementedException();
    }


    public URLMapper() {}

    public void configure(String... strings) throws InvalidStringMapError {
        if (checkStringsAreEven(strings)) {
            throw new NotImplementedException();
        } else {
            throw new InvalidStringMapError("Odd number of parameters has been inserted.");
        }
    }

    private boolean checkStringsAreEven(String[] strings) {
        return strings.length % 2 == 0;
    }

    private void add(Regexp urlMap, String className) {
        throw new NotImplementedException();

    }
}
