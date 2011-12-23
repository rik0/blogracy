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

import java.net.HttpURLConnection;

public class URLMapperTest {
    private URLMapper mapper;
    private final Integer differentParametersInteger = 1;
    private final Float differentParametersFloat = 4.0f;


    @org.junit.Before
    public void setUp() throws Exception {
        mapper = new URLMapper();
        mapper.configure(
                "^/$", "it.unipr.aotlab.blogracy.web.url.URLMapperTest$HomepageResolver", null,
                "^/profile$", "it.unipr.aotlab.blogracy.web.url.URLMapperTest$Profile", null,
                "^/messages$", "it.unipr.aotlab.blogracy.web.url.URLMapperTest$Messages", null,
                "^/messages/(\\d+)$", "it.unipr.aotlab.blogracy.web.url.URLMapperTest$Messages", null
        );
    }

    @Test(expected = ServerConfigurationError.class)
    public void testConfigureNonExistentClass() throws Exception {
        URLMapper tempMapper = new URLMapper();
        tempMapper.configure("/fail", "not.existing.class.djsahdsja");
    }

    @Test
    public void testIrrelevantTrailingSlash() throws Exception {
        RequestResolver withSlash = mapper.getResolver("/profile/");
        RequestResolver withoutSlash = mapper.getResolver("/profile");

        Assert.assertEquals(withoutSlash.getClass(), withSlash.getClass());
        Assert.assertEquals(Profile.class, withSlash.getClass());
    }

    @Test
    public void testIrrelevantLeadingSlash() throws Exception {
        RequestResolver withSlash = mapper.getResolver("/profile");
        RequestResolver withoutSlash = mapper.getResolver("profile");

        Assert.assertEquals(withoutSlash.getClass(), withSlash.getClass());
        Assert.assertEquals(Profile.class, withSlash.getClass());
    }

    @Test(expected = ServerConfigurationError.class)
    public void testMismatchConstructorAndUrl() throws Exception {
        URLMapper tempMapper = new URLMapper();
        tempMapper.configure("^/multi/(\\d+)$", "it.unipr.aotlab.blogracy.web.url.URLMapperTest$NoParamsResolver");
        tempMapper.getResolver("/multi/1");
    }

    @Test
    public void testMultiParameters() throws Exception {
        RequestResolver resolver = mapper.getResolver("/messages/3");
        Assert.assertEquals(Messages.class, resolver.getClass());
    }

    @Test(expected = URLMappingError.class)
    public void testErrorInResolverConstruction() throws Exception {
        mapper.getResolver("/mapper/foo");
    }

    @Test
    public void testHomePage() throws Exception {
        Assert.assertEquals(
                HomepageResolver.class,
                mapper.getResolver("/").getClass()
        );
        Assert.assertEquals(
                HomepageResolver.class,
                mapper.getResolver("").getClass()
        );
    }

    public static class NoParamsResolver implements RequestResolver {
        @Override
        public void resolve(final TrackerWebPageRequest request,
                            final TrackerWebPageResponse response) throws URLMappingError {
            // ok
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

    public static class HomepageResolver implements RequestResolver {
        @Override
        public void resolve(final TrackerWebPageRequest request,
                            final TrackerWebPageResponse response) throws URLMappingError {
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


    public static class Profile implements RequestResolver {
        @Override
        public void resolve(final TrackerWebPageRequest request,
                            final TrackerWebPageResponse response) throws URLMappingError {

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

    public static class Followers implements RequestResolver {
        @Override
        public void resolve(final TrackerWebPageRequest request,
                            final TrackerWebPageResponse response) throws URLMappingError {

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

    public static class Messages implements RequestResolver {
        int page;

        public Messages(String page) throws URLMappingError {
            try {
                Integer pageNumber = Integer.parseInt(page);
            } catch (NumberFormatException e) {
                throw new URLMappingError(HttpURLConnection.HTTP_NOT_FOUND, e);
            }
        }

        public Messages() {
            page = 0;
        }

        @Override
        public void resolve(final TrackerWebPageRequest request, final TrackerWebPageResponse response) throws URLMappingError {

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
