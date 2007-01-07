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

import org.apache.ftpserver.FtpSessionImpl;
import org.apache.ftpserver.FtpWriter;
import org.apache.ftpserver.ftplet.FileObject;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.listener.Connection;

/**
 * <code>RNFR &lt;SP&gt; &lt;pathname&gt; &lt;CRLF&gt;</code><br>
 *
 * This command specifies the old pathname of the file which is
 * to be renamed.  This command must be immediately followed by
 * a "rename to" command specifying the new file pathname.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class RNFR extends AbstractCommand {

    /**
     * Execute command
     */
    public void execute(Connection connection,
                        FtpRequest request,
                        FtpSessionImpl session, 
                        FtpWriter out) throws IOException, FtpException {
        
        // reset state variable
        session.resetState();
        
        // argument check
        String fileName = request.getArgument();
        if(fileName == null) {
            out.send(501, "RNFR", null);
            return;  
        }
                
        // get filename
        FileObject renFr = null;
        try {
            renFr = session.getFileSystemView().getFileObject(fileName);
        }
        catch(Exception ex) {
            log.debug("Exception getting file object", ex);
        }
            
        // check file
        if(renFr == null) {
            out.send(550, "RNFR", fileName);
        }
        else {
            session.setRenameFrom(renFr);
            fileName = renFr.getFullName();
            out.send(350, "RNFR", fileName);    
        }
    }
    
}
