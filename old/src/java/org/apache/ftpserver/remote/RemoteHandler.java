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

import org.apache.ftpserver.AbstractFtpConfig;
import org.apache.ftpserver.interfaces.FtpRemoteHandlerMonitor;
import org.apache.ftpserver.remote.interfaces.FtpConfigInterface;
import org.apache.ftpserver.remote.interfaces.RemoteHandlerInterface;
import org.apache.ftpserver.usermanager.UserManagerInterface;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UID;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.server.Unreferenced;

/**
 * Ftp server remote admin adapter. This is the starting point of remote admin.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public
class RemoteHandler implements RemoteHandlerInterface, Unreferenced {

    private RemoteFtpConfig mFtpConfig;
    private String mstAdminSession;
    private Registry mRegistry;
    private FtpRemoteHandlerMonitor ftpRemoteHandlerMonitor;

    /**
     * Constructor - set the actual user config object
     */
    public RemoteHandler(AbstractFtpConfig config, FtpRemoteHandlerMonitor ftpRemoteHandlerMonitor) throws RemoteException {
        this.ftpRemoteHandlerMonitor = ftpRemoteHandlerMonitor;

        // open registry
        int rmiPort = config.getRemoteAdminPort();
        try {
            mRegistry = LocateRegistry.getRegistry(rmiPort);
            mRegistry.list();
        }
        catch(RemoteException ex) {
            mRegistry = null;
        }

        if(mRegistry == null) {
           mRegistry = LocateRegistry.createRegistry(rmiPort);
        }

        UnicastRemoteObject.exportObject(this);
        mRegistry.rebind(BIND_NAME, this);
        mFtpConfig = new RemoteFtpConfig(config);
    }

    /**
     * Remote admin login
     */
    public synchronized String login(String id, String password) throws Exception {
        try {
            String clientHost = UnicastRemoteObject.getClientHost();
            ftpRemoteHandlerMonitor.remoteLoginAdminRequest(clientHost);
        }
        catch(Exception ex) {
            ftpRemoteHandlerMonitor.remoteAdminLoginRequestError(ex);
        }

        // data validation
        if(mstAdminSession != null) {
            throw new Exception("Multiple admin session is not possible.");
        }
        if(id == null) {
            throw new Exception("Please specify user Id");
        }
        if(password == null) {
            throw new Exception("Please specify password");
        }

        // admin login
        UserManagerInterface userManager = mFtpConfig.getConfig().getUserManager();
        String adminName = userManager.getAdminName();
        boolean bSuccess = false;
        if ( id.equals(adminName) ) {
            bSuccess = userManager.authenticate(id, password);
        }
        if(!bSuccess) {
            throw new Exception("Login failure.");
        }

        try {
            String clientHost = UnicastRemoteObject.getClientHost();
            ftpRemoteHandlerMonitor.remoteLoginAdminRequest(clientHost);
        }
        catch(Exception ex) {
            ftpRemoteHandlerMonitor.remoteAdminLoginRequestError(ex);
        }
        mstAdminSession = new UID().toString();
        return mstAdminSession;
    }


    /**
     * Remote admin logout
     */
    public synchronized boolean logout(String sessId) {
        if( (sessId == null) || (!sessId.equals(mstAdminSession)) ) {
            return false;
        }
        ftpRemoteHandlerMonitor.remoteAdminLogout();
        resetObservers();
        mstAdminSession = null;
        return true;
    }

    /**
     * Get configuration interface
     */
    public FtpConfigInterface getConfigInterface(String sessId) {
        if( (sessId == null) || (!sessId.equals(mstAdminSession)) ) {
            return null;
        }
        return mFtpConfig;
    }

    /**
     * Reset observers
     */
    private void resetObservers() {
        ConnectionService conService = (ConnectionService)mFtpConfig.getConnectionService();
        conService.setObserver(null);
        conService.getConnectionService().resetAllSpyObjects();

        FtpStatistics statistics = (FtpStatistics)mFtpConfig.getStatistics();
        statistics.setListener(null);
        statistics.setFileListener(null);
    }

    /**
     * Close the remote handler
     */
    public void dispose() {
        ftpRemoteHandlerMonitor.remoteAdminClose();
        resetObservers();
        try {
            if (mRegistry != null) {
                mRegistry.unbind(BIND_NAME);
                mRegistry = null;
            }
        }
        catch(Exception ex) {
        }
    }

    /**
     * Unreferenced - admin user idle timeout
     */
    public synchronized void unreferenced() {
        ftpRemoteHandlerMonitor.remoteAdminTimeout();
        logout(mstAdminSession);
    }



}
