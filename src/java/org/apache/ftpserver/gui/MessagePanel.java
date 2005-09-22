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
import org.apache.ftpserver.interfaces.IFtpConfig;
import org.apache.ftpserver.interfaces.IMessageResource;

/**
 * This is FTP server response panel. User can customize server responses.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class MessagePanel extends PluginPanel {

    private static final long serialVersionUID = -68038181884794057L;
    
    private IFtpConfig m_fconfig;
    private JComboBox m_comboBox;
    
    private JList m_list;
    private JTextArea m_txtArea;
    
    private String[] m_languages;
    private Vector m_messageKeys;
    private Properties m_messageProps;
    private int m_oldKeySelIndex = -1;
    
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
        m_comboBox = new JComboBox();
        m_comboBox.setPreferredSize(new Dimension(100, 22));
        m_comboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                changeLanguage();
            }
        });
        comboPanel.add(m_comboBox);
        
        // split pane
        JSplitPane splitPane = new JSplitPane();
        splitPane.setDividerSize(0);
        add(splitPane, BorderLayout.CENTER);
        
        // message list keys
        m_list = new JList();
        m_list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        m_list.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent evt) {
                changeKey();
            }
        });
        JScrollPane listScroller = new JScrollPane(m_list);
        splitPane.setLeftComponent(listScroller);
        
        // message text
        m_txtArea = new JTextArea();
        JScrollPane txtPane = new JScrollPane(m_txtArea, 
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
        m_oldKeySelIndex = -1;
        int selIdx = m_comboBox.getSelectedIndex();
        if(selIdx == -1) {
            return;
        }
        String language = null;
        if(selIdx >= 1) {
            language = m_languages[selIdx - 1];
        }
        
        // get properties
        IMessageResource msgRes = m_fconfig.getMessageResource();
        Properties prop = msgRes.getMessages(language);
        Vector keyList = new Vector();
        for(Enumeration keys = prop.propertyNames(); keys.hasMoreElements();) {
            String key = (String)keys.nextElement();
            keyList.add(key);
        }
        Collections.sort(keyList);
        m_messageKeys = keyList;
        m_messageProps = prop;
        
        // load list
        m_list.removeAll();
        m_list.setListData(keyList);
        m_list.setSelectedIndex(0);
    }
    
    /**
     * List selection changed.
     */
    public void changeKey() {
        
        // get key selection index
        int selIdx = m_list.getSelectedIndex();
        if(selIdx == -1) {
            return;
        }
        
        // save the last text area value
        if(m_oldKeySelIndex != -1) {
            String oldKey = (String)m_messageKeys.get(m_oldKeySelIndex);
            String oldTxt = m_txtArea.getText();
            m_messageProps.setProperty(oldKey, oldTxt);
        }
        m_oldKeySelIndex = selIdx;
        
        // update text area
        String key = (String)m_messageKeys.get(selIdx);
        String val = m_messageProps.getProperty(key);
        m_txtArea.setText(val);
        m_txtArea.setCaretPosition(0);
    }
    
    /**
     * Save entered properties 
     */
    private void save() {
        
        // get the selected language
        int selIdx = m_comboBox.getSelectedIndex();
        if(selIdx == -1) {
            return;
        }
        String language = null;
        if(selIdx >= 1) {
            language = m_languages[selIdx - 1];
        }
        
        // store existing text value
        String key = (String)m_comboBox.getSelectedItem();
        String val = m_txtArea.getText();
        m_messageProps.setProperty(key, val);
        
        
        // save custom messages
        try {
            m_fconfig.getMessageResource().save(m_messageProps, language);
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
        m_list.removeAll();
        m_oldKeySelIndex = -1;
        if(m_fconfig == null) {
            return;
        }
        
        // populate language list
        IMessageResource msgRes = m_fconfig.getMessageResource();
        m_languages = msgRes.getAvailableLanguages();
        m_comboBox.addItem("<default>");
        if(m_languages != null) {
            for(int i=0; i<m_languages.length; ++i) {
                m_comboBox.addItem(m_languages[i]);
            }
        }
        m_comboBox.setSelectedIndex(0);
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
