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
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.Ftplet;
import org.apache.ftpserver.ftplet.FtpletEnum;
import org.apache.ftpserver.interfaces.Command;
import org.apache.ftpserver.interfaces.FtpServerContext;
import org.apache.ftpserver.interfaces.ServerFtpStatistics;
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
class STOU implements Command {


    /**
     * Execute command.
     */
    public void execute(RequestHandler handler, 
                        FtpRequestImpl request, 
                        FtpWriter out) throws IOException, FtpException {
        
        try {
        
            // reset state variables
            request.resetState();
            FtpServerContext serverContext = handler.getServerContext();
            
            // call Ftplet.onUploadUniqueStart() method
            Ftplet ftpletContainer = serverContext.getFtpletContainer();
            FtpletEnum ftpletRet;
            try {
                ftpletRet = ftpletContainer.onUploadUniqueStart(request, out);
            } catch(Exception e) {
                ftpletRet = FtpletEnum.RET_DISCONNECT;
            }
            if(ftpletRet == FtpletEnum.RET_SKIP) {
                return;
            }
            else if(ftpletRet == FtpletEnum.RET_DISCONNECT) {
                serverContext.getConnectionManager().closeConnection(handler);
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
                file = request.getFileSystemView().getFileObject(filePrefix);
                if(file != null) {
                    file = getUniqueFile(handler, file);
                }
            }
            catch(Exception ex) {
            }
            if(file == null) {
                out.send(550, "STOU", null);
                return;
            }
            String fileName = file.getFullName();
            
            // check permission
            if(!file.hasWritePermission()) {
                out.send(550, "STOU.permission", fileName);
                return;
            }
            
            // get data connection
            out.send(150, "STOU", null);
            InputStream is = null;
            try {
                is = request.getDataInputStream();
            }
            catch(IOException ex) {
                out.send(425, "STOU", fileName);
                return;
            }
            
            // get data from client
            boolean failure = false;
            BufferedInputStream bis = null;
            BufferedOutputStream bos = null;
            out.send(250, "STOU", fileName);
            try {
                
                // open streams
                bis = IoUtils.getBufferedInputStream(is);
                bos = IoUtils.getBufferedOutputStream( file.createOutputStream(0L) );

                // transfer data
                int maxRate = request.getUser().getMaxUploadRate();
                long transSz = handler.transfer(bis, bos, maxRate);
                
                // log message
                String userName = request.getUser().getName();
                Log log = serverContext.getLogFactory().getInstance(getClass());
                log.info("File upload : " + userName + " - " + fileName);
                
                // notify the statistics component
                ServerFtpStatistics ftpStat = (ServerFtpStatistics)serverContext.getFtpStatistics();
                if(ftpStat != null) {
                    ftpStat.setUpload(handler, file, transSz);
                }
            }
            catch(SocketException ex) {
                failure = true;
                out.send(426, "STOU", fileName);
            }
            catch(IOException ex) {
                failure = true;
                out.send(551, "STOU", fileName);
            }
            finally {
                IoUtils.close(bis);
                IoUtils.close(bos);
            }
            
            // if data transfer ok - send transfer complete message
            if(!failure) {
                out.send(226, "STOU", fileName);
                
                // call Ftplet.onUploadUniqueEnd() method
                try {
                    ftpletRet = ftpletContainer.onUploadUniqueEnd(request, out);
                } catch(Exception e) {
                    ftpletRet = FtpletEnum.RET_DISCONNECT;
                }
                if(ftpletRet == FtpletEnum.RET_DISCONNECT) {
                    serverContext.getConnectionManager().closeConnection(handler);
                    return;
                }

            }
        }
        finally {
            request.getFtpDataConnection().closeDataSocket();
        }
        
    }

    /**
     * Get unique file object.
     */
    protected FileObject getUniqueFile(RequestHandler handler, FileObject oldFile) throws FtpException {
        FileObject newFile = oldFile;
        FileSystemView fsView = handler.getRequest().getFileSystemView();
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
