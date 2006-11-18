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
import org.apache.ftpserver.interfaces.Command;

/**
 * <code>SYST &lt;CRLF&gt;</code><br> 
 *
 * This command is used to find out the type of operating
 * system at the server.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class SYST implements Command {
    
    
    /**
     * Execute command
     */
    public void execute(RequestHandler handler,
                        FtpRequestImpl request, 
                        FtpWriter out) throws IOException {
        
        // reset state variables
        request.resetState();
        
        // get server system info 
        String systemName = System.getProperty("os.name");
        if(systemName == null) {
            systemName = "UNKNOWN";
        }
        else {
            systemName = systemName.toUpperCase();
            systemName = systemName.replace(' ', '-');
        }
        // print server system info
        out.send(215, "SYST", systemName);
    }

}
