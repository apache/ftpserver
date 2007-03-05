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

import java.io.File;
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
import org.apache.ftpserver.interfaces.ServerFtpStatistics;
import org.apache.ftpserver.listener.Connection;
import org.apache.ftpserver.util.FtpReplyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <code>MKD  &lt;SP&gt; &lt;pathname&gt; &lt;CRLF&gt;</code><br>
 *
 * This command causes the directory specified in the pathname
 * to be created as a directory (if the pathname is absolute)
 * or as a subdirectory of the current working directory (if
 * the pathname is relative).
 */
public 
class MKD extends AbstractCommand {
    
    private final Logger LOG = LoggerFactory.getLogger(MKD.class);

    /**
     * Execute command.
     */
    public void execute(Connection connection,
                        FtpRequest request, 
                        FtpSessionImpl session, 
                        FtpReplyOutput out) throws IOException, FtpException {
        
        // reset state
        session.resetState(); 
        FtpServerContext serverContext = connection.getServerContext();
        
        // argument check
        String fileName = request.getArgument();
        if(fileName == null || fileName.indexOf(File.pathSeparatorChar) > -1) {
            out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS, "MKD", null));
            return;  	
        }
        
        // call Ftplet.onMkdirStart() method
        Ftplet ftpletContainer = serverContext.getFtpletContainer();
        FtpletEnum ftpletRet;
        try{
            ftpletRet = ftpletContainer.onMkdirStart(session, request, out);
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
        
        // get file object
        FileObject file = null;
        try {
            file = session.getFileSystemView().getFileObject(fileName);
        }
        catch(Exception ex) {
            LOG.debug("Exception getting file object", ex);
        }
        if(file == null) {
            out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_550_REQUESTED_ACTION_NOT_TAKEN, "MKD.invalid", fileName));
            return;
        }
        
        // check permission
        fileName = file.getFullName();
        if( !file.hasWritePermission() ) {
            out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_550_REQUESTED_ACTION_NOT_TAKEN, "MKD.permission", fileName));
            return;
        }
        
        // check file existance
        if(file.doesExist()) {
            out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_550_REQUESTED_ACTION_NOT_TAKEN, "MKD.exists", fileName));
            return;
        }
        
        // now create directory
        if(file.mkdir()) {
            out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_250_REQUESTED_FILE_ACTION_OKAY, "MKD", fileName));
            
            // write log message
            String userName = session.getUser().getName();
            LOG.info("Directory create : " + userName + " - " + fileName);
            
            // notify statistics object
            ServerFtpStatistics ftpStat = (ServerFtpStatistics)connection.getServerContext().getFtpStatistics();
            ftpStat.setMkdir(connection, file);
            
            // call Ftplet.onMkdirEnd() method
            try{
                ftpletRet = ftpletContainer.onMkdirEnd(session, request, out);
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
            out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_550_REQUESTED_ACTION_NOT_TAKEN, "MKD", fileName));
        }
    }
}
