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

import java.awt.Component;

import org.apache.ftpserver.FtpUserImpl;
import org.apache.ftpserver.remote.interfaces.RemoteHandlerInterface;
import org.apache.ftpserver.remote.interfaces.FtpConfigInterface;
import org.apache.ftpserver.remote.interfaces.FtpStatisticsInterface;
import org.apache.ftpserver.remote.interfaces.ConnectionServiceInterface;
import org.apache.ftpserver.remote.interfaces.IpRestrictorInterface;
import org.apache.ftpserver.remote.interfaces.UserManagerInterface;


/**
 * GUI common handler methods.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public interface CommonHandler {

    /**
     * Handle exception
     */
    void handleException(Exception ex);

    /**
     * Terminate application
     */
    void terminate();

    /**
     * Get admin session
     */
    String getSessionId();

    /**
     * Get ftp server interface
     */
    RemoteHandlerInterface getRemoteHandler();

    /**
     * Get config interface
     */
    FtpConfigInterface getConfig();

    /**
     * Get statistics interface
     */
    FtpStatisticsInterface getStatistics();

    /**
     * Get connection service
     */
    ConnectionServiceInterface getConnectionService();

    /**
     * Get IP restrictor
     */
    IpRestrictorInterface getIpRestrictor();

    /**
     * Get user manager
     */
    UserManagerInterface getUserManager();

    /**
     * Get user object from the session id
     */
    FtpUserImpl getUser(String sessionId);

    /**
     * Get top frame
     */
    Component getTopFrame();
}
