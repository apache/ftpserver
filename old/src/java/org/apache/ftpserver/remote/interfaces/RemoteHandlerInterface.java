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

/**
 * Ftp server remote admin interface. This is the starting point of remote admin.
 * Call stack:
 * <pre>
 *    RemoteHandlerInterface
 *    |
 *    +---- FtpConfigInterface
 *          |
 *          +---- FtpStatisticsInterface &lt;- FtpStatisticsListener, FtpFileListener
 *          |
 *          +---- ConnectionServiceInterface &lt;- FtpConnectionObserver
 *          |
 *          +---- IpRestrictorInterface
 *          |
 *          +---- UserManagerInterface
 * </pre>
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public
interface RemoteHandlerInterface extends Remote {

    /**
     * Remote interface ID
     */
    String BIND_NAME = "ftp_admin";

    /**
     * Display server name
     */
    String DISPLAY_NAME = "Ftp";

    /**
     * Remote admin login
     */
    String login(final String id, final String password) throws Exception;

    /**
     * Remote admin logout
     */
    boolean logout(final String sessId) throws RemoteException;

    /**
     * Get configuration interface
     */
    FtpConfigInterface getConfigInterface(final String sessId) throws RemoteException;

}
