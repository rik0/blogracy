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

package it.unipr.aotlab.blogracy.web.resolvers;

import it.unipr.aotlab.blogracy.web.url.URLMapper;
import org.easymock.EasyMockSupport;
import org.gudy.azureus2.plugins.tracker.web.TrackerWebPageRequest;
import org.gudy.azureus2.plugins.tracker.web.TrackerWebPageResponse;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;

import static org.easymock.EasyMock.expect;

public class StaticFileResolverTestCreationWithMapper extends EasyMockSupport {
    URLMapper mapper;

    @Before
    public void setUp() throws Exception {
        mapper = new URLMapper();
        mapper.configure(
                "^/files/(?:.*)$", "it.unipr.aotlab.blogracy.web.resolvers.StaticFileResolver",
                new Object[]{getTestsStaticFilesRoot()}
        );
    }

    private String getTestsStaticFilesRoot() {
        Class<?> myClass = getClass();
        ClassLoader classLoader = myClass.getClassLoader();
        URL url = classLoader.getResource(".");
        return url.getPath();
    }

    @Test
    public void testResolveMainCss() throws Exception {
        final String exampleUrl = "/files/ExampleFile.class";
        RequestResolver resolver = mapper.getResolver(exampleUrl);
        TrackerWebPageRequest mockRequest = createMock(TrackerWebPageRequest.class);
        TrackerWebPageResponse mockResponse = createMock(TrackerWebPageResponse.class);

        expect(mockRequest.getURL()).andStubReturn(exampleUrl);
        expect(mockResponse.useFile(getTestsStaticFilesRoot(), exampleUrl)).andReturn(true);


        replayAll();
        resolver.resolve(mockRequest, mockResponse);
        verifyAll();
    }
}
