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
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.interfaces.FtpServerContext;
import org.apache.ftpserver.interfaces.ServerFtpStatistics;
import org.apache.ftpserver.listener.Connection;
import org.apache.ftpserver.listener.ConnectionManager;
import org.apache.ftpserver.usermanager.BaseUser;
import org.apache.ftpserver.usermanager.ConcurrentLoginRequest;
import org.apache.ftpserver.util.FtpReplyUtil;

/**
 * <code>USER &lt;SP&gt; &lt;username&gt; &lt;CRLF&gt;</code><br>
 *
 * The argument field is a Telnet string identifying the user.
 * The user identification is that which is required by the
 * server for access to its file system.  This command will
 * normally be the first command transmitted by the user after
 * the control connections are made.
 */
public 
class USER extends AbstractCommand {
    
    /**
     * Execute command.
     */
    public void execute(Connection connection, 
                        FtpRequest request,
                        FtpSessionImpl session, 
                        FtpReplyOutput out) throws IOException, FtpException {
    
        boolean success = false;
        FtpServerContext serverContext = connection.getServerContext();
        ConnectionManager conManager = serverContext.getConnectionManager();
        ServerFtpStatistics stat = (ServerFtpStatistics)serverContext.getFtpStatistics();
        try {
            
            // reset state variables
            session.resetState();
            
            // argument check
            String userName = request.getArgument();
            if(userName == null) {
                out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS, "USER", null));
                return;  
            }
            
            // already logged-in
            BaseUser user = (BaseUser)session.getUser();
            if(session.isLoggedIn()) {
                if( userName.equals(user.getName()) ) {
                    out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_230_USER_LOGGED_IN, "USER", null));
                    success = true;
                }
                else {
                    out.write(FtpReplyUtil.translate(session, 530, "USER.invalid", null));
                }
                return;
            }
            
            // anonymous login is not enabled
            boolean anonymous = userName.equals("anonymous");
            if( anonymous && (!conManager.isAnonymousLoginEnabled()) ) {
                out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_530_NOT_LOGGED_IN, "USER.anonymous", null));
                return;
            }
            
            // anonymous login limit check
            int currAnonLogin = stat.getCurrentAnonymousLoginNumber();
            int maxAnonLogin = conManager.getMaxAnonymousLogins();
            if( anonymous && (currAnonLogin >= maxAnonLogin) ) {
                out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_421_SERVICE_NOT_AVAILABLE_CLOSING_CONTROL_CONNECTION, "USER.anonymous", null));
                return;
            }
            
            // login limit check
            int currLogin = stat.getCurrentLoginNumber();
            int maxLogin = conManager.getMaxLogins();
            if(maxLogin != 0 && currLogin >= maxLogin) {
                out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_421_SERVICE_NOT_AVAILABLE_CLOSING_CONTROL_CONNECTION, "USER.login", null));
                return;
            }
            
            User configUser = connection.getServerContext().getUserManager().getUserByName(userName);
            if(configUser != null){
                //user login limit check
                
                ConcurrentLoginRequest loginRequest = new  ConcurrentLoginRequest(
                        stat.getCurrentUserLoginNumber(configUser) + 1,
                        stat.getCurrentUserLoginNumber(configUser, session.getClientAddress()) + 1);
                
                if(configUser.authorize(loginRequest) == null) {
                    out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_421_SERVICE_NOT_AVAILABLE_CLOSING_CONTROL_CONNECTION, "USER.login", null));
                    return;
                }
            }
            
            // finally set the user name
            success = true;
            session.setUserArgument(userName);
            if(anonymous) {
                out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_331_USER_NAME_OKAY_NEED_PASSWORD, "USER.anonymous", userName));
            }
            else {
                out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_331_USER_NAME_OKAY_NEED_PASSWORD, "USER", userName));
            }
        }
        finally {

            // if not ok - close connection
            if(!success) {
                conManager.closeConnection(connection);
            }
        }
    }
}
