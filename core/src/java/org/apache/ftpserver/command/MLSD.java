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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.SocketException;

import org.apache.ftpserver.FtpRequestImpl;
import org.apache.ftpserver.FtpWriter;
import org.apache.ftpserver.RequestHandler;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.interfaces.Command;
import org.apache.ftpserver.listing.DirectoryLister;
import org.apache.ftpserver.listing.FileFormater;
import org.apache.ftpserver.listing.ListArgument;
import org.apache.ftpserver.listing.ListArgumentParser;
import org.apache.ftpserver.listing.MLSTFileFormater;
import org.apache.ftpserver.util.IoUtils;

/**
 * <code>MLSD [&lt;SP&gt; &lt;pathname&gt;] &lt;CRLF&gt;</code><br>
 *
 * This command causes a list to be sent from the server to the
 * passive DTP.  The pathname must specify a directory and the
 * server should transfer a list of files in the specified directory.
 * A null argument implies the user's current working or  default directory.
 * The data transfer is over the data connection
 * 
 * @author Birkir A. Barkarson
 */
public 
class MLSD implements Command {

    private DirectoryLister directoryLister = new DirectoryLister();
    
    /**
     * Execute command.
     */
    public void execute(RequestHandler handler, 
                        FtpRequestImpl request, 
                        FtpWriter out) throws IOException, FtpException {
        
        try {
            
            // reset state
            request.resetState();
            
            // get data connection
            out.send(150, "MLSD", null);
            OutputStream os = null;
            try {
                os = request.getDataOutputStream();
            }
            catch(IOException ex) {
                out.send(425, "MLSD", null);
                return;
            }
            
            // print listing data
            boolean failure = false;
            Writer writer = null;
            try {
                
                // open stream
                writer = new OutputStreamWriter(os, "UTF-8");
                
                // parse argument
                ListArgument parsedArg = ListArgumentParser.parse(request.getArgument());
                
                FileFormater formater = new MLSTFileFormater((String[])handler.getAttribute("MLST.types"));
                writer.write(directoryLister.listFiles(parsedArg, request.getFileSystemView(), formater));
            }
            catch(SocketException ex) {
                failure = true;
                out.send(426, "MLSD", null);
            }
            catch(IOException ex) {
                failure = true;
                out.send(551, "MLSD", null);
            } catch(IllegalArgumentException e) {
                // if listing syntax error - send message
                out.send(501, "MLSD", null);
            }
            finally {
                writer.flush();
                IoUtils.close(writer);
            }
            
            // if data transfer ok - send transfer complete message
            if(!failure) {
                out.send(226, "MLSD", null);
            }
        }
        finally {
            request.getFtpDataConnection().closeDataSocket();
        }
    }
}
