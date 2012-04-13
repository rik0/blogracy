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

package net.blogracy.model.feeds;

import java.io.File;
import java.util.ArrayList;

import net.blogracy.config.Configurations;

import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

/**
 * Generic functions to manipulate feeds are defined in this class.
 */
public class Feeds {
	static public SyndFeed getFeed(String user) {
		System.out.println("Getting feed: " + user);
		SyndFeed feed = null;
		try {
			String folder = Configurations.getPathConfig().getCachedFilesDirectoryPath();
			File feedFile = new File(folder + File.separator + user + ".rss");
			feed = new SyndFeedInput().build(new XmlReader(feedFile));
			System.out.println("Feed loaded");
		} catch (Exception e) {
			feed = new SyndFeedImpl();
			feed.setFeedType("rss_2.0");
			feed.setTitle(user);
			feed.setLink("http://www.blogracy.net");
			feed.setDescription("This feed has been created using ROME (Java syndication utilities");
			feed.setEntries(new ArrayList());
			System.out.println("Feed created");
		}
		return feed;
	}
}
