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

package org.apache.ftpserver;

import org.apache.avalon.cornerstone.services.connection.ConnectionHandler;
import org.apache.avalon.cornerstone.services.connection.ConnectionHandlerFactory;
import org.apache.avalon.cornerstone.services.connection.ConnectionManager;
import org.apache.avalon.cornerstone.services.sockets.SocketManager;
import org.apache.ftpserver.interfaces.FtpServerInterface;

import java.net.ServerSocket;


/**
 * Ftp server starting point. Avalon framework will load this
 * from the jar file. This is also the starting point of remote
 * admin.
 *
 * @phoenix:block
 * @phoenix:service name="org.apache.ftpserver.interfaces.FtpServerInterface"
 *
 * @author  Rana Bhattacharyya <rana_b@yahoo.com>
 * @author  Paul Hammant
 * @version 1.0
 */
public class FtpServerImpl implements FtpServerInterface, ConnectionHandlerFactory {

    protected ServerSocket mServerSocket;
    protected SocketManager mSocManager;
    protected ConnectionManager mConManager;
    protected AbstractFtpConfig mConfig;

    /**
     * Construct an appropriate <code>ConnectionHandler</code>
     * to handle a new connection.
     *
     * @return the new ConnectionHandler
     * @exception Exception if an error occurs
     */
    public ConnectionHandler createConnectionHandler() throws Exception {
        BaseFtpConnection conHandle = new FtpConnection(mConfig);
        return conHandle;
    }

    /**
     * Release a previously created ConnectionHandler.
     * e.g. for spooling.
     */
    public void releaseConnectionHandler(ConnectionHandler connectionHandler) {
    }
}

