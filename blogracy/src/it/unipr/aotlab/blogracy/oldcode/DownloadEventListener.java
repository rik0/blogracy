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


import org.gudy.azureus2.plugins.PluginInterface;
import org.gudy.azureus2.plugins.download.Download;
import org.gudy.azureus2.plugins.download.DownloadException;
import org.gudy.azureus2.plugins.download.DownloadListener;
import org.gudy.azureus2.plugins.torrent.TorrentAttribute;

import java.io.File;

@Deprecated
class DownloadEventListener implements DownloadListener {
    private PluginInterface pluginInterface;

    public DownloadEventListener(PluginInterface s_pluginInterface) {
        pluginInterface = s_pluginInterface;
    }

    /**
     * the download listener
     *
     * @param download the download identificator
     * @param oldState the old download state
     * @param newState the new download state
     */
    public void stateChanged(Download download, int oldState, int newState) {

        // System.out.println("evento--------------------------------------------------------------");
        TorrentAttribute att = pluginInterface.getTorrentManager().getAttribute(TorrentAttribute.TA_CATEGORY);
        String category = download.getAttribute(att);
        // System.out.println(category);
        //String category= "alan_userRssPluginCategory";//download.getAttribute(ta_category);
        //// System.out.println("asdasdads "+category);

        // TODO Auto-generated catch block  LEGGERE CATEGORIA

        //if a download end, and that download is a plugin download
        if ((newState == 5) && (category.endsWith("userRssPluginCategory"))) {
            //// System.out.println("categoria "+category);
            String name = download.getName();
            String path = download.getSavePath();
            String newPath;

            // System.out.println("----->da qui: " + path);

            //if the file doesn't end with .rss, it's a file, so put it in friend_file dir
            if (!name.endsWith(".rss")) {
                String[] dirSpli = category.split("_");
                newPath = Controller.pluginInterface.getPluginDirectoryName() + "\\friends_dir\\" + dirSpli[0] + "_file";//+namePart[0];

            } else {// can be removed
                //extract the friends name
                String[] namePart = name.split("_");
                newPath = Controller.pluginInterface.getPluginDirectoryName() + "\\friends_dir\\" + namePart[0];

            }

            //check if dir exist, if doesn't exist, i make it
            File tmpDir = new File(newPath);
            if (!tmpDir.exists()) {
                tmpDir.mkdir();
            }


            // System.out.println("----->dovrei metterlo qui ... " + newPath);

            //move the original file in the correct Dir
            try {

                download.moveDataFiles(new File(newPath));
            } catch (DownloadException e) {

                e.printStackTrace();
            }


        }


        return;
    }

    public void positionChanged(Download download, int oldPosition, int newPosition) {
        return; //Could have been anything that you want to do when a download changes position.
    }


}
