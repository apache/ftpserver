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
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;

/**
 * <code>PWD  &lt;CRLF&gt;</code><br>
 *
 * This command causes the name of the current working
 * directory to be returned in the reply.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class PWD implements Command {

    /**
     * Execute command
     */
    public void execute(RequestHandler handler, 
                        FtpRequestImpl request, 
                        FtpWriter out) throws IOException, FtpException {
        request.resetState();
        FileSystemView fsview = request.getFileSystemView();
        String currDir = fsview.getCurrentDirectory().getFullName();
        out.send(257, "PWD", currDir);
    }
    
}
