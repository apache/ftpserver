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
import java.net.InetAddress;

/**
 * Ftp configuration remote interface. It is used by remote admin GUI.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public
interface FtpConfigInterface extends Remote {

    /**
     * Get user manager
     */
    UserManagerInterface getUserManager() throws RemoteException;

    /**
     * Get ip restrictor
     */
    IpRestrictorInterface getIpRestrictor() throws RemoteException;

    /**
     * Get server bind address.
     */
    InetAddress getServerAddress() throws RemoteException;

    /**
     * Get address string
     */
    String getAddressString() throws RemoteException;

    /**
     * Get server port.
     */
    int getServerPort() throws RemoteException;

    /**
     * Check annonymous login support.
     */
    boolean isAnonymousLoginAllowed() throws RemoteException;

    /**
     * Get user properties.
     */
    ConnectionServiceInterface getConnectionService() throws RemoteException;

    /**
     * Get maximum number of connections.
     */
    int getMaxConnections() throws RemoteException;

    /**
     * Get maximum number of anonymous connections.
     */
    int getMaxAnonymousLogins() throws RemoteException;

    /**
     * Get poll interval in seconds.
     */
    int getSchedulerInterval() throws RemoteException;

    /**
     * Get default idle time in seconds.
     */
    int getDefaultIdleTime() throws RemoteException;

    /**
     * Get default root directory
     */
    String getDefaultRoot() throws RemoteException;

    /**
     * Get global statistics object.
     */
    FtpStatisticsInterface getStatistics() throws RemoteException;

    /**
     * Get rmi port
     */
    int getRemoteAdminPort() throws RemoteException;

    /**
     * Is remote admin allowed
     */
    boolean isRemoteAdminAllowed() throws RemoteException;

    /**
     * Get base directory.
     */
    String getBaseDirectory() throws RemoteException;

}

