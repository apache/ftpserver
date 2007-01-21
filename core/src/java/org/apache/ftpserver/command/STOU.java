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
import org.apache.ftpserver.FtpWriter;
import org.apache.ftpserver.ftplet.FileObject;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.ftplet.FtpReply;
import org.apache.ftpserver.ftplet.FtpSession;
import org.apache.ftpserver.ftplet.Ftplet;
import org.apache.ftpserver.ftplet.FtpletEnum;
import org.apache.ftpserver.interfaces.FtpServerContext;
import org.apache.ftpserver.interfaces.ServerFtpStatistics;
import org.apache.ftpserver.listener.Connection;
import org.apache.ftpserver.util.FtpReplyUtil;
import org.apache.ftpserver.util.IoUtils;

/**
 * <code>STOU &lt;CRLF&gt;</code><br>
 *
 * This command behaves like STOR except that the resultant
 * file is to be created in the current directory under a name
 * unique to that directory.  The 250 Transfer Started response
 * must include the name generated.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class STOU extends AbstractCommand {


    /**
     * Execute command.
     */
    public void execute(Connection connection, 
                        FtpRequest request,
                        FtpSessionImpl session, 
                        FtpWriter out) throws IOException, FtpException {
        
        try {
        
            // reset state variables
            session.resetState();
            FtpServerContext serverContext = connection.getServerContext();
            
            // call Ftplet.onUploadUniqueStart() method
            Ftplet ftpletContainer = serverContext.getFtpletContainer();
            FtpletEnum ftpletRet;
            try {
                ftpletRet = ftpletContainer.onUploadUniqueStart(session, request, out);
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
            
            String dirName = request.getArgument();
            
            String filePrefix;
            if(dirName == null) {
                filePrefix = "ftp.dat";
            } else {
                filePrefix = dirName + "/ftp.dat";
            }
            
            // get filenames
            FileObject file = null;
            try {
                file = session.getFileSystemView().getFileObject(filePrefix);
                if(file != null) {
                    file = getUniqueFile(connection, session, file);
                }
            }
            catch(Exception ex) {
                log.debug("Exception getting file object", ex);
            }
            if(file == null) {
                out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_550_REQUESTED_ACTION_NOT_TAKEN, "STOU", null));
                return;
            }
            String fileName = file.getFullName();
            
            // check permission
            if(!file.hasWritePermission()) {
                out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_550_REQUESTED_ACTION_NOT_TAKEN, "STOU.permission", fileName));
                return;
            }
            
            // get data connection
            out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_150_FILE_STATUS_OKAY, "STOU", null));
            
            // get data from client
            boolean failure = false;
            OutputStream os = null;
            out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_250_REQUESTED_FILE_ACTION_OKAY, "STOU", fileName));
            
            FtpDataConnection dataConnection;
            try {
                dataConnection = session.getFtpDataConnection().openConnection();
            } catch (Exception e) {
                log.debug("Exception getting the input data stream", e);
                out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_425_CANT_OPEN_DATA_CONNECTION, "STOU", fileName));
                return;
            }
            
            try {
                
                // open streams
                os = file.createOutputStream(0L);

                // transfer data
                long transSz = dataConnection.transferFromClient(os);
                
                // log message
                String userName = session.getUser().getName();
                log.info("File upload : " + userName + " - " + fileName);
                
                // notify the statistics component
                ServerFtpStatistics ftpStat = (ServerFtpStatistics)serverContext.getFtpStatistics();
                if(ftpStat != null) {
                    ftpStat.setUpload(connection, file, transSz);
                }
            }
            catch(SocketException ex) {
                log.debug("Socket exception during data transfer", ex);
                failure = true;
                out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_426_CONNECTION_CLOSED_TRANSFER_ABORTED, "STOU", fileName));
            }
            catch(IOException ex) {
                log.debug("IOException during data transfer", ex);
                failure = true;
                out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_551_REQUESTED_ACTION_ABORTED_PAGE_TYPE_UNKNOWN, "STOU", fileName));
            }
            finally {
                IoUtils.close(os);
            }
            
            // if data transfer ok - send transfer complete message
            if(!failure) {
                out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_226_CLOSING_DATA_CONNECTION, "STOU", fileName));
                
                // call Ftplet.onUploadUniqueEnd() method
                try {
                    ftpletRet = ftpletContainer.onUploadUniqueEnd(session, request, out);
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
            session.getFtpDataConnection().closeDataSocket();
        }
        
    }

    /**
     * Get unique file object.
     */
    protected FileObject getUniqueFile(Connection connection, FtpSession session, FileObject oldFile) throws FtpException {
        FileObject newFile = oldFile;
        FileSystemView fsView = session.getFileSystemView();
        String fileName = newFile.getFullName();
        while( newFile.doesExist() ) {
            newFile = fsView.getFileObject(fileName + '.' + System.currentTimeMillis());
            if(newFile == null) {
                break;
            }
        }
        return newFile;
    }

}
