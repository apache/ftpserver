/* ====================================================================
 * Copyright 2002 - 2004
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
 *
 *
 * $Id$
 */

package org.apache.ftpserver.gui;

import java.util.Date;
import java.util.Vector;
import java.text.SimpleDateFormat;
import javax.swing.table.AbstractTableModel;
import org.apache.ftpserver.core.UserImpl;

/**
 * This table model tracks user file related activities.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>.
 */
public
class FtpFileTableModel extends AbstractTableModel {

    private static final int MAX_SIZE = 1000;
    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("MM/dd HH:mm:ss");
    private static final String[] COL_NAMES = {"File",
                                               "User",
                                               "Time"};

    private Vector mEntryList = new Vector();


    /**
     * Reload the model.
     */
    public void reset() {
        mEntryList.clear();
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
        return mEntryList.size();
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
            entry = (TableEntry)mEntryList.get(row);
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
    public void newEntry(String file, UserImpl user) {

        TableEntry entry = new TableEntry();
        entry.fileName = file;
        entry.userName = user.getName();
        entry.date = DATE_FMT.format(new Date());

        int sz = mEntryList.size();
        if ( (MAX_SIZE > 0) && (sz >= MAX_SIZE) ) {
            reset();
            sz = 0;
        }

        synchronized(mEntryList) {
            mEntryList.add(entry);
            ++sz;
        }
        fireTableRowsInserted(sz, sz);
    }

    /**
     * Remove all entries
     */
    public void close() {
        mEntryList.clear();
    }

    //////////////////////////////////////////////////////////
    ///////////////////////list entry  ///////////////////////
    public class TableEntry {
        String fileName;
        String userName;
        String date;
    }

}
