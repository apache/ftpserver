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
import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.interfaces.MessageResource;
import org.apache.ftpserver.interfaces.FtpServerContext;

/**
 * This is FTP server response panel. User can customize server responses.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class MessagePanel extends PluginPanel {

    private static final long serialVersionUID = -68038181884794057L;
    
    private FtpServerContext fconfig;
    private JComboBox comboBox;
    
    private JList list;
    private JTextArea txtArea;
    
    private String[] languages;
    private Vector messageKeys;
    private Properties messageProps;
    private int oldKeySelIndex = -1;
    
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
        
        // top panel
        JPanel comboPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        add(comboPanel, BorderLayout.NORTH);
        
        // Language label
        JLabel label = new JLabel("Language : ");
        comboPanel.add(label);
        
        // all languages
        comboBox = new JComboBox();
        comboBox.setPreferredSize(new Dimension(100, 22));
        comboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                changeLanguage();
            }
        });
        comboPanel.add(comboBox);
        
        // split pane
        JSplitPane splitPane = new JSplitPane();
        splitPane.setDividerSize(0);
        add(splitPane, BorderLayout.CENTER);
        
        // message list keys
        list = new JList();
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent evt) {
                changeKey();
            }
        });
        JScrollPane listScroller = new JScrollPane(list);
        splitPane.setLeftComponent(listScroller);
        
        // message text
        txtArea = new JTextArea();
        JScrollPane txtPane = new JScrollPane(txtArea, 
                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        splitPane.setRightComponent(txtPane);
        
        // button panel
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
                changeLanguage();
            }
        });
        buttonPanel.add(reloadBtn);
    }
    
    /**
     * Combo selection changed. 
     */
    public void changeLanguage() {
        
        // get selected language
        oldKeySelIndex = -1;
        int selIdx = comboBox.getSelectedIndex();
        if(selIdx == -1) {
            return;
        }
        String language = null;
        if(selIdx >= 1) {
            language = languages[selIdx - 1];
        }
        
        // get properties
        MessageResource msgRes = fconfig.getMessageResource();
        Properties prop = msgRes.getMessages(language);
        Vector keyList = new Vector();
        for(Enumeration keys = prop.propertyNames(); keys.hasMoreElements();) {
            String key = (String)keys.nextElement();
            keyList.add(key);
        }
        Collections.sort(keyList);
        messageKeys = keyList;
        messageProps = prop;
        
        // load list
        list.removeAll();
        list.setListData(keyList);
        list.setSelectedIndex(0);
    }
    
    /**
     * List selection changed.
     */
    public void changeKey() {
        
        // get key selection index
        int selIdx = list.getSelectedIndex();
        if(selIdx == -1) {
            return;
        }
        
        // save the last text area value
        if(oldKeySelIndex != -1) {
            String oldKey = (String)messageKeys.get(oldKeySelIndex);
            String oldTxt = txtArea.getText();
            messageProps.setProperty(oldKey, oldTxt);
        }
        oldKeySelIndex = selIdx;
        
        // update text area
        String key = (String)messageKeys.get(selIdx);
        String val = messageProps.getProperty(key);
        txtArea.setText(val);
        txtArea.setCaretPosition(0);
    }
    
    /**
     * Save entered properties 
     */
    private void save() {
        
        // get the selected language
        int selIdx = comboBox.getSelectedIndex();
        if(selIdx == -1) {
            return;
        }
        String language = null;
        if(selIdx >= 1) {
            language = languages[selIdx - 1];
        }
        
        // store existing text value
        String key = list.getSelectedValue().toString();
        String val = txtArea.getText();
        messageProps.setProperty(key, val);
        
        
        // save custom messages
        try {
            fconfig.getMessageResource().save(messageProps, language);
        }
        catch(FtpException ex) {
            GuiUtils.showErrorMessage(this, "Cannot save messages.");
        }
    }
    
    /** 
     * Refresh the ftp configuration
     */
    public void refresh(FtpServerContext ftpConfig) {
        fconfig = ftpConfig;
        comboBox.removeAllItems();
        list.removeAll();
        oldKeySelIndex = -1;
        if(fconfig == null) {
            return;
        }
        
        // populate language list
        MessageResource msgRes = fconfig.getMessageResource();
        languages = msgRes.getAvailableLanguages();
        comboBox.addItem("<default>");
        if(languages != null) {
            for(int i=0; i<languages.length; ++i) {
                comboBox.addItem(languages[i]);
            }
        }
        comboBox.setSelectedIndex(0);
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
        return "Messages";
    }
}
