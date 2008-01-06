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

import org.apache.ftpserver.ServerDataConnectionFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpReply;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.interfaces.FtpIoSession;
import org.apache.ftpserver.interfaces.FtpServerContext;
import org.apache.ftpserver.util.FtpReplyUtil;

/**
 * Data channel protection level.
 */
public 
class PROT extends AbstractCommand {

    /**
     * Execute command.
     */
    public void execute(FtpIoSession session,
                        FtpServerContext context,
                        FtpRequest request) throws IOException, FtpException {
    
        // reset state variables
        session.resetState();
        
        // check argument
        String arg = request.getArgument();
        if(arg == null) {
            session.write(FtpReplyUtil.translate(session, request, context, FtpReply.REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS, "PROT", null));
            return;
        }
        
        // check argument
        arg = arg.toUpperCase();
        ServerDataConnectionFactory dcon = session.getDataConnection();
        if(arg.equals("C")) {
            dcon.setSecure(false);
            session.write(FtpReplyUtil.translate(session, request, context, FtpReply.REPLY_200_COMMAND_OKAY, "PROT", null));
        }
        else if(arg.equals("P")) {
            if(session.getListener().getDataConnectionConfig().getSSL() == null) {
                session.write(FtpReplyUtil.translate(session, request, context, 431, "PROT", null));
            }
            else {
                dcon.setSecure(true);
                session.write(FtpReplyUtil.translate(session, request, context, FtpReply.REPLY_200_COMMAND_OKAY, "PROT", null));
            }
        }
        else {
            session.write(FtpReplyUtil.translate(session, request, context, FtpReply.REPLY_504_COMMAND_NOT_IMPLEMENTED_FOR_THAT_PARAMETER, "PROT", null));
        }
    }
    
}
