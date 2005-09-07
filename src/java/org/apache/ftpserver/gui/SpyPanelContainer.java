// $Id$
/*
 * Copyright 2004 The Apache Software Foundation
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
 */
package org.apache.ftpserver.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;

import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.interfaces.IConnection;
import org.apache.ftpserver.interfaces.IFtpConfig;

/**
 * This panel monitors user request/replies.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class SpyPanelContainer extends PluginPanel {
    
    private static final long serialVersionUID = 7426681776615720958L;
    
    private JTabbedPane m_tabbedPane;
    private JLabel m_defaultComp;
    
    private IFtpConfig m_fconfig;
    
    
    /**
     * Constructor - create empty tabbed frame
     */
    public SpyPanelContainer(PluginPanelContainer container) {
        super(container);
        initComponents();
    }
    
    /**
     * Initialize UI components.
     */
    private void initComponents() {
        setLayout(new BorderLayout());
        m_tabbedPane = new JTabbedPane();
        m_tabbedPane.setPreferredSize(new Dimension(470, 340));
        add(m_tabbedPane, BorderLayout.CENTER);
        
        // initialize component to be displayed if 
        // there is no currently monitored connection
        m_defaultComp = new JLabel("Please, select a connection.", JLabel.CENTER);
        m_defaultComp.setFont(new Font(null, Font.BOLD, 17));
        m_defaultComp.setForeground(Color.blue);
        m_defaultComp.setBackground(Color.white);
        m_defaultComp.setBorder(BorderFactory.createEtchedBorder());
        m_defaultComp.setOpaque(true);
        
        m_tabbedPane.addTab("Spy", m_defaultComp);
    }

    /**
     * Monitor connection.
     */
    public void monitorConnection(IConnection con) {    
        String userName = getCaption(con); 
        
        // don't add another tab if already being monitored
        int tabCount = m_tabbedPane.getTabCount();
        for(int i=0; i<tabCount; i++) {
            Component selComp = m_tabbedPane.getComponentAt(i);
            if ( (selComp != null) && (selComp != m_defaultComp) ) {
                IConnection tabcon = ((SpyPanel)selComp).getConnection();
                if (tabcon == con) {
                    m_tabbedPane.setTitleAt(i, userName);
                    m_tabbedPane.setSelectedIndex(i);
                    return;
                }
            }
        }
        
        // add new tab
        SpyPanel spyPane = new SpyPanel(m_fconfig, con, m_tabbedPane, m_defaultComp);
        m_tabbedPane.remove(m_defaultComp);
        m_tabbedPane.add(userName, spyPane);
        m_tabbedPane.setSelectedComponent(spyPane);
    } 
    
    /**
     * Get tab caption.
     */
    private String getCaption(IConnection con) {
        User user = con.getRequest().getUser();
        String name = "UNKNOWN";
        if(user != null) {
            String tmp = user.getName();
            if(tmp != null) {
                name = tmp;
            }
        }
        return name;
    } 
        
    /** 
     * Refresh the ftp configuration
     */
    public void refresh(IFtpConfig ftpConfig) {
        m_fconfig = ftpConfig;
        int tabCount = m_tabbedPane.getTabCount();
        for(int i=0; i<tabCount; i++) {
            Component tabComp = m_tabbedPane.getComponentAt(i);
            if ( (tabComp != null) && (tabComp != m_defaultComp) ) {
                ((SpyPanel)tabComp).closePane();
                m_tabbedPane.remove(tabComp);
            }
        }
        
        m_tabbedPane.addTab("Spy", m_defaultComp);
    }

    
    /** 
     * This can be displayed only when the server is running.
     */
    public boolean canBeDisplayed() {
        return (m_fconfig != null);
    }

    
    /**
     * Get the string representation.
     */
    public String toString() {
        return "Spy";
    }
}
