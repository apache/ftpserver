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

import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;
import java.net.InetAddress;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.table.AbstractTableModel;


/**
 * This table model is used to display <code>Properties</code>.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public
class PropertiesTableModel extends AbstractTableModel {

    private final static Class[] RETURN_TYPES = {
        Boolean.TYPE,
        Boolean.class,
        Integer.TYPE,
        Integer.class,
        String.class,
        InetAddress.class
    };

    private final static String[] COL_NAMES = {"Parameter", "Value"};

    /**
     * <code>Properties</code> object to be displayed.
     */
    protected Properties mTableProp;

    /**
     * All properties key.
     */
    protected Vector mTableKeys;


    /**
     * Create this <code>TableModel</code> with empty properties.
     */
    public PropertiesTableModel() {
        this(new Properties());
    }

    /**
     * Create this <code>TableModel</code>.
     */
    public PropertiesTableModel(Properties prop) {
        mTableProp = prop;
        mTableKeys = new Vector();
        initializeTable();
    }

    /**
     * Initialize table - populate vector.
     */
    protected void initializeTable() {
        mTableKeys.clear();
        Enumeration keyNames = mTableProp.propertyNames();
        while(keyNames.hasMoreElements()) {
            String key = keyNames.nextElement().toString();
            if(key.trim().equals("")) {
                continue;
            }
            mTableKeys.add(key);
        }
        Collections.sort(mTableKeys);
    }


    /**
     * Get column class - always string.
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
        return mTableKeys.size();
    }

    /**
     * Get value at.
     */
    public Object getValueAt(int row, int col) {
        if(col == 0) {
            return mTableKeys.get(row);
        }
        else {
            return mTableProp.getProperty(mTableKeys.get(row).toString());
        }
    }

    /**
     * Is cell editable - currently true.
     */
    public boolean isCellEditable(int row, int col) {
        return true;
    }

   /**
    * Set value at - does not set value - dummy metod.
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
    * Reload properties.
    */
   public void reload() {
       initializeTable();
       fireTableDataChanged();
   }

   /**
    * Reload a new properties object.
    */
   public void reload(Properties prop) {
       mTableProp = prop;
       initializeTable();
       fireTableDataChanged();
   }


   /**
    * Create configuration table model.
    */
   public static PropertiesTableModel getTableModel(Object obj) throws Exception {
        Properties prop = new Properties();
        Class clazz = obj.getClass();
        BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
        PropertyDescriptor descriptors[] = beanInfo.getPropertyDescriptors();

        if(descriptors != null) {
            for(int i=0; i<descriptors.length; ++i) {
                Method met = descriptors[i].getReadMethod();
                if(met != null) {
                    Class retType = met.getReturnType();
                    boolean isDisplayable = false;
                    for(int j=0; j<RETURN_TYPES.length; ++j) {
                        if(RETURN_TYPES[j].equals(retType)) {
                            isDisplayable = true;
                            break;
                        }
                    }

                    if(!isDisplayable) {
                        continue;
                    }

                    Object valObj = null;
                    try {
                        valObj = met.invoke(obj, new Object[0]);
                    }
                    catch(InvocationTargetException ex) {
                        Throwable th = ex.getCause();
                        if(th instanceof Exception) {
                            throw (Exception)th;
                        }
                        else if(th instanceof Error) {
                            throw (Error)th;
                        }
                    }

                    String val = "";
                    if(valObj != null) {
                        val = String.valueOf(valObj);
                    }

                    String name = descriptors[i].getName();
                    name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
                    prop.setProperty(name, val);
                }
            }
        }
        return new PropertiesTableModel(prop);
   }

}
