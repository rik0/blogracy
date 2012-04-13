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

import net.blogracy.model.hashes.Hash;

/**
 * User: enrico
 * Package: net.blogracy.model.users
 * Date: 10/27/11
 * Time: 1:19 PM
 */
public class UserImpl implements User {
    String localNick;
    Hash hash;

    UserImpl(final String localNick, final Hash hash) {
        this.localNick = localNick;
        this.hash = hash;
    }

    /**
     * {@inheritDoc}
     */
    public String getLocalNick() {
        return localNick;
    }

    /**
     * {@inheritDoc}
     */
    public void setLocalNick(final String localNick) {
        this.localNick = localNick;
    }

    /**
     * {@inheritDoc}
     */
    public Hash getHash() {
        return hash;
  }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserImpl user = (UserImpl) o;

        if (!hash.equals(user.hash)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return hash.hashCode();
    }
}
