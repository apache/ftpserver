// $Id$
/*
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ftpserver.command;

import java.io.IOException;

import org.apache.ftpserver.Command;
import org.apache.ftpserver.FtpRequestImpl;
import org.apache.ftpserver.FtpWriter;
import org.apache.ftpserver.RequestHandler;
import org.apache.ftpserver.ftplet.FileObject;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.Ftplet;
import org.apache.ftpserver.ftplet.FtpletEnum;
import org.apache.ftpserver.ftplet.Logger;
import org.apache.ftpserver.interfaces.IFtpConfig;
import org.apache.ftpserver.interfaces.IFtpStatistics;

/**
 * <code>RMD  &lt;SP&gt; &lt;pathname&gt; &lt;CRLF&gt;</code><br>
 *
 * This command causes the directory specified in the pathname
 * to be removed as a directory (if the pathname is absolute)
 * or as a subdirectory of the current working directory (if
 * the pathname is relative).
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class RMD implements Command {
    
    /**
     * Execute command
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
            out.send(501, "RMD", null);
            return;  
        }
        
        // call Ftplet.onRmdirStart() method
        Ftplet ftpletContainer = fconfig.getFtpletContainer();
        FtpletEnum ftpletRet = ftpletContainer.onRmdirStart(request, out);
        if(ftpletRet == FtpletEnum.RET_SKIP) {
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
        }
        if(file == null) {
            out.send(550, "RMD.no.permission", fileName);
            return;
        }
        
        // check permission
        fileName = file.getFullName();
        if( !file.hasDeletePermission() ) {
            out.send(450, "RMD.no.permission", fileName);
            return;
        }
        
        // check file
        if(!file.isDirectory()) {
            out.send(450, "RMD.directory.invalid", fileName);
            return;
        }
        
        // now delete directory
        if(file.delete()) {
            out.send(250, "RMD", fileName); 
            
            // write log message
            Logger logger = fconfig.getLogger();
            String userName = request.getUser().getName();
            logger.info("Directory remove : " + userName + " - " + fileName);
            
            // notify statistics object
            IFtpStatistics ftpStat = (IFtpStatistics)fconfig.getFtpStatistics();
            ftpStat.setRmdir(handler, file);
            
            // call Ftplet.onRmdirEnd() method
            ftpletRet = ftpletContainer.onRmdirEnd(request, out);
            if(ftpletRet == FtpletEnum.RET_DISCONNECT) {
                fconfig.getConnectionManager().closeConnection(handler);
                return;
            }
        }
        else {
            out.send(450, "RMD", fileName);
        }
    }
}
