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
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * User: enrico
 * Package: it.unipr.aotlab.blogracy.web.post
 * Date: 11/15/11
 * Time: 11:03 AM
 */
public class PostQueryImpl implements PostQuery {
    final Map<String, byte[]> map = new TreeMap<String, byte[]>();
    final String encoding;



    public PostQueryImpl(final String encoding) {
        this.encoding = encoding;
    }

    @Override
    public Set<String> getKeys() {
        return Collections.unmodifiableSet(map.keySet());
    }

    @Override
    public byte[] getValueBytes(final String key) {
        return map.get(key);
    }

    @Override
    public <E> E getValue(final String key) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getStringValue(final String key) throws UnsupportedEncodingException {
        byte[] valueBytes = getValueBytes(key);
        if(valueBytes != null) {
            return new String(valueBytes, encoding);
        } else {
            return null;
        }
    }

    public int size() {
        return map.size();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public byte[] put(final String s, final byte[] bytes) {
        return map.put(s, bytes);
    }

    public void putAll(final Map<? extends String, ? extends byte[]> map) {
        this.map.putAll(map);
    }

    @Override
    public int hashCode() {
        int result = map != null ? map.hashCode() : 0;
        result = 31 * result + (encoding != null ? encoding.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final PostQueryImpl postQuery = (PostQueryImpl) o;

        if (encoding != null ? !encoding.equals(postQuery.encoding) : postQuery.encoding != null) return false;
        if (map != null ? !map.equals(postQuery.map) : postQuery.map != null) return false;

        return true;
    }
}
