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

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.apache.ftpserver.User;

/**
 * This observer interface monitors all the ftp connections.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public
interface FtpConnectionObserver extends Remote {

    /**
     * New connection notification.
     * @param user new connected user.
     */
    void newConnection(final User user) throws RemoteException;

    /**
     * Close connection notification
     * @param user closed user object
     */
    void removeConnection(final User user) throws RemoteException;

    /**
     * Update connection notification
     * @param user updated user
     */
    void updateConnection(final User user) throws RemoteException;
}
