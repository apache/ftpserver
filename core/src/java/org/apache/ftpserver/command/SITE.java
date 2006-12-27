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
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.ftpserver.FtpRequestImpl;
import org.apache.ftpserver.FtpWriter;
import org.apache.ftpserver.RequestHandler;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.Ftplet;
import org.apache.ftpserver.ftplet.FtpletEnum;
import org.apache.ftpserver.interfaces.Command;
import org.apache.ftpserver.interfaces.FtpServerContext;


/**
 * Handle SITE command.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class SITE extends AbstractCommand {

    private static final HashMap COMMAND_MAP = new HashMap(16);
    
    
    /**
     * Execute command.
     */
    public void execute(RequestHandler handler,
                        FtpRequestImpl request, 
                        FtpWriter out) throws IOException, FtpException {
        
        // call Ftplet.onSite method
        FtpServerContext serverContext = handler.getServerContext();
        Ftplet ftpletContainer = serverContext.getFtpletContainer();
        FtpletEnum ftpletRet;
        try {
            ftpletRet = ftpletContainer.onSite(request, out);
        } catch(Exception e) {
            ftpletRet = FtpletEnum.RET_DISCONNECT;
        }
        if(ftpletRet == FtpletEnum.RET_SKIP) {
            return;
        }
        else if(ftpletRet == FtpletEnum.RET_DISCONNECT) {
            serverContext.getConnectionManager().closeConnection(handler);
            return;
        }
        
        // get request name
        String argument = request.getArgument();
        if(argument != null) {
            int spaceIndex = argument.indexOf(' ');
            if(spaceIndex != -1) {
                argument = argument.substring(0, spaceIndex);
            }
            argument = argument.toUpperCase();
        }
        
        // no params
        if(argument == null) {
            request.resetState();
            out.send(200, "SITE", null);
            return;
        }
        
        // call appropriate command method
        String siteRequest = "SITE_" + argument; 
        Command command = (Command)COMMAND_MAP.get( siteRequest );
        try {
            if(command != null) {
                command.execute(handler, request, out);
            }
            else {
                request.resetState();
                out.send(502, "SITE", argument);
            }
        }
        catch(Exception ex) {
            Log log = serverContext.getLogFactory().getInstance(getClass());
            log.warn("SITE.execute()", ex);
            request.resetState();
            out.send(500, "SITE", null);
        }
    
    }
    
    // initialize all the SITE command handlers
    static {
        COMMAND_MAP.put("SITE_DESCUSER", new org.apache.ftpserver.command.SITE_DESCUSER());
        COMMAND_MAP.put("SITE_HELP",     new org.apache.ftpserver.command.SITE_HELP());
        COMMAND_MAP.put("SITE_STAT",     new org.apache.ftpserver.command.SITE_STAT());
        COMMAND_MAP.put("SITE_WHO",      new org.apache.ftpserver.command.SITE_WHO());
        COMMAND_MAP.put("SITE_ZONE",     new org.apache.ftpserver.command.SITE_ZONE());
    }
}
