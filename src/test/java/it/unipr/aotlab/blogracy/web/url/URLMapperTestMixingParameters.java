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
import it.unipr.aotlab.blogracy.web.resolvers.HTTPRequestType;
import it.unipr.aotlab.blogracy.web.resolvers.RequestResolver;
import org.gudy.azureus2.plugins.tracker.web.TrackerWebPageRequest;
import org.gudy.azureus2.plugins.tracker.web.TrackerWebPageResponse;
import org.junit.Assert;
import org.junit.Test;

public class URLMapperTestMixingParameters {
    private final String stringOne = "STRING1";
    private final String stringTwo = "STRING2";
    private final String stringThree = "STRING3";

    final String stringA = "STRINGA";
    final String stringB = "STRINGB";
    final String stringC = "STRINGC";

    final String basicURL = "/messing-with-parameters/";
    final String oneParam = basicURL + stringA;
    final String twoParams = oneParam + "/" + stringB;
    final String threeParams = twoParams + "/" + stringC;

    private URLMapper mapper;

    @org.junit.Before
    public void setUp() throws Exception {
        mapper = new URLMapper();
        mapper.configure(
                "^/messing-with-parameters",
                "it.unipr.aotlab.blogracy.web.url.URLMapperTestMixingParameters$ConfusionBetweenStartingAndLaterParameters",
                new Object[]{stringOne, stringTwo, stringThree},

                "^/messing-with-parameters/(\\w+)$",
                "it.unipr.aotlab.blogracy.web.url.URLMapperTestMixingParameters$ConfusionBetweenStartingAndLaterParameters",
                new Object[]{stringOne, stringTwo},

                "^/messing-with-parameters/(\\w+)/(\\w+)$",
                "it.unipr.aotlab.blogracy.web.url.URLMapperTestMixingParameters$ConfusionBetweenStartingAndLaterParameters",
                new Object[]{stringOne},

                "^/messing-with-parameters/(\\w+)/(\\w+)/(\\w+)$",
                "it.unipr.aotlab.blogracy.web.url.URLMapperTestMixingParameters$ConfusionBetweenStartingAndLaterParameters",
                null
        );
    }

    @Test
    public void testThreeUrlParameters() throws ServerConfigurationError, URLMappingError {
        ConfusionBetweenStartingAndLaterParameters resolver =
                (ConfusionBetweenStartingAndLaterParameters) mapper.getResolver(threeParams);
        Assert.assertEquals(stringA, resolver.getStringOne());
        Assert.assertEquals(stringB, resolver.getStringTwo());
        Assert.assertEquals(stringC, resolver.getStringThree());
    }

    @Test
    public void testTwoUrlParameters() throws ServerConfigurationError, URLMappingError {
        ConfusionBetweenStartingAndLaterParameters resolver =
                (ConfusionBetweenStartingAndLaterParameters) mapper.getResolver(twoParams);
        Assert.assertEquals(stringOne, resolver.getStringOne());
        Assert.assertEquals(stringA, resolver.getStringTwo());
        Assert.assertEquals(stringB, resolver.getStringThree());
    }

    @Test
    public void testOneUrlParameter() throws ServerConfigurationError, URLMappingError {
        ConfusionBetweenStartingAndLaterParameters resolver =
                (ConfusionBetweenStartingAndLaterParameters) mapper.getResolver(oneParam);
        Assert.assertEquals(stringOne, resolver.getStringOne());
        Assert.assertEquals(stringTwo, resolver.getStringTwo());
        Assert.assertEquals(stringA, resolver.getStringThree());
    }

    @Test
    public void testNoUrlParameters() throws ServerConfigurationError, URLMappingError {
        ConfusionBetweenStartingAndLaterParameters resolver =
                (ConfusionBetweenStartingAndLaterParameters) mapper.getResolver(basicURL);
        Assert.assertEquals(stringOne, resolver.getStringOne());
        Assert.assertEquals(stringTwo, resolver.getStringTwo());
        Assert.assertEquals(stringThree, resolver.getStringThree());
    }

    public static class ConfusionBetweenStartingAndLaterParameters implements RequestResolver {
        final private String stringOne;
        final private String stringTwo;
        final private String stringThree;

        public ConfusionBetweenStartingAndLaterParameters(final String stringOne,
                                                          final String stringTwo,
                                                          final String stringThree) {
            this.stringOne = stringOne;
            this.stringTwo = stringTwo;
            this.stringThree = stringThree;
        }

        @Override
        public void resolve(final TrackerWebPageRequest request,
                            final TrackerWebPageResponse response)
                throws URLMappingError {

        }

        public String getStringOne() {
            return stringOne;
        }

        public String getStringTwo() {
            return stringTwo;
        }

        public String getStringThree() {
            return stringThree;
        }

        @Override
        public HTTPRequestType getRequestHTTPRequestType() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isAJAXRequest(final TrackerWebPageRequest request) {
            throw new UnsupportedOperationException();
        }
    }
}
