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
import java.util.Iterator;
import java.util.List;

import org.apache.ftpserver.Command;
import org.apache.ftpserver.FtpRequestImpl;
import org.apache.ftpserver.FtpWriter;
import org.apache.ftpserver.RequestHandler;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.interfaces.IConnection;
import org.apache.ftpserver.util.DateUtils;
import org.apache.ftpserver.util.StringUtils;


/**
 * Sends the list of all the connected users.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class SITE_WHO implements Command {
    
    /**
     * Execute command.
     */
    public void execute(RequestHandler handler,
                        FtpRequestImpl request, 
                        FtpWriter out) throws IOException, FtpException {
        
        // reset state variables
        request.resetState();
        
        // only administrator can execute this
        UserManager userManager = handler.getConfig().getUserManager(); 
        boolean isAdmin = userManager.getAdminName().equals(request.getUser().getName());
        if(!isAdmin) {
            out.send(530, "SITE", null);
            return;
        }
        
        // print all the connected user information
        StringBuffer sb = new StringBuffer();
        List allCons = handler.getConfig().getConnectionManager().getAllConnections();
        
        sb.append('\n');
        for(Iterator conIt = allCons.iterator(); conIt.hasNext(); ) {
            IConnection tmpCon = (IConnection)conIt.next();
            FtpRequest tmpReq = tmpCon.getRequest();
            if(!tmpReq.isLoggedIn()) {
                continue;
            }
            
            User tmpUsr = tmpReq.getUser();
            sb.append( StringUtils.pad(tmpUsr.getName(), ' ', true, 16) );
            sb.append( StringUtils.pad(tmpReq.getRemoteAddress().getHostAddress(), ' ', true, 16) );
            sb.append( StringUtils.pad(DateUtils.getISO8601Date(tmpReq.getLoginTime().getTime()), ' ', true, 20) );
            sb.append( StringUtils.pad(DateUtils.getISO8601Date(tmpReq.getLastAccessTime().getTime()), ' ', true, 20) );
            sb.append('\n');
        }
        sb.append('\n');
        out.write(200, sb.toString());
    }

}
