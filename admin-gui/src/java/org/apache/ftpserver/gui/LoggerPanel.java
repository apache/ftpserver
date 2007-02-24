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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.apache.ftpserver.interfaces.FtpServerContext;
import org.apache.ftpserver.util.IoUtils;
import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.spi.LoggingEvent;

/**
 * This logger panel writes the log messages.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class LoggerPanel extends PluginPanel {
    
    private static final long serialVersionUID = 5988947217051710193L;
    private static final int MAX_CHARS = 8192;
    
    private final static Color COLOR_DEBUG = new Color(63, 127, 95);
    private final static Color COLOR_INFO  = new Color(0, 0, 0);
    private final static Color COLOR_WARN  = new Color(245, 150, 45);
    private final static Color COLOR_ERROR = new Color(255, 0, 0);

    private final static Level[] LEVELS = new Level[] {
        Level.TRACE,
        Level.DEBUG,
        Level.INFO,
        Level.WARN,
        Level.ERROR,
        Level.FATAL,
        Level.OFF
    };
    

    private final static int LEVEL_INFO  = 2;
    
    private int logLevel = LEVEL_INFO;

    private FtpServerContext serverContext;
    
    private JComboBox logCombo;
    private JTextPane logTxt;
    private Document doc;
    
    private SimpleAttributeSet debugAttr;
    private SimpleAttributeSet infoAttr;
    private SimpleAttributeSet warnAttr;
    private SimpleAttributeSet errorAttr;

    private class LoggerPanelAppender extends AppenderSkeleton {

        protected void append(LoggingEvent event) {
            if(isEnabled(event.getLevel())) {
                String msg = '[' + event.getLevel().toString() + "] " + event.getMessage().toString() + '\n';
                write(event.getLevel(), String.valueOf(msg));
                
                if(event.getThrowableInformation() != null) {
                    write(event.getLevel(), IoUtils.getStackTrace(event.getThrowableInformation().getThrowable()));
                }
            }
            
        }

        public void close() {
            // do nothing
        }

        public boolean requiresLayout() {
            return false;
        }
    }
    
    /**
     * Constructor - set the container.
     */
    public LoggerPanel(PluginPanelContainer container) {
        super(container);
        
        // create style attributes
        debugAttr = new SimpleAttributeSet();
        StyleConstants.setForeground(debugAttr, COLOR_DEBUG);
        
        infoAttr = new SimpleAttributeSet();
        StyleConstants.setForeground(infoAttr, COLOR_INFO);
        
        warnAttr = new SimpleAttributeSet();
        StyleConstants.setForeground(warnAttr, COLOR_WARN);
        
        errorAttr = new SimpleAttributeSet();
        StyleConstants.setForeground(errorAttr, COLOR_ERROR);
        
        initComponents();
        
        Appender appender = new LoggerPanelAppender();
        LogManager.getRootLogger().addAppender(appender);
    }
    
    /**
     * Return appropriate attribute.
     */
    private SimpleAttributeSet getAttributeSet(Level level) {
        
        SimpleAttributeSet attr = null;
        if(level.isGreaterOrEqual(Level.ERROR)) {
            attr = errorAttr;
        } else if(level.isGreaterOrEqual(Level.WARN)) {
            attr = warnAttr;
        } else if(level.isGreaterOrEqual(Level.INFO)) {
            attr = infoAttr;
        }else {
            attr = debugAttr;
        }

        return attr;
    }
    
    /**
     * Initialize UI components.
     */
    private void initComponents() {
        
        setLayout(new BorderLayout());
        
        // add top combo panel
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        add(topPanel, BorderLayout.NORTH);
        
        JLabel comboLab = new JLabel("Log Level :: ");
        comboLab.setForeground(Color.black);
        topPanel.add(comboLab);
           
        logCombo = new JComboBox(LEVELS);
        logCombo.setSelectedIndex(LEVEL_INFO);
        Dimension dim = new Dimension(90, 22);
        logCombo.setPreferredSize(dim);
        logCombo.setMaximumSize(dim);
        logCombo.setToolTipText("Set Log Level");
        logCombo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent evt) {
                logLevel = logCombo.getSelectedIndex();
            }
        });
        topPanel.add(logCombo);
        
        // add text pane
        logTxt = new JTextPane();
        logTxt.setFont(new Font("Monospaced", Font.PLAIN, 12));
        logTxt.setEditable(false);
        doc = logTxt.getDocument();
        
        JPanel noWrapPanel = new JPanel(new BorderLayout());
        noWrapPanel.add(logTxt);
        add(new JScrollPane(noWrapPanel), BorderLayout.CENTER);
        
        // add clear button panel
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        add(bottomPanel, BorderLayout.SOUTH);
        
        JButton clearAction = new JButton("Clear");
        clearAction.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                try {
                    doc.remove(0, doc.getLength());
                }
                catch(Exception ex) {
                    ex.printStackTrace();
                }
            }
         });
        bottomPanel.add(clearAction);
    }
    
    /** 
     * Refresh the ftp configuration
     */
    public void refresh(FtpServerContext serverContext) {
        
        this.serverContext = serverContext;
        
        // remove old log messages
        try {
            doc.remove(0, doc.getLength());
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }
    
    /** 
     * This can be displayed only when the server is running.
     */
    public boolean canBeDisplayed() {
        return (serverContext != null);
    }

    /**
     * Get the string representation.
     */
    public String toString() {
        return "Log";
    }
    
    /**
     * Dispose - does nothing
     */
    public void dispose() {
    }
    
    ///////////////////////////////////////////////////////////
    ////////////////// Logger implementation //////////////////
    /**
     * Check log enable. 
     */
    private boolean isEnabled(Level level) {
        Level activeLevel = LEVELS[logLevel];
        
        return level.isGreaterOrEqual(activeLevel);
    }
    
    
    /**
     * Write message
     */
    private void write(final Level level, final String message) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {    
                    // clear if already the char count exceeds
                    int docLen = doc.getLength();
                    if(docLen > MAX_CHARS) {
                        doc.remove(0, docLen);
                        docLen = 0;
                    }
                    
                    // insert string
                    SimpleAttributeSet attr = getAttributeSet(level);
                    doc.insertString(docLen, message, attr);
                }
                catch(Exception ex) {
                    ex.printStackTrace();
                } 
            }
        });
    }
}
