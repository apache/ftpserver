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

import org.apache.ftpserver.FtpDataConnectionFactory;
import org.apache.ftpserver.FtpSessionImpl;
import org.apache.ftpserver.FtpWriter;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.ftplet.FtpReply;
import org.apache.ftpserver.listener.Connection;
import org.apache.ftpserver.util.FtpReplyUtil;

/**
 * The EPSV command requests that a server listen on a data port and
 * wait for a connection.  The EPSV command takes an optional argument.
 * The response to this command includes only the TCP port number of the
 * listening connection.  The format of the response, however, is
 * similar to the argument of the EPRT command.  This allows the same
 * parsing routines to be used for both commands.  In addition, the
 * format leaves a place holder for the network protocol and/or network
 * address, which may be needed in the EPSV response in the future.  The
 * response code for entering passive mode using an extended address
 * MUST be 229.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class EPSV extends AbstractCommand {

    /**
     * Execute command.
     */
    public void execute(Connection connection,
                        FtpRequest request,
                        FtpSessionImpl session, 
                        FtpWriter out) throws IOException {
        
        // reset state variables
        session.resetState();
        
        // set data connection
        FtpDataConnectionFactory dataCon = session.getFtpDataConnection();
        if (!dataCon.setPasvCommand()) {
            out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_425_CANT_OPEN_DATA_CONNECTION, "EPSV", null));
            return;   
        }
        
        // get connection info
        int servPort = dataCon.getPort();
        
        // send connection info to client
        String portStr = "|||" + servPort + '|';
        out.write(FtpReplyUtil.translate(session, 229, "EPSV", portStr));
    }
}
