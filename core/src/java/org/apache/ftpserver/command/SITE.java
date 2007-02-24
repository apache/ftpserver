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

import org.apache.ftpserver.FtpSessionImpl;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpReply;
import org.apache.ftpserver.ftplet.FtpReplyOutput;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.ftplet.Ftplet;
import org.apache.ftpserver.ftplet.FtpletEnum;
import org.apache.ftpserver.interfaces.Command;
import org.apache.ftpserver.interfaces.FtpServerContext;
import org.apache.ftpserver.listener.Connection;
import org.apache.ftpserver.util.FtpReplyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Handle SITE command.
 */
public 
class SITE extends AbstractCommand {

    private static final Logger LOG = LoggerFactory.getLogger(SITE.class);
    
    private static final HashMap COMMAND_MAP = new HashMap(16);
    
    
    /**
     * Execute command.
     */
    public void execute(Connection connection,
                        FtpRequest request,
                        FtpSessionImpl session, 
                        FtpReplyOutput out) throws IOException, FtpException {
        
        // call Ftplet.onSite method
        FtpServerContext serverContext = connection.getServerContext();
        Ftplet ftpletContainer = serverContext.getFtpletContainer();
        FtpletEnum ftpletRet;
        try {
            ftpletRet = ftpletContainer.onSite(session, request, out);
        } catch(Exception e) {
            LOG.debug("Ftplet container threw exception", e);
            ftpletRet = FtpletEnum.RET_DISCONNECT;
        }
        if(ftpletRet == FtpletEnum.RET_SKIP) {
            return;
        }
        else if(ftpletRet == FtpletEnum.RET_DISCONNECT) {
            serverContext.getConnectionManager().closeConnection(connection);
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
            session.resetState();
            out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_200_COMMAND_OKAY, "SITE", null));
            return;
        }
        
        // call appropriate command method
        String siteRequest = "SITE_" + argument; 
        Command command = (Command)COMMAND_MAP.get( siteRequest );
        try {
            if(command != null) {
                command.execute(connection, request, session, out);
            }
            else {
                session.resetState();
                out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_502_COMMAND_NOT_IMPLEMENTED, "SITE", argument));
            }
        }
        catch(Exception ex) {
            LOG.warn("SITE.execute()", ex);
            session.resetState();
            out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_500_SYNTAX_ERROR_COMMAND_UNRECOGNIZED, "SITE", null));
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
