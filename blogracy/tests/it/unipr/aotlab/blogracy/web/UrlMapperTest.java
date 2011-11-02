package it.unipr.aotlab.blogracy.web;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Enrico Franchi, 2011 (c)
 * <p/>
 * This program or module is released under the terms of the MIT license.
 * <p/>
 * User: enrico
 * Date: 11/2/11
 * Time: 12:01 PM
 */
public class UrlMapperTest {
    private UrlMapper mapper;

    @org.junit.Before
    public void setUp() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        map.put("/profile/", "it.unipr.aotlab.blogracy.web.FakeProfile");
        map.put("/messages/", "it.unipr.aotlab.blogracy.web.FakeMessages");
        mapper = new UrlMapper(map);
    }

    @Test
    public void testLevel0() throws Exception {
        //mapper.map()
    }
}
