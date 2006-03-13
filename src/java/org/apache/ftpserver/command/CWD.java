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
 * <code>CWD  &lt;SP&gt; &lt;pathname&gt; &lt;CRLF&gt;</code><br>
 *
 * This command allows the user to work with a different
 * directory for file storage or retrieval without
 * altering his login or accounting information.  Transfer
 * parameters are similarly unchanged.  The argument is a
 * pathname specifying a directory.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class CWD implements ICommand {

    /**
     * Execute command
     */
    public void execute(RequestHandler handler, 
                        FtpRequestImpl request, 
                        FtpWriter out) throws IOException, FtpException {
        
        // reset state variables
        request.resetState();
        
        // get new directory name
        String dirName = "/";
        if (request.hasArgument()) {
            dirName = request.getArgument();
        } 
        
        // change directory
        FileSystemView fsview = request.getFileSystemView();
        boolean success = false;
        try {
            success = fsview.changeDirectory(dirName);
        }
        catch(Exception ex) {
        }
        if(success) {
            dirName = fsview.getCurrentDirectory().getFullName();
            out.send(250, "CWD", dirName);
        }
        else {
            out.send(550, "CWD", null);
        }
    }
}
