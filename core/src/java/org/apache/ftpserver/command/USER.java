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

import org.apache.ftpserver.FtpRequestImpl;
import org.apache.ftpserver.FtpWriter;
import org.apache.ftpserver.RequestHandler;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.interfaces.Command;
import org.apache.ftpserver.interfaces.ConnectionManager;
import org.apache.ftpserver.interfaces.FtpServerContext;
import org.apache.ftpserver.interfaces.ServerFtpStatistics;
import org.apache.ftpserver.usermanager.BaseUser;

/**
 * <code>USER &lt;SP&gt; &lt;username&gt; &lt;CRLF&gt;</code><br>
 *
 * The argument field is a Telnet string identifying the user.
 * The user identification is that which is required by the
 * server for access to its file system.  This command will
 * normally be the first command transmitted by the user after
 * the control connections are made.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class USER implements Command {
    
    /**
     * Execute command.
     */
    public void execute(RequestHandler handler, 
                        FtpRequestImpl request, 
                        FtpWriter out) throws IOException, FtpException {
    
        boolean success = false;
        FtpServerContext fconfig = handler.getConfig();
        ConnectionManager conManager = fconfig.getConnectionManager();
        ServerFtpStatistics stat = (ServerFtpStatistics)fconfig.getFtpStatistics();
        try {
            
            // reset state variables
            request.resetState();
            
            // argument check
            String userName = request.getArgument();
            if(userName == null) {
                out.send(501, "USER", null);
                return;  
            }
            
            // already logged-in
            BaseUser user = (BaseUser)request.getUser();
            if(request.isLoggedIn()) {
                if( userName.equals(user.getName()) ) {
                    out.send(230, "USER", null);
                    success = true;
                }
                else {
                    out.send(530, "USER.invalid", null);
                }
                return;
            }
            
            // anonymous login is not enabled
            boolean anonymous = userName.equals("anonymous");
            if( anonymous && (!conManager.isAnonymousLoginEnabled()) ) {
                out.send(530, "USER.anonymous", null);
                return;
            }
            
            // anonymous login limit check
            int currAnonLogin = stat.getCurrentAnonymousLoginNumber();
            int maxAnonLogin = conManager.getMaxAnonymousLogins();
            if( anonymous && (currAnonLogin >= maxAnonLogin) ) {
                out.send(421, "USER.anonymous", null);
                return;
            }
            
            // login limit check
            int currLogin = stat.getCurrentLoginNumber();
            int maxLogin = conManager.getMaxLogins();
            if(maxLogin != 0 && currLogin >= maxLogin) {
                out.send(421, "USER.login", null);
                return;
            }
            
            User configUser = handler.getConfig().getUserManager().getUserByName(userName);
            if(configUser != null){
              //user login limit check
              int maxUserLoginNumber = configUser.getMaxLoginNumber();
              if(maxUserLoginNumber > 0){// has a limit on user login
                int currUserLogin = stat.getCurrentUserLoginNumber(configUser);
                if(currUserLogin >= maxUserLoginNumber){
                  out.send(421, "USER.login", null);
                  return;
                }
              }
              
              //user login from same IP check
              int maxUserLoginPerIP = configUser.getMaxLoginPerIP();
              if(maxUserLoginPerIP > 0){// has a limit on user login per ip
                int currUserLoginPerIP = stat.getCurrentUserLoginNumber(configUser, request.getRemoteAddress());
                if(currUserLoginPerIP >= maxUserLoginPerIP){
                  out.send(421, "USER.login", null);
                  return;
                }
              }
            }
            
            // finally set the user name
            success = true;
            request.setUserArgument(userName);
            if(anonymous) {
                out.send(331, "USER.anonymous", userName);
            }
            else {
                out.send(331, "USER", userName);
            }
        }
        finally {

            // if not ok - close connection
            if(!success) {
                conManager.closeConnection(handler);
            }
        }
    }
}
