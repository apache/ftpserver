// $Id$
/*
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ftpserver.command;

import java.io.IOException;

import org.apache.ftpserver.Command;
import org.apache.ftpserver.FtpRequestImpl;
import org.apache.ftpserver.FtpWriter;
import org.apache.ftpserver.RequestHandler;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.interfaces.IConnectionManager;
import org.apache.ftpserver.interfaces.IFtpConfig;
import org.apache.ftpserver.interfaces.IFtpStatistics;
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
    
        boolean bSuccess = false;
        IFtpConfig fconfig = handler.getConfig();
        IConnectionManager conManager = fconfig.getConnectionManager();
        IFtpStatistics stat = (IFtpStatistics)fconfig.getFtpStatistics();
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
                    bSuccess = true;
                }
                else {
                    out.send(530, "USER.user.invalid", null);
                }
                return;
            }
            
            // anonymous login is not enabled
            boolean bAnonymous = userName.equals("anonymous");
            if( bAnonymous && (!conManager.isAnonymousLoginEnabled()) ) {
                out.send(530, "USER.no.anonymous.support", null);
                return;
            }
            
            // anonymous login limit check
            int currAnonLogin = stat.getCurrentAnonymousLoginNumber();
            int maxAnonLogin = conManager.getMaxAnonymousLogins();
            if( bAnonymous && (currAnonLogin >= maxAnonLogin) ) {
                out.send(421, "USER.anonymous.limit", null);
                return;
            }
            
            // login limit check
            int currLogin = stat.getCurrentLoginNumber();
            int maxLogin = conManager.getMaxLogins();
            if(currLogin >= maxLogin) {
                out.send(421, "USER.login.limit", null);
                return;
            }
            
            // finally set the user name
            bSuccess = true;
            user.setName(userName);
            if(bAnonymous) {
                out.send(331, "USER.anonymous", userName);
            }
            else {
                out.send(331, "USER", userName);
            }
        }
        finally {

            // if not ok - close connection
            if(!bSuccess) {
                conManager.closeConnection(handler);
            }
        }
    }
}
