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

import java.io.InputStream;
import java.io.IOException;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import org.apache.ftpserver.FtpUser;
import org.apache.ftpserver.util.IoUtils;

/**
 * This panel holds all connection spy panels.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public
class FtpSpyContainerPanel extends PluginPanel implements ChangeListener {

    public static final String CLEAR_IMG = "org/apache/ftpserver/gui/clear.gif";
    public static final String CLOSE_IMG = "org/apache/ftpserver/gui/close.gif";
    public static final String DISCONNECT_IMG = "org/apache/ftpserver/gui/disconnect.gif";

    public static final String SPY_PAGE = "org/apache/ftpserver/gui/spy.html";

    private JTabbedPane mjTabbedPane   = null;

    private JButton mjClearButton      = null;
    private JButton mjCloseButton      = null;
    private JButton mjDisconnectButton = null;
    private JScrollPane mjAboutPane    = null;


    /**
     * Constructor - create empty tabbed frame
     */
    public FtpSpyContainerPanel(CommonHandler commonHandler, JTree tree) {
        super(commonHandler, tree);
        initComponents();
    }

    /**
     * Initialize all components
     */
    private void initComponents() {
        setLayout(new BorderLayout());
        mjTabbedPane = new JTabbedPane();
        mjTabbedPane.setPreferredSize(new Dimension(470, 340));
        mjTabbedPane.addChangeListener(this);
        add(mjTabbedPane, BorderLayout.CENTER);

        JPanel bottomPane = new JPanel();
        bottomPane.setLayout(new FlowLayout(FlowLayout.CENTER));

        mjClearButton = new JButton("Clear", GuiUtils.createImageIcon(CLEAR_IMG));
        bottomPane.add(mjClearButton);
        mjClearButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                clearLog();
             }
        });

        mjDisconnectButton = new JButton("Disconnect", GuiUtils.createImageIcon(DISCONNECT_IMG));
        bottomPane.add(mjDisconnectButton);
        mjDisconnectButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                disconnectUser();
             }
        });

        mjCloseButton = new JButton("Close", GuiUtils.createImageIcon(CLOSE_IMG));
        bottomPane.add(mjCloseButton);
        mjCloseButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                closePane();
             }
        });
        add(bottomPane, BorderLayout.SOUTH);

        // initialize component to be displayed if
        // there is no currently monitored connection
        JEditorPane editorPane = new JEditorPane();
        editorPane.setEditable(false);
        editorPane.setContentType("text/html");
        InputStream is = null;
        try {
            is = getClass().getClassLoader().getResourceAsStream(SPY_PAGE);
            if (is != null) {
                editorPane.read(is, null);
            }
        }
        catch(IOException ex) {
        }
        finally {
            IoUtils.close(is);
        }

        mjAboutPane = new JScrollPane(editorPane,
                            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        mjTabbedPane.addTab("Spy", mjAboutPane);
    }


    /**
     * Clear user log
     */
    private void clearLog() {
        Component selComp = mjTabbedPane.getSelectedComponent();
        if ( (selComp != null) && (selComp != mjAboutPane) ) {
            ((SpyPanel)selComp).clearLog();
        }
    }

    /**
     * Close connection spy panel.
     */
    private void closePane() {
        Component selComp = mjTabbedPane.getSelectedComponent();
        if ( (selComp != null) && (selComp != mjAboutPane) ) {
            ((SpyPanel)selComp).close();
            mjTabbedPane.remove(selComp);
            if (mjTabbedPane.getTabCount() == 0) {
                mjTabbedPane.addTab("Spy", mjAboutPane);
            }
        }
    }

    /**
     * Disconnected user connection
     */
    private void disconnectUser() {
        Component selComp = mjTabbedPane.getSelectedComponent();
        if ( (selComp != null) && (selComp != mjAboutPane) ) {
            boolean bConf = GuiUtils.getConfirmation(getCommonHandler().getTopFrame(), "Do you want to close the connection?");
            if(bConf) {
                ((SpyPanel)selComp).disconnect();
            }
        }
    }

    /**
     * Monitor connection
     */
    public void monitorConnection(FtpUser user) {
        String userName = getCaption(user);
        String userSession = user.getSessionId();

        // don't add another tab if already being monitored
        int tabCount = mjTabbedPane.getTabCount();
        for(int i=0; i<tabCount; i++) {
            Component selComp = mjTabbedPane.getComponentAt(i);
            if ( (selComp != null) && (selComp != mjAboutPane) ) {
                String tabUserSessionId = ((SpyPanel)selComp).getSessionId();
                if (tabUserSessionId.equals(userSession)) {
                    mjTabbedPane.setTitleAt(i, userName);
                    mjTabbedPane.setSelectedIndex(i);
                    return;
                }
            }
        }

        // add new tab
        try {
            SpyPanel spyPane = new SpyPanel(getCommonHandler(), user);
            mjTabbedPane.remove(mjAboutPane);
            mjTabbedPane.add(userName, spyPane);
            mjTabbedPane.setSelectedComponent(spyPane);
        }
        catch(Exception ex) {
            getCommonHandler().handleException(ex);
        }
    }

    /**
     * Get tab caption.
     */
    private String getCaption(FtpUser user) {
        String name = "";
        if (user != null) {
            name = user.getName();
            if (name == null) {
                name = "UNKNOWN";
            }
        }
        return name;
    }


    /**
     * Tab change notification
     */
    public void stateChanged(ChangeEvent e) {
        Component selComp = mjTabbedPane.getSelectedComponent();
        boolean isUserPane = selComp != mjAboutPane;
        mjClearButton.setEnabled(isUserPane);
        mjCloseButton.setEnabled(isUserPane);
        mjDisconnectButton.setEnabled(isUserPane);
    }

    /**
     * Stop all spying
     */
    public void close() {
        int tabCount = mjTabbedPane.getTabCount();
        for(int i=tabCount; --i>=0; ) {
            Component selComp = mjTabbedPane.getComponentAt(i);
            if ( (selComp != null) && (selComp != mjAboutPane) ) {
                ((SpyPanel)selComp).close();
                mjTabbedPane.remove(selComp);
            }
        }
    }

}
