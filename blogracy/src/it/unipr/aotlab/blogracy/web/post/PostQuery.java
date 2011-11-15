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

package it.unipr.aotlab.blogracy.web.post;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Set;

/**
 * User: enrico
 * Package: it.unipr.aotlab.blogracy.web.post
 * Date: 11/15/11
 * Time: 11:01 AM
 */
public interface PostQuery {
    /**
     * Obtains a collection with the keys mentioned in the actual post query
     * @return the collection (possibly empty).
     */
    Set<String> getKeys();

    /**
     * Get the byte array representation of the value corresponding to {@param key}
     * @param key the key to search
     * @return the representation or null if key is missing.
     */
    byte[] getValueBytes(String key);

    /**
     * Get the object associated with the corresponding to {@param key}
     * @param key the key to search
     * @return the object or null if key is missing.
     */
    <E> E getValue(String key) throws IOException;

    /**
     * Get the string associated with the corresponding to {@param key}
     * @param key the key to search
     * @return the string or null if key is missing.
     */
    String getStringValue(String key) throws UnsupportedEncodingException;

}
