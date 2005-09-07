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

import org.apache.ftpserver.interfaces.IFtpConfig;
import org.apache.ftpserver.interfaces.IIpRestrictor;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * IP restrictor panel.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class IPRestrictorPanel extends PluginPanel {

    private static final long serialVersionUID = -1871174667851171193L;
    
    private IFtpConfig m_fconfig;
    private IPRestrictorTable m_table;
    
    /**
     * Default constructor.
     */
    public IPRestrictorPanel(PluginPanelContainer container) {
        super(container);
        initComponents();
    }
    
    /**
     * Initialize UI components
     */
    private void initComponents() {
        setLayout(new BorderLayout());
        
        m_table = new IPRestrictorTable();
        add(m_table, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBorder(BorderFactory.createEtchedBorder());
        add(buttonPanel, BorderLayout.SOUTH);
        
        JButton saveBtn = new JButton("Save");
        saveBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                saveData();
            }
        });
        
        buttonPanel.add(saveBtn);
        
        JButton reloadBtn = new JButton("Reload");
        reloadBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                reloadData();
            }
        });
        buttonPanel.add(reloadBtn);
    }

    /**
     * Save data.
     */
    private void saveData() {
        try {
            IIpRestrictor restrictor = m_fconfig.getIpRestrictor();
            restrictor.setPermissions(m_table.getData());
        }
        catch(Exception ex) {
            GuiUtils.showErrorMessage(this, "Cannot save IP entries.");
        }
    }

    /**
     * Reload data.
     */
    private void reloadData() {
        try {
            Object[][] perms = null;
            if(m_fconfig != null) {
                IIpRestrictor restrictor = m_fconfig.getIpRestrictor();
                perms = restrictor.getPermissions();
            }
            m_table.setData(perms);
        }
        catch(Exception ex) {
            GuiUtils.showErrorMessage(this, "Cannot load IP entries.");
        }
    }

    /**
     * Refresh - set the ftp config.
     */
    public void refresh(IFtpConfig ftpConfig) {
        m_fconfig = ftpConfig;
        reloadData();
    }
    
    /**
     * This panel can be displayed only when server is
     * running ie. ftp config is not null.
     */
    public boolean canBeDisplayed() {
        return (m_fconfig != null);
    }
    
    /**
     * Get the string representation.
     */
    public String toString() {
        return "IP Restrictor";
    }
}
