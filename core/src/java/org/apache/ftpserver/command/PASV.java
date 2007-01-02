/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */  

package org.apache.ftpserver.command;

import java.io.IOException;
import java.net.InetAddress;

import org.apache.ftpserver.FtpDataConnection;
import org.apache.ftpserver.FtpSessionImpl;
import org.apache.ftpserver.FtpWriter;
import org.apache.ftpserver.RequestHandler;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpRequest;

/**
 * <code>PASV &lt;CRLF&gt;</code><br>
 *
 * This command requests the server-DTP to "listen" on a data
 * port (which is not its default data port) and to wait for a
 * connection rather than initiate one upon receipt of a
 * transfer command.  The response to this command includes the
 * host and port address this server is listening on.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class PASV extends AbstractCommand {

    /**
     * Execute command
     */
    public void execute(RequestHandler handler, 
                        FtpRequest request,
                        FtpSessionImpl session, 
                        FtpWriter out) throws IOException, FtpException {
        
        // reset state variables
        session.resetState();
        
        // set data connection
        FtpDataConnection dataCon = session.getFtpDataConnection();
        if (!dataCon.setPasvCommand()) {
            out.send(425, "PASV", null);
            return;   
        }
        
        // get connection info
        InetAddress servAddr = dataCon.getInetAddress();
        int servPort = dataCon.getPort();
        
        // send connection info to client
        String addrStr = servAddr.getHostAddress().replace( '.', ',' ) + ',' + (servPort>>8) + ',' + (servPort&0xFF);
        out.send(227, "PASV", addrStr);
    }
}
