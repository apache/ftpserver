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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;

import org.apache.ftpserver.ftplet.FileObject;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.interfaces.FtpServerContext;
import org.apache.ftpserver.listener.Connection;

/**
 * This panel monitor all user directory create and remove activities.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class DirectoryPanel extends PluginPanel {
    
    private static final long serialVersionUID = 2890691567985219146L;

    private final static String HEADERS[] = {
            "Created",
            "Removed"
    };
    
    private FtpServerContext        serverContext;
    private FtpDirectoryTableModel[] models;
    
    
    /**
     * Instantiate directory panel.
     */
    public DirectoryPanel(PluginPanelContainer container) {
        super(container);
        initComponents();
    }
     
    
    /**
     * Initialize UI components
     */
    private void initComponents() {
        setLayout(new BorderLayout());
        JTabbedPane tabPane = new JTabbedPane();
        add(tabPane, BorderLayout.CENTER);
        
        models = new FtpDirectoryTableModel[2];
        for(int i=0; i<models.length; ++i) {
            models[i] = new FtpDirectoryTableModel();
            tabPane.addTab(HEADERS[i], createTabComponent(i));
        }
    }
     
    /**
     * Create tab component.
     */
    private JComponent createTabComponent(final int index) {        
        
        JPanel panel = new JPanel(new BorderLayout()); 
        
        JTable dirTable = new JTable(models[index]);
        dirTable.setPreferredScrollableViewportSize(new Dimension(470, 300));
        dirTable.setColumnSelectionAllowed(false);
        JScrollPane scrollPane = new JScrollPane(dirTable, 
                                     JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                     JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);                          
        panel.add(scrollPane, BorderLayout.CENTER);
        
        
        // button
        JPanel bottomPanel = new JPanel();
        JButton jResetBtn = new JButton("Clear");
        bottomPanel.add(jResetBtn);
        panel.add(bottomPanel, BorderLayout.SOUTH);
        
        // event handler
        jResetBtn.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                models[index].clear();
             }
        });
        return panel;
    }
    
    
    /**
     * Make directory notification.
     */
    public void notifyMkdir(Connection con, FileObject file) {
        User user = con.getSession().getUser();
        models[0].newEntry(file.getFullName(), user);
    }
    
    
    /**
     * Remove directory notification.
     */
    public void notifyRmdir(Connection con, FileObject file) {
        User user = con.getSession().getUser();
        models[1].newEntry(file.getFullName(), user);
    }
    
    
    /**
     * Refresh the panel - set the ftp config.
     */
    public void refresh(FtpServerContext serverContext) {
        this.serverContext = serverContext;
        models[0].clear();
        models[1].clear();
    }
    
    
    /**
     * Can this panel be displayed.
     */
    public boolean canBeDisplayed() {
        return (serverContext != null);
    }
 
    
    /**
     * String representation.
     */
    public String toString() {
        return "Directory";
    }   
}
