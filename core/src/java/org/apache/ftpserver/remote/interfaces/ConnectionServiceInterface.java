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

package org.apache.ftpserver.remote.interfaces;

import java.util.List;
import java.rmi.Remote;
import java.rmi.RemoteException;
import org.apache.ftpserver.UserImpl;

/**
 * Ftp user service interface - used by remote admin GUI.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public
interface ConnectionServiceInterface extends Remote {

    /**
     * It returns a list of all the currently connected user objects.
     */
    List getAllUsers() throws RemoteException;

    /**
     * Set user manager observer.
     */
    void setObserver(final FtpConnectionObserver obsr) throws RemoteException;

    /**
     * Get the observer.
     */
    FtpConnectionObserver getObserver() throws RemoteException;

    /**
     * Close ftp connection for this session id.
     */
    void closeConnection(final String sessionId) throws RemoteException;

    /**
     * Close all - close all the connections.
     */
    void closeAllConnections() throws RemoteException;

    /**
     * Get connected user
     */
    UserImpl getUser(String sessId) throws RemoteException;

    /**
     * Set spy object
     */
    void setSpyObject(String sessId, SpyConnectionInterface spy) throws RemoteException;
}
