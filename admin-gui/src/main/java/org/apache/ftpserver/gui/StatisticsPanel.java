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
import java.net.InetAddress;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.apache.ftpserver.ftplet.FileObject;
import org.apache.ftpserver.interfaces.FileObserver;
import org.apache.ftpserver.interfaces.FtpIoSession;
import org.apache.ftpserver.interfaces.FtpServerContext;
import org.apache.ftpserver.interfaces.ServerFtpStatistics;
import org.apache.ftpserver.interfaces.StatisticsObserver;
import org.apache.ftpserver.util.DateUtils;

/**
 * This panel displays all the global statistics information.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class StatisticsPanel extends PluginPanel 
                      implements StatisticsObserver, FileObserver, TableModel {

    private static final long serialVersionUID = -8316984131627702453L;
    
    private final static int I_START_TIME        = 0;
    private final static int I_DIR_CREATED       = 1;
    private final static int I_DIR_REMOVED       = 2;
    private final static int I_FILE_UPLOAD       = 3;
    private final static int I_FILE_DOWNLOAD     = 4;
    private final static int I_FILE_REMOVED      = 5;
    private final static int I_UPLOAD_BYTES      = 6;
    private final static int I_DOWNLOAD_BYTES    = 7;
    private final static int I_CURR_LOGINS       = 8;
    private final static int I_TOTAL_LOGINS      = 9;
    private final static int I_CURR_ANON_LOGINS  = 10;
    private final static int I_TOTAL_ANON_LOGINS = 11;
    private final static int I_TOTAL_LOGIN_FAILS = 12;
    private final static int I_CURR_CONS         = 13;
    private final static int I_TOTAL_CONS        = 14;
    
    private final static String[] COL_NAMES = {
            "Name",  
            "Value"
    };

    private final static String[] STAT_NAMES = {
            "Server start time",
            "Number of directories created",
            "Number of directories removed",
            "Number of files uploaded",
            "Number of files downloaded",
            "Number of files deleted",
            "Uploaded bytes",
            "Downloaded bytes",
            "Current logins",
            "Total logins",
            "Current anonymous logins",
            "Total anonymous logins",
            "Total failed logins",
            "Current connections",
            "Total connections"
    };

    private FtpServerContext serverContext;
    private ServerFtpStatistics statistics;
    private String data[] = new String[STAT_NAMES.length];
    private EventListenerList listeners = new EventListenerList();
    
    /**
     * Constructor - create all UI components.
     */
    public StatisticsPanel(PluginPanelContainer container) {
        super(container);
        
        // initialize string array
        for(int i=0; i<data.length; ++i) {
            data[i] = "";
        }
        
        initComponents();
    } 
    
    
    /**
     * Initialize all UI components.
     */
    private void initComponents() {
        
        setLayout(new BorderLayout());
        
        // top table
        JTable dataTable = new JTable(this); 
        JScrollPane scrollPane = new JScrollPane(dataTable, 
                                                 JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                                 JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);
        
        // reload statistics
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        add(btnPanel, BorderLayout.SOUTH);
        
        JButton reloadButton = new JButton("Reload");
        btnPanel.add(reloadButton);
        reloadButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                refresh(serverContext);
            }
        });

        JButton resetButton = new JButton("Reset");
        btnPanel.add(resetButton);
        resetButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                resetStats(serverContext);
            }
        });
    }
    
    /**
     * Get column class - always string
     */
    public Class<String> getColumnClass(int index) {
        return String.class;
    }
    
    /**
     * Get column count.
     */
    public int getColumnCount() {
        return COL_NAMES.length;
    }
    
    /**
     * Get column name.
     */
    public String getColumnName(int index) {
        return COL_NAMES[index];
    } 
    
    /**
     * Get row count.
     */
    public int getRowCount() {
        return STAT_NAMES.length;
    }
    
    /**
     * Is cell editable - currently false.
     */
    public boolean isCellEditable(int row, int col) {
        return false;
    }

    /**
     * Get table cell value.
     */
    public Object getValueAt(int rowIndex, int columnIndex) {
        if(columnIndex == 0) {
            return STAT_NAMES[rowIndex];
        }
        else {
            return data[rowIndex];
        }
    }
    
    /**
     * Set table cell value.
     */
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    }
    
    /**
     * Add table model listener.
     */
    public void addTableModelListener(TableModelListener l) {
        listeners.add(TableModelListener.class, l);
    } 
    
    /**
     * Remove table model listener.
     */
    public void removeTableModelListener(TableModelListener l) {
        listeners.remove(TableModelListener.class, l);
    }

    /**
     * Set value.
     */
    private void setValue(int index, String val) {
                
        // set string data
        if(val == null) {
            val = "";
        }
        data[index] = val;
        
        // notify table listeners
        TableModelEvent e = new TableModelEvent(StatisticsPanel.this, index, index, 1);
        Object[] listenerList = listeners.getListenerList();
        for (int i = listenerList.length-2; i>=0; i-=2) {
            if (listenerList[i]==TableModelListener.class) {
                ((TableModelListener)listenerList[i+1]).tableChanged(e);
            }
        }
    }
    
    /** 
     * Refresh the ftp configuration
     */
    public void refresh(FtpServerContext serverContext) {
        this.serverContext = serverContext;
        if (this.serverContext != null) {
            statistics = (ServerFtpStatistics)this.serverContext.getFtpStatistics();
            statistics.setObserver(this);
            statistics.setFileObserver(this);
            
            // reset component values
            String startTime = DateUtils.getISO8601Date(statistics.getStartTime().getTime());
            setValue(I_START_TIME, startTime);
            notifyMkdir();
            notifyRmdir();
            notifyUpload();
            notifyDownload();
            notifyDelete();
            notifyLogin(true);
            notifyLoginFail(null);
            notifyOpenConnection();
        }
        else {
            if(statistics != null) {
                statistics.setObserver(null);
                statistics.setFileObserver(null);
            }
            statistics = null;
        }
    }
    
    public void resetStats(FtpServerContext serverContext) {
        this.serverContext = serverContext;
        if (this.serverContext != null) {
            statistics = (ServerFtpStatistics)this.serverContext.getFtpStatistics();
            statistics.resetStatisticsCounters();
            
            refresh(serverContext);
        }
    }

    /**
     * Upload notification.
     */
    public void notifyUpload() {
        Runnable runnable = new Runnable() {
            public void run() { 
                ServerFtpStatistics stat = statistics;
                if(stat != null) {
                    int totalUpload = stat.getTotalUploadNumber();
                    setValue(I_FILE_UPLOAD, String.valueOf(totalUpload));
                
                    long totalUploadSz = stat.getTotalUploadSize();
                    setValue(I_UPLOAD_BYTES, String.valueOf(totalUploadSz));
                }
            }
        };
        SwingUtilities.invokeLater(runnable);
    }
    
    /**
     * File upload notification.
     */
    public void notifyUpload(final FtpIoSession session, final FileObject file, final long size) {
        Runnable runnable = new Runnable() {
            public void run() { 
                FilePanel filePanel = (FilePanel)getContainer().getPluginPanel(PluginPanelContainer.FILE_INDEX);
                filePanel.notifyUpload(session, file, size);        
            }
        };
        SwingUtilities.invokeLater(runnable);
    }
    
    /**
     * Download notification.
     */
    public void notifyDownload() {
        Runnable runnable = new Runnable() {
            public void run() {
                ServerFtpStatistics stat = statistics;
                if(stat != null) {
                    int totalDownload = stat.getTotalDownloadNumber();
                    setValue(I_FILE_DOWNLOAD, String.valueOf(totalDownload));
                    
                    long totalDownloadSz = stat.getTotalDownloadSize();
                    setValue(I_DOWNLOAD_BYTES, String.valueOf(totalDownloadSz));
                }
            }
        };
        SwingUtilities.invokeLater(runnable);
    }

    /**
     * File download notification.
     */
    public void notifyDownload(final FtpIoSession session, final FileObject file, final long size) {
        Runnable runnable = new Runnable() {
            public void run() { 
                FilePanel filePanel = (FilePanel)getContainer().getPluginPanel(PluginPanelContainer.FILE_INDEX);
                filePanel.notifyDownload(session, file, size);        
            }
        };
        SwingUtilities.invokeLater(runnable);
    }
    
    /**
     * Delete notification.
     */
    public void notifyDelete() {
        Runnable runnable = new Runnable() {
            public void run() { 
                ServerFtpStatistics stat = statistics;
                if(stat != null) {
                    int totalDelete = stat.getTotalDeleteNumber();
                    setValue(I_FILE_REMOVED, String.valueOf(totalDelete));
                }
            }
        };
        SwingUtilities.invokeLater(runnable);
    }
    
    /**
     * File delete notification.
     */
    public void notifyDelete(final FtpIoSession session, final FileObject file) {
        Runnable runnable = new Runnable() {
            public void run() { 
                FilePanel filePanel = (FilePanel)getContainer().getPluginPanel(PluginPanelContainer.FILE_INDEX);
                filePanel.notifyDelete(session, file);        
            }
        };
        SwingUtilities.invokeLater(runnable);
    }
    
    /**
     * User login notification.
     */
    public void notifyLogin(final boolean anonymous) {
        Runnable runnable = new Runnable() {
            public void run() { 
                ServerFtpStatistics stat = statistics;
                if(stat != null) {
                    int loginNbr = stat.getCurrentLoginNumber();
                    setValue(I_CURR_LOGINS, String.valueOf(loginNbr));
                    
                    int totalLoginNbr = stat.getTotalLoginNumber();
                    setValue(I_TOTAL_LOGINS, String.valueOf(totalLoginNbr));
                    
                    if(anonymous) {
                        int anonLoginNbr = stat.getCurrentAnonymousLoginNumber();
                        setValue(I_CURR_ANON_LOGINS, String.valueOf(anonLoginNbr));
                        
                        int totalAnonLoginNbr = stat.getTotalAnonymousLoginNumber();
                        setValue(I_TOTAL_ANON_LOGINS, String.valueOf(totalAnonLoginNbr));
                    }
                }
            }
        };
        SwingUtilities.invokeLater(runnable);
    }
    
    /**
     * User logout notification.
     */
    public void notifyLogout(boolean anonymous) {
        notifyLogin(anonymous);
    } 

    /** 
     * User failed login notification.
     */
    public void notifyLoginFail(InetAddress address) {
        Runnable runnable = new Runnable() {
            public void run() { 
                ServerFtpStatistics stat = statistics;
                if(stat != null) {
                    int failedNbr = stat.getTotalFailedLoginNumber();
                    setValue(I_TOTAL_LOGIN_FAILS, String.valueOf(failedNbr));
                }
            }
        };
        SwingUtilities.invokeLater(runnable);
    }
     
    /**
     * Notify open connection
     */ 
    public void notifyOpenConnection() {
        Runnable runnable = new Runnable() {
            public void run() { 
                ServerFtpStatistics stat = statistics;
                if(stat != null) {
                    int currCon = stat.getCurrentConnectionNumber();
                    setValue(I_CURR_CONS, String.valueOf(currCon));
                
                    int totalCon = stat.getTotalConnectionNumber();
                    setValue(I_TOTAL_CONS, String.valueOf(totalCon));
                }
            }
        };
        SwingUtilities.invokeLater(runnable);
    }
    
    /**
     * Notify close connection.
     */
    public void notifyCloseConnection() {
        notifyOpenConnection();
    }
    
    /**
     * Make directory notification.
     */
    public void notifyMkdir() {
        Runnable runnable = new Runnable() {
            public void run() { 
                ServerFtpStatistics stat = statistics;
                if(stat != null) {
                    int totalMkdir = stat.getTotalDirectoryCreated();
                    setValue(I_DIR_CREATED, String.valueOf(totalMkdir));
                }
            }
        };
        SwingUtilities.invokeLater(runnable);
    }

    /**
     * Make directry notification.
     */
    public void notifyMkdir(final FtpIoSession session, final FileObject file) {
        Runnable runnable = new Runnable() {
            public void run() { 
                DirectoryPanel dirPanel = (DirectoryPanel)getContainer().getPluginPanel(PluginPanelContainer.DIR_INDEX);
                dirPanel.notifyMkdir(session, file);        
            }
        };
        SwingUtilities.invokeLater(runnable);
    }
    
    /**
     * Directory removal notification.
     */
    public void notifyRmdir() {
        Runnable runnable = new Runnable() {
            public void run() { 
                ServerFtpStatistics stat = statistics;
                if(stat != null) {
                    int totalRmdir = stat.getTotalDirectoryRemoved();
                    setValue(I_DIR_REMOVED, String.valueOf(totalRmdir));
                }
            }
        };
        SwingUtilities.invokeLater(runnable);
    }   
    
    /**
     * Remove directry notification.
     */
    public void notifyRmdir(final FtpIoSession session, final FileObject file) {
        Runnable runnable = new Runnable() {
            public void run() { 
                DirectoryPanel dirPanel = (DirectoryPanel)getContainer().getPluginPanel(PluginPanelContainer.DIR_INDEX);
                dirPanel.notifyRmdir(session, file);        
            }
        };
        SwingUtilities.invokeLater(runnable);
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
        return "Statistics";
    }

}
