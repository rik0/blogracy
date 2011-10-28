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

package it.unipr.aotlab.userRss.model.hashes;

import org.gudy.azureus2.core3.util.SHA1Hasher;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Arrays;

/**
 * Generic functions to manipulate hashes are defined in this class.
 */
public class Hashes {
    private static class HashImpl implements Hash {
        final byte[] hash;

        HashImpl(final byte[] hash) {
            this.hash = hash;
        }

        public String getStringValue() {
            String stringRepr = new String(hash);
            return stringRepr;
        }

        public byte[] getValue() {
            return Arrays.copyOf(hash, hash.length);
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append("HashImpl");
            sb.append("{hash=").append(hash == null ? "null" : "");
            for (int i = 0; hash != null && i < hash.length; ++i)
                sb.append(i == 0 ? "" : ", ").append(hash[i]);
            sb.append('}');
            return sb.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            HashImpl hash1 = (HashImpl) o;

            if (!Arrays.equals(hash, hash1.hash)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(hash);
        }
    }

    /**
     * Creates an hash from a {@code String}
     * @param value is the {@code String} to be hashed
     * @return the Hash of {@param value}
     */
    static public Hash newHash(String value) {
        SHA1Hasher hasher = new SHA1Hasher();
        final byte[] hash =  hasher.calculateHash(value.getBytes());
        return new HashImpl(hash);
    }
    /**
     * Creates an hash from a generic {@code Object}
     * @param o is the {@code Object} to be hashed
     * @return the Hash of {@param o}
     */
    static public Hash newHash(Object o) {
        throw new NotImplementedException();
    }

    /**
     * Checks if the hash is a valid hash
     * @param hash is the hash to be validated
     * @return false if we know for sure it is not a valid hash
     */
    static public boolean validateHash(Hash hash) {
        byte[] theHash;
        if(hash instanceof HashImpl) {
            theHash = ((HashImpl)hash).hash;
        } else {
            theHash = hash.getValue();
        }
        if(theHash.length != 20) {
            return false;
        }
        return true;
    }

    /**
     * Private constructor: no instances shall be created.
     */
    private Hashes() {
        /* no instances, thanks */
    }
}
