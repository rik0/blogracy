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

package it.unipr.aotlab.userRss;


public class Rss {

    private Long id;
    private String text;
    private String title;
    private String date;
    private String author;
    private String type;
    private String link;
    private int idRss;

    /**
     * Constructor of the Rss
     *
     * @param timestamp set the Rss timestamp
     * @param s_author  set the Rss author
     * @param s_text    set the Rss text
     * @param s_title   set the Rss Title
     * @param s_date    set the Rss date
     */
    public Rss(Long timestamp, String s_author, String s_text, String s_title, String s_date, String s_type, String s_link, int s_idRss) {
        setRssAuthor(s_author);
        setRssId(timestamp);
        setRssTitle(s_text);
        setRssText(s_title);
        setRssDate(s_date);
        setRssType(s_type);
        setRssLink(s_link);
        setIdRss(s_idRss);
    }

    /**
     * set the rss id
     *
     * @param s_idRss the rss id
     */
    private void setIdRss(int s_idRss) {
        idRss = s_idRss;

    }

    /**
     * set the Rss link
     *
     * @param s_link the Rss link
     */
    private void setRssLink(String s_link) {
        link = s_link;
    }

    /**
     * set the Rss type
     *
     * @param s_type the Rss type
     */
    private void setRssType(String s_type) {
        type = s_type;
    }

    /**
     * set the rss timestamp
     *
     * @param timestamp the Rss timestamp
     */
    public void setRssId(Long timestamp) {
        id = timestamp;
    }

    /**
     * set the rss text
     *
     * @param s_text the Rss text
     */
    public void setRssTitle(String s_text) {
        text = s_text;
    }

    /**
     * set the rss title
     *
     * @param s_title the Rss title
     */
    public void setRssText(String s_title) {
        title = s_title;
    }

    /**
     * set the rss date
     *
     * @param s_date the Rss date
     */
    public void setRssDate(String s_date) {
        date = s_date;
    }

    /**
     * set the rss author
     *
     * @param s_author the Rss author
     */
    public void setRssAuthor(String s_author) {
        author = s_author;
    }


    /**
     * return the Rss author
     *
     * @return author the Rss author
     */
    public String getRssAuthor() {
        return author;
    }

    /**
     * return the Rss id
     *
     * @return id the Rss id
     */
    public long getRssId() {
        return id;
    }

    /**
     * return the Rss title
     *
     * @return title the Rss title
     */
    public String getRssTitle() {
        return title;
    }

    /**
     * return the Rss text
     *
     * @return text the Rss text
     */
    public String getRssText() {
        return text;
    }


    /**
     * return the Rss date
     *
     * @return date the Rss date
     */
    public String getRssDate() {
        return date;
    }

    /**
     * return the rss type
     *
     * @return type the Rss type
     */
    public String getRssType() {
        return type;
    }

    /**
     * return the rss link
     *
     * @return link the rss link
     */
    public String getRssLink() {
        return link;
    }

    /**
     * return the rss id
     *
     * @return isRss the rss id
     */
    public int getIdRss() {
        return idRss;
    }

}
