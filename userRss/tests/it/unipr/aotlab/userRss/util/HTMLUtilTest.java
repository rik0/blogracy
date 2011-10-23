package it.unipr.aotlab.userRss.util;

import org.junit.Assert;

/**
 * Created by IntelliJ IDEA.
 * User: enrico
 * Date: 10/23/11
 * Time: 11:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class HTMLUtilTest {
    @org.junit.Test
    public void testErrorString() throws Exception {
        String errorPage = HTMLUtil.errorString(new Exception("foo"));

        Assert.assertTrue(
                errorPage.indexOf("foo") > 0
        );
    }
}
