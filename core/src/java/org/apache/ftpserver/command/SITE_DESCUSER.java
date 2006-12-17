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
import org.apache.ftpserver.ftplet.FtpletContext;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.interfaces.Command;

/**
 * This SITE command returns the specified user information.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class SITE_DESCUSER implements Command {

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
        boolean isAdmin = userManager.isAdmin(request.getUser().getName());
        if(!isAdmin) {
            out.send(530, "SITE", null);
            return;
        }
        
        // get the user name
        String argument = request.getArgument();
        int spIndex = argument.indexOf(' ');
        if(spIndex == -1) {
            out.send(503, "SITE.DESCUSER", null);
            return;
        }
        String userName = argument.substring(spIndex + 1);
        
        // check the user existance
        FtpletContext fconfig = handler.getConfig();
        UserManager usrManager = fconfig.getUserManager();
        User user = null;
        try {
            if(usrManager.doesExist(userName)) {
                user = usrManager.getUserByName(userName);
            }
        }
        catch(FtpException ex) {
            user = null;
        }
        if(user == null) {
            out.send(501, "SITE.DESCUSER", userName);
            return;
        }
        
        // send the user information
        StringBuffer sb = new StringBuffer(128);
        sb.append("\n");
        sb.append("uid             : ").append(user.getName()).append("\n");
        sb.append("userpassword    : ********\n");
        sb.append("homedirectory   : ").append(user.getHomeDirectory()).append("\n");
        sb.append("writepermission : ").append(user.getWritePermission()).append("\n");
        sb.append("enableflag      : ").append(user.getEnabled()).append("\n");
        sb.append("idletime        : ").append(user.getMaxIdleTime()).append("\n");
        sb.append("uploadrate      : ").append(user.getMaxUploadRate()).append("\n");
        sb.append("downloadrate    : ").append(user.getMaxDownloadRate()).append("\n");
        sb.append('\n');
        out.write(200, sb.toString());
    }

}
