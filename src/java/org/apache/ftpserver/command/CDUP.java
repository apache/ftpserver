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

import org.apache.ftpserver.FtpRequestImpl;
import org.apache.ftpserver.FtpWriter;
import org.apache.ftpserver.RequestHandler;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.interfaces.ICommand;

/**
 * <code>CDUP &lt;CRLF&gt;</code><br>
 *
 * This command is a special case of CWD, and is included to
 * simplify the implementation of programs for transferring
 * directory trees between operating systems having different
 * syntaxes for naming the parent directory.  The reply codes
 * shall be identical to the reply codes of CWD.    
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class CDUP implements ICommand {

    /**
     * Execute command.
     */
    public void execute(RequestHandler handler, 
                        FtpRequestImpl request, 
                        FtpWriter out) throws IOException, FtpException {
        
        // reset state variables
        request.resetState();
        
        // change directory
        FileSystemView fsview = request.getFileSystemView();
        boolean success = false;
        try {
            success = fsview.changeDirectory("..");
        }
        catch(Exception ex) {
        }
        if(success) {
            String dirName = fsview.getCurrentDirectory().getFullName();
            out.send(250, "CDUP", dirName);
        }
        else {
            out.send(550, "CDUP", null);
        }
    }
}
