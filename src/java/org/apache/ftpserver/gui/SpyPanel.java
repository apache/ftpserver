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

import org.apache.ftpserver.interfaces.ConnectionObserver;
import org.apache.ftpserver.interfaces.IConnection;
import org.apache.ftpserver.interfaces.IConnectionManager;
import org.apache.ftpserver.interfaces.IFtpConfig;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;


/**
 * This panel is used to monitor user activities.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class SpyPanel extends JPanel implements ConnectionObserver {
    
    private static final long serialVersionUID = 4258123980008335623L;
    
    private JTextPane m_logTxt   = null;
    private JTabbedPane m_parent = null;
    private JComponent m_defaultTab = null;
    
    private IFtpConfig m_fconfig   = null;
    private IConnection m_connection = null;
    
    private SimpleAttributeSet m_reqAttrs = null;
    private SimpleAttributeSet m_resAttrs = null;
    
    
    /**
     * Instantiate this dialog box
     */
    public SpyPanel(IFtpConfig config, 
                    IConnection con, 
                    JTabbedPane parent,
                    JComponent defaultTab) {
        
        m_fconfig = config;
        m_connection = con;
        m_parent = parent;
        m_defaultTab = defaultTab;
        initComponents();
        
        m_reqAttrs = new SimpleAttributeSet();
        StyleConstants.setForeground(m_reqAttrs, new Color(0xFF, 0x00, 0xFF));
        
        m_resAttrs = new SimpleAttributeSet();
        StyleConstants.setForeground(m_resAttrs, new Color(0x00, 0x00, 0x8B));
    }
    
    /**
     * Initialize the UI components
     */
    private void initComponents() {
        setLayout(new BorderLayout());
        m_logTxt = new JTextPane();
        m_logTxt.setEditable(false);
        JScrollPane txtPane = new JScrollPane(m_logTxt, 
                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(txtPane, BorderLayout.CENTER);
        m_connection.setObserver(this);
        
        JPanel bottomPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
        
        JButton clearButton = new JButton("Clear");
        bottomPane.add(clearButton);
        clearButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                clearLog();
             }
        });
        
        JButton disconnectButton = new JButton("Disconnect");
        bottomPane.add(disconnectButton);
        disconnectButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                disconnectUser();
             }
        });
        
        JButton closeButton = new JButton("Close");
        bottomPane.add(closeButton);
        closeButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                closePane();
             }
        });
        add(bottomPane, BorderLayout.SOUTH);
    }
        
    /**
     * Write server response.
     */
    public void response(final String msg) {
        Runnable runnable = new Runnable() {
            public void run() { 
                Document doc = m_logTxt.getDocument();
                try {
                    doc.insertString(doc.getLength(), msg, m_resAttrs);
                }
                catch(BadLocationException ex) {
                    ex.printStackTrace();
                }        
            }
        };
        SwingUtilities.invokeLater(runnable);
    }
    
    /**
     * Write user request.
     */
    public void request(final String msg) {
        Runnable runnable = new Runnable() {
            public void run() { 
                Document doc = m_logTxt.getDocument();
                try {
                    doc.insertString(doc.getLength(), msg, m_reqAttrs);
                }
                catch(BadLocationException ex) {
                    ex.printStackTrace();
                }        
            }
        };
        SwingUtilities.invokeLater(runnable);
    }
    
    /**
     * Clear log messages
     */
    public void clearLog() {
        m_logTxt.setText("");
    }
    
    /**
     * Close pane
     */
    public void closePane() {
        m_parent.remove(this);
        if(m_parent.getTabCount() == 0) {
            m_parent.addTab("Spy", m_defaultTab);
        }
        m_connection.setObserver(null);
        clearLog();
    }
    
    /**
     * Disconnected user connection
     */
    private void disconnectUser() {
        boolean bConf = GuiUtils.getConfirmation(this, "Do you want to close the connection?");
        if(bConf) {
            IConnectionManager manager = m_fconfig.getConnectionManager();
            if (manager != null) {
                manager.closeConnection(m_connection);
            }
            m_connection.setObserver(null);
        }
    }    
    
    /**
     * Get the connection object being monitored.
     */
    public IConnection getConnection() {
        return m_connection;
    }
}
