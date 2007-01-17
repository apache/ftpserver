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

import org.apache.ftpserver.FtpSessionImpl;
import org.apache.ftpserver.FtpWriter;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.ftplet.FtpResponse;
import org.apache.ftpserver.listener.Connection;
import org.apache.ftpserver.listing.DirectoryLister;
import org.apache.ftpserver.listing.LISTFileFormater;
import org.apache.ftpserver.listing.ListArgument;
import org.apache.ftpserver.listing.ListArgumentParser;
import org.apache.ftpserver.util.IoUtils;

/**
 * <code>LIST [&lt;SP&gt; &lt;pathname&gt;] &lt;CRLF&gt;</code><br>
 *
 * This command causes a list to be sent from the server to the
 * passive DTP.  If the pathname specifies a directory or other
 * group of files, the server should transfer a list of files
 * in the specified directory.  If the pathname specifies a
 * file then the server should send current information on the
 * file.  A null argument implies the user's current working or
 * default directory.  The data transfer is over the data
 * connection.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class LIST extends AbstractCommand {
    
    private static final LISTFileFormater LIST_FILE_FORMATER = new LISTFileFormater();
    private DirectoryLister directoryLister = new DirectoryLister();
    
    /**
     * Execute command.
     */
    public void execute(Connection connection,
                        FtpRequest request, 
                        FtpSessionImpl session, 
                        FtpWriter out) throws IOException, FtpException {
        
        try {
        
            // reset state variables
            session.resetState();
            
            // get data connection
            out.send(FtpResponse.REPLY_150_FILE_STATUS_OKAY, "LIST", null);
            OutputStream os = null;
            try {
                os = session.getDataOutputStream();
            }
            catch(IOException ex) {
                log.debug("Exception getting the output data stream", ex);
                out.send(FtpResponse.REPLY_425_CANT_OPEN_DATA_CONNECTION, "LIST", null);
                return;
            }
            
            // transfer listing data
            boolean failure = false;
            Writer writer = null;
            try {
            
                // open stream
                writer = new OutputStreamWriter(os, "UTF-8");
                
                // parse argument
                ListArgument parsedArg = ListArgumentParser.parse(request.getArgument());
                
                writer.write(directoryLister.listFiles(parsedArg, session.getFileSystemView(), LIST_FILE_FORMATER));
            }
            catch(SocketException ex) {
                log.debug("Socket exception during list transfer", ex);
                failure = true;
                out.send(FtpResponse.REPLY_426_CONNECTION_CLOSED_TRANSFER_ABORTED, "LIST", null);
            }
            catch(IOException ex) {
                log.debug("IOException during list transfer", ex);
                failure = true;
                out.send(FtpResponse.REPLY_551_REQUESTED_ACTION_ABORTED_PAGE_TYPE_UNKNOWN, "LIST", null);
            } catch(IllegalArgumentException e) {
                log.debug("Illegal list syntax: " + request.getArgument(), e);
                // if listing syntax error - send message
                out.send(FtpResponse.REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS, "LIST", null);
            } finally {
                writer.flush();
                IoUtils.close(writer);
            }
            
            // if data transfer ok - send transfer complete message
            if(!failure) {
                out.send(FtpResponse.REPLY_226_CLOSING_DATA_CONNECTION, "LIST", null);
            }
        }
        finally {
            session.getFtpDataConnection().closeDataSocket();
        }
    }

}
