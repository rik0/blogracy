package net.blogracy.errors;

/**
 * Enrico Franchi, 2011 (c)
 * <p/>
 * This program or module is released under the terms of the MIT license.
 * <p/>
 * User: enrico
 * Date: 11/2/11
 * Time: 6:30 PM
 */
public class URLMappingError extends BlogracyError {
    private int httpErrorStatus;

    public URLMappingError(int httpErrorStatus) {
        this.httpErrorStatus = httpErrorStatus;
    }

    public URLMappingError(int httpErrorStatus, String s) {
        super(s);
        this.httpErrorStatus = httpErrorStatus;
    }

    public URLMappingError(int httpErrorStatus, String s, Throwable throwable) {
        super(s, throwable);
        this.httpErrorStatus = httpErrorStatus;
    }

    public URLMappingError(int httpErrorStatus, Throwable throwable) {
        super(throwable);
        this.httpErrorStatus = httpErrorStatus;
    }

    public int getHttpErrorStatus() {
        return httpErrorStatus;
    }
}
