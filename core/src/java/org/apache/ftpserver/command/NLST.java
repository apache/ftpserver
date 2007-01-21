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
import java.net.SocketException;

import org.apache.ftpserver.FtpDataConnection;
import org.apache.ftpserver.FtpSessionImpl;
import org.apache.ftpserver.FtpWriter;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.ftplet.FtpResponse;
import org.apache.ftpserver.listener.Connection;
import org.apache.ftpserver.listing.DirectoryLister;
import org.apache.ftpserver.listing.FileFormater;
import org.apache.ftpserver.listing.LISTFileFormater;
import org.apache.ftpserver.listing.ListArgument;
import org.apache.ftpserver.listing.ListArgumentParser;
import org.apache.ftpserver.listing.NLSTFileFormater;
import org.apache.ftpserver.util.FtpReplyUtil;

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
    public void execute(Connection connection, 
                        FtpRequest request,
                        FtpSessionImpl session, 
                        FtpWriter out) throws IOException, FtpException {
        
        try {
            
            // reset state
            session.resetState();
            
            // get data connection
            out.write(FtpReplyUtil.translate(session, FtpResponse.REPLY_150_FILE_STATUS_OKAY, "NLST", null));

            
            // print listing data
            FtpDataConnection dataConnection;
            try {
                dataConnection = session.getFtpDataConnection().openConnection();
            } catch (Exception e) {
                log.debug("Exception getting the output data stream", e);
                out.write(FtpReplyUtil.translate(session, FtpResponse.REPLY_425_CANT_OPEN_DATA_CONNECTION, "NLST", null));
                return;
            }
            
            boolean failure = false;
            try {
                // parse argument
                ListArgument parsedArg = ListArgumentParser.parse(request.getArgument());
                
                FileFormater formater;
                if(parsedArg.hasOption('l')) {
                    formater = LIST_FILE_FORMATER;
                } else {
                    formater = NLST_FILE_FORMATER;
                }
                
                dataConnection.transferToClient(directoryLister.listFiles(parsedArg, session.getFileSystemView(), formater));
            }
            catch(SocketException ex) {
                log.debug("Socket exception during data transfer", ex);
                failure = true;
                out.write(FtpReplyUtil.translate(session, FtpResponse.REPLY_426_CONNECTION_CLOSED_TRANSFER_ABORTED, "NLST", null));
            }
            catch(IOException ex) {
                log.debug("IOException during data transfer", ex);
                failure = true;
                out.write(FtpReplyUtil.translate(session, FtpResponse.REPLY_551_REQUESTED_ACTION_ABORTED_PAGE_TYPE_UNKNOWN, "NLST", null));
            } catch(IllegalArgumentException e) {
                log.debug("Illegal listing syntax: " + request.getArgument(), e);
                // if listing syntax error - send message
                out.write(FtpReplyUtil.translate(session, FtpResponse.REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS, "LIST", null));
            }
            
            // if data transfer ok - send transfer complete message
            if(!failure) {
                out.write(FtpReplyUtil.translate(session, FtpResponse.REPLY_226_CLOSING_DATA_CONNECTION, "NLST", null));
            }
        }
        finally {
            session.getFtpDataConnection().closeDataSocket();
        }
    }
}
