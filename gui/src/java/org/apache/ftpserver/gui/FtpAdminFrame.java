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

import org.apache.ftpserver.remote.interfaces.RemoteHandlerInterface;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.rmi.RemoteException;


/**
 * Ftp server admin user interface.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public
class FtpAdminFrame extends JFrame implements TreeSelectionListener {

    private static final ImageIcon ICON_IMG = GuiUtils.createImageIcon("org/apache/ftpserver/gui/server.gif");

    private JTabbedPane mjTabPane;

    private FtpTree mjFtpTree = null;
    private JPanel mjFtpPane = null;

    private CommonHandler mCommonHandler = null;

    /**
     * Creates new form MyServerFrame
     */
    public FtpAdminFrame(CommonHandler commonHandler) throws RemoteException {
        mCommonHandler = commonHandler;
        initComponents();
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     */
    private void initComponents() throws RemoteException {
        mjTabPane = new JTabbedPane();

        // top level
        getContentPane().setLayout(new BorderLayout());
        JSplitPane jSplitPane = new JSplitPane();
        jSplitPane.setDividerSize(2);

        // left pane
        mjFtpTree = new FtpTree(mCommonHandler);
        mjFtpTree.addTreeSelectionListener(this);
        JScrollPane custPane = new JScrollPane(mjFtpTree,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        jSplitPane.setLeftComponent(custPane);

        // right pane
        mjFtpPane = new JPanel();
        mjFtpPane.setLayout(new BorderLayout());
        jSplitPane.setRightComponent(mjFtpPane);

        mjFtpPane.add(mjFtpTree.getRootPanel());

        jSplitPane.setDividerLocation(100);
        mjTabPane.addTab(RemoteHandlerInterface.DISPLAY_NAME, jSplitPane);

        getContentPane().add(mjTabPane, BorderLayout.CENTER);

        pack();
        setTitle("Ftp Server");
        if (ICON_IMG != null) {
            setIconImage(ICON_IMG.getImage());
        }
        setSize(new Dimension(610, 450));
        GuiUtils.setLocation(this);
    }

    /*
     * Handle window closing event.
     */
    protected void processWindowEvent(WindowEvent e) {
        int id = e.getID();
        if (id == WindowEvent.WINDOW_CLOSING) {
            if (!GuiUtils.getConfirmation(mCommonHandler.getTopFrame(), "Do you really want to exit?")) {
                return;
            }
            mCommonHandler.terminate();
        } else {
            super.processWindowEvent(e);
        }
    }

    /**
     * Handle tree selection
     */
    public void valueChanged(TreeSelectionEvent e) {
        JPanel dispPane = mjFtpTree.getSelectedPanel();
        if (dispPane != null) {
            GuiUtils.showNewPanel(mjFtpPane, dispPane);
        }
    }

    /**
     * Release resources
     */
    public void close() {
        setVisible(false);
        mjFtpTree.close();
        dispose();
    }

}
