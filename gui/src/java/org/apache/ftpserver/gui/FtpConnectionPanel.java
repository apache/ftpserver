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

import org.apache.ftpserver.core.UserImpl;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * This panel displays all the logged in users.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public
class FtpConnectionPanel extends PluginPanel {

    private static final String SPY_IMG = "org/apache/ftpserver/gui/spy.gif";
    private static final String DISCONNECT_IMG = "org/apache/ftpserver/gui/disconnect.gif";
    private static final String RELOAD_IMG = "org/apache/ftpserver/gui/reload.gif";

    private JTable mjConnectionTable;
    private FtpConnectionTableModel mModel;


    /**
     * Instantiate login panel.
     */
    public FtpConnectionPanel(CommonHandler commonHandler, JTree tree) {
        super(commonHandler, tree);
        mModel = new FtpConnectionTableModel(commonHandler);
        initComponents();
    }

    /**
     * Initialize UI components
     */
    private void initComponents() {

        setLayout(new BorderLayout());

        mjConnectionTable = new JTable(mModel);
        mjConnectionTable.setPreferredScrollableViewportSize(new Dimension(470, 320));
        mjConnectionTable.setColumnSelectionAllowed(false);
        JScrollPane bottomPane = new JScrollPane(mjConnectionTable,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(bottomPane, BorderLayout.CENTER);


        // buttons
        JPanel btnPane = new JPanel(new FlowLayout(FlowLayout.CENTER));

        JButton jLogoutBtn = new JButton("Disconnect", GuiUtils.createImageIcon(DISCONNECT_IMG));
        btnPane.add(jLogoutBtn);

        JButton jSpyBtn = new JButton("Spy User", GuiUtils.createImageIcon(SPY_IMG));
        btnPane.add(jSpyBtn);

        JButton jReloadBtn = new JButton("Reload", GuiUtils.createImageIcon(RELOAD_IMG));
        btnPane.add(jReloadBtn);

        add(btnPane, BorderLayout.SOUTH);

        // event handlers
        jLogoutBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                closeConnection();
            }
        });

        jSpyBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                spyUser();
            }
        });

        jReloadBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                reload();
            }
        });

    }

    /**
     * Logout this user
     */
    private void closeConnection() {
        int indices[] = mjConnectionTable.getSelectedRows();
        if (indices.length == 0) {
            GuiUtils.showErrorMessage(getCommonHandler().getTopFrame(), "Please select connection(s).");
            return;
        }

        boolean response = GuiUtils.getConfirmation(getCommonHandler().getTopFrame(), "Do you really want to close the selected connection(s)?");
        if (!response) {
            return;
        }

        try {
            for (int i = indices.length; --i >= 0;) {
                UserImpl user = mModel.getUser(indices[i]);
                if (user != null) {
                    getCommonHandler().getConnectionService().closeConnection(user.getSessionId());
                }
            }
        } catch (Exception ex) {
            getCommonHandler().handleException(ex);
        }
    }

    /**
     * Monitor the selected user.
     */
    private void spyUser() {
        int indices[] = mjConnectionTable.getSelectedRows();
        if (indices.length == 0) {
            GuiUtils.showErrorMessage(getCommonHandler().getTopFrame(), "Please select connection(s).");
            return;
        }

        // monitor all the selected users
        try {
            for (int i = indices.length; --i >= 0;) {
                UserImpl thisUser = mModel.getUser(indices[i]);
                if (thisUser != null) {
                    FtpSpyContainerPanel spyPanel = (FtpSpyContainerPanel) ((FtpTree) getTree()).getPluginPanel("Spy");
                    spyPanel.monitorConnection(thisUser);
                }
            }
        } catch (Exception ex) {
            getCommonHandler().handleException(ex);
        }

        // select tree spy node
        Object[] spyPath = new Object[]{
            getTree().getModel().getRoot(), "Spy"
        };
        getTree().setSelectionPath(new TreePath(spyPath));

    }

    /**
     * Refresh window.
     */
    private void reload() {
        try {
            mModel.reload();
        } catch (Exception ex) {
            getCommonHandler().handleException(ex);
        }
    }

    /**
     * Close it
     */
    public void close() {
        mModel.close();
    }

}
