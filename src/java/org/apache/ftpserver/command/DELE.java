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

/**
 * <code>DELE &lt;SP&gt; &lt;pathname&gt; &lt;CRLF&gt;</code><br>
 *
 * This command causes the file specified in the pathname to be
 * deleted at the server site.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class DELE implements ICommand {
    

    /**
     * Execute command.
     */
    public void execute(RequestHandler handler,
                        FtpRequestImpl request, 
                        FtpWriter out) throws IOException, FtpException {
        
        // reset state variables
        request.resetState(); 
        IFtpConfig fconfig = handler.getConfig();
        
        // argument check
        String fileName = request.getArgument();
        if(fileName == null) {
            out.send(501, "DELE", null);
            return;  
        }
        
        // call Ftplet.onDeleteStart() method
        Ftplet ftpletContainer = fconfig.getFtpletContainer();
        FtpletEnum ftpletRet;
        try {
            ftpletRet = ftpletContainer.onDeleteStart(request, out);
        } catch(Exception e) {
            ftpletRet = FtpletEnum.RET_DISCONNECT;
        }
        if(ftpletRet == FtpletEnum.RET_SKIP) {
            out.send(450, "DELE", fileName);
            return;
        }
        else if(ftpletRet == FtpletEnum.RET_DISCONNECT) {
            fconfig.getConnectionManager().closeConnection(handler);
            return;
        }

        // get file object
        FileObject file = null;
        
        try {
            file = request.getFileSystemView().getFileObject(fileName);
        }
        catch(Exception ex) {
            // TODO: handle exception
        }
        if(file == null) {
            out.send(550, "DELE.invalid", fileName);
            return;
        }

        // check file
        fileName = file.getFullName();

        if( !file.hasDeletePermission() ) {
            out.send(450, "DELE.permission", fileName);
            return;
        }
        
        // now delete
        if(file.delete()) {
            out.send(250, "DELE", fileName); 
            
            // log message
            String userName = request.getUser().getName();
            Log log = fconfig.getLogFactory().getInstance(getClass());
            log.info("File delete : " + userName + " - " + fileName);
            
            // notify statistics object
            IFtpStatistics ftpStat = (IFtpStatistics)fconfig.getFtpStatistics();
            ftpStat.setDelete(handler, file);
            
            // call Ftplet.onDeleteEnd() method
            try{
                ftpletRet = ftpletContainer.onDeleteEnd(request, out);
            } catch(Exception e) {
                ftpletRet = FtpletEnum.RET_DISCONNECT;
            }
            if(ftpletRet == FtpletEnum.RET_DISCONNECT) {
                fconfig.getConnectionManager().closeConnection(handler);
                return;
            }

        }
        else {
            out.send(450, "DELE", fileName);
        }
    }

}
