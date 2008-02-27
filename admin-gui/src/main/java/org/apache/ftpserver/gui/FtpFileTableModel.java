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

import java.util.List;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.util.DateUtils;

/**
 * This table model tracks user file related activities.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>.
 */
public
class FtpFileTableModel extends AbstractTableModel {
    
    private static final long serialVersionUID = 2111896856959304666L;
    
    private final static int MAX_SIZE = 1000;
    private final static String[] COL_NAMES = {"File", 
                                               "User", 
                                               "Time"};    

    private List<TableEntry> entries = new Vector<TableEntry>();
         
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
                retVal = entry.fileName;
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
        String fileName = file;
        String userName = user.getName();
        String date = DateUtils.getISO8601Date(System.currentTimeMillis());
        TableEntry entry = new TableEntry(fileName, userName, date);
        
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
        fireTableRowsInserted(sz, sz);
    }  
    
    /**
     * Remove all entries.
     */
    public void clear() {
        entries.clear();
        fireTableDataChanged();
    }
    
    //////////////////////////////////////////////////////////
    /////////////////////// list entry  //////////////////////
    private static class TableEntry {
        public TableEntry(String fileName, String userName, String date) {
            this.fileName = fileName;
            this.userName = userName;
            this.date = date;
        }
        final String fileName;
        final String userName;
        final String date;
    }
    
}    
