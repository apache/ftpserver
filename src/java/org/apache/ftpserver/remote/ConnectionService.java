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
