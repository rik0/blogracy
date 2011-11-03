package it.unipr.aotlab.blogracy.web;

import it.unipr.aotlab.blogracy.errors.URLMappingError;
import org.gudy.azureus2.plugins.tracker.web.TrackerWebPageRequest;
import org.gudy.azureus2.plugins.tracker.web.TrackerWebPageResponse;
import org.junit.Assert;
import org.junit.Test;

/**
 * Enrico Franchi, 2011 (c)
 * <p/>
 * This program or module is released under the terms of the MIT license.
 * <p/>
 * User: enrico
 * Date: 11/2/11
 * Time: 12:01 PM
 */
public class URLMapperTest {
    private URLMapper mapper;

    @org.junit.Before
    public void setUp() throws Exception {
        mapper = new URLMapper();
        mapper.configure(
                "^/profile$", "it.unipr.aotlab.blogracy.web.FakeProfile",
                "^/messages$", "it.unipr.aotlab.blogracy.web.FakeMessages",
                "^/messages/(\\d+)$", "it.unipr.aotlab.blogracy.web.FakeMessages"
        );
    }

    @Test(expected = URLMappingError.class)
    public void testConfigureNonExistentClass() throws Exception {
        URLMapper tempMapper = new URLMapper();
        tempMapper.configure("/fail", "not.existing.class.djsahdsja");
    }

    @Test
    public void testIrrelevantTrailingSlash() throws Exception {
        RequestResolver withSlash = mapper.getResolver("/profile/");
        RequestResolver withoutSlash = mapper.getResolver("/profile");

        Assert.assertEquals(withoutSlash.getClass(), withSlash.getClass());
    }

    @Test(expected = URLMappingError.class)
    public void testRelevantStartingSlash() throws Exception {
        mapper.getResolver("profile");
    }

    public static class NoParamsResolver implements RequestResolver {
        @Override
        public void
        resolve(final TrackerWebPageRequest request, final TrackerWebPageResponse response)
                throws Exception {
            // ok
        }
    }

    @Test(expected = URLMappingError.class)
    public void testMismatchConstructorAndUrl() throws Exception {
        URLMapper tempMapper = new URLMapper();
        tempMapper.configure("^/multi/(\\d+)$", "it.unipr.aotlab.blogracy.web.URLMapperTest$NoParamsResolver");
        tempMapper.getResolver("/multi/1");
    }

    @Test
    public void testMultiParameters() throws Exception {
        mapper.getResolver("/messages/3");
    }

    @Test(expected = URLMappingError.class)
    public void testErrorInResolverConstruction() throws Exception {
        mapper.getResolver("/mapper/foo");
    }
}
