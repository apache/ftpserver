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
import org.apache.ftpserver.listing.DirectoryLister;
import org.apache.ftpserver.listing.FileFormater;
import org.apache.ftpserver.listing.LISTFileFormater;
import org.apache.ftpserver.listing.ListArgument;
import org.apache.ftpserver.listing.ListArgumentParser;
import org.apache.ftpserver.listing.NLSTFileFormater;
import org.apache.ftpserver.util.IoUtils;

/**
 * <code>NLST [&lt;SP&gt; &lt;pathname&gt;] &lt;CRLF&gt;</code><br>
 *
 * This command causes a directory listing to be sent from
 * server to user site.  The pathname should specify a
 * directory or other system-specific file group descriptor; a
 * null argument implies the current directory.  The server
 * will return a stream of names of files and no other
 * information.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class NLST extends AbstractCommand {

    private static final NLSTFileFormater NLST_FILE_FORMATER = new NLSTFileFormater();
    private static final LISTFileFormater LIST_FILE_FORMATER = new LISTFileFormater();
    private DirectoryLister directoryLister = new DirectoryLister();
    
    /**
     * Execute command
     */
    public void execute(RequestHandler handler, 
                        FtpRequestImpl request, 
                        FtpWriter out) throws IOException, FtpException {
        
        try {
            
            // reset state
            request.resetState();
            
            // get data connection
            out.send(150, "NLST", null);
            OutputStream os = null;
            try {
                os = request.getDataOutputStream();
            }
            catch(IOException ex) {
                log.debug("Exception getting the output data stream", ex);
                out.send(425, "NLST", null);
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
                
                FileFormater formater;
                if(parsedArg.hasOption('l')) {
                    formater = LIST_FILE_FORMATER;
                } else {
                    formater = NLST_FILE_FORMATER;
                }
                
                writer.write(directoryLister.listFiles(parsedArg, request.getFileSystemView(), formater));
            }
            catch(SocketException ex) {
                log.debug("Socket exception during data transfer", ex);
                failure = true;
                out.send(426, "NLST", null);
            }
            catch(IOException ex) {
                log.debug("IOException during data transfer", ex);
                failure = true;
                out.send(551, "NLST", null);
            } catch(IllegalArgumentException e) {
                log.debug("Illegal listing syntax: " + request.getArgument(), e);
                // if listing syntax error - send message
                out.send(501, "LIST", null);
            } finally {
                writer.flush();
                IoUtils.close(writer);
            }
            
            // if data transfer ok - send transfer complete message
            if(!failure) {
                out.send(226, "NLST", null);
            }
        }
        finally {
            request.getFtpDataConnection().closeDataSocket();
        }
    }
}
