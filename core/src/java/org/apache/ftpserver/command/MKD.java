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
import org.apache.ftpserver.interfaces.FtpServerContext;
import org.apache.ftpserver.interfaces.ServerFtpStatistics;
import org.apache.ftpserver.listener.Connection;

/**
 * <code>MKD  &lt;SP&gt; &lt;pathname&gt; &lt;CRLF&gt;</code><br>
 *
 * This command causes the directory specified in the pathname
 * to be created as a directory (if the pathname is absolute)
 * or as a subdirectory of the current working directory (if
 * the pathname is relative).
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class MKD extends AbstractCommand {
    

    /**
     * Execute command.
     */
    public void execute(Connection connection,
                        FtpRequest request, 
                        FtpSessionImpl session, 
                        FtpWriter out) throws IOException, FtpException {
        
        // reset state
        session.resetState(); 
        FtpServerContext serverContext = connection.getServerContext();
        
        // argument check
        String fileName = request.getArgument();
        if(fileName == null) {
            out.send(501, "MKD", null);
            return;  
        }
        
        // call Ftplet.onMkdirStart() method
        Ftplet ftpletContainer = serverContext.getFtpletContainer();
        FtpletEnum ftpletRet;
        try{
            ftpletRet = ftpletContainer.onMkdirStart(session, request, out);
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
            out.send(550, "MKD.invalid", fileName);
            return;
        }
        
        // check permission
        fileName = file.getFullName();
        if( !file.hasWritePermission() ) {
            out.send(550, "MKD.permission", fileName);
            return;
        }
        
        // check file existance
        if(file.doesExist()) {
            out.send(550, "MKD.exists", fileName);
            return;
        }
        
        // now create directory
        if(file.mkdir()) {
            out.send(250, "MKD", fileName);
            
            // write log message
            String userName = session.getUser().getName();
            Log log = serverContext.getLogFactory().getInstance(getClass());
            log.info("Directory create : " + userName + " - " + fileName);
            
            // notify statistics object
            ServerFtpStatistics ftpStat = (ServerFtpStatistics)connection.getServerContext().getFtpStatistics();
            ftpStat.setMkdir(connection, file);
            
            // call Ftplet.onMkdirEnd() method
            try{
                ftpletRet = ftpletContainer.onMkdirEnd(session, request, out);
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
            out.send(550, "MKD", fileName);
        }
    }
}
