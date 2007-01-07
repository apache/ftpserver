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

import org.apache.ftpserver.FtpDataConnection;
import org.apache.ftpserver.FtpSessionImpl;
import org.apache.ftpserver.FtpWriter;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.interfaces.Connection;
import org.apache.ftpserver.interfaces.FtpServerContext;

/**
 * Data channel protection level.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class PROT extends AbstractCommand {

    /**
     * Execute command.
     */
    public void execute(Connection connection,
                        FtpRequest request,
                        FtpSessionImpl session, 
                        FtpWriter out) throws IOException, FtpException {
    
        // reset state variables
        session.resetState();
        
        // check argument
        String arg = request.getArgument();
        if(arg == null) {
            out.send(501, "PROT", null);
            return;
        }
        
        // check argument
        arg = arg.toUpperCase();
        FtpDataConnection dcon = session.getFtpDataConnection();
        if(arg.equals("C")) {
            dcon.setSecure(false);
            out.send(200, "PROT", null);
        }
        else if(arg.equals("P")) {
            FtpServerContext serverContext = connection.getServerContext();
            if(serverContext.getDataConnectionConfig().getSSL() == null) {
                out.send(431, "PROT", null);
            }
            else {
                dcon.setSecure(true);
                out.send(200, "PROT", null);
            }
        }
        else {
            out.send(504, "PROT", null);
        }
    }
    
}
