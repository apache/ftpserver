/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1997-2003 The Apache Software Foundation. All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *    "This product includes software developed by the
 *    Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software
 *    itself, if and wherever such third-party acknowledgments
 *    normally appear.
 *
 * 4. The names "Incubator", "FtpServer", and "Apache Software Foundation"
 *    must not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation. For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * $Id$
 */
package org.apache.ftpserver.gui;

import java.util.Date;
import java.util.Vector;
import java.text.SimpleDateFormat;
import javax.swing.table.AbstractTableModel;
import org.apache.ftpserver.FtpUser;

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
    public void newEntry(String file, FtpUser user) {

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
