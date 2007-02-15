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
import org.apache.ftpserver.ftplet.FtpReply;
import org.apache.ftpserver.ftplet.FtpReplyOutput;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.interfaces.MessageResource;
import org.apache.ftpserver.listener.Connection;
import org.apache.ftpserver.util.FtpReplyUtil;

/**
 * A new command "LANG" is added to the FTP command set to allow
 * server-FTP process to determine in which language to present server
 * greetings and the textual part of command responses.
 */
public 
class LANG extends AbstractCommand {

    /**
     * Execute command.
     */
    public void execute(Connection connection, 
                        FtpRequest request, 
                        FtpSessionImpl session, 
                        FtpReplyOutput out) throws IOException, FtpException {
        
        // reset state
        session.resetState();
        
        // default language
        String language = request.getArgument();
        if(language == null) {
            session.setLanguage(null);
            out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_200_COMMAND_OKAY, "LANG", null));
            return;
        }
        
        // check and set language
        language = language.toLowerCase();
        MessageResource msgResource = connection.getServerContext().getMessageResource();
        String[] availableLanguages = msgResource.getAvailableLanguages();
        if(availableLanguages != null) {
            for(int i=0; i<availableLanguages.length; ++i) {
                if(availableLanguages[i].equals(language)) {
                    session.setLanguage(language);
                    out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_200_COMMAND_OKAY, "LANG", null));
                    return;
                }
            }
        }
        
        // not found - send error message
        out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_504_COMMAND_NOT_IMPLEMENTED_FOR_THAT_PARAMETER, "LANG", null));
    }
}
