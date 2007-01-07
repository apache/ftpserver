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
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;

import org.apache.commons.logging.Log;
import org.apache.ftpserver.FtpSessionImpl;
import org.apache.ftpserver.FtpWriter;
import org.apache.ftpserver.ftplet.DataType;
import org.apache.ftpserver.ftplet.FileObject;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.ftplet.Ftplet;
import org.apache.ftpserver.ftplet.FtpletEnum;
import org.apache.ftpserver.interfaces.Connection;
import org.apache.ftpserver.interfaces.FtpServerContext;
import org.apache.ftpserver.interfaces.ServerFtpStatistics;
import org.apache.ftpserver.usermanager.TransferRateRequest;
import org.apache.ftpserver.util.IoUtils;

/**
 * <code>RETR &lt;SP&gt; &lt;pathname&gt; &lt;CRLF&gt;</code><br>
 *
 * This command causes the server-DTP to transfer a copy of the
 * file, specified in the pathname, to the server- or user-DTP
 * at the other end of the data connection.  The status and
 * contents of the file at the server site shall be unaffected.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class RETR extends AbstractCommand {
    

    /**
     * Execute command.
     */
    public void execute(Connection connection,
                        FtpRequest request,
                        FtpSessionImpl session, 
                        FtpWriter out) throws IOException, FtpException {
        
        try {
        
            // get state variable
            long skipLen = session.getFileOffset();
            FtpServerContext serverContext = connection.getServerContext();
            
            // argument check
            String fileName = request.getArgument();
            if(fileName == null) {
                out.send(501, "RETR", null);
                return;  
            }
    
            // call Ftplet.onDownloadStart() method
            Ftplet ftpletContainer = serverContext.getFtpletContainer();
            FtpletEnum ftpletRet;
            try {
                ftpletRet = ftpletContainer.onDownloadStart(session, request, out);
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
            
            // get file object
            FileObject file = null;
            try {
                file = session.getFileSystemView().getFileObject(fileName);
            }
            catch(Exception ex) {
                log.debug("Exception getting file object", ex);
            }
            if(file == null) {
                out.send(550, "RETR.missing", fileName);
                return;
            }
            fileName = file.getFullName();
            
            // check file existance
            if(!file.doesExist()) {
                out.send(550, "RETR.missing", fileName);
                return;
            }
            
            // check valid file
            if(!file.isFile()) {
                out.send(550, "RETR.invalid", fileName);
                return;
            }
            
            // check permission
            if(!file.hasReadPermission()) {
                out.send(550, "RETR.permission", fileName);
                return;
            }
            
            // get data connection
            out.send(150, "RETR", null);
            OutputStream os = null;
            try {
                os = session.getDataOutputStream();
            }
            catch(IOException ex) {
                log.debug("Exception getting the output data stream", ex);
                out.send(425, "RETR", null);
                return;
            }
            
            // send file data to client
            boolean failure = false;
            BufferedInputStream bis = null;
            BufferedOutputStream bos = null;
            try {
                
                // open streams
                bis = IoUtils.getBufferedInputStream(openInputStream(connection, session, file, skipLen) );
                bos = IoUtils.getBufferedOutputStream(os);
                
                // transfer data
                TransferRateRequest transferRateRequest = new TransferRateRequest();
                transferRateRequest = (TransferRateRequest) session.getUser().authorize(transferRateRequest);
            
                int maxRate = 0;
                if(transferRateRequest != null) {
                    maxRate = transferRateRequest.getMaxDownloadRate();
                }
                
                long transSz = connection.transfer(bis, bos, maxRate);
                
                // log message
                String userName = session.getUser().getName();
                Log log = serverContext.getLogFactory().getInstance(getClass());
                log.info("File download : " + userName + " - " + fileName);
                
                // notify the statistics component
                ServerFtpStatistics ftpStat = (ServerFtpStatistics)serverContext.getFtpStatistics();
                ftpStat.setDownload(connection, file, transSz);
            }
            catch(SocketException ex) {
                log.debug("Socket exception during data transfer", ex);
                failure = true;
                out.send(426, "RETR", fileName);
            }
            catch(IOException ex) {
                log.debug("IOException during data transfer", ex);
                failure = true;
                out.send(551, "RETR", fileName);
            }
            finally {
                IoUtils.close(bis);
                IoUtils.close(bos);
            }
            
            // if data transfer ok - send transfer complete message
            if(!failure) {
                out.send(226, "RETR", fileName);
                
                // call Ftplet.onDownloadEnd() method
                try {
                    ftpletRet = ftpletContainer.onDownloadEnd(session, request, out);
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
    
    /**
     * Skip length and open input stream.
     */
    public InputStream openInputStream(Connection connection,
                                       FtpSessionImpl session, 
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
