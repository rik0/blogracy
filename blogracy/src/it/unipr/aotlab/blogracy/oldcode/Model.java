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

package it.unipr.aotlab.blogracy.oldcode;

import java.util.Observable;
import java.util.SortedMap;
import java.util.TreeMap;


public class Model extends Observable {


    private int orderingType = 2;
    private int maxNOfRss = 50;
    private int refreshMaxCount = 60;
    private int maxNOfItem = 30;
    private SortedMap<String, Rss> rssMap = new TreeMap<String, Rss>();
    private SortedMap<String, Rss> userContentMap = new TreeMap<String, Rss>();
    private boolean titoloTxtEnabled = true;
    private boolean nContentExist = false;
    private boolean updateContentFlag;
    private String userStrRss = "Tutti";
    private String statusLbl = "";
    private String myRss = "";
    private String nOfRss = "0";
    private String TitoloTxtText = "";
    private String TextTxtText = "";
    private String userNContent = "1";
    private String userId = "alan";
    private String channelTitle = "channelTitle";
    private String channelLink = "channelLink";
    private String channelDescription = "channelDescription";


    /**
     * set if the plugin have to auto update the content
     *
     * @param s_updateContentFlag (true-false)
     */
    public void setUpdateContentFlag(boolean s_updateContentFlag) {
        updateContentFlag = s_updateContentFlag;
        Notifica();
    }

    /**
     * set the titleTxt
     *
     * @param s_TitoloTxtText the title txt
     */
    public void setTitoloTxt(String s_TitoloTxtText) {
        TitoloTxtText = s_TitoloTxtText;
        Notifica();
    }

    /**
     * set the textTxt
     *
     * @param s_TextTxtText the text txt
     */
    public void setTextTxtText(String s_TextTxtText) {
        TextTxtText = s_TextTxtText;
        Notifica();
    }

    /**
     * enabled/disabled the title text
     *
     * @param enabled true/false
     */
    public void setTitoloTxtEnabled(boolean enabled) {
        titoloTxtEnabled = enabled;
        Notifica();
    }

    /**
     * set the Rss map
     *
     * @param map set the Rss map
     */
    public void setRssTableGrid(SortedMap<String, Rss> map) {
        rssMap = map;
        Notifica();
    }

    /**
     * type of RSS ordering
     *
     * @param type 0 undefined, 1 Ordering by Name, 2 Ordering by date
     */
    public void setOrderingType(int type) {
        orderingType = type;

    }

    /**
     * set the current user's name
     *
     * @param user set the current user's name
     */
    public void setNameUserRss(String user) {
        userStrRss = user;
        Notifica();

    }

    /**
     * set the setUserContentmap
     *
     * @param s_userContentMap the actual userContent Map
     */
    public void setUserContentmap(SortedMap<String, Rss> s_userContentMap) {
        userContentMap = s_userContentMap;
        Notifica();
    }


    /**
     * set the statusbar string
     *
     * @param str set the statusbar value
     */
    public void setStatus(String str) {
        statusLbl = str;
        Notifica();
    }

    /**
     * set the Rss Map
     *
     * @param rssString set the Rss Map
     */
    public void setPrivateRss(String rssString) {
        myRss = rssString;
        Notifica();
    }

    /**
     * set the number of Rss
     *
     * @param nOfRssMessage set the number of Rss
     */
    public void setNOfRss(int nOfRssMessage) {
        nOfRss = "" + nOfRssMessage;
        Notifica();
    }

    /**
     * return the number of Rss
     *
     * @return nOfRss  numero di RSS trovati
     */
    public String getNOfRss() {
        return nOfRss;
    }

    /**
     * return the type of the rss ordering
     *
     * @return orderingType 1 Ordering by Name, 2 Ordering by date
     */
    public int getOrderingType() {
        return orderingType;
    }

    /**
     * get the current user's name
     *
     * @return userStrRss get the current user's name
     */
    public String getNameUserRss() {
        return userStrRss;
    }

    /**
     * get the max rss number
     *
     * @return maxNOfRss get the max rss number
     */
    public int getmaxNOfRss() {
        return maxNOfRss;
    }

    /**
     * get the Rss map
     *
     * @return rssMap get the Rss map
     */
    public SortedMap<String, Rss> getRssTableGrid() {
        return rssMap;
    }


    /**
     * get the max number of refresh before update the rss table
     *
     * @return refreshMaxCount the max number of refresh before update the rss table
     */
    public int getRefreshMaxCount() {
        return refreshMaxCount;
    }

    /**
     * get the userId
     *
     * @return userId the user Id
     */
    public String getUserId() {

        return userId;
    }

    /**
     * get the userContentMap
     *
     * @return userContentMap
     */
    public SortedMap<String, Rss> getUserContentmap() {
        return userContentMap;
    }

    /**
     * set the number of new Rss Content Item and if the content have almost one item
     *
     * @param nContent number of content
     * @param exist    if the content exists
     */
    public void setUserNContent(String nContent, boolean exist) {
        nContentExist = exist;
        userNContent = nContent;
        Notifica();
    }

    /**
     * get the number of item of the new content
     *
     * @return userNContent the number of item of the new content
     */
    public String getUserNContent() {
        return userNContent;

    }

    /**
     * return if the title text have to stay enabled
     *
     * @return titoloTxtEnabled (true - false)
     */
    public boolean getTitoloTxtEnabled() {
        return titoloTxtEnabled;
    }

    /**
     * return the title text
     *
     * @return TitoloTxtText the title text
     */
    public String getTitoloTxt() {
        return TitoloTxtText;
    }

    /**
     * return the text text
     *
     * @return TextTxtText the text text
     */
    public String getTextTxtText() {
        return TextTxtText;
    }

    /**
     * return nContentExist
     *
     * @return nContentExist
     */
    public boolean getNContentExist() {
        return nContentExist;
    }

    /**
     * return if the plugin have to auto update the content
     *
     * @return updateContentFlag (true - false))
     */
    public boolean getUpdateContentFlag() {
        return updateContentFlag;
    }

    /**
     * return the userContent map size
     *
     * @return userContentMap.size();
     */
    public int getUserContentMapSize() {
        return userContentMap.size();
    }

    /**
     * return the channel title
     *
     * @return channelTitle the channel title
     */
    public String getChannelTitle() {
        return channelTitle;
    }

    /**
     * return the Channel link
     *
     * @return channelLink the cannel link
     */
    public String getChannelLink() {
        return channelLink;
    }

    /**
     * return the channel description
     *
     * @return getChannelDescription channel description
     */
    public String getChannelDescription() {
        return channelDescription;
    }

    /**
     * return the status Bar Value
     *
     * @return statusLbl the status Bar Value
     */
    public String getStatus() {
        return statusLbl;
    }

    /**
     * get the rss Map
     *
     * @return myRss get the rss Map
     */
    public String getPrivateRss() {
        return myRss;
    }

    /**
     * notify to observer the change
     */
    private void Notifica() {
        setChanged();
        notifyObservers();
    }

    /**
     * return the max number of item x content
     *
     * @return maxNOfItem
     */
    public int getMaxNOfItem() {
        return maxNOfItem;
    }
}
