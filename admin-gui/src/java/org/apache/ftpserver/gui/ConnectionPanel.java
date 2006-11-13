/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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

import org.apache.ftpserver.ftplet.FtpConfig;
import org.apache.ftpserver.interfaces.Connection;
import org.apache.ftpserver.interfaces.ServerFtpConfig;

/**
 * This panel shows all the connections.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class ConnectionPanel extends PluginPanel {

    private static final long serialVersionUID = 3774741162954995177L;
    
    private ServerFtpConfig fconfig;
    private JTable conTable;   
    private FtpConnectionTableModel model;
    
    /**
     * Instantiate connection panel.
     */
    public ConnectionPanel(PluginPanelContainer container) {
        super(container);
        model = new FtpConnectionTableModel();
        initComponents();
    }

    
    /**
     * Initialize UI components.
     */
    private void initComponents() {
        setLayout(new BorderLayout());
        
        conTable = new JTable(model);
        conTable.setPreferredScrollableViewportSize(new Dimension(470, 320));
        conTable.setColumnSelectionAllowed(false);
        JScrollPane bottomPane = new JScrollPane(conTable, 
                                     JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                     JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);                          
        add(bottomPane, BorderLayout.CENTER);
        
        
        // buttons
        JPanel btnPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
        
        JButton jDisconnectBtn = new JButton("Disconnect");
        btnPane.add(jDisconnectBtn);
        
        JButton jSpyBtn = new JButton("Spy User");
        btnPane.add(jSpyBtn);
        
        JButton jReloadBtn = new JButton("Reload");
        btnPane.add(jReloadBtn);
        
        add(btnPane, BorderLayout.SOUTH);

        // event handlers
        jDisconnectBtn.addActionListener(new ActionListener() {
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
                refresh(fconfig);
             }
        });
    }
    
    
    /**
     * Close connection.
     */
    private void closeConnection() {
        int indices[] = conTable.getSelectedRows();
        if(indices.length == 0) {
            GuiUtils.showErrorMessage(this, "Please select connection(s).");
            return;
        }
        
        boolean response = GuiUtils.getConfirmation(this, "Do you really want to close the selected connection(s)?");
        if(!response) {
            return;
        }
        
        for(int i=indices.length; --i>=0; ) {
            Connection con = model.getConnection(indices[i]);
            if(con != null) {
                fconfig.getConnectionManager().closeConnection(con);
            }
        }
    }
    
    
    /**
     * Spy user
     */
    private void spyUser() {
        int indices[] = conTable.getSelectedRows();
        if(indices.length == 0) {
            GuiUtils.showErrorMessage(this, "Please select connection(s).");
            return;
        }
        
        // monitor all the selected users
        SpyPanelContainer spyContainer = (SpyPanelContainer)getContainer().getPluginPanel(PluginPanelContainer.SPY_INDEX);
        for(int i=indices.length; --i>=0; ) {   
            Connection con = model.getConnection(indices[i]);
            if (con != null) {
                spyContainer.monitorConnection(con); 
            }
        }
        
        // open the spy panel
        getContainer().setSelectedIndex(PluginPanelContainer.SPY_INDEX);
    }
    
    
    /** 
     * Refresh the ftp configuration
     */
    public void refresh(ServerFtpConfig ftpConfig) {
        fconfig = ftpConfig;
        model.refresh(ftpConfig);
    }

    
    /** 
     * This can be displayed only when the server is running.
     */
    public boolean canBeDisplayed() {
        return (fconfig != null);
    }

    
    /**
     * Get the string representation.
     */
    public String toString() {
        return "Connections";
    }
}
