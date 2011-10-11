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


import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.gudy.azureus2.plugins.PluginInterface;
import org.gudy.azureus2.ui.swt.plugins.UISWTViewEvent;
import org.gudy.azureus2.ui.swt.plugins.UISWTViewEventListener;

import java.util.*;


public class View implements UISWTViewEventListener, Observer {


    boolean isCreated = false;
    String directoryName;
    int contRefresh = 0;
    Label lblNContent;
    Button updateBtn, addFriend, updateRSSBtn, deleteFriend;
    Text titoloTxt, textTxt, idTxt, privateKeyTxt, rssTxt, friendIdTxt;
    Label lblDinamicState;
    Group compositeDxAllRss;
    Combo friendsCmb, deleteFriendsCmb;
    Browser browser;

    UserRSS plugin = null;
    //The Plugin interface
    PluginInterface pluginInterface;
    private Composite compositeMain;
    private Composite compositeMainSx;
    private Composite compositeMainDx;

    //The current Display
    private Display display;
    Model cModel;
    Controller cControl;


    //The String holding current directory value
    private Composite compositeOptionSx;
    private Button browseFileFile;
    private Button browseFileImage;
    private Locale locale;
    private ResourceBundle messages;


    /**
     * create the plugininterface
     *
     * @param plugin
     * @param pluginInterface set the current plugin interface
     */
    public View(UserRSS plgn, PluginInterface plgnInterface) {
        locale = new Locale("en", "EN");
        messages = ResourceBundle.getBundle("messages.Messages", locale);
        plugin = plgn;
        pluginInterface = plgnInterface;

        //set model and controller
        cModel = new Model();
        cControl = new Controller(this, cModel, plugin, pluginInterface, messages.getString("allUsersMessage"));
        contRefresh = cModel.getRefreshMaxCount();

    }

