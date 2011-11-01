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


public class Rss {

    private Long id;
    private String text;
    private String title;
    private String date;
    private String author;
    private String type;
    private String link;
    private int rssId;

    /**
     * Constructor of the Rss
     *
     * @param timestamp set the Rss timestamp
     * @param author    set the Rss author
     * @param text      set the Rss text
     * @param title     set the Rss Title
     * @param date      set the Rss date
     */
    public Rss(Long timestamp, String author, String text, String title, String date, String type, String link, int rssId) {
        setAuthor(author);
        setId(timestamp);
        setTitle(text);
        setText(title);
        setDate(date);
        setType(type);
        setLink(link);
        setId(rssId);
    }

    /**
     * set the rss id
     *
     * @param rssId the rss id
     */
    private void setId(int rssId) {
        this.rssId = rssId;

    }

    /**
     * set the Rss link
     *
     * @param link the Rss link
     */
    private void setLink(String link) {
        this.link = link;
    }

    /**
     * set the Rss type
     *
     * @param type the Rss type
     */
    private void setType(String type) {
        this.type = type;
    }

    /**
     * set the rss timestamp
     *
     * @param timestamp the Rss timestamp
     */
    public void setId(Long timestamp) {
        id = timestamp;
    }

    /**
     * set the rss text
     *
     * @param text the Rss text
     */
    public void setTitle(String text) {
        this.text = text;
    }

    /**
     * set the rss title
     *
     * @param title the Rss title
     */
    public void setText(String title) {
        this.title = title;
    }

    /**
     * set the rss date
     *
     * @param date the Rss date
     */
    public void setDate(String date) {
        this.date = date;
    }

    /**
     * set the rss author
     *
     * @param author the Rss author
     */
    public void setAuthor(String author) {
        this.author = author;
    }


    /**
     * return the Rss author
     *
     * @return author the Rss author
     */
    public String getAuthor() {
        return author;
    }

    /**
     * return the Rss id
     *
     * @return id the Rss id
     */
    public long getId() {
        return id;
    }

    /**
     * return the Rss title
     *
     * @return title the Rss title
     */
    public String getTitle() {
        return title;
    }

    /**
     * return the Rss text
     *
     * @return text the Rss text
     */
    public String getText() {
        return text;
    }


    /**
     * return the Rss date
     *
     * @return date the Rss date
     */
    public String getDate() {
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
    public String getLink() {
        return link;
    }

    /**
     * return the rss id
     *
     * @return isRss the rss id
     */
    public int getRssId() {
        return rssId;
    }

}
