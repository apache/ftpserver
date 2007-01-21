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
import java.util.Iterator;
import java.util.List;

import org.apache.ftpserver.DefaultFtpReply;
import org.apache.ftpserver.FtpSessionImpl;
import org.apache.ftpserver.FtpWriter;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.ftplet.FtpReply;
import org.apache.ftpserver.ftplet.FtpSession;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.Connection;
import org.apache.ftpserver.util.DateUtils;
import org.apache.ftpserver.util.FtpReplyUtil;
import org.apache.ftpserver.util.StringUtils;


/**
 * Sends the list of all the connected users.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class SITE_WHO extends AbstractCommand {
    
    /**
     * Execute command.
     */
    public void execute(Connection connection,
                        FtpRequest request,
                        FtpSessionImpl session, 
                        FtpWriter out) throws IOException, FtpException {
        
        // reset state variables
        session.resetState();
        
        // only administrator can execute this
        UserManager userManager = connection.getServerContext().getUserManager(); 
        boolean isAdmin = userManager.isAdmin(session.getUser().getName());
        if(!isAdmin) {
            out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_530_NOT_LOGGED_IN, "SITE", null));
            return;
        }
        
        // print all the connected user information
        StringBuffer sb = new StringBuffer();
        List allCons = connection.getServerContext().getConnectionManager().getAllConnections();
        
        sb.append('\n');
        for(Iterator conIt = allCons.iterator(); conIt.hasNext(); ) {
            Connection tmpCon = (Connection)conIt.next();
            FtpSession tmpReq = tmpCon.getSession();
            if(!tmpReq.isLoggedIn()) {
                continue;
            }
            
            User tmpUsr = tmpReq.getUser();
            sb.append( StringUtils.pad(tmpUsr.getName(), ' ', true, 16) );
            sb.append( StringUtils.pad(tmpReq.getClientAddress().getHostAddress(), ' ', true, 16) );
            sb.append( StringUtils.pad(DateUtils.getISO8601Date(tmpReq.getLoginTime().getTime()), ' ', true, 20) );
            sb.append( StringUtils.pad(DateUtils.getISO8601Date(tmpReq.getLastAccessTime().getTime()), ' ', true, 20) );
            sb.append('\n');
        }
        sb.append('\n');
        out.write(new DefaultFtpReply(FtpReply.REPLY_200_COMMAND_OKAY, sb.toString()));
    }

}