    /**
     * Here stands any GUI initialisation
     */
    private void initialize(final Composite parent) {

        // We store the Display variable as we'll need it for async GUI Changes
        cModel.addObserver(this);
        this.display = parent.getDisplay();

        // make the main grid with 2 coloumn
        this.compositeMain = new Composite(parent, SWT.NULL);
        GridLayout layoutMain = new GridLayout();
        layoutMain.numColumns = 2;
        compositeMain.setLayout(layoutMain);
        compositeMain.setLayoutData(new GridData(GridData.FILL_BOTH));
        //put in the first column of the main grid the rssView
        this.compositeMainSx = new Composite(compositeMain, SWT.NULL);
        GridLayout layoutMainSx = new GridLayout();
        layoutMainSx.numColumns = 1;
        compositeMainSx.setLayout(layoutMainSx);
        compositeMainSx.setLayoutData(new GridData(GridData.FILL_BOTH));
        //put in the left column of the main layout the control menu
        this.compositeMainDx = new Composite(compositeMain, SWT.NULL);
        GridLayout layoutMainDx = new GridLayout();
        layoutMainDx.numColumns = 1;
        compositeMainDx.setLayout(layoutMainDx);
        compositeMainDx.setLayoutData(new GridData(GridData.FILL_BOTH));

//--------- make the RSS VIEW on the left------------------------------------------------//				


        //create the left side of screen
        compositeDxAllRss = new Group(compositeMainSx, SWT.SHADOW_IN);
        compositeDxAllRss.setText(messages.getString("yourRssGroupMessage"));
        compositeDxAllRss.setLayout(new GridLayout(1, false));
        compositeDxAllRss.setLayoutData(new GridData(GridData.FILL_BOTH));

        //add the browser window
        browser = new Browser(compositeDxAllRss, SWT.BORDER);
        browser.setLayoutData(new GridData(GridData.FILL_BOTH));
        browser.setSize(600, 600);

//--------- make the UPDATE YOUR STAT on the left------------------------------------------------//	    
        //create the right screen
        Group compositeDx = new Group(compositeMainDx, SWT.SHADOW_IN);
        compositeDx.setText(messages.getString("updateStatusGroupMessage"));
        compositeDx.setLayout(new GridLayout(2, false));
        compositeDx.setLayoutData(new GridData(GridData.FILL_BOTH));

        //create the id label
        Label idLbl = new Label(compositeDx, SWT.NULL);
        idLbl.setText("Id:");

        //create the private idTxt
        idTxt = new Text(compositeDx, SWT.BORDER | SWT.SINGLE);
        idTxt.setText("alan");

        /* //create the privateKeyLbl label
          Label privateKeyLbl = new Label(compositeDx,SWT.NULL);
          privateKeyLbl.setText("Chiave privata:");

          //create the privateKeyTxt textbox
          privateKeyTxt = new Text(compositeDx, SWT.BORDER | SWT.SINGLE);
          privateKeyTxt.setText("");*/


        //create the titoloLbl label
        Label titoloLbl = new Label(compositeDx, SWT.NULL);
        titoloLbl.setText(messages.getString("titleContentMessage") + ":");

        //create the titoloTxt textbox
        titoloTxt = new Text(compositeDx, SWT.BORDER | SWT.SINGLE);
        titoloTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        titoloTxt.setText("");


        //create the content button group
        compositeOptionSx = new Composite(compositeDx, SWT.NULL);
        GridLayout layoutOptionSx = new GridLayout();
        layoutOptionSx.numColumns = 1;
        compositeOptionSx.setLayout(layoutOptionSx);
        compositeOptionSx.setLayoutData(new GridData(GridData.FILL_VERTICAL));


        //create the textLbl label
        Label textLbl = new Label(compositeOptionSx, SWT.NULL);
        textLbl.setText(messages.getString("textContentMessage") + ":");

        //create the textTxt textbox
        textTxt = new Text(compositeDx, SWT.BORDER | SWT.SINGLE);
        textTxt.setLayoutData(new GridData(GridData.FILL_BOTH));
        textTxt.setText("");


        //create the content button group
        Composite compositeOptionSx3 = new Composite(compositeOptionSx, SWT.NULL);
        GridLayout layoutOptionSx3 = new GridLayout();
        layoutOptionSx3.numColumns = 3;
        compositeOptionSx3.setLayout(layoutOptionSx3);
        compositeOptionSx3.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        //create prev button
        Button prevContent = new Button(compositeOptionSx3, SWT.PUSH);
        prevContent.setText("<-");
        prevContent.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        prevContent.addListener(SWT.MouseDown, new Listener() {
            public void handleEvent(Event arg0) {
                cControl.changeLblNContent(-1);
            }
        });

        //create the # content label
        lblNContent = new Label(compositeOptionSx3, SWT.NULL);
        lblNContent.setText("1");
        lblNContent.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        lblNContent.setForeground(display.getSystemColor(SWT.COLOR_DARK_GREEN));
        lblNContent.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        //create the next content button
        Button nextContent = new Button(compositeOptionSx3, SWT.PUSH);
        nextContent.setText("->");
        nextContent.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        nextContent.addListener(SWT.MouseDown, new Listener() {
            public void handleEvent(Event arg0) {
                cControl.changeLblNContent(1);//fileListener("file",parent.getShell());
            }
        });

        ////create the add content button
        Button addContent = new Button(compositeOptionSx, SWT.PUSH);
        addContent.setText(messages.getString("saveContentMessage"));
        addContent.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        addContent.addListener(SWT.MouseDown, new Listener() {
            public void handleEvent(Event arg0) {
                if (!(textTxt.getText().equals(""))) {
                    cControl.addContent(lblNContent.getText());//fileListener("file",parent.getShell());
                }
            }
        });

        //create the delete content
        Button deleteContent = new Button(compositeOptionSx, SWT.PUSH);
        deleteContent.setText(messages.getString("deleteContentMessage"));
        deleteContent.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        deleteContent.addListener(SWT.MouseDown, new Listener() {
            public void handleEvent(Event arg0) {
                cControl.deleteContent(lblNContent.getText());
            }
        });

        //create the add image button
        browseFileImage = new Button(compositeOptionSx, SWT.PUSH);
        browseFileImage.setText(messages.getString("addImageContentMessage"));
        browseFileImage.setEnabled(false);
        browseFileImage.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        browseFileImage.addListener(SWT.MouseDown, new Listener() {
            public void handleEvent(Event arg0) {
                fileListener("img", parent.getShell());
            }
        });

        //create the browse file button
        browseFileFile = new Button(compositeOptionSx, SWT.PUSH);
        browseFileFile.setText(messages.getString("addFileContentMessage"));
        browseFileFile.setEnabled(false);
        browseFileFile.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        browseFileFile.addListener(SWT.MouseDown, new Listener() {
            public void handleEvent(Event arg0) {
                fileListener("link", parent.getShell());
            }
        });

        //create the preview button
        Button preViewBtn = new Button(compositeOptionSx, SWT.PUSH);
        preViewBtn.setText(messages.getString("previewContentMessage"));
        preViewBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        preViewBtn.addListener(SWT.MouseDown, new Listener() {
            public void handleEvent(Event arg0) {
                showContent(cModel.getUserContentmap(), false);
            }
        });

        //create the Update button
        updateBtn = new Button(compositeOptionSx, SWT.PUSH);
        updateBtn.setText(messages.getString("updateBtnMessage"));
        updateBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        updateBtn.setEnabled(false);
        updateBtn.addListener(SWT.MouseDown, new Listener() {
            public void handleEvent(Event arg0) {
                cControl.updateRss();
            }
        });

        //create the state label
        Label lblStaticState = new Label(compositeDx, SWT.NULL);
        lblStaticState.setText(messages.getString("statusContentMessage"));

        //create the lblDinamicState label
        lblDinamicState = new Label(compositeDx, SWT.NULL);
        lblDinamicState.setText(messages.getString(cModel.getStatus()));
        lblDinamicState.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//--------- make the UPDATE THE RSS VIEW on the left------------------------------------------------//
        //create the Rss option  filter composite
        Group compositeDxRss = new Group(compositeMainDx, SWT.SHADOW_IN);
        compositeDxRss.setText(messages.getString("filterRssMessage"));
        compositeDxRss.setLayout(new GridLayout(2, false));
        compositeDxRss.setLayoutData(new GridData(GridData.FILL_BOTH));

        //create the updateRSSBtn button
        updateRSSBtn = new Button(compositeDxRss, SWT.PUSH);
        updateRSSBtn.setText(messages.getString("updateRssMessage"));
        updateRSSBtn.addListener(SWT.MouseDown, new Listener() {
            public void handleEvent(Event arg0) {
                cControl.viewRSS(0, messages.getString("allUsersMessage"));
            }
        });
        //create the orderby group
        Group orderingRssGroup = new Group(compositeDxRss, SWT.SHADOW_IN);
        orderingRssGroup.setText(messages.getString("orderRssByMessage") + ": ");
        orderingRssGroup.setLayout(new GridLayout(3, false));

        //create the order by user button
        Button orderByNameBtn = new Button(orderingRssGroup, SWT.RADIO);
        orderByNameBtn.setText(messages.getString("orderRssByUserMessage"));
        orderByNameBtn.addListener(SWT.MouseDown, new Listener() {
            public void handleEvent(Event arg0) {
                cControl.viewRSS(1, messages.getString("allUsersMessage"));
            }
        });
        //create the order by date button
        Button orderByDateBtn = new Button(orderingRssGroup, SWT.RADIO);
        orderByDateBtn.setText(messages.getString("orderRssByDateMessage"));
        orderByDateBtn.setSelection(true);
        orderByDateBtn.addListener(SWT.MouseDown, new Listener() {
            public void handleEvent(Event arg0) {
                cControl.viewRSS(2, messages.getString("allUsersMessage"));
            }
        });

        //create the order by single friend  combo
        friendsCmb = new Combo(orderingRssGroup, SWT.DROP_DOWN);
        friendsCmb.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                cControl.viewRSS(0, friendsCmb.getText());
            }
        });

