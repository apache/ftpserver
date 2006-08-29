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

import org.apache.ftpserver.FtpRequestImpl;
import org.apache.ftpserver.FtpWriter;
import org.apache.ftpserver.RequestHandler;
import org.apache.ftpserver.interfaces.ICommand;

/**
 * <code>REST &lt;SP&gt; <marker> &lt;CRLF&gt;</code><br>
 *
 * The argument field represents the server marker at which
 * file transfer is to be restarted.  This command does not
 * cause file transfer but skips over the file to the specified
 * data checkpoint.  This command shall be immediately followed
 * by the appropriate FTP service command which shall cause
 * file transfer to resume.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class REST implements ICommand {

    /**
     * Execute command
     */
    public void execute(RequestHandler handler, 
                        FtpRequestImpl request, 
                        FtpWriter out) throws IOException {
        
        // argument check
        String argument = request.getArgument();
        if(argument == null) {
            out.send(501, "REST", null);
            return;  
        }
        
        // get offset number
        request.resetState();
        long skipLen = 0L;
        try {
            skipLen = Long.parseLong(argument);
            
            // check offset number
            if(skipLen < 0L) {
                skipLen = 0L;
                out.send(501, "REST.negetive", null);
            }
            else {
                out.send(350, "REST", null);
            }
        }
        catch(NumberFormatException ex) {
            out.send(501, "REST.invalid", null); 
        }
        
        request.setFileOffset(skipLen);
    } 
    
}
