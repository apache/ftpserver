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

import org.apache.ftpserver.FtpSessionImpl;
import org.apache.ftpserver.ftplet.FtpReply;
import org.apache.ftpserver.ftplet.FtpReplyOutput;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.interfaces.MessageResource;
import org.apache.ftpserver.listener.Connection;
import org.apache.ftpserver.util.FtpReplyUtil;

/**
 * <code>HELP [&lt;SP&gt; <string>] &lt;CRLF&gt;</code><br>
 *
 * This command shall cause the server to send helpful
 * information regarding its implementation status over the
 * control connection to the user.  The command may take an
 * argument (e.g., any command name) and return more specific
 * information as a response.
 */
public 
class HELP extends AbstractCommand {
    
    /**
     * Execute command.
     */
    public void execute(Connection connection,
                        FtpRequest request, 
                        FtpSessionImpl session, 
                        FtpReplyOutput out) throws IOException {
        
        // reset state variables
        session.resetState();
        
        // print global help
        if(!request.hasArgument()) {
            out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_214_HELP_MESSAGE, null, null));
            return;
        }
        
        // print command specific help if available
        String ftpCmd = request.getArgument().toUpperCase();
        MessageResource resource = connection.getServerContext().getMessageResource();
        if(resource.getMessage(FtpReply.REPLY_214_HELP_MESSAGE, ftpCmd, session.getLanguage()) == null) {
            ftpCmd = null;
        }
        out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_214_HELP_MESSAGE, ftpCmd, null));
    }
}
