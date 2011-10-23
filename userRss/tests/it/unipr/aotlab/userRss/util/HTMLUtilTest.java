package it.unipr.aotlab.userRss.util;

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
        System.out.println(HTMLUtil.errorString(new Exception("foo")));
    }
}
