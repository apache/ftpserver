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
import java.net.Socket;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocket;

import org.apache.commons.logging.Log;
import org.apache.ftpserver.FtpRequestImpl;
import org.apache.ftpserver.FtpWriter;
import org.apache.ftpserver.RequestHandler;
import org.apache.ftpserver.ftplet.Authentication;
import org.apache.ftpserver.ftplet.FileSystemManager;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.Ftplet;
import org.apache.ftpserver.ftplet.FtpletEnum;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.interfaces.Command;
import org.apache.ftpserver.interfaces.ConnectionManager;
import org.apache.ftpserver.interfaces.FtpServerContext;
import org.apache.ftpserver.interfaces.ServerFtpStatistics;
import org.apache.ftpserver.usermanager.AnonymousAuthentication;
import org.apache.ftpserver.usermanager.UserMetadata;
import org.apache.ftpserver.usermanager.UsernamePasswordAuthentication;

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
class PASS implements Command {
    
    /**
     * Execute command.
     */
    public void execute(RequestHandler handler, 
                        FtpRequestImpl request, 
                        FtpWriter out) throws IOException, FtpException {
    
        boolean success = false;
        FtpServerContext serverContext = handler.getServerContext();
        Log log = serverContext.getLogFactory().getInstance(getClass());
        ConnectionManager conManager = serverContext.getConnectionManager();
        ServerFtpStatistics stat = (ServerFtpStatistics)serverContext.getFtpStatistics();
        try {
            
            // reset state variables
            request.resetState();
            
            // argument check
            String password = request.getArgument();
            if(password == null) {
                out.send(501, "PASS", null);
                return; 
            }
            
            // check user name
            String userName = request.getUserArgument();
            User user = request.getUser();
            if(userName == null && user == null) {
                out.send(503, "PASS", null);
                return;
            }
            
            // already logged-in
            if(request.isLoggedIn()) {
                out.send(202, "PASS", null);
                success = true;
                return;
            }
            
            // anonymous login limit check
            boolean anonymous = userName.equals("anonymous");
            int currAnonLogin = stat.getCurrentAnonymousLoginNumber();
            int maxAnonLogin = conManager.getMaxAnonymousLogins();
            if( anonymous && (currAnonLogin >= maxAnonLogin) ) {
                out.send(421, "PASS.anonymous", null);
                return;
            }
            
            // login limit check
            int currLogin = stat.getCurrentLoginNumber();
            int maxLogin = conManager.getMaxLogins();
            if(maxLogin != 0 && currLogin >= maxLogin) {
                out.send(421, "PASS.login", null);
                return;
            }
            
            // authenticate user
            UserManager userManager = serverContext.getUserManager();
            user = null;
            try {
                UserMetadata userMetadata = new UserMetadata();
                Socket controlSocket = handler.getControlSocket();
                userMetadata.setInetAddress(controlSocket.getInetAddress());
                
                if(controlSocket instanceof SSLSocket) {
                    SSLSocket sslControlSocket = (SSLSocket) controlSocket;
                    
                    try {
                        userMetadata.setCertificateChain(sslControlSocket.getSession().getPeerCertificates());
                    } catch(SSLPeerUnverifiedException e) {
                        // ignore
                    }
                }
                
                Authentication auth;
                if(anonymous) {
                    auth = new AnonymousAuthentication(userMetadata);
                }
                else {
                    auth = new UsernamePasswordAuthentication(userName, password, userMetadata);
                }
                user = userManager.authenticate(auth);
                success = true;
            }
            catch(Exception ex) {
                success = false;
                log.warn("PASS.execute()", ex);
            }

            // call Ftplet.onLogin() method
            Ftplet ftpletContainer = serverContext.getFtpletContainer();
            if(ftpletContainer != null) {
                FtpletEnum ftpletRet;
                try{
                    ftpletRet = ftpletContainer.onLogin(request, out);
                } catch(Exception e) {
                    ftpletRet = FtpletEnum.RET_DISCONNECT;
                }
                if(ftpletRet == FtpletEnum.RET_DISCONNECT) {
                    serverContext.getConnectionManager().closeConnection(handler);
                    return;
                } else if(ftpletRet == FtpletEnum.RET_SKIP) {
                    success = false;
                }
            }
            
            if(success) {
                request.setUser(user);
                request.setUserArgument(null);
                request.setMaxIdleTime(user.getMaxIdleTime());
            } else {
                log.warn("Login failure - " + userName);
                out.send(530, "PASS", userName);
                stat.setLoginFail(handler);
                return;
            }
            
            // update different objects
            FileSystemManager fmanager = serverContext.getFileSystemManager(); 
            FileSystemView fsview = fmanager.createFileSystemView(user);
            request.setLogin(fsview);
            stat.setLogin(handler);

            // everything is fine - send login ok message
            out.send(230, "PASS", userName);
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
                request.reinitialize();
            }
        }
    }
}
