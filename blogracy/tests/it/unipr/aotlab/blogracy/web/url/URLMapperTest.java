package it.unipr.aotlab.blogracy.web.url;

import it.unipr.aotlab.blogracy.errors.ServerConfigurationError;
import it.unipr.aotlab.blogracy.errors.URLMappingError;
import it.unipr.aotlab.blogracy.web.resolvers.RequestResolver;
import it.unipr.aotlab.blogracy.web.url.URLMapper;
import org.gudy.azureus2.plugins.tracker.web.TrackerWebPageRequest;
import org.gudy.azureus2.plugins.tracker.web.TrackerWebPageResponse;
import org.junit.Assert;
import org.junit.Test;

import java.net.HttpURLConnection;

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
                "^/$", "it.unipr.aotlab.blogracy.web.url.URLMapperTest$HomepageResolver", null,
                "^/profile$", "it.unipr.aotlab.blogracy.web.url.URLMapperTest$FakeProfile", null,
                "^/messages$", "it.unipr.aotlab.blogracy.web.url.URLMapperTest$FakeMessages", null,
                "^/messages/(\\d+)$", "it.unipr.aotlab.blogracy.web.url.URLMapperTest$FakeMessages", null
        );
        //mapper.setHomePage(new HomepageResolver());
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
        Assert.assertEquals(FakeProfile.class, withSlash.getClass());
    }

    @Test
    public void testIrrelevantLeadingSlash() throws Exception {
        RequestResolver withSlash = mapper.getResolver("/profile");
        RequestResolver withoutSlash = mapper.getResolver("profile");

        Assert.assertEquals(withoutSlash.getClass(), withSlash.getClass());
        Assert.assertEquals(FakeProfile.class, withSlash.getClass());
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
        Assert.assertEquals(FakeMessages.class, resolver.getClass());
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
    }

    public static class HomepageResolver implements RequestResolver {
        @Override
        public void resolve(final TrackerWebPageRequest request,
                            final TrackerWebPageResponse response) throws URLMappingError {

        }
    }


    public static class FakeProfile implements RequestResolver {
        @Override
        public void resolve(final TrackerWebPageRequest request,
                            final TrackerWebPageResponse response) throws URLMappingError {

        }
    }

    public static class FakeFollowers implements RequestResolver {
        @Override
        public void resolve(final TrackerWebPageRequest request,
                            final TrackerWebPageResponse response) throws URLMappingError {

        }
    }

    public static class FakeMessages implements RequestResolver {
        int page;

        public FakeMessages(String page) throws URLMappingError {
            try {
                Integer pageNumber = Integer.parseInt(page);
            } catch (NumberFormatException e) {
                throw new URLMappingError(HttpURLConnection.HTTP_NOT_FOUND, e);
            }
        }

        public FakeMessages() {
            page = 0;
        }

        @Override
        public void resolve(final TrackerWebPageRequest request, final TrackerWebPageResponse response) throws URLMappingError {

        }
    }
}
