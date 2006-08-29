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

import java.awt.Dimension;
import java.awt.event.WindowEvent;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;

/**
 * This is the FTP server UI starting point.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class ServerFrame extends JFrame {
    
    private static final long serialVersionUID = 8399655106217258507L;
    
    private PluginPanelContainer pluginContainer;
    
    /**
     * Constructor - create tree and show the root panel.
     */
    public ServerFrame() {
        JTabbedPane tabPane = new JTabbedPane(); 
        getContentPane().add(tabPane);
        
        // add all plugin panels
        pluginContainer = new TreePluginPanelContainer();
        pluginContainer.add(new RootPanel(pluginContainer));
        pluginContainer.add(new UserManagerPanel(pluginContainer));
        pluginContainer.add(new IPRestrictorPanel(pluginContainer));
        pluginContainer.add(new MessagePanel(pluginContainer));
        pluginContainer.add(new ConnectionPanel(pluginContainer));
        pluginContainer.add(new SpyPanelContainer(pluginContainer));
        pluginContainer.add(new FilePanel(pluginContainer));
        pluginContainer.add(new DirectoryPanel(pluginContainer));
        pluginContainer.add(new LoggerPanel(pluginContainer));
        pluginContainer.add(new StatisticsPanel(pluginContainer));
        
        tabPane.addTab("FTP", pluginContainer.getComponent());
        pluginContainer.setSelectedIndex(PluginPanelContainer.ROOT_INDEX);
        
        // show frame
        setTitle("Apache FTP Server");
        ImageIcon icon = GuiUtils.createImageIcon("org/apache/ftpserver/gui/logo.gif");
        if (icon != null) {
            setIconImage(icon.getImage());
        }
        setSize(new Dimension(620, 420));
        GuiUtils.setLocation(this);
        setVisible(true);
        toFront();
    }
    
    /*
     * Handle window closing event.
     */ 
    protected void processWindowEvent(WindowEvent e) {
        int id = e.getID();
        if (id == WindowEvent.WINDOW_CLOSING) {
            if ( !GuiUtils.getConfirmation(this, "Do you really want to exit?") ) {
                return;
            }
            super.processWindowEvent(e);
            RootPanel root = (RootPanel)pluginContainer.getPluginPanel(0);
            root.stopServer();
            dispose();
            System.exit(0);
        } 
        else {
            super.processWindowEvent(e);
        }    
    }

    /**
     * Server GUI starting point.
     */
     public static void main (String args[]) {
        System.out.println("Opening UI window, please wait...");            
        new ServerFrame();
     }
}
