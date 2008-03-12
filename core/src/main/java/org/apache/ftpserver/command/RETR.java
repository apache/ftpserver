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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.SocketException;

import org.apache.ftpserver.DefaultFtpReply;
import org.apache.ftpserver.IODataConnectionFactory;
import org.apache.ftpserver.ftplet.DataConnection;
import org.apache.ftpserver.ftplet.DataConnectionFactory;
import org.apache.ftpserver.ftplet.DataType;
import org.apache.ftpserver.ftplet.FileObject;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpReply;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.ftplet.Ftplet;
import org.apache.ftpserver.ftplet.FtpletEnum;
import org.apache.ftpserver.interfaces.FtpIoSession;
import org.apache.ftpserver.interfaces.FtpServerContext;
import org.apache.ftpserver.interfaces.ServerFtpStatistics;
import org.apache.ftpserver.util.FtpReplyUtil;
import org.apache.ftpserver.util.IoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <code>RETR &lt;SP&gt; &lt;pathname&gt; &lt;CRLF&gt;</code><br>
 *
 * This command causes the server-DTP to transfer a copy of the
 * file, specified in the pathname, to the server- or user-DTP
 * at the other end of the data connection.  The status and
 * contents of the file at the server site shall be unaffected.
 */
public 
class RETR extends AbstractCommand {
    
    private final Logger LOG = LoggerFactory.getLogger(RETR.class);

    /**
     * Execute command.
     */
    public void execute(FtpIoSession session,
                        FtpServerContext context,
                        FtpRequest request) throws IOException, FtpException {
        
        try {
        
            // get state variable
            long skipLen = session.getFileOffset();
            
            // argument check
            String fileName = request.getArgument();
            if(fileName == null) {
                session.write(FtpReplyUtil.translate(session, request, context, FtpReply.REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS, "RETR", null));
                return;  
            }
    
            // call Ftplet.onDownloadStart() method
            Ftplet ftpletContainer = context.getFtpletContainer();
            FtpletEnum ftpletRet;
            try {
                ftpletRet = ftpletContainer.onDownloadStart(session.getFtpletSession(), request);
            } catch(Exception e) {
                LOG.debug("Ftplet container threw exception", e);
                ftpletRet = FtpletEnum.RET_DISCONNECT;
            }
            if(ftpletRet == FtpletEnum.RET_SKIP) {
                return;
            }
            else if(ftpletRet == FtpletEnum.RET_DISCONNECT) {
                session.closeOnFlush().awaitUninterruptibly(10000);
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
                session.write(FtpReplyUtil.translate(session, request, context, FtpReply.REPLY_550_REQUESTED_ACTION_NOT_TAKEN, "RETR.missing", fileName));
                return;
            }
            fileName = file.getFullName();
            
            // check file existance
            if(!file.doesExist()) {
                session.write(FtpReplyUtil.translate(session, request, context, FtpReply.REPLY_550_REQUESTED_ACTION_NOT_TAKEN, "RETR.missing", fileName));
                return;
            }
            
            // check valid file
            if(!file.isFile()) {
                session.write(FtpReplyUtil.translate(session, request, context, FtpReply.REPLY_550_REQUESTED_ACTION_NOT_TAKEN, "RETR.invalid", fileName));
                return;
            }
            
            // check permission
            if(!file.hasReadPermission()) {
                session.write(FtpReplyUtil.translate(session, request, context, FtpReply.REPLY_550_REQUESTED_ACTION_NOT_TAKEN, "RETR.permission", fileName));
                return;
            }

            // 24-10-2007 - added check if PORT or PASV is issued, see https://issues.apache.org/jira/browse/FTPSERVER-110
            DataConnectionFactory connFactory = session.getDataConnection();
            if (connFactory instanceof IODataConnectionFactory) {
                InetAddress address = ((IODataConnectionFactory)connFactory).getInetAddress();
                if (address == null) {
                    session.write(new DefaultFtpReply(FtpReply.REPLY_503_BAD_SEQUENCE_OF_COMMANDS, "PORT or PASV must be issued first"));
                    return;
                }
            }
            
            // get data connection
            session.write(FtpReplyUtil.translate(session, request, context, FtpReply.REPLY_150_FILE_STATUS_OKAY, "RETR", null));

            
            // send file data to client
            boolean failure = false;
            InputStream is = null;

            DataConnection dataConnection;
            try {
                dataConnection = session.getDataConnection().openConnection();
            } catch (Exception e) {
                LOG.debug("Exception getting the output data stream", e);
                session.write(FtpReplyUtil.translate(session, request, context, FtpReply.REPLY_425_CANT_OPEN_DATA_CONNECTION, "RETR", null));
                return;
            }
            
            try {
                
                // open streams
                is = openInputStream(session, file, skipLen);
                
                // transfer data
                long transSz = dataConnection.transferToClient(is);
                
                // log message
                String userName = session.getUser().getName();
                LOG.info("File download : " + userName + " - " + fileName);
                
                // notify the statistics component
                ServerFtpStatistics ftpStat = (ServerFtpStatistics)context.getFtpStatistics();
                if(ftpStat != null)  {
                    ftpStat.setDownload(session, file, transSz);
                }
            }
            catch(SocketException ex) {
                LOG.debug("Socket exception during data transfer", ex);
                failure = true;
                session.write(FtpReplyUtil.translate(session, request, context, FtpReply.REPLY_426_CONNECTION_CLOSED_TRANSFER_ABORTED, "RETR", fileName));
            }
            catch(IOException ex) {
                LOG.debug("IOException during data transfer", ex);
                failure = true;
                session.write(FtpReplyUtil.translate(session, request, context, FtpReply.REPLY_551_REQUESTED_ACTION_ABORTED_PAGE_TYPE_UNKNOWN, "RETR", fileName));
            }
            finally {
                IoUtils.close(is);
            }
            
            // if data transfer ok - send transfer complete message
            if(!failure) {
                session.write(FtpReplyUtil.translate(session, request, context, FtpReply.REPLY_226_CLOSING_DATA_CONNECTION, "RETR", fileName));
                
                // call Ftplet.onDownloadEnd() method
                try {
                    ftpletRet = ftpletContainer.onDownloadEnd(session.getFtpletSession(), request);
                } catch(Exception e) {
                    LOG.debug("Ftplet container threw exception", e);
                    ftpletRet = FtpletEnum.RET_DISCONNECT;
                }
                if(ftpletRet == FtpletEnum.RET_DISCONNECT) {
                    session.closeOnFlush().awaitUninterruptibly(10000);
                    return;
                }

            }
        }
        finally {
            session.resetState();
            session.getDataConnection().closeDataConnection();
        }
    }
    
    /**
     * Skip length and open input stream.
     */
    public InputStream openInputStream(FtpIoSession session, 
                                       FileObject file, 
                                       long skipLen) throws IOException {
        InputStream in;
        if(session.getDataType() == DataType.ASCII) {
            int c;
            long offset = 0L;
            in = new BufferedInputStream(file.createInputStream(0L));
            while (offset++ < skipLen) {
                if ( (c=in.read()) == -1) {
                    throw new IOException("Cannot skip");
                }
                if (c == '\n') {
                    offset++;
                }
            }
        }
        else {
            in = file.createInputStream(skipLen);
        }
        return in;
    }

}
