/*
 * Copyright (c)  2011 Enrico Franchi, Michele Tomaiuolo and University of Parma.
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

package net.blogracy.model.users;

/**
 * User: enrico
 * Package: net.blogracy.model.users
 * Date: 10/27/11
 * Time: 12:50 PM
 */

import net.blogracy.errors.NetworkConfigurationError;
import net.blogracy.errors.NetworkError;
import net.blogracy.model.hashes.Hash;
//import net.blogracy.network.Network;
//import net.blogracy.network.NetworkManager;

/**
 * Utility methods to manipulate the users.
 */
public class Users {
    /**
     * Creates a new user from given hash. In this case the user is identified in the system by its own hash.
     *
     * @param hash to identify the user
     * @return a new user
     */
    public static User newUser(Hash hash) {
        return newUser(hash.getPrintableValue(), hash);
    }

    /**
     * Creates a new user from a given hash and locally associates the user with the given nickname
     *
     * @param hash to identify the user globally
     * @param name to identify the user locally
     * @return the user
     */
    public static User newUser(final String name, final Hash hash) {
        return new UserImpl(name, hash);
    }

    /**
     * Get profile for the specified user. If the profile is cached, it does
     * not look for a new one.if one is cached.
     *
     * @param user for which to return the profile.
     * @return the user's profile
     * @throws NetworkError
     */
    public static Profile getProfile(User user) throws NetworkError {
        Profile p = getCachedProfile(user.getHash());
        if (p == null) {
            return updateProfile(user);
        } else {
            return p;
        }
    }

    private static Profile getCachedProfile(Hash hash) {
        throw new UnsupportedOperationException();
    }

    /**
     * Get profile for the specified user.
     * <p/>
     * The profile is always downloaded from the network.
     *
     * @param user for which to return the profile.
     * @return the user's profile
     * @throws NetworkError
     */
    public static Profile updateProfile(User user) throws NetworkConfigurationError {
        // Network network = NetworkManager.getNetwork();
        // insert profile in cache.
        throw new UnsupportedOperationException();
    }

    /**
     * Example method which indicates that we should have stuff to perform searches of users /in memory/
     *
     * @param query
     * @return
     */
    public static User searchUser(String query) {
        throw new UnsupportedOperationException();
    }

    private Users() {
    }
}
