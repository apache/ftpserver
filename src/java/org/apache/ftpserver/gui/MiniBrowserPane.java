/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1997-2003 The Apache Software Foundation. All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *    "This product includes software developed by the
 *    Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software
 *    itself, if and wherever such third-party acknowledgments
 *    normally appear.
 *
 * 4. The names "Incubator", "FtpServer", and "Apache Software Foundation"
 *    must not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation. For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
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
