package it.unipr.aotlab.blogracy.errors;

/**
 * Enrico Franchi, 2011 (c)
 * <p/>
 * This program or module is released under the terms of the MIT license.
 * <p/>
 * User: enrico
 * Date: 11/2/11
 * Time: 6:30 PM
 */
public class InvalidStringMapError extends BlogracyError {
    public InvalidStringMapError() {
    }

    public InvalidStringMapError(String s) {
        super(s);
    }

    public InvalidStringMapError(String s, Throwable throwable) {
        super(s, throwable);
    }

    public InvalidStringMapError(Throwable throwable) {
        super(throwable);
    }
}
