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

package it.unipr.aotlab.userRss.model.users;

import it.unipr.aotlab.userRss.errors.InformationMissing;
import it.unipr.aotlab.userRss.model.hashes.Hash;

/**
 * User: enrico
 * Package: it.unipr.aotlab.userRss.model.users
 * Date: 10/27/11
 * Time: 1:11 PM
 */
public class UserImpl implements User {
    String localNick;
    final Hash hash;

    UserImpl(final String localNick, final Hash hash) {
        this.localNick = localNick;
        this.hash = hash;
    }

    public String getLocalNick() {
        return localNick;
    }

    public void setLocalNick(final String nick) {
        localNick = nick;
    }

    public Hash getHash() {
        return hash;
    }

    public Profile getProfile() throws InformationMissing {
        throw new InformationMissing("User profile not downloaded.");
    }

    public boolean hasProfile() {
        return false;
    }
}
