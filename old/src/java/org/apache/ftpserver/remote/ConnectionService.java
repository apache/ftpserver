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
package org.apache.ftpserver.remote;

import java.util.List;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import org.apache.ftpserver.FtpUser;
import org.apache.ftpserver.BaseFtpConnection;
import org.apache.ftpserver.remote.adapter.FtpConnectionObserverAdapter;
import org.apache.ftpserver.remote.adapter.SpyConnectionAdapter;
import org.apache.ftpserver.remote.interfaces.SpyConnectionInterface;
import org.apache.ftpserver.remote.interfaces.FtpConnectionObserver;

/**
 * Ftp remote user service adapter class - used by remote admin GUI.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public
class ConnectionService implements org.apache.ftpserver.remote.interfaces.ConnectionServiceInterface {

    private org.apache.ftpserver.ConnectionService mConnectionService;
    private FtpConnectionObserverAdapter mConnectionObserverAdapter;


    /**
     * Constructor - sets the actual connection service object
     */
    public ConnectionService(final org.apache.ftpserver.ConnectionService conService) throws RemoteException {
        mConnectionService = conService;
        mConnectionObserverAdapter = new FtpConnectionObserverAdapter();
        UnicastRemoteObject.exportObject(this);
    }

    /**
     * Get the actual object.
     */
    public org.apache.ftpserver.ConnectionService getConnectionService() {
        return mConnectionService;
    }

    /**
     * It returns a list of all the currently connected users.
     */
    public List getAllUsers() {
        return mConnectionService.getAllUsers();
    }

    /**
     * Set connection observer.
     */
    public void setObserver(final FtpConnectionObserver obsr) {
        mConnectionObserverAdapter.setConnectionObserver(obsr);
        if (obsr == null) {
            mConnectionService.setObserver(null);
        }
        else {
            mConnectionService.setObserver(mConnectionObserverAdapter);
        }
    }

    /**
     * Get the observer.
     */
    public FtpConnectionObserver getObserver() {
        return mConnectionObserverAdapter.getConnectionObserver();
    }

    /**
     * Get connected user
     */
    public FtpUser getUser(final String sessId) {
        BaseFtpConnection con = mConnectionService.getConnection(sessId);
        return (con != null) ? con.getUser() : null;
    }

    /**
     * Set spy object
     */
    public void setSpyObject(final String sessId, final SpyConnectionInterface spy) {
        if (spy == null) {
            mConnectionService.setSpyObject(sessId, null);
        }
        else {
            SpyConnectionAdapter newAdapter = new SpyConnectionAdapter(spy);
            mConnectionService.setSpyObject(sessId, newAdapter);
        }
    }

    /**
     * Close ftp connection for this session id.
     */
    public void closeConnection(final String sessionId) {
        mConnectionService.closeConnection(sessionId);
    }

    /**
     * Close all - close all the connections.
     */
    public void closeAllConnections() {
        mConnectionService.closeAllConnections();
    }
}
