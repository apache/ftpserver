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

import org.apache.commons.logging.Log;
import org.apache.ftpserver.FtpSessionImpl;
import org.apache.ftpserver.FtpWriter;
import org.apache.ftpserver.ftplet.FileObject;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.ftplet.Ftplet;
import org.apache.ftpserver.ftplet.FtpletEnum;
import org.apache.ftpserver.interfaces.Connection;
import org.apache.ftpserver.interfaces.FtpServerContext;

/**
 * <code>RNTO &lt;SP&gt; &lt;pathname&gt; &lt;CRLF&gt;</code><br>
 *
 * This command specifies the new pathname of the file
 * specified in the immediately preceding "rename from"
 * command.  Together the two commands cause a file to be
 * renamed.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class RNTO extends AbstractCommand {
    

    /**
     * Execute command.
     */
    public void execute(Connection connection, 
                        FtpRequest request,
                        FtpSessionImpl session, 
                        FtpWriter out) throws IOException, FtpException {
        try {
            
            // argument check
            String toFileStr = request.getArgument();
            if(toFileStr == null) {
                out.send(501, "RNTO", null);
                return;  
            }
            
            // call Ftplet.onRenameStart() method
            FtpServerContext serverContext = connection.getServerContext();
            Ftplet ftpletContainer = serverContext.getFtpletContainer();
            FtpletEnum ftpletRet;
            try {
                ftpletRet = ftpletContainer.onRenameStart(session, request, out);
            } catch(Exception e) {
                log.debug("Ftplet container threw exception", e);
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
                out.send(503, "RNTO", null);
                return;
            }
            
            // get target file
            FileObject toFile = null;
            try {
                toFile = session.getFileSystemView().getFileObject(toFileStr);
            }
            catch(Exception ex) {
                log.debug("Exception getting file object", ex);
            }
            if(toFile == null) {
                out.send(553, "RNTO.invalid", null);
                return;
            }
            toFileStr = toFile.getFullName();
            
            // check permission
            if( !toFile.hasWritePermission() ) {
                out.send(553, "RNTO.permission", null);
                return;
            }
            
            // check file existance
            if( !frFile.doesExist() ) {
                out.send(553, "RNTO.missing", null);
                return;
            }
            
            // now rename
            if( frFile.move(toFile) ) { 
                out.send(250, "RNTO", toFileStr);

                Log log = serverContext.getLogFactory().getInstance(getClass());
                log.info("File rename (" + session.getUser().getName() + ") " 
                                         + frFile.getFullName() + " -> " + toFile.getFullName());
                
                // call Ftplet.onRenameEnd() method
                try {
                    ftpletRet = ftpletContainer.onRenameEnd(session, request, out);
                } catch(Exception e) {
                    log.debug("Ftplet container threw exception", e);
                    ftpletRet = FtpletEnum.RET_DISCONNECT;
                }
                if(ftpletRet == FtpletEnum.RET_DISCONNECT) {
                    serverContext.getConnectionManager().closeConnection(connection);
                    return;
                }
            }
            else {
                out.send(553, "RNTO", toFileStr);
            }
        
        }
        finally {
            session.resetState(); 
        }
    } 

}
