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

import org.apache.ftpserver.ftplet.Configuration;
import org.apache.ftpserver.ftplet.Logger;
import org.apache.ftpserver.interfaces.IFtpConfig;
import org.apache.ftpserver.logger.CompositeLogger;
import org.apache.ftpserver.util.IoUtils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
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

/**
 * This logger panel writes log information.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class LoggerPanel extends PluginPanel implements Logger {
    
    private static final long serialVersionUID = -5197421525973377762L;
    private static final int MAX_CHARS = 8192;
    
    public static final int LEVEL_DEBUG = 0;
    public static final int LEVEL_INFO  = 1;
    public static final int LEVEL_WARN  = 2;
    public static final int LEVEL_ERROR = 3;
    public static final int LEVEL_NONE  = 4;

    private final static Color COLOR_DEBUG = new Color(63, 127, 95);
    private final static Color COLOR_INFO  = new Color(0, 0, 0);
    private final static Color COLOR_WARN  = new Color(245, 150, 45);
    private final static Color COLOR_ERROR = new Color(255, 0, 0);
    
    private final static String[] COMBO_LEVEL = {
            "DEBUG", 
            "INFO", 
            "WARNING", 
            "ERROR", 
            "NONE"
    };
    private final static String[] TXT_LEVEL = {
            "DEB",
            "INF",
            "WAR",
            "ERR"
    };
    
    private int m_logLevel = LEVEL_INFO;

    private IFtpConfig m_ftpConfig;
    
    private JComboBox m_logCombo;
    private JTextPane m_logTxt;
    private Document m_doc;
    
    private SimpleAttributeSet m_debugAttr;
    private SimpleAttributeSet m_infoAttr;
    private SimpleAttributeSet m_warnAttr;
    private SimpleAttributeSet m_errorAttr;

    
    /**
     * Default constructor
     */
    public LoggerPanel(PluginPanelContainer container) {
        super(container);
        
        // create style attributes
        m_debugAttr = new SimpleAttributeSet();
        StyleConstants.setForeground(m_debugAttr, COLOR_DEBUG);
        
        m_infoAttr = new SimpleAttributeSet();
        StyleConstants.setForeground(m_infoAttr, COLOR_INFO);
        
        m_warnAttr = new SimpleAttributeSet();
        StyleConstants.setForeground(m_warnAttr, COLOR_WARN);
        
        m_errorAttr = new SimpleAttributeSet();
        StyleConstants.setForeground(m_errorAttr, COLOR_ERROR);
        
        initComponents();
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
           
        m_logCombo = new JComboBox(COMBO_LEVEL);
        m_logCombo.setSelectedIndex(m_logLevel);
        Dimension dim = new Dimension(90, 22);
        m_logCombo.setPreferredSize(dim);
        m_logCombo.setMaximumSize(dim);
        m_logCombo.setToolTipText("Set Log Level");
        m_logCombo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent evt) {
                int selIdx = m_logCombo.getSelectedIndex();
                m_logLevel = selIdx;
            }
        });
        topPanel.add(m_logCombo);
        
            
        // add text pane
        m_logTxt = new JTextPane();
        m_logTxt.setEditable(false);
        m_doc = m_logTxt.getDocument();
        JScrollPane scrollPane = new JScrollPane(m_logTxt, 
                                                 JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                                 JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);
        
        // add clear button panel
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        add(bottomPanel, BorderLayout.SOUTH);
        
        JButton clearAction = new JButton("Clear");
        clearAction.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                try {
                    m_doc.remove(0, m_doc.getLength());
                }
                catch(Exception ex) {
                    // ex.printStackTrace();
                }
            }
         });
        bottomPanel.add(clearAction);
    }
    
    /** 
     * Refresh the ftp configuration
     */
    public void refresh(IFtpConfig ftpConfig) {
        
        // remove it from the previous one
        if(m_ftpConfig != null) {
            CompositeLogger clogger = (CompositeLogger)m_ftpConfig.getLogger();
            if(clogger != null) {
                clogger.removeLogger(this);
            }
        }
        
        // add this logger now
        m_ftpConfig = ftpConfig;
        if(m_ftpConfig != null) {
            CompositeLogger clogger = (CompositeLogger)m_ftpConfig.getLogger();
            clogger.addLogger(this);
        }
    }
    
    /** 
     * This can be displayed only when the server is running.
     */
    public boolean canBeDisplayed() {
        return (m_ftpConfig != null);
    }

    
    /**
     * Get the string representation.
     */
    public String toString() {
        return "Log";
    }
    
    
    /**
     * Get current log level
     */
    public int getLogLevel() {
        return m_logLevel;
    }
    
    
    /**
     * Set log level
     */
    public void setLogLevel(int level) {
        m_logLevel = level;
        m_logCombo.setSelectedIndex(level);
    }
    
    /**
     * Set logger - does nothing
     */
    public void setLogger(Logger logger) {
    }
    
    /**
     * Configure - does nothing.
     */
    public void configure(Configuration conf) {
    }
    
    /**
     * Dispose - does nothing
     */
    public void dispose() {
    }
    
    /**
     * Write string
     */
    private void writeString(int logLevel, String msg) {
        try {
            int docLen = m_doc.getLength();
            if(docLen > MAX_CHARS) {
                m_doc.remove(0, docLen);
                docLen = 0;
            }
            
            switch(logLevel) {
                case LEVEL_DEBUG:
                    m_doc.insertString(docLen, msg, m_debugAttr);
                    break;
                    
                case LEVEL_INFO:
                    m_doc.insertString(docLen, msg, m_infoAttr);
                    break;   
                    
                case LEVEL_WARN:
                    m_doc.insertString(docLen, msg, m_warnAttr);
                    break;
                    
                case LEVEL_ERROR:
                    m_doc.insertString(docLen, msg, m_errorAttr);
                    break;
            }
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }        
    }
    
    /**
     * Log message
     */
    private void log(int logLevel, String msg) {

        // check message
        if( (msg == null) || msg.equals("") ) {
            return;
        }
        
        // write message
        writeString(logLevel, '[' + TXT_LEVEL[logLevel] + "] " + msg + '\n');
    }
    
    /**
     * Log throwable
     */
    private void log(int logLevel, String msg, Throwable th) {
        log(logLevel, msg);
        writeString(logLevel, IoUtils.getStackTrace(th));
    }
    
    /**
     * Debug message
     */
    public void debug(final String msg) {
        Runnable runnable = new Runnable() {
            public void run() { 
                if(m_logLevel <= LEVEL_DEBUG) {
                    log(LEVEL_DEBUG, msg);
                }        
            }
        };
        SwingUtilities.invokeLater(runnable);
    } 
    
    /**
     * Debug message - throwable
     */
    public void debug(final String msg, final Throwable th) {
        Runnable runnable = new Runnable() {
            public void run() { 
                if(m_logLevel <= LEVEL_DEBUG) {
                    log(LEVEL_DEBUG, msg, th);
                }        
            }
        };
        SwingUtilities.invokeLater(runnable);
    }
        
    /**
     * Info message
     */
    public void info(final String msg) {
        Runnable runnable = new Runnable() {
            public void run() { 
                if(m_logLevel <= LEVEL_INFO) {
                    log(LEVEL_INFO, msg);
                }        
            }
        };
        SwingUtilities.invokeLater(runnable);
    }
    
    /**
     * Info message - throwable
     */
    public void info(final String msg, final Throwable th) {
        Runnable runnable = new Runnable() {
            public void run() { 
                if(m_logLevel <= LEVEL_INFO) {
                    log(LEVEL_INFO, msg, th);
                }        
            }
        };
        SwingUtilities.invokeLater(runnable);
    }
        
    /**
     * Warning message
     */
    public void warn(final String msg) {
        Runnable runnable = new Runnable() {
            public void run() { 
                if(m_logLevel <= LEVEL_WARN) {
                    log(LEVEL_WARN, msg);
                }        
            }
        };
        SwingUtilities.invokeLater(runnable);
    }
    
    /**
     * Warning message - throwable
     */
    public void warn(final String msg, final Throwable th) {
        Runnable runnable = new Runnable() {
            public void run() { 
                if(m_logLevel <= LEVEL_WARN) {
                    log(LEVEL_WARN, msg, th);
                }        
            }
        };
        SwingUtilities.invokeLater(runnable);
    }
    
    /**
     * Write error message
     */
    public void error(final String msg) {
        Runnable runnable = new Runnable() {
            public void run() { 
                if(m_logLevel <= LEVEL_ERROR) {
                    log(LEVEL_ERROR, msg);
                }        
            }
        };
        SwingUtilities.invokeLater(runnable);
    }
    
    /**
     * Error message - throwable
     */
    public void error(final String msg, final Throwable th) {
        Runnable runnable = new Runnable() {
            public void run() { 
                if(m_logLevel <= LEVEL_ERROR) {
                    log(LEVEL_ERROR, msg, th);
                }        
            }
        };
        SwingUtilities.invokeLater(runnable);
    }
}
