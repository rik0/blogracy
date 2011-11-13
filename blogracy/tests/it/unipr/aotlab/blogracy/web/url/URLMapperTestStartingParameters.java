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
import org.gudy.azureus2.plugins.tracker.web.TrackerWebPageRequest;
import org.gudy.azureus2.plugins.tracker.web.TrackerWebPageResponse;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class URLMapperTestStartingParameters {
    private URLMapper mapper;
    private final Integer differentParametersInteger = 1;
    private final Float differentParametersFloat = 4.0f;


    @org.junit.Before
    public void setUp() throws Exception {
        mapper = new URLMapper();
        mapper.configure(
                "^/starting-parameters$",
                "it.unipr.aotlab.blogracy.web.url.URLMapperTestStartingParameters$DifferentStartingArguments",
                new Object[]{differentParametersInteger, differentParametersFloat},

                "^/inverting-starting-parameters$",
                "it.unipr.aotlab.blogracy.web.url.URLMapperTestStartingParameters$DifferentStartingArguments",
                new Object[]{differentParametersFloat, differentParametersInteger},

                "^/missing-starting-parameter1$",
                "it.unipr.aotlab.blogracy.web.url.URLMapperTestStartingParameters$DifferentStartingArguments",
                new Object[]{differentParametersInteger},

                "^/missing-starting-parameter2$",
                "it.unipr.aotlab.blogracy.web.url.URLMapperTestStartingParameters$DifferentStartingArguments",
                new Object[]{}
        );
    }

    @Test
    public void testCreateWithStartingParameters() throws Exception {
        RequestResolver resolver = mapper.getResolver("/starting-parameters");
        Assert.assertNotNull(resolver);
        Assert.assertTrue(resolver instanceof DifferentStartingArguments);
        DifferentStartingArguments differentStartingArgumentsResolver = (DifferentStartingArguments) resolver;
        Assert.assertEquals(differentParametersInteger, differentStartingArgumentsResolver.getIntegerParameter());
        Assert.assertEquals(differentParametersFloat, differentStartingArgumentsResolver.getFloatParameter());

    }

    @Test(expected = ServerConfigurationError.class)
    public void testCreateWithInvertedStartingParameters() throws Exception {
        mapper.getResolver("/inverting-starting-parameters");
    }

    @Test(expected = ServerConfigurationError.class)
    public void testCreateWithMissingOneParameters() throws Exception {
        mapper.getResolver("/missing-starting-parameter1");
    }

    @Test(expected = ServerConfigurationError.class)
    public void testCreateWithMissingAllParameters() throws Exception {
        mapper.getResolver("/missing-starting-parameter2");
    }

    public static class DifferentStartingArguments implements RequestResolver {
        final private Integer integerParameter;
        final private Float floatParameter;

        public DifferentStartingArguments(final Integer integerParameter, final Float floatParameter) {
            this.integerParameter = integerParameter;
            this.floatParameter = floatParameter;
        }

        @Override
        public void resolve(final TrackerWebPageRequest request,
                            final TrackerWebPageResponse response) throws URLMappingError, IOException {
        }

        public Integer getIntegerParameter() {
            return integerParameter;
        }

        public Float getFloatParameter() {
            return floatParameter;
        }
    }

}
