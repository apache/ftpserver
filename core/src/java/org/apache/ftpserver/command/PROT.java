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
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpReply;
import org.apache.ftpserver.ftplet.FtpReplyOutput;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.interfaces.FtpServerContext;
import org.apache.ftpserver.listener.Connection;
import org.apache.ftpserver.util.FtpReplyUtil;

/**
 * Data channel protection level.
 */
public 
class PROT extends AbstractCommand {

    /**
     * Execute command.
     */
    public void execute(Connection connection,
                        FtpRequest request,
                        FtpSessionImpl session, 
                        FtpReplyOutput out) throws IOException, FtpException {
    
        // reset state variables
        session.resetState();
        
        // check argument
        String arg = request.getArgument();
        if(arg == null) {
            out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS, "PROT", null));
            return;
        }
        
        // check argument
        arg = arg.toUpperCase();
        FtpDataConnectionFactory dcon = session.getFtpDataConnection();
        if(arg.equals("C")) {
            dcon.setSecure(false);
            out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_200_COMMAND_OKAY, "PROT", null));
        }
        else if(arg.equals("P")) {
            FtpServerContext serverContext = connection.getServerContext();
            if(serverContext.getDataConnectionConfig().getSSL() == null) {
                out.write(FtpReplyUtil.translate(session, 431, "PROT", null));
            }
            else {
                dcon.setSecure(true);
                out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_200_COMMAND_OKAY, "PROT", null));
            }
        }
        else {
            out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_504_COMMAND_NOT_IMPLEMENTED_FOR_THAT_PARAMETER, "PROT", null));
        }
    }
    
}
