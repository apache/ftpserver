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
class RNTO implements ICommand {
    

    /**
     * Execute command.
     */
    public void execute(RequestHandler handler, 
                        FtpRequestImpl request, 
                        FtpWriter out) throws IOException, FtpException {
        try {
            
            // argument check
            String toFileStr = request.getArgument();
            if(toFileStr == null) {
                out.send(501, "RNTO", null);
                return;  
            }
            
            // call Ftplet.onRenameStart() method
            IFtpConfig fconfig = handler.getConfig();
            Ftplet ftpletContainer = fconfig.getFtpletContainer();
            FtpletEnum ftpletRet = ftpletContainer.onRenameStart(request, out);
            if(ftpletRet == FtpletEnum.RET_SKIP) {
                return;
            }
            else if(ftpletRet == FtpletEnum.RET_DISCONNECT) {
                fconfig.getConnectionManager().closeConnection(handler);
                return;
            }
            
            // get the "rename from" file object
            FileObject frFile = request.getRenameFrom();
            if( frFile == null ) {
                out.send(503, "RNTO", null);
                return;
            }
            
            // get target file
            FileObject toFile = null;
            try {
                toFile = request.getFileSystemView().getFileObject(toFileStr);
            }
            catch(Exception ex) {
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
                Log log = fconfig.getLogFactory().getInstance(getClass());
                log.info("File rename (" + request.getUser().getName() + ") " 
                                         + frFile.getFullName() + " -> " + toFile.getFullName());
                out.send(250, "RNTO", toFileStr);
                
                // call Ftplet.onRenameEnd() method
                ftpletRet = ftpletContainer.onRenameEnd(request, out);
                if(ftpletRet == FtpletEnum.RET_DISCONNECT) {
                    fconfig.getConnectionManager().closeConnection(handler);
                    return;
                }
            }
            else {
                out.send(553, "RNTO", toFileStr);
            }
        
        }
        finally {
            request.resetState(); 
        }
    } 

}
