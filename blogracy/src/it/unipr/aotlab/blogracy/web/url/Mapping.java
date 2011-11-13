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
    final private Pattern rex;
    final private Class<? extends RequestResolver> resolverClass;
    final private Object[] startingParameters;

    /**
     * This variable is used to hold stuff after a match. We cache the parameters for performance reasons.
     */
    private Object[] tempParameters = null;

    /**
     * Creates a Mapping object that connects pages matching {@param regexpString} to the
     * resolver specified by {@param classString}
     *
     * @param regexpString       is the regexp to match
     * @param classString        is the fully qualified name of the resolver
     * @param startingParameters the parameters we should pass to the constructor (null if no parameters)
     * @throws ServerConfigurationError if the mapping cannot be established, e.g., because the
     *                                  regex is not valid or because the specified classfile cannot be found.
     */
    public Mapping(String regexpString, String classString, final Object[] startingParameters)
            throws ServerConfigurationError {
        try {
            this.rex = Pattern.compile(regexpString);
            this.resolverClass = Class.forName(classString).asSubclass(RequestResolver.class);
            this.startingParameters = startingParameters;
        } catch (Exception e) {
            throw new ServerConfigurationError(e);
        }
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
            Constructor<? extends RequestResolver> constructor = resolverClass.getConstructor(constructorFormalParameters);
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
}

