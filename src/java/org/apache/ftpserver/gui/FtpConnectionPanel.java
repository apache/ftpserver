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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

import org.apache.ftpserver.FtpUser;


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
        if(indices.length == 0) {
            GuiUtils.showErrorMessage(getCommonHandler().getTopFrame(), "Please select connection(s).");
            return;
        }

        boolean response = GuiUtils.getConfirmation(getCommonHandler().getTopFrame(), "Do you really want to close the selected connection(s)?");
        if(!response) {
            return;
        }

        try {
            for(int i=indices.length; --i>=0; ) {
                FtpUser user = mModel.getUser(indices[i]);
                if(user != null) {
                    getCommonHandler().getConnectionService().closeConnection(user.getSessionId());
                }
            }
        }
        catch(Exception ex) {
            getCommonHandler().handleException(ex);
        }
    }

    /**
     * Monitor the selected user.
     */
    private void spyUser() {
        int indices[] = mjConnectionTable.getSelectedRows();
        if(indices.length == 0) {
            GuiUtils.showErrorMessage(getCommonHandler().getTopFrame(), "Please select connection(s).");
            return;
        }

        // monitor all the selected users
        try {
            for(int i=indices.length; --i>=0; ) {
                FtpUser thisUser = mModel.getUser(indices[i]);
                if (thisUser != null) {
                    FtpSpyContainerPanel spyPanel = (FtpSpyContainerPanel)((FtpTree)getTree()).getPluginPanel("Spy");
                    spyPanel.monitorConnection(thisUser);
                }
            }
        }
        catch(Exception ex) {
            getCommonHandler().handleException(ex);
        }

        // select tree spy node
        Object[] spyPath = new Object[] {
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
        }
        catch(Exception ex) {
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
