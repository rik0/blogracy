package it.unipr.aotlab.userRss.errors;

/**
 * Created by IntelliJ IDEA.
 * User: enrico
 * Date: 10/24/11
 * Time: 12:05 AM
 * To change this template use File | Settings | File Templates.
 */
public class BlogracyError extends Exception {
    public BlogracyError() {
    }

    public BlogracyError(String s) {
        super(s);
    }

    public BlogracyError(String s, Throwable throwable) {
        super(s, throwable);
    }

    public BlogracyError(Throwable throwable) {
        super(throwable);
    }
}
