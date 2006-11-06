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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.apache.ftpserver.interfaces.IFtpConfig;
import org.apache.ftpserver.interfaces.IIpRestrictor;

/**
 * IP restrictor panel.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public
class IPRestrictorPanel extends PluginPanel {

    private static final long serialVersionUID = -1871174667851171193L;

    private IFtpConfig fconfig;
    private IPRestrictorTable table;

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

        table = new IPRestrictorTable();
        add(table, BorderLayout.CENTER);

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
            IIpRestrictor restrictor = fconfig.getIpRestrictor();
            restrictor.setPermissions(table.getData());
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
            if(fconfig != null) {
                IIpRestrictor restrictor = fconfig.getIpRestrictor();
                perms = restrictor.getPermissions();
            }
            table.setData(perms);
        }
        catch(Exception ex) {
            GuiUtils.showErrorMessage(this, "Cannot load IP entries.");
        }
    }

    /**
     * Refresh - set the ftp config.
     */
    public void refresh(IFtpConfig ftpConfig) {
        fconfig = ftpConfig;
        reloadData();
    }

    /**
     * This panel can be displayed only when server is
     * running that is ftp config is not null.
     */
    public boolean canBeDisplayed() {
        return (fconfig != null);
    }

    /**
     * Get the string representation.
     */
    public String toString() {
        return "IP Restrictor";
    }
}
