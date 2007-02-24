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
import org.apache.ftpserver.ftplet.FileObject;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpReply;
import org.apache.ftpserver.ftplet.FtpReplyOutput;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.ftplet.Ftplet;
import org.apache.ftpserver.ftplet.FtpletEnum;
import org.apache.ftpserver.interfaces.FtpServerContext;
import org.apache.ftpserver.listener.Connection;
import org.apache.ftpserver.util.FtpReplyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <code>RNTO &lt;SP&gt; &lt;pathname&gt; &lt;CRLF&gt;</code><br>
 *
 * This command specifies the new pathname of the file
 * specified in the immediately preceding "rename from"
 * command.  Together the two commands cause a file to be
 * renamed.
 */
public 
class RNTO extends AbstractCommand {
    
    private static final Logger LOG = LoggerFactory.getLogger(RNTO.class);

    /**
     * Execute command.
     */
    public void execute(Connection connection, 
                        FtpRequest request,
                        FtpSessionImpl session, 
                        FtpReplyOutput out) throws IOException, FtpException {
        try {
            
            // argument check
            String toFileStr = request.getArgument();
            if(toFileStr == null) {
                out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS, "RNTO", null));
                return;  
            }
            
            // call Ftplet.onRenameStart() method
            FtpServerContext serverContext = connection.getServerContext();
            Ftplet ftpletContainer = serverContext.getFtpletContainer();
            FtpletEnum ftpletRet;
            try {
                ftpletRet = ftpletContainer.onRenameStart(session, request, out);
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
            
            // get the "rename from" file object
            FileObject frFile = session.getRenameFrom();
            if( frFile == null ) {
                out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_503_BAD_SEQUENCE_OF_COMMANDS, "RNTO", null));
                return;
            }
            
            // get target file
            FileObject toFile = null;
            try {
                toFile = session.getFileSystemView().getFileObject(toFileStr);
            }
            catch(Exception ex) {
                LOG.debug("Exception getting file object", ex);
            }
            if(toFile == null) {
                out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_553_REQUESTED_ACTION_NOT_TAKEN_FILE_NAME_NOT_ALLOWED, "RNTO.invalid", null));
                return;
            }
            toFileStr = toFile.getFullName();
            
            // check permission
            if( !toFile.hasWritePermission() ) {
                out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_553_REQUESTED_ACTION_NOT_TAKEN_FILE_NAME_NOT_ALLOWED, "RNTO.permission", null));
                return;
            }
            
            // check file existance
            if( !frFile.doesExist() ) {
                out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_553_REQUESTED_ACTION_NOT_TAKEN_FILE_NAME_NOT_ALLOWED, "RNTO.missing", null));
                return;
            }
            
            // now rename
            if( frFile.move(toFile) ) { 
                out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_250_REQUESTED_FILE_ACTION_OKAY, "RNTO", toFileStr));

                LOG.info("File rename (" + session.getUser().getName() + ") " 
                                         + frFile.getFullName() + " -> " + toFile.getFullName());
                
                // call Ftplet.onRenameEnd() method
                try {
                    ftpletRet = ftpletContainer.onRenameEnd(session, request, out);
                } catch(Exception e) {
                    LOG.debug("Ftplet container threw exception", e);
                    ftpletRet = FtpletEnum.RET_DISCONNECT;
                }
                if(ftpletRet == FtpletEnum.RET_DISCONNECT) {
                    serverContext.getConnectionManager().closeConnection(connection);
                    return;
                }
            }
            else {
                out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_553_REQUESTED_ACTION_NOT_TAKEN_FILE_NAME_NOT_ALLOWED, "RNTO", toFileStr));
            }
        
        }
        finally {
            session.resetState(); 
        }
    } 

}
