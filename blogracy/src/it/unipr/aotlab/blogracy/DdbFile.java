/*
 * Copyright (c)  2011 Alan Nonnato, Enrico Franchi, Michele Tomaiuolo and University of Parma.
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

package it.unipr.aotlab.blogracy;

import java.io.Serializable;
import java.net.URL;


public class DdbFile implements Serializable {

    private static final long serialVersionUID = 1L;
    private String fileName;
    private URL magnetLink;
    private String fileKey;

    public DdbFile() {
    }

    public DdbFile(String torrentHash, String s_fileName, URL torrentMagnet) {
        fileKey = torrentHash;
        fileName = s_fileName;
        magnetLink = torrentMagnet;

    }

    public String getfileName() {
        return fileName;
    }


    public URL getMagnetLink() {
        return magnetLink;
    }


    public void setfileName(String s_fileName) {
        fileName = s_fileName;
    }


    public void setMagnetLink(URL s_magnetLink) {
        magnetLink = s_magnetLink;
    }

    @Override
    public String toString() {
        return "key=" + fileKey + "  --  fileName=" + fileName + "  --  magnetLink=" + magnetLink + "";
    }
}
