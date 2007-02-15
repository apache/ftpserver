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
import org.apache.ftpserver.listener.Connection;
import org.apache.ftpserver.util.FtpReplyUtil;

/**
 * <code>SYST &lt;CRLF&gt;</code><br> 
 *
 * This command is used to find out the type of operating
 * system at the server.
 */
public 
class SYST extends AbstractCommand {
    
    
    /**
     * Execute command
     */
    public void execute(Connection connection,
                        FtpRequest request,
                        FtpSessionImpl session, 
                        FtpReplyOutput out) throws IOException {
        
        // reset state variables
        session.resetState();
        
        // get server system info 
        String systemName = System.getProperty("os.name");
        if(systemName == null) {
            systemName = "UNKNOWN";
        }
        else {
            systemName = systemName.toUpperCase();
            systemName = systemName.replace(' ', '-');
        }
        // print server system info
        out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_215_NAME_SYSTEM_TYPE, "SYST", systemName));
    }

}
