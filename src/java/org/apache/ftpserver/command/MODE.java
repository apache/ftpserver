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

import org.apache.ftpserver.Command;
import org.apache.ftpserver.FtpRequestImpl;
import org.apache.ftpserver.FtpWriter;
import org.apache.ftpserver.RequestHandler;

import java.io.IOException;

/**
 * <code>MODE &lt;SP&gt; <mode-code> &lt;CRLF&gt;</code><br>
 *
 * The argument is a single Telnet character code specifying
 * the data transfer modes described in the Section on
 * Transmission Modes.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class MODE implements Command {
    
    /**
     * Execute command
     */
    public void execute(RequestHandler handler,
                       FtpRequestImpl request, 
                       FtpWriter out) throws IOException {
        
        // reset state
        request.resetState();
        
        // argument check
        if(!request.hasArgument()) {
            out.send(501, "MODE", null);
            return;  
        }
        
        // set mode
        char md = request.getArgument().charAt(0);
        md = Character.toUpperCase(md);
        if(md == 'S') {
            request.getFtpDataConnection().setZipMode(false);
            out.send(200, "MODE", "S");
        }
        else if(md == 'Z') {
            request.getFtpDataConnection().setZipMode(true);
            out.send(200, "MODE", "Z");
        }
        else {
            out.send(504, "MODE", null);
        }
    }
}
