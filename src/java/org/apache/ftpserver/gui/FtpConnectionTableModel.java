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

import java.net.InetAddress;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.interfaces.ConnectionManagerObserver;
import org.apache.ftpserver.interfaces.IConnection;
import org.apache.ftpserver.interfaces.IFtpConfig;
import org.apache.ftpserver.util.DateUtils;


/**
 * This table model tracks currently connected users.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>.
 */
public
class FtpConnectionTableModel extends AbstractTableModel 
                              implements ConnectionManagerObserver {
    
    private static final long serialVersionUID = -6586674924776468079L;
    private final static String[] COL_NAMES = {"User", 
                                               "Login Time", 
                                               "Last Access Time",    
                                               "IP"};
    private List m_connections;
    private IFtpConfig m_fonfig;
    
    /**
     * Constructor - initialize user list
     */
    public FtpConnectionTableModel() {
        m_connections = new Vector();
    }
    
    /**
     * Reload the model.
     */
    public void refresh(IFtpConfig cfg) {
        m_fonfig = cfg;
        if (m_fonfig != null) {
            m_connections = m_fonfig.getConnectionManager().getAllConnections();
            m_fonfig.getConnectionManager().setObserver(this);
        }
        else {
            m_connections.clear();
        }
        fireTableDataChanged();
    }
     
    /**
     * Get column class - always string
     */
    public Class getColumnClass(int index) {
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
        return m_connections.size();
    }
    
    /**
     * Is cell editable - currently false.
     */
    public boolean isCellEditable(int row, int col) {
        return true;
    }
    
   /**
    * Set value at - dummy method
    */
   public void setValueAt(Object val, int row, int col) {
   }
   
   /**
    * Find column index.
    */
   public int findColumn(String columnName) {
        int index = -1;
        for(int i=COL_NAMES.length; --i>=0; ) {
            if (COL_NAMES[i].equals(columnName)) {
                index = i;
                break;
            }
        }
        return index;
   }
   
    /**
     * Get value at.
     */
    public Object getValueAt(int row, int col) {
        
        // error check
        String retVal = "";
        
        IConnection thisCon = null;
        if (row < m_connections.size()) {
            thisCon = (IConnection)m_connections.get(row);
        }
        if (thisCon == null) {
            return retVal;
        }
        FtpRequest request = thisCon.getRequest();
        User user = request.getUser();
        InetAddress addr = null;
        Date date = null;
        switch(col) {
            case 0:
                if(user != null) {
                    String name = user.getName();
                    if(name != null) {
                        retVal = name;
                    }
                }
                break;
                
            case 1:
                date = request.getLoginTime();
                if(date != null) {
                    retVal = DateUtils.getISO8601Date(date.getTime());
                }
                break;
                
            case 2:
                date = request.getLastAccessTime();
                if(date != null) {
                    retVal = DateUtils.getISO8601Date(date.getTime());
                }
                break;
                
            case 3:
                addr = request.getRemoteAddress();
                if (addr != null) {
                    retVal = addr.getHostAddress();
                }
                break;   
        }
        return retVal;
    }
    
    /**
     * Get connection at an index.
     */
    public IConnection getConnection(int index) {
        if(index < m_connections.size()) {
            return (IConnection)m_connections.get(index);
        }
        return null;
    }
     
    /////////////////////////// Observer Methods ///////////////////////////
    /**
     * Add a new connection
     */
    public void openedConnection(final IConnection con) {
        Runnable runnable = new Runnable() {
            public void run() { 
                m_connections.add(con);
                int sz = m_connections.size();
                fireTableRowsInserted(sz, sz);        
            }
        };
        SwingUtilities.invokeLater(runnable);
    }  
    
    /**
     * Closed connection.
     */
    public void closedConnection(final IConnection con) {
        Runnable runnable = new Runnable() {
            public void run() { 
                int index = m_connections.indexOf(con);
                if (index != -1) {
                    m_connections.remove(index);
                    fireTableRowsDeleted(index, index);
                }        
            }
        };
        SwingUtilities.invokeLater(runnable);
    } 
    
    /**
     * Existing connected connection update notification.
     */
    public void updatedConnection(final IConnection con) {
        Runnable runnable = new Runnable() {
            public void run() { 
                int index = m_connections.indexOf(con);
                if(index != -1) {       
                    fireTableRowsUpdated(index, index);
                }        
            }
        };
        SwingUtilities.invokeLater(runnable);
    } 

}    
