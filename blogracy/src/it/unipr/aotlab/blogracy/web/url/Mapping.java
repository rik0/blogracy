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

package it.unipr.aotlab.blogracy.web.url;

import it.unipr.aotlab.blogracy.errors.ServerConfigurationError;
import it.unipr.aotlab.blogracy.web.resolvers.RequestResolver;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * User: enrico
 * Package: it.unipr.aotlab.blogracy.web
 * Date: 11/3/11
 * Time: 11:47 AM
 */

/**
 * A Mapping is a package local helper class which actually converts a matching URL to the expected resolver.
 */
class Mapping {
    private Pattern rex;
    private Class<RequestResolver> resolverClass;

    /**
     * This variable is used to hold stuff after a match. We cache the parameters for performance reasons.
     */
    private Object[] tempParameters = null;

    /**
     * Creates a Mapping object that connects pages matching {@param regexpString} to the
     * resolver specified by {@param classString}
     *
     * @param regexpString is the regexp to match
     * @param classString  is the fully qualified name of the resolver
     * @throws ServerConfigurationError if the mapping cannot be established, e.g., because the
     *                                  regex is not valid or because the specified classfile cannot be found.
     */
    public Mapping(String regexpString, String classString) throws ServerConfigurationError {
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
     * @throws IllegalStateException    if the last call to match was not successful. Stateful programming sucks.
     * @throws ServerConfigurationError if the resolver cannot be built (e.g., wrong number of parameters)
     */
    public RequestResolver buildResolver() throws ServerConfigurationError {
        // TODO split the resolver constructor loading from the actual resolver building.
        if (tempParameters == null) {
            throw new ServerConfigurationError("buildResolver can be called only after a successful match.");
        }
        Class<String> constructorFormalParameters[] = buildConstructorFormalParameters();
        try {
            Constructor<RequestResolver> constructor = resolverClass.getConstructor(constructorFormalParameters);
            return constructor.newInstance(tempParameters);
        } catch (NoSuchMethodException e) {
            throw new ServerConfigurationError(e);
        } catch (InvocationTargetException e) {
            throw new ServerConfigurationError(e);
        } catch (InstantiationException e) {
            throw new ServerConfigurationError(e);
        } catch (IllegalAccessException e) {
            throw new ServerConfigurationError(e);
        }
    }

    private Class<String>[] buildConstructorFormalParameters() {
        @SuppressWarnings({"unchecked"})
        Class<String>[] constructorFormalParameters = (Class<String>[]) new Class[tempParameters.length];
        for (int i = 0; i < tempParameters.length; ++i) {
            constructorFormalParameters[i] = String.class;
        }
        return constructorFormalParameters;
    }

    private Object[] buildParameters(final Matcher m) {
        List<String> tempParameters = new LinkedList<String>();
        for (int groupIndex = 1; groupIndex <= m.groupCount(); ++groupIndex) {
            String parameter = m.group(groupIndex);
            if (parameter != null) {
                tempParameters.add(parameter);
            }
        }
        return tempParameters.toArray();
    }

    /**
     * Set the resolver class object attribute. May have classloading issues.
     *
     * @param classString the fully qualified name of the class
     * @throws ServerConfigurationError if the relevant classfile dows not exist
     */
    @SuppressWarnings({"unchecked"})
    protected void setResolverClass(String classString) throws ServerConfigurationError {
        try {
            // WARNING: there may be classloader issues.
            resolverClass = (Class<RequestResolver>) Class.forName(classString);

        } catch (ClassNotFoundException e) {
            throw new ServerConfigurationError(e);
        }
    }

    /**
     * Sets the pattern attribute.
     *
     * @param regexpString the string to compile
     * @throws ServerConfigurationError if it is not a valid regex
     */
    protected void setRex(String regexpString) throws ServerConfigurationError {
        try {
            rex = Pattern.compile(regexpString);
        } catch (PatternSyntaxException e) {
            throw new ServerConfigurationError(e);
        }
    }
}

