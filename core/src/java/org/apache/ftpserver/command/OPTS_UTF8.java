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
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpReplyOutput;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.ftplet.FtpReply;
import org.apache.ftpserver.listener.Connection;
import org.apache.ftpserver.util.FtpReplyUtil;


/**
 * Client-Server encoding negotiation.
 * Force server from default encoding to UTF-8 and back.
 * Note that the servers default encoding is UTF-8.
 * So this command has no effect.
 */
public 
class OPTS_UTF8 extends AbstractCommand {
    
    /**
     * Execute command.
     */
    public void execute(Connection connection,
                        FtpRequest request,
                        FtpSessionImpl session, 
                        FtpReplyOutput out) throws IOException, FtpException {
        
        // reset state
        session.resetState();
        
        // send default message
        out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_200_COMMAND_OKAY, "OPTS.UTF8", null));
    }
}
