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

import java.net.InetAddress;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.ftplet.FtpSession;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.interfaces.Connection;
import org.apache.ftpserver.interfaces.ConnectionManager;
import org.apache.ftpserver.interfaces.ConnectionManagerObserver;
import org.apache.ftpserver.interfaces.FtpServerContext;
import org.apache.ftpserver.util.DateUtils;


/**
 * This table model tracks currently connected users.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>.
 */
public
class FtpConnectionTableModel implements TableModel, ConnectionManagerObserver {
    
    private static final long serialVersionUID = -6586674924776468079L;
    private final static String[] COL_NAMES = {"User", 
                                               "Login Time", 
                                               "Last Access Time",    
                                               "IP"};
    private List connections = new Vector();
    private FtpServerContext serverContext;
    private EventListenerList listeners = new EventListenerList();
    
    /**
     * Reload the model.
     */
    public void refresh(FtpServerContext serverContext) {
        this.serverContext = serverContext;
        if (this.serverContext != null) {
            ConnectionManager conManager = this.serverContext.getConnectionManager();
            connections = conManager.getAllConnections();
            conManager.setObserver(this);
        }
        else {
            connections.clear();
        }
        fireTableChanged(new TableModelEvent(this));
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
        return connections.size();
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
        Connection thisCon = null;
        if (row < connections.size()) {
            thisCon = (Connection)connections.get(row);
        }
        if (thisCon == null) {
            return retVal;
        }
        
        FtpSession session = thisCon.getSession();
        if(session == null) {
            return retVal;
        }
        
        User user = session.getUser();
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
                date = session.getLoginTime();
                if(date != null) {
                    retVal = DateUtils.getISO8601Date(date.getTime());
                }
                break;
                
            case 2:
                date = session.getLastAccessTime();
                if(date != null) {
                    retVal = DateUtils.getISO8601Date(date.getTime());
                }
                break;
                
            case 3:
                addr = session.getRemoteAddress();
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
    public Connection getConnection(int index) {
        if(index < connections.size()) {
            return (Connection)connections.get(index);
        }
        return null;
    }
     
    /**
     * Adds a listener to the list that's notified each time a change
     * to the data model occurs.
     */
    public void addTableModelListener(TableModelListener l) {
        listeners.add(TableModelListener.class, l);
    }

    /**
     * Removes a listener from the list that's notified each time a
     * change to the data model occurs.
     */
    public void removeTableModelListener(TableModelListener l) {
        listeners.remove(TableModelListener.class, l);
    }
    
    /**
     * Forwards the given notification event to all
     * <code>TableModelListeners</code> that registered
     * themselves as listeners for this table model.
     */
    private void fireTableChanged(TableModelEvent e) {
        Object[] listenerArr = listeners.getListenerList();
        for (int i = listenerArr.length-2; i>=0; i-=2) {
            if (listenerArr[i]==TableModelListener.class) {
                ((TableModelListener)listenerArr[i+1]).tableChanged(e);
            }
        }
    }
    
    /////////////////////////// Observer Methods ///////////////////////////
    /**
     * Add a new connection
     */
    public void openedConnection(final Connection con) {
        Runnable runnable = new Runnable() {
            public void run() { 
                connections.add(con);
                int sz = connections.size();
                fireTableChanged(new TableModelEvent(FtpConnectionTableModel.this, 
                                                     sz, 
                                                     sz,
                                                     TableModelEvent.ALL_COLUMNS, 
                                                     TableModelEvent.INSERT));        
            }
        };
        SwingUtilities.invokeLater(runnable);
    }  
    
    /**
     * Closed connection.
     */
    public void closedConnection(final Connection con) {
        Runnable runnable = new Runnable() {
            public void run() { 
                int index = connections.indexOf(con);
                if (index != -1) {
                    connections.remove(index);
                    fireTableChanged(new TableModelEvent(FtpConnectionTableModel.this, 
                                                         index, 
                                                         index,
                                                         TableModelEvent.ALL_COLUMNS, 
                                                         TableModelEvent.DELETE));
                }        
            }
        };
        SwingUtilities.invokeLater(runnable);
    } 
    
    /**
     * Existing connected connection update notification.
     */
    public void updatedConnection(final Connection con) {
        Runnable runnable = new Runnable() {
            public void run() { 
                int index = connections.indexOf(con);
                if(index != -1) {
                    fireTableChanged(new TableModelEvent(FtpConnectionTableModel.this, 
                                                         index, 
                                                         index,
                                                         TableModelEvent.ALL_COLUMNS, 
                                                         TableModelEvent.UPDATE));
                }        
            }
        };
        SwingUtilities.invokeLater(runnable);
    } 
}    
