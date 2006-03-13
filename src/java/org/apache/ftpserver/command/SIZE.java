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
import org.apache.ftpserver.ftplet.FileObject;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.interfaces.ICommand;

/**
 * <code>SIZE &lt;SP&gt; &lt;pathname&gt; &lt;CRLF&gt;</code><br>
 *
 * Returns the size of the file in bytes.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class SIZE implements ICommand {
    
    /**
     * Execute command.
     */
    public void execute(RequestHandler handler,
                        FtpRequestImpl request, 
                        FtpWriter out) throws IOException, FtpException {
        
        // reset state variables
        request.resetState();
        
        // argument check
        String fileName = request.getArgument();
        if(fileName == null) {
            out.send(501, "SIZE", null);
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
            out.send(550, "SIZE.missing", fileName);
            return;
        }
        
        // print file size
        fileName = file.getFullName();
        if(!file.doesExist()) {
            out.send(550, "SIZE.missing", fileName);
        }
        else if(!file.isFile()) {
            out.send(550, "SIZE.invalid", fileName);
        }
        else {
            String fileLen = String.valueOf(file.getSize());             
            out.send(213, "SIZE", fileLen);
        }
    } 

}
