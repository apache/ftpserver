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
import java.net.SocketException;

import org.apache.commons.logging.Log;
import org.apache.ftpserver.FtpRequestImpl;
import org.apache.ftpserver.FtpWriter;
import org.apache.ftpserver.RequestHandler;
import org.apache.ftpserver.ftplet.FileObject;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.Ftplet;
import org.apache.ftpserver.ftplet.FtpletEnum;
import org.apache.ftpserver.interfaces.ICommand;
import org.apache.ftpserver.interfaces.IFtpConfig;
import org.apache.ftpserver.interfaces.IFtpStatistics;
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
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class STOR implements ICommand {
    

    /**
     * Execute command.
     */
    public void execute(RequestHandler handler, 
                        FtpRequestImpl request, 
                        FtpWriter out) throws IOException, FtpException {
        
        try {
        
            // get state variable
            long skipLen = request.getFileOffset();
            IFtpConfig fconfig = handler.getConfig();
            
            // argument check
            String fileName = request.getArgument();
            if(fileName == null) {
                out.send(501, "STOR", null);
                return;  
            }
            
            // call Ftplet.onUploadStart() method
            Ftplet ftpletContainer = fconfig.getFtpletContainer();
            FtpletEnum ftpletRet;
            try {
                ftpletRet = ftpletContainer.onUploadStart(request, out);
            } catch(Exception e) {
                ftpletRet = FtpletEnum.RET_DISCONNECT;
            }
            if(ftpletRet == FtpletEnum.RET_SKIP) {
                return;
            }
            else if(ftpletRet == FtpletEnum.RET_DISCONNECT) {
                fconfig.getConnectionManager().closeConnection(handler);
                return;
            }
            
            // get filename
            FileObject file = null;
            try {
                file = request.getFileSystemView().getFileObject(fileName);
            }
            catch(Exception ex) {
            }
            if(file == null) {
                out.send(550, "STOR.invalid", fileName);
                return;
            }
            fileName = file.getFullName();
            
            // get permission
            if( !file.hasWritePermission() ) {
                out.send(550, "STOR.permission", fileName);
                return;
            }
            
            // get data connection
            out.send(150, "STOR", fileName);
            InputStream is = null;
            try {
                is = request.getDataInputStream();
            }
            catch(IOException ex) {
                out.send(425, "STOR", fileName);
                return;
            }
            
            // get data from client
            boolean failure = false;
            BufferedInputStream bis = null;
            BufferedOutputStream bos = null;
            try {
            
                // open streams
                bis = IoUtils.getBufferedInputStream(is);
                bos = IoUtils.getBufferedOutputStream( file.createOutputStream(skipLen) );
                
                // transfer data
                int maxRate = handler.getRequest().getUser().getMaxUploadRate();
                long transSz = handler.transfer(bis, bos, maxRate);
                
                // log message
                String userName = request.getUser().getName();
                Log log = fconfig.getLogFactory().getInstance(getClass());
                log.info("File upload : " + userName + " - " + fileName);
                
                // notify the statistics component
                IFtpStatistics ftpStat = (IFtpStatistics)fconfig.getFtpStatistics();
                ftpStat.setUpload(handler, file, transSz);
            }
            catch(SocketException ex) {
                failure = true;
                out.send(426, "STOR", fileName);
            }
            catch(IOException ex) {
                failure = true;
                out.send(551, "STOR", fileName);
            }
            finally {
                IoUtils.close(bis);
                IoUtils.close(bos);
            }
            
            // if data transfer ok - send transfer complete message
            if(!failure) {
                out.send(226, "STOR", fileName);
                
                // call Ftplet.onUploadEnd() method
                try {
                    ftpletRet = ftpletContainer.onUploadEnd(request, out);
                } catch(Exception e) {
                    ftpletRet = FtpletEnum.RET_DISCONNECT;
                }
                if(ftpletRet == FtpletEnum.RET_DISCONNECT) {
                    fconfig.getConnectionManager().closeConnection(handler);
                    return;
                }

            }
        }
        finally {
            request.resetState();
            request.getFtpDataConnection().closeDataSocket();
        }
    }
}
