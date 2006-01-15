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

import org.apache.commons.logging.Log;
import org.apache.ftpserver.FtpLogFactory;
import org.apache.ftpserver.interfaces.IFtpConfig;
import org.apache.ftpserver.util.IoUtils;

/**
 * This logger panel writes the log messages.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class LoggerPanel extends PluginPanel implements Log {
    
    private static final long serialVersionUID = 5988947217051710193L;
    private static final int MAX_CHARS = 8192;
    
    private final static Color COLOR_DEBUG = new Color(63, 127, 95);
    private final static Color COLOR_INFO  = new Color(0, 0, 0);
    private final static Color COLOR_WARN  = new Color(245, 150, 45);
    private final static Color COLOR_ERROR = new Color(255, 0, 0);

    private final static String[] LEVELS = new String[] {
        "TRACE",
        "DEBUG",
        "INFO ",
        "WARN ",
        "ERROR",
        "FATAL",
        "NONE "
    };
    
    private final static int LEVEL_TRACE = 0;
    private final static int LEVEL_DEBUG = 1;
    private final static int LEVEL_INFO  = 2;
    private final static int LEVEL_WARN  = 3;
    private final static int LEVEL_ERROR = 4;
    private final static int LEVEL_FATAL = 5;
    
    private int logLevel = LEVEL_INFO;

    private IFtpConfig ftpConfig;
    
    private JComboBox logCombo;
    private JTextPane logTxt;
    private Document doc;
    
    private SimpleAttributeSet debugAttr;
    private SimpleAttributeSet infoAttr;
    private SimpleAttributeSet warnAttr;
    private SimpleAttributeSet errorAttr;

    
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
    }
    
    /**
     * Return appropriate attribute.
     */
    private SimpleAttributeSet getAttributeSet(int level) {
        
        SimpleAttributeSet attr = null;
        switch(level) {
            case LEVEL_TRACE:
            case LEVEL_DEBUG:
                attr = debugAttr;
                break;
            
            case LEVEL_INFO:
                attr = infoAttr;
                break;
                
            case LEVEL_WARN:
                attr = warnAttr;
                break;
                
            case LEVEL_ERROR:
            case LEVEL_FATAL:
                attr = errorAttr;
                break;
            
            default:
                attr = infoAttr;
                break;
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
    public void refresh(IFtpConfig ftpConfig) {
        
        // remove old log messages
        try {
            doc.remove(0, doc.getLength());
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        
        // remove from the previous log factory
        if(this.ftpConfig != null) {
            FtpLogFactory factory = (FtpLogFactory)this.ftpConfig.getLogFactory();
            if(factory != null) {
                factory.removeLog(this);
            }
        }

        // add this logger
        this.ftpConfig = ftpConfig;
        if(ftpConfig != null) {
            FtpLogFactory factory = (FtpLogFactory)ftpConfig.getLogFactory();
            factory.addLog(this);
        }
    }
    
    /** 
     * This can be displayed only when the server is running.
     */
    public boolean canBeDisplayed() {
        return (ftpConfig != null);
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
    private boolean isEnabled(int level) {
        return logLevel <= level;
    }
    
    /**
     * Is trace enebled?
     */
    public boolean isTraceEnabled() {
        return isEnabled(LEVEL_TRACE);
    }
    
    /**
     * Is debug enabled?
     */
    public boolean isDebugEnabled() {
        return isEnabled(LEVEL_DEBUG);
    }
    
    /**
     * Is info enabled?
     */
    public boolean isInfoEnabled() {
        return isEnabled(LEVEL_INFO);
    }
    
    /**
     * Is warn enabled?
     */
    public boolean isWarnEnabled() {
        return isEnabled(LEVEL_WARN);
    }
    
    /**
     * Is error enabled?
     */
    public boolean isErrorEnabled() {
        return isEnabled(LEVEL_ERROR);
    }
    
    /**
     * Is fatal enabled?
     */
    public boolean isFatalEnabled() {
        return isEnabled(LEVEL_FATAL);
    }
    
    /**
     * Write message
     */
    private void write(final int level, final String message) {
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
    
    /**
     * Write trace message.
     */
    public void trace(Object msg) {
        if(isEnabled(LEVEL_TRACE)) {
            msg = '[' + LEVELS[LEVEL_TRACE] + ']' + String.valueOf(msg) + '\n';
            write(LEVEL_TRACE, String.valueOf(msg));
        }
    }
    
    /**
     * Write trace message.
     */
    public void trace(Object msg, Throwable t) {
        if(isEnabled(LEVEL_TRACE)) {
            msg = '[' + LEVELS[LEVEL_TRACE] + ']' + String.valueOf(msg) + '\n';
            write(LEVEL_TRACE, String.valueOf(msg));
            write(LEVEL_TRACE, IoUtils.getStackTrace(t));
        }
    }
    
    /**
     * Write debug message.
     */
    public void debug(Object msg) {
        if(isEnabled(LEVEL_DEBUG)) {
            msg = '[' + LEVELS[LEVEL_DEBUG] + ']' + String.valueOf(msg) + '\n';
            write(LEVEL_DEBUG, String.valueOf(msg));
        }
    }
    
    /**
     * Write debug message.
     */
    public void debug(Object msg, Throwable t) {
        if(isEnabled(LEVEL_DEBUG)) {
            msg = '[' + LEVELS[LEVEL_DEBUG] + ']' + String.valueOf(msg) + '\n';
            write(LEVEL_DEBUG, String.valueOf(msg));
            write(LEVEL_DEBUG, IoUtils.getStackTrace(t));
        }
    }
    
    /**
     * Write info message.
     */
    public void info(Object msg) {
        if(isEnabled(LEVEL_INFO)) {
            msg = '[' + LEVELS[LEVEL_INFO] + ']' + String.valueOf(msg) + '\n';
            write(LEVEL_INFO, String.valueOf(msg));
        }
    }
    
    /**
     * Write info message.
     */
    public void info(Object msg, Throwable t) {
        if(isEnabled(LEVEL_INFO)) {
            msg = '[' + LEVELS[LEVEL_INFO] + ']' + String.valueOf(msg) + '\n';
            write(LEVEL_INFO, String.valueOf(msg));
            write(LEVEL_INFO, IoUtils.getStackTrace(t));
        }
    }
    
    /**
     * Write warning message.
     */
    public void warn(Object msg) {
        if(isEnabled(LEVEL_WARN)) {
            msg = '[' + LEVELS[LEVEL_WARN] + ']' + String.valueOf(msg) + '\n';
            write(LEVEL_WARN, String.valueOf(msg));
        }
    }
    
    /**
     * Write warning message.
     */
    public void warn(Object msg, Throwable t) {
        if(isEnabled(LEVEL_WARN)) {
            msg = '[' + LEVELS[LEVEL_WARN] + ']' + String.valueOf(msg) + '\n';
            write(LEVEL_WARN, String.valueOf(msg));
            write(LEVEL_WARN, IoUtils.getStackTrace(t));
        }
    }
    
    /**
     * Write error message.
     */
    public void error(Object msg) {
        if(isEnabled(LEVEL_ERROR)) {
            msg = '[' + LEVELS[LEVEL_ERROR] + ']' + String.valueOf(msg) + '\n';
            write(LEVEL_ERROR, String.valueOf(msg));
        }
    }
    
    /**
     * Write error message.
     */
    public void error(Object msg, Throwable t) {
        if(isEnabled(LEVEL_ERROR)) {
            msg = '[' + LEVELS[LEVEL_ERROR] + ']' + String.valueOf(msg) + '\n';
            write(LEVEL_ERROR, String.valueOf(msg));
            write(LEVEL_ERROR, IoUtils.getStackTrace(t));
        }
    }
    
    /**
     * Write fatal message.
     */
    public void fatal(Object msg) {
        if(isEnabled(LEVEL_FATAL)) {
            msg = '[' + LEVELS[LEVEL_FATAL] + ']' + String.valueOf(msg) + '\n';
            write(LEVEL_FATAL, String.valueOf(msg));
        }
    }
    
    /**
     * Write fatal message.
     */
    public void fatal(Object msg, Throwable t) {
        if(isEnabled(LEVEL_FATAL)) {
            msg = '[' + LEVELS[LEVEL_FATAL] + ']' + String.valueOf(msg) + '\n';
            write(LEVEL_FATAL, String.valueOf(msg));
            write(LEVEL_FATAL, IoUtils.getStackTrace(t));
        }
    }
}
