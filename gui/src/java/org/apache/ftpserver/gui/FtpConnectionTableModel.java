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

import java.net.InetAddress;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import org.apache.ftpserver.FtpUser;
import org.apache.ftpserver.FtpUserImpl;
import org.apache.ftpserver.gui.remote.FtpConnectionObserverAdapter;
import org.apache.ftpserver.remote.interfaces.ConnectionServiceInterface;
import org.apache.ftpserver.remote.interfaces.FtpConnectionObserver;


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
    public FtpUserImpl getUser(int index) {
        FtpUserImpl user = null;
        synchronized(mConnectedUserList) {
            if ( (index >= 0) && (index < mConnectedUserList.size()) ) {
                user = (FtpUserImpl)mConnectedUserList.get(index);
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
        FtpUserImpl thisUser = getUser(row);
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



