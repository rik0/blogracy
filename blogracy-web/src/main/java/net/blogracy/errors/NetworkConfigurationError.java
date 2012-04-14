package net.blogracy.errors;

/**
 * Enrico Franchi, 2011 (c)
 * <p/>
 * This program or module is released under the terms of the MIT license.
 * <p/>
 * User: enrico
 * Date: 10/27/11
 * Time: 4:46 PM
 */
public class NetworkConfigurationError extends NetworkError {
    public NetworkConfigurationError() {
        super();
    }

    public NetworkConfigurationError(String s) {
        super(s);
    }

    public NetworkConfigurationError(String s, Throwable throwable) {
        super(s, throwable);
    }

    public NetworkConfigurationError(Throwable throwable) {
        super(throwable);
    }
}
