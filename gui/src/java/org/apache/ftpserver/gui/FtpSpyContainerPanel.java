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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.ftpserver.FtpUserImpl;
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
    public void monitorConnection(FtpUserImpl user) {
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
    private String getCaption(FtpUserImpl user) {
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
