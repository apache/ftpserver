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
import org.apache.ftpserver.ftplet.FileObject;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.listing.FileFormater;
import org.apache.ftpserver.listing.ListArgument;
import org.apache.ftpserver.listing.ListArgumentParser;
import org.apache.ftpserver.listing.MLSTFileFormater;

/**
 * <code>MLST &lt;SP&gt; &lt;pathname&gt; &lt;CRLF&gt;</code><br>
 *
 * Returns info on the file over the control connection.
 * 
 * @author Birkir A. Barkarson
 */
public 
class MLST extends AbstractCommand {

    /**
     * Execute command.
     */
    public void execute(RequestHandler handler,
                        FtpRequestImpl request, 
                        FtpWriter out) throws IOException {
        
        // reset state variables
        request.resetState();
        
//      parse argument
        ListArgument parsedArg = ListArgumentParser.parse(request.getArgument());
        
        FileObject file = null;
        try {
            file = request.getFileSystemView().getFileObject(parsedArg.getFile());
            if(file != null && file.doesExist()) {
                FileFormater formater = new MLSTFileFormater((String[])handler.getAttribute("MLST.types"));
                out.send(250, "MLST", formater.format(file));
            } else {            
                out.send(501, "MLST", null);
            }
        }
        catch(FtpException ex) {
            log.debug("Exception sending the file listing", ex);
            out.send(501, "MLST", null);
        }     
    }   
}
