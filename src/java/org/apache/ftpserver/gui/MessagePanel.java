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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.interfaces.IFtpConfig;
import org.apache.ftpserver.interfaces.IMessageResource;

/**
 * This is FTP server response panel. User can customize server responses.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class MessagePanel extends PluginPanel implements ActionListener {

    private static final long serialVersionUID = -68038181884794057L;
    
    private IFtpConfig m_fconfig;
    private JTextArea m_txtArea;
    private JComboBox m_comboBox;
    
    private ArrayList m_messageKeys = new ArrayList();
    private Properties m_messageProps = new Properties();
    private int m_oldSelIndex = -1;
    
    /**
     * Constructor - set the container.
     */
    public MessagePanel(PluginPanelContainer container) {
        super(container);
        initComponents();
    }
    
    /**
     * Initialize UI components.
     */
    private void initComponents() {
        
        setLayout(new BorderLayout());
        
        JPanel comboPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        add(comboPanel, BorderLayout.NORTH);
        
        // all replies
        m_comboBox = new JComboBox();
        m_comboBox.setPreferredSize(new Dimension(250, 22));
        m_comboBox.addActionListener(this);
        comboPanel.add(m_comboBox);
        
        // message text
        m_txtArea = new JTextArea();
        JScrollPane txtPane = new JScrollPane(m_txtArea, 
                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(txtPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        add(buttonPanel, BorderLayout.SOUTH);
        
        // save button
        JButton saveBtn = new JButton("Save");
        saveBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                save();
            }
        });
        buttonPanel.add(saveBtn);
        
        // reload button
        JButton reloadBtn = new JButton("Reload");
        reloadBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                refresh(m_fconfig);
            }
        });
        buttonPanel.add(reloadBtn);
    }
    
    /**
     * List selection changed. 
     */
    public void actionPerformed(ActionEvent e) {
        int selIdx = m_comboBox.getSelectedIndex();
        if(selIdx != -1) {
            Object selVal = m_messageKeys.get(selIdx);
            String val = m_messageProps.getProperty((String)selVal);
            if(val == null) {
                val = "";
            }
            
            // save the last text area value
            if(m_oldSelIndex != -1) {
                String oldKey = (String)m_messageKeys.get(m_oldSelIndex);
                String oldTxt = m_txtArea.getText();
                m_messageProps.setProperty(oldKey, oldTxt);
            }
            
            // update text area
            m_txtArea.setText(val);
        }
    } 

    /**
     * Save entered properties 
     */
    private void save() {
        
        try {
            // save the text value
            String key = (String)m_comboBox.getSelectedItem();
            String val = m_txtArea.getText();
            m_messageProps.setProperty(key, val);
            
            // save messages
            getContainer().getFtpConfig().getMessageResource().save(m_messageProps, null);
        }
        catch(FtpException ex) {
            GuiUtils.showErrorMessage(this, "Cannot save messages.");
        }
    }
    
    /** 
     * Refresh the ftp configuration
     */
    public void refresh(IFtpConfig ftpConfig) {
        m_fconfig = ftpConfig;
        m_comboBox.removeAllItems();
        m_txtArea.setText("");
        m_messageKeys.clear();
        m_oldSelIndex = -1;
        
        if(m_fconfig != null) {
            IMessageResource msgRes = m_fconfig.getMessageResource();
            m_messageProps = msgRes.getMessages(null);
            Enumeration props = m_messageProps.propertyNames();
            while(props.hasMoreElements()) {
                String key = (String)props.nextElement();
                m_messageKeys.add(key);
            }
            
            Collections.sort(m_messageKeys);
            Iterator it = m_messageKeys.iterator();
            while(it.hasNext()) {
                m_comboBox.addItem(it.next());
            }
            if(!m_messageKeys.isEmpty()) {
                m_comboBox.setSelectedIndex(0);
            }
        }
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
        return "Messages";
    }
}
