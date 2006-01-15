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
import java.awt.FlowLayout;
import java.awt.Font;
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

import org.apache.ftpserver.interfaces.ConnectionObserver;
import org.apache.ftpserver.interfaces.IConnection;
import org.apache.ftpserver.interfaces.IConnectionManager;
import org.apache.ftpserver.interfaces.IFtpConfig;


/**
 * This panel is used to monitor user activities.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class SpyPanel extends JPanel implements ConnectionObserver {
    
    private static final long serialVersionUID = -8673659781727175707L;
    
    private JTextPane logTxt   = null;
    private JTabbedPane parent = null;
    private JComponent defaultTab = null;
    
    private IFtpConfig fconfig   = null;
    private IConnection connection = null;
    
    private SimpleAttributeSet reqAttrs = null;
    private SimpleAttributeSet resAttrs = null;
    
    
    /**
     * Instantiate this dialog box
     */
    public SpyPanel(IFtpConfig config, 
                    IConnection con, 
                    JTabbedPane parent,
                    JComponent defaultTab) {
        
        fconfig = config;
        connection = con;
        this.parent = parent;
        this.defaultTab = defaultTab;
        initComponents();
        
        reqAttrs = new SimpleAttributeSet();
        StyleConstants.setForeground(reqAttrs, new Color(0xFF, 0x00, 0xFF));
        
        resAttrs = new SimpleAttributeSet();
        StyleConstants.setForeground(resAttrs, new Color(0x00, 0x00, 0x8B));
    }
    
    /**
     * Initialize the UI components
     */
    private void initComponents() {
        setLayout(new BorderLayout());
        logTxt = new JTextPane();
        logTxt.setFont(new Font("Monospaced", Font.PLAIN, 12));
        logTxt.setEditable(false);
        
        JPanel noWrapPanel = new JPanel(new BorderLayout());
        noWrapPanel.add(logTxt);
        add(new JScrollPane(noWrapPanel), BorderLayout.CENTER);
        connection.setObserver(this);
        
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
                Document doc = logTxt.getDocument();
                try {
                    doc.insertString(doc.getLength(), msg, resAttrs);
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
                Document doc = logTxt.getDocument();
                try {
                    doc.insertString(doc.getLength(), msg, reqAttrs);
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
        logTxt.setText("");
    }
    
    /**
     * Close pane
     */
    public void closePane() {
        parent.remove(this);
        if(parent.getTabCount() == 0) {
            parent.addTab("Spy", defaultTab);
        }
        connection.setObserver(null);
        clearLog();
    }
    
    /**
     * Disconnected user connection
     */
    private void disconnectUser() {
        boolean bConf = GuiUtils.getConfirmation(this, "Do you want to close the connection?");
        if(bConf) {
            IConnectionManager manager = fconfig.getConnectionManager();
            if (manager != null) {
                manager.closeConnection(connection);
            }
            connection.setObserver(null);
        }
    }    
    
    /**
     * Get the connection object being monitored.
     */
    public IConnection getConnection() {
        return connection;
    }
}