//--------- make the FRIENDS on the left------------------------------------------------//
        //create the manage group
        Group compositeDxFriend = new Group(compositeMainDx, SWT.SHADOW_IN);
        compositeDxFriend.setText(messages.getString("friendMessage"));
        compositeDxFriend.setLayout(new GridLayout(3, false));
        compositeDxFriend.setLayoutData(new GridData(GridData.FILL_BOTH));

        //create the add friend Label button
        Label friendLbl = new Label(compositeDxFriend, SWT.NULL);
        friendLbl.setText(messages.getString("addFriendMessage"));

        //create the add friend txt
        friendIdTxt = new Text(compositeDxFriend, SWT.BORDER | SWT.SINGLE);
        friendIdTxt.setText("");
        friendIdTxt.addModifyListener(new org.eclipse.swt.events.ModifyListener() {
            public void modifyText(ModifyEvent arg0) {
                checkFriendTxt();
            }
        });

        //create the add Friend button
        addFriend = new Button(compositeDxFriend, SWT.PUSH);
        addFriend.setText(messages.getString("addFriendBtnMessage"));
        addFriend.setEnabled(false);
        addFriend.addListener(SWT.MouseDown, new Listener() {
            public void handleEvent(Event arg0) {
                cControl.manageFriend(1, "");
            }
        });

        //create the delete friend Label
        Label deleteFriendLbl = new Label(compositeDxFriend, SWT.NULL);
        deleteFriendLbl.setText(messages.getString("deleteFriendMessage"));

        //combo with the friends's name
        deleteFriendsCmb = new Combo(compositeDxFriend, SWT.DROP_DOWN);

        //create the delete Friend button
        deleteFriend = new Button(compositeDxFriend, SWT.PUSH);
        deleteFriend.setText(messages.getString("deleteFriendBtnMessage"));
        deleteFriend.addListener(SWT.MouseDown, new Listener() {
            public void handleEvent(Event arg0) {
                cControl.manageFriend(2, deleteFriendsCmb.getText());
            }
        });


        /*	//create the addFriend button
            Button testRead = new Button(compositeDxFriend, SWT.PUSH);
            testRead.setText("TEST READ");
            testRead.addListener(SWT.MouseDown, new Listener()
                {
                    public void handleEvent(Event arg0)
                    {
                            cControl.testRead("alanUser");
                    }
                });

            Button testWrite = new Button(compositeDxFriend, SWT.PUSH);
            testWrite.setText("TEST WRITE");
            testWrite.addListener(SWT.MouseDown, new Listener()
                {
                     public void handleEvent(Event arg0)
                     {
                        cControl.testWrite("alanUser");
                    }
                });*/


    }

    /**
     * meke the file navigation windows
     *
     * @param type  the type of file(img-file)
     * @param shell the plugin shell
     * @return null
     */
    private Listener fileListener(String type, Shell shell) {
        FileDialog fileDialog = new FileDialog(shell);

        if (type.equals("img")) {
            fileDialog.setText("Seleziona un'immagine");
            String[] filterName = {"JPG", "PNG", "BMP", "GIF", "ICO"};
            String[] filterExt = {"*.jpg", "*.png", "*.bmp", "*.gif", "*.ico"};
            fileDialog.setFilterExtensions(filterExt);
            fileDialog.setFilterNames(filterName);
        } else if (type.equals("file")) {
            fileDialog.setText("Seleziona un file");
            String[] filterName = {"TUTTI"};
            String[] filterExt = {"*.*"};
            fileDialog.setFilterExtensions(filterExt);
            fileDialog.setFilterNames(filterName);
        }


        String file = fileDialog.open();
        if (file != null) {
            // Set the text box to the new selection
            cControl.addFileToRSS(file, type, lblNContent.getText());
        }
        return null;
    }

    /**
     * turn enabled the addfriend button if the textFriend have text
     */
    private void checkFriendTxt() {
        if (friendIdTxt.getText().isEmpty()) {
            addFriend.setEnabled(false);
        } else {
            addFriend.setEnabled(true);

        }
    }


    /**
     * update the view when the controller change the model
     */
    public void update(Observable obs, Object obj) {

        lblDinamicState.setText(messages.getString(cModel.getStatus()));
        compositeDxAllRss.setText(messages.getString("yourRssGroupMessage") + ": (" + cModel.getNOfRss() + ")  [" + cModel.getNameUserRss() + "]");
        this.lblNContent.setText(cModel.getUserNContent());
        this.titoloTxt.setEnabled(cModel.getTitoloTxtEnabled());
        this.browseFileImage.setEnabled(!cModel.getTitoloTxtEnabled());
        this.browseFileFile.setEnabled(!cModel.getTitoloTxtEnabled());

        this.titoloTxt.setText(cModel.getTitoloTxt());
        this.textTxt.setText(cModel.getTextTxtText());
        if (cModel.getUserContentMapSize() > 0) {
            this.updateBtn.setEnabled(true);
        } else {
            this.updateBtn.setEnabled(false);
        }


        if (cModel.getNContentExist()) {
            lblNContent.setForeground(display.getSystemColor(SWT.COLOR_RED));
        } else {
            lblNContent.setForeground(display.getSystemColor(SWT.COLOR_DARK_GREEN));
        }
        if (cModel.getUpdateContentFlag()) {
            showContent(cModel.getRssTableGrid(), true);
        } else {
            showContent(cModel.getUserContentmap(), false);
        }
    }

    /**
     * output the content in the browser windows
     *
     * @param map    the map with the content
     * @param normal true if the map is the friendsMap, false if is the userContentMap
     */
    private void showContent(SortedMap<String, Rss> map, boolean normal) {
        SortedMap<String, Rss> mapRss = new TreeMap<String, Rss>();
        mapRss = map;
        //create a  Rss element
        Rss rssItem = null;
        Iterator<String> iterator = mapRss.keySet().iterator();
        int cont = 0, c = 0, lastIdOpened = 0;
        String key;
        String str = "";
        String[] strColor = {"#CCFFFF", "#FFFFCC"};

        //for each Rss in the map
        while (iterator.hasNext()) {
            cont++;
            //extract the Rss

            key = iterator.next();
            rssItem = mapRss.get(key);
            //if we have a new Rss
            if ((rssItem.getIdRss() != lastIdOpened)) {
                c++;
                if (cont > 1) {
                    str += "<p style='font-size:10; text-align:right;'>" + rssItem.getRssDate() + "</p>";
                    str += "<br /></div>";
                }
                str += "<hr>";
                str += "<div style=' font-size:12;background-color:" + strColor[c % 2] + "'><br />";
                str += "<p style='color:#ff0000;'>" + rssItem.getRssAuthor() + "</p>";
                str += "<p style='text-align:CENTER;' ><b>" + rssItem.getRssTitle() + "</b></p>";
                lastIdOpened = rssItem.getIdRss();
            }

            //check type, and append the output
            if (rssItem.getRssType().equals("text")) {
                str += "<p>" + rssItem.getRssText() + "</p>";
            }


            if (rssItem.getRssType().equals("img")) {
                String srcStr = "";
                if (normal) {
                    //srcStr=pluginInterface.getPluginDirectoryName()+"\\friends_dir\\file\\"+rssItem.getRssTitle();
                    srcStr = pluginInterface.getPluginDirectoryName() + "\\friends_dir\\" + rssItem.getRssAuthor() + "_file\\" + rssItem.getRssTitle();
                } else {
                    srcStr = rssItem.getRssLink();
                }
                str += "<div style='text-align:center;'><img  src='" + srcStr + "' />";
                str += "<p>" + rssItem.getRssText() + "</p></div>";
            }

            if (rssItem.getRssType().equals("link")) {
                str += "<p><a href='" + rssItem.getRssLink() + "' />" + rssItem.getRssText() + "</a></p>";
            }
            System.out.println(rssItem.getRssLink());


        }
        if (cont > 1) {
            str += "<p style='font-size:10; text-align:right;'>" + rssItem.getRssDate() + "</p>";
            str += "<br /></div>";
        }
        //output the content
        browser.setText(str);
    }


    /**
     * manage the UI events
     */
    public boolean eventOccurred(UISWTViewEvent event) {
        switch (event.getType()) {
            case UISWTViewEvent.TYPE_CREATE:
                if (isCreated) // Comment this out if you want to allow multiple views!
                    return false;
                isCreated = true;
                break;

            case UISWTViewEvent.TYPE_DESTROY:
                //delete(); // Remove if not defined
                isCreated = false;
                break;

            case UISWTViewEvent.TYPE_INITIALIZE:
                initialize((Composite) event.getData());
                break;

            case UISWTViewEvent.TYPE_LANGUAGEUPDATE:
                //updateLanguage(); // Remove if not defined
                break;

            case UISWTViewEvent.TYPE_REFRESH:
                //refresh(); // Remove if not defined
                contRefresh++;
                //System.out.println(contRefresh);
                if ((contRefresh > 60) && (cModel.getUpdateContentFlag())) {
                    //cControl.viewRSS(0,"");
                    contRefresh = 0;
                }
                break;
        }

        return true;
    }
}
