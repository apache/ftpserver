/* ====================================================================
 * Copyright 2002 - 2004
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * $Id$
 */

package org.apache.ftpserver.gui;

import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;

/**
 * Mini browser panel to display HTML pages.
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public
class MiniBrowserPane extends JPanel implements HyperlinkListener {

    private final static String HOME_IMG = "org/apache/ftpserver/gui/home.gif";
    private final static String LOAD_IMG = "org/apache/ftpserver/gui/load.gif";
    private final static String BLANK_PAGE = "org/apache/ftpserver/gui/blank.html";


    private Component mFrame;
    private URL mHome;
    private boolean mDisplayUrl;
    private boolean mDisplayHome;

    private JEditorPane mjEditorPane;
    private JTextField mjUrlField;


    /**
     * Constructor
     * @param frame Frame component
     * @param home url, may be null
     * @param displayUrl display url at the beginning
     * @param displayHome home button displayed
     */
    public MiniBrowserPane(Component frame, URL home, boolean dispUrl, boolean dispHome) {
        super(new BorderLayout());
        mFrame = frame;
        mHome = home;
        mDisplayUrl = dispUrl;
        mDisplayHome = dispHome;
        initComponents();
        loadURL(mHome);
    }


    /**
     * Initialize all UI components.
     */
    private void initComponents() {

        // top pane
        if(mDisplayUrl) {
            JPanel topPane = new JPanel(new BorderLayout());
            add(topPane, BorderLayout.NORTH);

            mjUrlField = new JTextField();
            mjUrlField.setColumns(20);
            mjUrlField.setBackground(new Color(0xEE, 0xEE, 0xEE));
            topPane.add(mjUrlField, BorderLayout.CENTER);

            JButton loadBtn = new JButton("Load", GuiUtils.createImageIcon(LOAD_IMG));
            topPane.add(loadBtn, BorderLayout.EAST);
            loadBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    String urlTxt = mjUrlField.getText();
                    URL url = null;
                    try {
                        url = new URL(urlTxt);
                    }
                    catch(Exception ex) {
                        GuiUtils.showWarningMessage(mFrame, "Not a valid url : " + urlTxt);
                    }
                    loadURL(url);
                }
            });
        }


        // middle pane
        mjEditorPane = new JEditorPane();
        mjEditorPane.setEditable(false);
        mjEditorPane.setContentType("text/html");
        mjEditorPane.addHyperlinkListener(this);

        JScrollPane editorScrollPane = new JScrollPane(mjEditorPane);
        add(editorScrollPane, BorderLayout.CENTER);


        // bottom pane
        if(mDisplayHome) {
            JPanel bottomPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
            add(bottomPane, BorderLayout.SOUTH);

            JButton homeBtn = new JButton("Home", GuiUtils.createImageIcon(HOME_IMG));
            bottomPane.add(homeBtn);
            homeBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    loadURL(mHome);
                }
            });
        }
    }


    /**
     * Handle user mouse click.
     */
    public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            if (e instanceof HTMLFrameHyperlinkEvent) {
                HTMLFrameHyperlinkEvent  evt = (HTMLFrameHyperlinkEvent)e;
                HTMLDocument doc = (HTMLDocument)mjEditorPane.getDocument();
                doc.processHTMLFrameHyperlinkEvent(evt);
            }
            else {
                loadURL(e.getURL());
            }
        }
    }


    /**
     * Load URL
     */
    private void loadURL(URL url) {

        try {

            // load blank page
            if(url == null) {
                url = getClass().getClassLoader().getResource(BLANK_PAGE);
                if(mjUrlField != null) {
                    mjUrlField.setText("");
                }
            }
            else {
                if(mjUrlField != null) {
                    mjUrlField.setText(url.toString());
                }
            }

            // load URL
            if(url != null) {
                mjEditorPane.setPage(url);
            }
        }
        catch(Exception ex) {
            GuiUtils.showWarningMessage(mFrame, "Cannot load " + url);
        }
    }

}
