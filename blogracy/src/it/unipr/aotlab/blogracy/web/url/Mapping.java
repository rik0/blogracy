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
import it.unipr.aotlab.blogracy.web.resolvers.RequestResolver;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Mapping {
    final private Pattern rex;
    final private Class<? extends RequestResolver> resolverClass;
    final private Object[] startingParameters;

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
    public Mapping(String regexpString,
                   String classString,
                   final Object[] startingParameters)
            throws ServerConfigurationError {
        try {
            this.rex = Pattern.compile(regexpString);
            this.resolverClass = Class.forName(classString).asSubclass(RequestResolver.class);
            this.startingParameters = (startingParameters != null) ? startingParameters : new Object[0];
        } catch (Exception e) {
            throw new ServerConfigurationError(e);
        }
        checkStartingParameters();
    }



    private void checkStartingParameters() throws ServerConfigurationError {
        Class[] declaredFormalParameters = extractFormalParametersAnnotation(resolverClass);
        if(declaredFormalParameters != null) {
            Class[] presentedActualParameterClasses = buildActualParametersClassArray(startingParameters);
            if(!Arrays.equals(presentedActualParameterClasses, declaredFormalParameters)) {
                String message = buildStaticCheckingParametersErrorMessage(declaredFormalParameters);
                throw new ServerConfigurationError(message);
            }
        }
    }

    private String buildStaticCheckingParametersErrorMessage(final Class[] declaredFormalParameters) {
        StringBuilder builder = new StringBuilder();
        builder.append("RequestResolver ").append(resolverClass.toString());
        builder.append(" for url ").append(rex.pattern());
        builder.append(" configured with parameters [");
        for(Object o : startingParameters) {
            builder.append(o.toString());
            builder.append(", ");
        }
        builder.append("] but it required parameters of type [");
        for(Class parameterType : declaredFormalParameters) {
            builder.append(parameterType.getCanonicalName());
            builder.append(", ");
        }
        builder.append("].");
        return builder.toString();
    }

    private Class[] extractFormalParametersAnnotation(final Class<? extends RequestResolver> resolverClass) {
        ConfigurationTimeParameters parameters = resolverClass.getAnnotation(ConfigurationTimeParameters.class);
        if(parameters != null) {
        return parameters.value();
        } else {
            return null;
        }
    }

    /**
     * Return the resolver parameters derived from the URL if the mapping matches
     * such URL, null otherwise.
     *
     * @param url is checked against the regexp characterizing the current mapping
     * @return the list of parameter matched (can be empty) or null
     */
    private List<String> matches(String url) {
        Matcher m = rex.matcher(url);
        if (m.matches()) {
            return buildStartingParameters(m);
        } else {
            return null;
        }
    }

    /**
     * Builds the correct resolver for the url that successfully matched against this mapping
     *
     * @param url is checked against the regexp characterizing the current mapping
     * @return the appropriate resolver or null
     * @throws ServerConfigurationError if the resolver cannot be built (e.g., wrong number of parameters)
     */
    public RequestResolver buildResolver(final String url) throws ServerConfigurationError {
        List<String> urlMatchingParameters = matches(url);
        if (urlMatchingParameters == null) {
            return null;
        } else {
            Class[] constructorFormalParameters = buildConstructorFormalParameters(
                    startingParameters,
                    urlMatchingParameters
            );
            Object[] constructorActualParameters = buildConstructorActualParameters(
                    startingParameters,
                    urlMatchingParameters
            );
            return instantiateResolver(constructorFormalParameters, constructorActualParameters);
        }
    }

    private RequestResolver instantiateResolver(final Class[] constructorFormalParameters,
                                                final Object[] constructorActualParameters)
            throws ServerConfigurationError {
        try {
            Constructor<? extends RequestResolver> constructor =
                    resolverClass.getConstructor(constructorFormalParameters);
            return constructor.newInstance(constructorActualParameters);
        } catch (NoSuchMethodException e) {
            throw new ServerConfigurationError(e);
        } catch (InvocationTargetException e) {
            // InvocationTargetException is just a wrapper itself!
            return unwrapPossibleInternalServerConfigurationErrorAndThrow(e);
        } catch (InstantiationException e) {
            throw new ServerConfigurationError(e);
        } catch (IllegalAccessException e) {
            throw new ServerConfigurationError(e);
        }
    }

    private RequestResolver unwrapPossibleInternalServerConfigurationErrorAndThrow(final InvocationTargetException e)
            throws ServerConfigurationError {
        final Throwable originalException = e.getCause();
        if (originalException instanceof ServerConfigurationError) {
            throw (ServerConfigurationError) originalException;
        } else {
            throw new ServerConfigurationError(originalException);
        }
    }

    private Object[] buildConstructorActualParameters(final Object[] startingParameters,
                                                      final List<String> urlMatchingParameters) {
        final int totalSize = startingParameters.length + urlMatchingParameters.size();
        int parameterToInsert = 0;
        Object[] actualParameters = new Object[totalSize];
        for (Object o : startingParameters) {
            actualParameters[parameterToInsert++] = o;
        }
        for (String s : urlMatchingParameters) {
            actualParameters[parameterToInsert++] = s;
        }
        return actualParameters;
    }

    private Class[] buildConstructorFormalParameters(final Object[] startingParameters,
                                                     final List<String> urlMatchingParameters) {
        final int totalSize = startingParameters.length + urlMatchingParameters.size();
        int parameterToInsert = 0;
        Class[] formalParameters = new Class[totalSize];
        for (Object o : startingParameters) {
            formalParameters[parameterToInsert++] = o.getClass();
        }
        for (String _ : urlMatchingParameters) {
            formalParameters[parameterToInsert++] = String.class;
        }
        return formalParameters;
    }

    private Class[] buildActualParametersClassArray(Object[] objects) {
        final int totalSize = objects.length;
        Class[] formalParametersArray = new Class[totalSize];
        int parameterToInsert = 0;
        for(Object o : objects) {
            formalParametersArray[parameterToInsert++] = o.getClass();
        }
        return formalParametersArray;
    }


    private List<String> buildStartingParameters(final Matcher m) {
        List<String> startingParameters = new LinkedList<String>();
        for (int groupIndex = 1; groupIndex <= m.groupCount(); ++groupIndex) {
            String parameter = m.group(groupIndex);
            if (parameter != null) {
                startingParameters.add(parameter);
            }
        }
        return startingParameters;
    }
}

