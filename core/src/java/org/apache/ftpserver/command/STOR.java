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
import java.io.OutputStream;
import java.net.SocketException;

import org.apache.ftpserver.FtpDataConnection;
import org.apache.ftpserver.FtpSessionImpl;
import org.apache.ftpserver.ftplet.FileObject;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpReplyOutput;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.ftplet.FtpReply;
import org.apache.ftpserver.ftplet.Ftplet;
import org.apache.ftpserver.ftplet.FtpletEnum;
import org.apache.ftpserver.interfaces.FtpServerContext;
import org.apache.ftpserver.interfaces.ServerFtpStatistics;
import org.apache.ftpserver.listener.Connection;
import org.apache.ftpserver.util.FtpReplyUtil;
import org.apache.ftpserver.util.IoUtils;

/**
 * <code>STOR &lt;SP&gt; &lt;pathname&gt; &lt;CRLF&gt;</code><br>
 *
 * This command causes the server-DTP to accept the data
 * transferred via the data connection and to store the data as
 * a file at the server site.  If the file specified in the
 * pathname exists at the server site, then its contents shall
 * be replaced by the data being transferred.  A new file is
 * created at the server site if the file specified in the
 * pathname does not already exist.
 */
public 
class STOR extends AbstractCommand {
    

    /**
     * Execute command.
     */
    public void execute(Connection connection, 
                        FtpRequest request,
                        FtpSessionImpl session, 
                        FtpReplyOutput out) throws IOException, FtpException {
        
        try {
        
            // get state variable
            long skipLen = session.getFileOffset();
            FtpServerContext serverContext = connection.getServerContext();
            
            // argument check
            String fileName = request.getArgument();
            if(fileName == null) {
                out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS, "STOR", null));
                return;  
            }
            
            // call Ftplet.onUploadStart() method
            Ftplet ftpletContainer = serverContext.getFtpletContainer();
            FtpletEnum ftpletRet;
            try {
                ftpletRet = ftpletContainer.onUploadStart(session, request, out);
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
            
            // get filename
            FileObject file = null;
            try {
                file = session.getFileSystemView().getFileObject(fileName);
            }
            catch(Exception ex) {
                log.debug("Exception getting file object", ex);
            }
            if(file == null) {
                out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_550_REQUESTED_ACTION_NOT_TAKEN, "STOR.invalid", fileName));
                return;
            }
            fileName = file.getFullName();
            
            // get permission
            if( !file.hasWritePermission() ) {
                out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_550_REQUESTED_ACTION_NOT_TAKEN, "STOR.permission", fileName));
                return;
            }
            
            // get data connection
            out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_150_FILE_STATUS_OKAY, "STOR", fileName));
            
            FtpDataConnection dataConnection;
            try {
                dataConnection = session.getFtpDataConnection().openConnection();
            } catch (Exception e) {
                log.debug("Exception getting the input data stream", e);
                out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_425_CANT_OPEN_DATA_CONNECTION, "STOR", fileName));
                return;
            }
            
            // transfer data
            boolean failure = false;
            OutputStream outStream = null;
            try {
                outStream = file.createOutputStream(skipLen);
                long transSz = dataConnection.transferFromClient(outStream);
                
                // log message
                String userName = session.getUser().getName();
                log.info("File upload : " + userName + " - " + fileName);
                
                // notify the statistics component
                ServerFtpStatistics ftpStat = (ServerFtpStatistics)serverContext.getFtpStatistics();
                ftpStat.setUpload(connection, file, transSz);
            }
            catch(SocketException ex) {
                log.debug("Socket exception during data transfer", ex);
                failure = true;
                out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_426_CONNECTION_CLOSED_TRANSFER_ABORTED, "STOR", fileName));
            }
            catch(IOException ex) {
                log.debug("IOException during data transfer", ex);
                failure = true;
                out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_551_REQUESTED_ACTION_ABORTED_PAGE_TYPE_UNKNOWN, "STOR", fileName));
            }
            finally {
                IoUtils.close(outStream);
            }
            
            // if data transfer ok - send transfer complete message
            if(!failure) {
                out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_226_CLOSING_DATA_CONNECTION, "STOR", fileName));
                
                // call Ftplet.onUploadEnd() method
                try {
                    ftpletRet = ftpletContainer.onUploadEnd(session, request, out);
                } catch(Exception e) {
                    log.debug("Ftplet container threw exception", e);
                    ftpletRet = FtpletEnum.RET_DISCONNECT;
                }
                if(ftpletRet == FtpletEnum.RET_DISCONNECT) {
                    serverContext.getConnectionManager().closeConnection(connection);
                    return;
                }

            }
        }
        finally {
            session.resetState();
            session.getFtpDataConnection().closeDataSocket();
        }
    }
}
