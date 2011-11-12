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

package it.unipr.aotlab.blogracy.web.resolvers.staticfiles;

import it.unipr.aotlab.blogracy.errors.ServerConfigurationError;
import it.unipr.aotlab.blogracy.errors.URLMappingError;
import it.unipr.aotlab.blogracy.web.resolvers.RequestResolver;
import org.easymock.EasyMockSupport;
import org.gudy.azureus2.plugins.tracker.web.TrackerWebPageRequest;
import org.gudy.azureus2.plugins.tracker.web.TrackerWebPageResponse;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.*;

import static org.easymock.EasyMock.expect;

/**
 * User: enrico
 * Package: it.unipr.aotlab.blogracy.web.resolvers.staticfiles
 * Date: 11/12/11
 * Time: 8:40 AM
 */
public class StaticFileResolverImplTest extends EasyMockSupport {
    /**
     * This test is essentially flawed in the sense that it assumes that the
     * current working directory is the project root (where the build.xml and
     * pom.xml are. Consequently, we assume that the directory where we have
     * resources is blogracy/src/resources/...
     * <p/>
     * This may not be the case, of course. If you have troubles running this
     * test file, you may want to improve it. Unfortunately, depending on
     * Files is an awful idea and something we should drop asap.
     * <p/>
     * TODO: PowerMock may solve the issue
     */
    final private static File STATIC_ROOT_DIR = new File("blogracy/src/resources/static");

    StaticFileResolver resolver;

    @Before
    public void setUp() throws Exception {
        resolver = StaticFileResolvers.getStaticFileResolver(STATIC_ROOT_DIR);
    }

    @Test(expected = ServerConfigurationError.class)
    public void testNonExistingDirectory() throws Exception {
        RequestResolver failingResolver = StaticFileResolvers.getStaticFileResolver(
                new File("not_existing")
        );
    }

    @Test(expected = ServerConfigurationError.class)
    public void testNonDirectory() throws Exception {
        RequestResolver failingResolver = StaticFileResolvers.getStaticFileResolver(
                new File("pom.xml")
        );
    }


    @Test
    public void testResolveStyle() throws Exception {
        final String filename = "/css/style.css";
        final String contentType = "text/css";

        testResolve(filename, contentType);
    }

    @Test
    public void testResolveJavascript() throws Exception {
        final String filename = "/scripts/main.js";
        final String contentType = "text/javascript";

        testResolve(filename, contentType);
    }

    private void testResolve(final String filename, final String contentType) throws URLMappingError, IOException {
        final TrackerWebPageRequest requestMock = prepareRequestMock(filename);
        final TrackerWebPageResponse responseMock = prepareResponseMock(filename, contentType);

        replayAll();
        resolver.resolve(requestMock, responseMock);
        verifyAll();
    }

    private TrackerWebPageResponse prepareResponseMock(final String filename, final String contentType) throws IOException {
        TrackerWebPageResponse responseMock = createNiceMock(TrackerWebPageResponse.class);
        expect(responseMock.useFile(STATIC_ROOT_DIR.getAbsolutePath(), filename)).andStubReturn(true);
        return responseMock;
    }

    private TrackerWebPageRequest prepareRequestMock(final String filename) {
        TrackerWebPageRequest requestMock = createNiceMock(TrackerWebPageRequest.class);
        expect(requestMock.getURL()).andStubReturn(filename);
        return requestMock;
    }

    @Test
    public void testCouldResolve() throws Exception {
        Assert.assertTrue(resolver.couldResolve("css/style.css"));
        Assert.assertTrue(resolver.couldResolve("scripts/main.js"));
    }


}
