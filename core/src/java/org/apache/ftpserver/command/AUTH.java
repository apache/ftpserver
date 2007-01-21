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

import org.apache.commons.logging.Log;
import org.apache.ftpserver.FtpSessionImpl;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpReplyOutput;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.ftplet.FtpReply;
import org.apache.ftpserver.interfaces.FtpServerContext;
import org.apache.ftpserver.listener.Connection;
import org.apache.ftpserver.util.FtpReplyUtil;

/**
 * This server supports explicit SSL support.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class AUTH extends AbstractCommand {
    

    /**
     * Execute command
     */
    public void execute(Connection connection,
                        FtpRequest request, 
                        FtpSessionImpl session, 
                        FtpReplyOutput out) throws IOException, FtpException {
        
        // reset state variables
        session.resetState();
        
        // argument check
        if(!request.hasArgument()) {
            out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS, "AUTH", null));
            return;  
        }
        
        // check SSL configuration
        FtpServerContext serverContext = connection.getServerContext();
        Log log = serverContext.getLogFactory().getInstance(getClass());
        if(serverContext.getSocketFactory().getSSL() == null) {
            out.write(FtpReplyUtil.translate(session, 431, "AUTH", null));
            return;
        }
        
        // check parameter
        String authType = request.getArgument().toUpperCase();
        if(authType.equals("SSL")) {
            try {
                connection.beforeSecureControlChannel("SSL");
                out.write(FtpReplyUtil.translate(session, 234, "AUTH.SSL", null));
                connection.afterSecureControlChannel("SSL");
            } catch(FtpException ex) {
                throw ex;
            } catch(Exception ex) {
                log.warn("AUTH.execute()", ex);
                throw new FtpException("AUTH.execute()", ex);
            }
        }
        else if(authType.equals("TLS")) {
            try {
                connection.beforeSecureControlChannel("TLS");
                out.write(FtpReplyUtil.translate(session, 234, "AUTH.TLS", null));
                connection.afterSecureControlChannel("TLS");
            } catch(FtpException ex) {
                throw ex;
            } catch(Exception ex) {
                log.warn("AUTH.execute()", ex);
                throw new FtpException("AUTH.execute()", ex);
            }
        }
        else {
            out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_502_COMMAND_NOT_IMPLEMENTED, "AUTH", null));
        }
    }
}
