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

import java.rmi.RemoteException;
import java.net.InetAddress;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.text.SimpleDateFormat;
import javax.swing.table.AbstractTableModel;

import org.apache.ftpserver.FtpUser;
import org.apache.ftpserver.remote.interfaces.ConnectionServiceInterface;
import org.apache.ftpserver.remote.interfaces.FtpConnectionObserver;
import org.apache.ftpserver.gui.remote.FtpConnectionObserverAdapter;


/**
 * This table model tracks currently logged in users.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>.
 */
public
class FtpConnectionTableModel extends AbstractTableModel
                              implements FtpConnectionObserver {

    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("MM/dd HH:mm:ss");

    private static final String[] COL_NAMES = {"User",
                                               "Login Time",
                                               "Last Access Time",
                                               "Client"};
    private List mConnectedUserList;

    private ConnectionServiceInterface mConService;
    private CommonHandler mCommonHandler;
    private FtpConnectionObserverAdapter mObserver;


    /**
     * Constructor - initialize user list
     */
    public FtpConnectionTableModel(CommonHandler commonHandler) {
        mCommonHandler = commonHandler;
        mConService = mCommonHandler.getConnectionService();
        mConnectedUserList = new Vector();
        try {
            mObserver = new FtpConnectionObserverAdapter(mConService, this);
            reload();
        }
        catch(Exception ex) {
            commonHandler.handleException(ex);
        }
    }

    /**
     * Get user
     */
    public FtpUser getUser(int index) {
        FtpUser user = null;
        synchronized(mConnectedUserList) {
            if ( (index >= 0) && (index < mConnectedUserList.size()) ) {
                user = (FtpUser)mConnectedUserList.get(index);
            }
        }
        return user;
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
        return mConnectedUserList.size();
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

        // error check
        String retVal = "";
        FtpUser thisUser = getUser(row);
        if (thisUser == null) {
            return retVal;
        }

        switch(col) {
            case 0:
                retVal = thisUser.getName();
                if (retVal == null) {
                    retVal = "";
                }
                break;

            case 1:
                long loginTime = thisUser.getLoginTime();
                if (loginTime > 0) {
                    retVal = DATE_FMT.format(new Date(loginTime));
                }
                break;

            case 2:
                long accessTime = thisUser.getLastAccessTime();
                if (accessTime > 0) {
                    retVal = DATE_FMT.format(new Date(accessTime));
                }
                break;

            case 3:
                InetAddress remoteHost = thisUser.getClientAddress();
                if(remoteHost != null) {
                    retVal = remoteHost.getHostAddress();
                }
                break;
        }
        return retVal;
    }


    ///////////////////////////  Observer Methods ///////////////////////////
    /**
     * Add a new user
     */
    public void newConnection(final FtpUser thisUser) {
        if (thisUser == null) {
            return;
        }

        int sz = -1;
        synchronized(mConnectedUserList) {
            mConnectedUserList.add(thisUser);
            sz = mConnectedUserList.size();
        }

        if (sz != -1) {
            fireTableRowsInserted(sz, sz);
        }
    }


    /**
     * Close .
     */
    public void removeConnection(final FtpUser user) {
        if (user == null) {
            return;
        }

        int index = -1;
        synchronized(mConnectedUserList) {
            index = mConnectedUserList.indexOf(user);
            if (index != -1) {
                mConnectedUserList.remove(index);
            }
        }

        if (index != -1) {
            fireTableRowsDeleted(index, index);
        }
    }

    /**
     * Existing connected user update notification.
     */
    public void updateConnection(final FtpUser user) {
        if (user == null) {
            return;
        }

        int index = -1;
        synchronized(mConnectedUserList) {
            index = mConnectedUserList.indexOf(user);
            if (index != -1) {
                mConnectedUserList.set(index, user);
            }
        }

        if (index != -1) {
            fireTableRowsUpdated(index, index);
        }
    }


    /**
     * Reload table model
     */
    public void reload() throws RemoteException {
        synchronized(mConnectedUserList) {
            mConnectedUserList.clear();
            mConnectedUserList.addAll(mConService.getAllUsers());
        }
        fireTableDataChanged();
    }


    /**
     * Close the resource
     */
    public void close() {
        mObserver.close();
        mConnectedUserList.clear();
    }

}



