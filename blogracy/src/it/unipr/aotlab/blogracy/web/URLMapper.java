package it.unipr.aotlab.blogracy.web;

import it.unipr.aotlab.blogracy.errors.InvalidStringMapError;
import sun.misc.Regexp;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

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
    List<Mapping> lst;

    static private class Mapping {
        private Pattern rex;
        private RequestResolver resolver;

        public Mapping(String regexpString, String classString) throws InvalidStringMapError {
            setRex(regexpString);
            setResolver(classString);
        }

        public boolean matches(String url) {
            Matcher m = rex.matcher(url);
            if(m.matches()) {
                // TODO: do not have stuff called two times.

            }
            return false;
        }

        private void setResolver(String classString) throws InvalidStringMapError {
            try {
                Class<RequestResolver> resolverClass = (Class<RequestResolver>) Class.forName(
                        classString
                );
                resolver = resolverClass.newInstance();
            } catch (ClassNotFoundException e) {
                throw new InvalidStringMapError(e);
            } catch (InstantiationException e) {
                throw new InvalidStringMapError(e);
            } catch (IllegalAccessException e) {
                throw new InvalidStringMapError(e);
            }
        }

        private void setRex(String regexpString) throws InvalidStringMapError {
            try {
                rex = Pattern.compile(regexpString);
            } catch (PatternSyntaxException e) {
                throw new InvalidStringMapError(e);
            }
        }
    }

    public RequestResolver getResolver(String url) {
        throw new NotImplementedException();
    }


    public URLMapper() {
    }

    public void configure(String... strings) throws InvalidStringMapError {
        if (checkStringsAreEven(strings)) {
            lst = new ArrayList<Mapping>(strings.length / 2);
            for (int i = 0; i < strings.length; ++i) {
                lst.add(i, new Mapping(strings[i], strings[i + 1]));
                i++;
            }
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
