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
package org.apache.ftpserver.remote;

import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.server.Unreferenced;
import java.rmi.server.UID;

import org.apache.ftpserver.usermanager.User;
import org.apache.ftpserver.usermanager.UserManagerInterface;
import org.apache.ftpserver.remote.interfaces.FtpConfigInterface;
import org.apache.ftpserver.remote.interfaces.RemoteHandlerInterface;

/**
 * Ftp server remote admin adapter. This is the starting point of remote admin.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public
class RemoteHandler implements RemoteHandlerInterface, Unreferenced {

    private FtpConfig mFtpConfig;
    private String mstAdminSession;
    private Registry mRegistry;

    /**
     * Constructor - set the actual user config object
     */
    public RemoteHandler(org.apache.ftpserver.FtpConfig config) throws RemoteException {

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
        mFtpConfig = new FtpConfig(config);
    }

    /**
     * Remote admin login
     */
    public synchronized String login(String id, String password) throws Exception {
        try {
            mFtpConfig.getConfig().getLogger().info("Remote admin login request from " + UnicastRemoteObject.getClientHost());
        }
        catch(Exception ex) {
            mFtpConfig.getConfig().getLogger().error("RemoteHandler.login()", ex);
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
            mFtpConfig.getConfig().getLogger().info("Remote admin login from " + UnicastRemoteObject.getClientHost());
        }
        catch(Exception ex) {
            mFtpConfig.getConfig().getLogger().error("RemoteHandler.login()", ex);
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
        mFtpConfig.getConfig().getLogger().info("Remote admin logout");
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
        mFtpConfig.getConfig().getLogger().info("Closing remote handler...");
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
        mFtpConfig.getConfig().getLogger().info("Remote admin timeout");
        logout(mstAdminSession);
    }
}
