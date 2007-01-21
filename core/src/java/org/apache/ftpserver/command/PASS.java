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
import org.apache.ftpserver.FtpWriter;
import org.apache.ftpserver.ftplet.Authentication;
import org.apache.ftpserver.ftplet.AuthenticationFailedException;
import org.apache.ftpserver.ftplet.FileSystemManager;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.ftplet.FtpReply;
import org.apache.ftpserver.ftplet.Ftplet;
import org.apache.ftpserver.ftplet.FtpletEnum;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.interfaces.FtpServerContext;
import org.apache.ftpserver.interfaces.ServerFtpStatistics;
import org.apache.ftpserver.listener.Connection;
import org.apache.ftpserver.listener.ConnectionManager;
import org.apache.ftpserver.usermanager.AnonymousAuthentication;
import org.apache.ftpserver.usermanager.UserMetadata;
import org.apache.ftpserver.usermanager.UsernamePasswordAuthentication;
import org.apache.ftpserver.util.FtpReplyUtil;

/**
 * <code>PASS &lt;SP&gt; <password> &lt;CRLF&gt;</code><br>
 *
 * The argument field is a Telnet string specifying the user's
 * password.  This command must be immediately preceded by the
 * user name command.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class PASS extends AbstractCommand {
    
    /**
     * Execute command.
     */
    public void execute(Connection connection, 
                        FtpRequest request,
                        FtpSessionImpl session, 
                        FtpWriter out) throws IOException, FtpException {
    
        boolean success = false;
        FtpServerContext serverContext = connection.getServerContext();
        Log log = serverContext.getLogFactory().getInstance(getClass());
        ConnectionManager conManager = serverContext.getConnectionManager();
        ServerFtpStatistics stat = (ServerFtpStatistics)serverContext.getFtpStatistics();
        try {
            
            // reset state variables
            session.resetState();
            
            // argument check
            String password = request.getArgument();
            if(password == null) {
                out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS, "PASS", null));
                return; 
            }
            
            // check user name
            String userName = session.getUserArgument();

            if(userName == null && session.getUser() == null) {
                out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_503_BAD_SEQUENCE_OF_COMMANDS, "PASS", null));
                return;
            }
            
            // already logged-in
            if(session.isLoggedIn()) {
                out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_202_COMMAND_NOT_IMPLEMENTED, "PASS", null));
                success = true;
                return;
            }
            
            // anonymous login limit check
            boolean anonymous = userName.equals("anonymous");
            int currAnonLogin = stat.getCurrentAnonymousLoginNumber();
            int maxAnonLogin = conManager.getMaxAnonymousLogins();
            if( anonymous && (currAnonLogin >= maxAnonLogin) ) {
                out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_421_SERVICE_NOT_AVAILABLE_CLOSING_CONTROL_CONNECTION, "PASS.anonymous", null));
                return;
            }
            
            // login limit check
            int currLogin = stat.getCurrentLoginNumber();
            int maxLogin = conManager.getMaxLogins();
            if(maxLogin != 0 && currLogin >= maxLogin) {
                out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_421_SERVICE_NOT_AVAILABLE_CLOSING_CONTROL_CONNECTION, "PASS.login", null));
                return;
            }
            
            // authenticate user
            UserManager userManager = serverContext.getUserManager();
            User authenticatedUser = null;
            try {
                UserMetadata userMetadata = new UserMetadata();
                userMetadata.setInetAddress(session.getClientAddress());
                userMetadata.setCertificateChain(session.getClientCertificates());
                
                Authentication auth;
                if(anonymous) {
                    auth = new AnonymousAuthentication(userMetadata);
                }
                else {
                    auth = new UsernamePasswordAuthentication(userName, password, userMetadata);
                }
                authenticatedUser = userManager.authenticate(auth);
                success = true;
            } catch(AuthenticationFailedException e) { 
                success = false;
                authenticatedUser = null;
                log.warn("User failed to log in", e);                
            }
            catch(Exception e) {
                success = false;
                authenticatedUser = null;
                log.warn("PASS.execute()", e);
            }

            // set the user so that the Ftplets will be able to verify it
            
            // first save old values so that we can reset them if Ftplets
            // tell us to fail
            User oldUser = session.getUser();
            String oldUserArgument = session.getUserArgument();
            int oldMaxIdleTime = session.getMaxIdleTime();

            if(success) {
                session.setUser(authenticatedUser);
                session.setUserArgument(null);
                session.setMaxIdleTime(authenticatedUser.getMaxIdleTime());
            } else {
                session.setUser(null);
            }
            
            // call Ftplet.onLogin() method
            Ftplet ftpletContainer = serverContext.getFtpletContainer();
            if(ftpletContainer != null) {
                FtpletEnum ftpletRet;
                try{
                    ftpletRet = ftpletContainer.onLogin(session, request, out);
                } catch(Exception e) {
                    log.debug("Ftplet container threw exception", e);
                    ftpletRet = FtpletEnum.RET_DISCONNECT;
                }
                if(ftpletRet == FtpletEnum.RET_DISCONNECT) {
                    serverContext.getConnectionManager().closeConnection(connection);
                    return;
                } else if(ftpletRet == FtpletEnum.RET_SKIP) {
                    success = false;
                }
            }
            
            if(!success) {
                // reset due to failure
                session.setUser(oldUser);
                session.setUserArgument(oldUserArgument);
                session.setMaxIdleTime(oldMaxIdleTime);
                
                log.warn("Login failure - " + userName);
                out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_530_NOT_LOGGED_IN, "PASS", userName));
                stat.setLoginFail(connection);
                return;
            }
            
            // update different objects
            FileSystemManager fmanager = serverContext.getFileSystemManager(); 
            FileSystemView fsview = fmanager.createFileSystemView(authenticatedUser);
            session.setLogin(fsview);
            stat.setLogin(connection);

            // everything is fine - send login ok message
            out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_230_USER_LOGGED_IN, "PASS", userName));
            if(anonymous) {
                log.info("Anonymous login success - " + password);
            }
            else {
                log.info("Login success - " + userName);
            }
            
        }
        finally {
            
            // if login failed - reset user
            if(!success) {
                session.reinitialize();
            }
        }
    }
}
