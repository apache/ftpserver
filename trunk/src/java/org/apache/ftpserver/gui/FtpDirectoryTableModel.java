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

import java.util.Vector;

import javax.swing.event.EventListenerList;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.util.DateUtils;

/**
 * This table model tracks user file related activities.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>.
 */
public
class FtpDirectoryTableModel implements TableModel {
    
    private static final long serialVersionUID = 2468107659374857692L;
    
    private final static int MAX_SIZE = 1000;
    private final static String[] COL_NAMES = {"Directory", 
                                               "User", 
                                               "Time"};    

    private Vector entries = new Vector();
    private EventListenerList listeners = new EventListenerList();
         
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
        return entries.size();
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
     * Get value at.
     */
    public Object getValueAt(int row, int col) {
        
        String retVal = "";
        TableEntry entry = null;
        try {
            entry = (TableEntry)entries.get(row);
        }
        catch(Exception ex) {
        }
        if (entry == null) {
            return retVal;
        }
        
        switch(col) {
            case 0:
                retVal = entry.dirName;
                break;
            
            case 1:
                retVal = entry.userName;
                break;
            
            case 2:
                retVal = entry.date;
                break;  
        }
        return retVal;
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
     * Add a new user
     */
    public void newEntry(String file, User user) {
        
        // create a new table entry
        String dirName = file;
        String userName = user.getName();
        String date = DateUtils.getISO8601Date(System.currentTimeMillis());
        TableEntry entry = new TableEntry(dirName, userName, date);
        
        // clear if already too many entries
        int sz = entries.size();
        if ( (MAX_SIZE > 0) && (sz >= MAX_SIZE) ) {
            clear();
            sz = 0;
        }
        
        // add the new entry
        synchronized(entries) {
            entries.add(entry);
            ++sz;
        }
        fireTableChanged(new TableModelEvent(FtpDirectoryTableModel.this, 
                                             sz, 
                                             sz,
                                             TableModelEvent.ALL_COLUMNS, 
                                             TableModelEvent.INSERT));
    }
    
    /**
     * Remove all entries
     */
    public void clear() {
        entries.clear();
        fireTableChanged(new TableModelEvent(this));
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
    
    //////////////////////////////////////////////////////////
    ///////////////////////list entry  ///////////////////////
    private static class TableEntry {
        public TableEntry(String dirName, String userName, String date) {
            this.dirName = dirName;
            this.userName = userName;
            this.date = date;
        }
        final String dirName;
        final String userName;
        final String date;
    }
    
}    
