package it.unipr.aotlab.blogracy.web;

import it.unipr.aotlab.blogracy.errors.URLMappingError;
import sun.misc.Regexp;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedList;
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
        private Class<RequestResolver> resolverClass;

        /**
         * This variable is used to hold stuff after a match. We cache the parameters for performance reasons.
         */
        private String[] tempParameters = null;

        public Mapping(String regexpString, String classString) throws URLMappingError {
            setRex(regexpString);
            setResolverClass(classString);
        }

        /**
         * Return if this is the correct mapping. In case, it changes the state so that buildResolver can
         * be called without throwing exceptions.
         *
         * @param url is checked against the regexp characterizing the current match
         * @return true if it is a succesful match
         */
        public boolean matches(String url) {
            Matcher m = rex.matcher(url);
            if (m.matches()) {
                tempParameters = buildParameters(m);
                return true;
            } else {
                tempParameters = null;
                return false;
            }
        }

        /**
         * Builds the correct resolver for the url that successfully matched against this mapping
         *
         * @return the appropriate resolver
         * @throws IllegalStateException if the last call to match was not successful. Stateful programming sucks.
         * @throws it.unipr.aotlab.blogracy.errors.URLMappingError
         *                               if the resolver cannot be built (e.g., wrong number of parameters)
         */
        public RequestResolver buildResolver() throws IllegalStateException, URLMappingError {
            if (tempParameters == null) {
                throw new IllegalStateException("buildResolver can be called only after a successful match.");
            }
            Class<String> constructorFormalParameters[] = buildConstructorFormalParameters();
            try {
                Constructor<RequestResolver> constructor = resolverClass.getConstructor(constructorFormalParameters);
                return constructor.newInstance(tempParameters);
            } catch (NoSuchMethodException e) {
                throw new URLMappingError(e);
            } catch (InvocationTargetException e) {
                throw new URLMappingError(e);
            } catch (InstantiationException e) {
                throw new URLMappingError(e);
            } catch (IllegalAccessException e) {
                throw new URLMappingError(e);
            }
        }

        private Class<String>[] buildConstructorFormalParameters() {
            Class<String>[] constructorFormalParameters = new Class[tempParameters.length];
            for (int i = 0; i < tempParameters.length; ++i) {
                constructorFormalParameters[i] = String.class;
            }
            return constructorFormalParameters;
        }

        private String[] buildParameters(final Matcher m) {
            List<String> tempParameters = new LinkedList<String>();
            for (int groupIndex = 1; groupIndex <= m.groupCount(); ++groupIndex) {
                String parameter = m.group(groupIndex);
                if (parameter != null) {
                    tempParameters.add(parameter);
                }
            }
            return tempParameters.toArray(new String[0]);
        }

        private void setResolverClass(String classString) throws URLMappingError {
            try {
                // WARNING: there may be classloader issues.
                resolverClass = (Class<RequestResolver>) Class.forName(classString);
            } catch (ClassNotFoundException e) {
                throw new URLMappingError(e);
            }
        }

        private void setRex(String regexpString) throws URLMappingError {
            try {
                rex = Pattern.compile(regexpString);
            } catch (PatternSyntaxException e) {
                throw new URLMappingError(e);
            }
        }
    }

    public RequestResolver getResolver(String url) {
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


    public URLMapper() {
    }

    public void configure(String... strings) throws URLMappingError {
        if (checkStringsAreEven(strings)) {
            prepareList(strings);
            addMappings(strings);
        } else {
            throw new URLMappingError("Odd number of parameters has been inserted.");
        }
    }

    private void addMappings(String[] strings) throws URLMappingError {
        for (int nextIndex = 0; nextIndex < strings.length; ++nextIndex) {
            nextIndex = addMapping(strings, nextIndex);
        }
    }

    private int addMapping(String[] strings, int i) throws URLMappingError {
        lst.add(i, new Mapping(strings[i], strings[i + 1]));
        i++;
        return i;
    }

    private void prepareList(String[] strings) {
        lst = new ArrayList<Mapping>(strings.length / 2);
    }

    private boolean checkStringsAreEven(String[] strings) {
        return strings.length % 2 == 0;
    }

    private void add(Regexp urlMap, String className) {
        throw new NotImplementedException();

    }
}
