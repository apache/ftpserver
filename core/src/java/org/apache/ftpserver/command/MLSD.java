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
import org.apache.ftpserver.listing.ListArgument;
import org.apache.ftpserver.listing.ListArgumentParser;
import org.apache.ftpserver.listing.MLSTFileFormater;
import org.apache.ftpserver.util.FtpReplyUtil;

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
class MLSD extends AbstractCommand {

    private DirectoryLister directoryLister = new DirectoryLister();
    
    /**
     * Execute command.
     */
    public void execute(Connection connection, 
                        FtpRequest request,
                        FtpSessionImpl session, 
                        FtpWriter out) throws IOException, FtpException {
        
        try {
            
            // reset state
            session.resetState();
            
            // get data connection
            out.write(FtpReplyUtil.translate(session, FtpResponse.REPLY_150_FILE_STATUS_OKAY, "MLSD", null));

            
            // print listing data
            FtpDataConnection dataConnection;
            try {
                dataConnection = session.getFtpDataConnection().openConnection();
            } catch (Exception e) {
                log.debug("Exception getting the output data stream", e);
                out.write(FtpReplyUtil.translate(session, FtpResponse.REPLY_425_CANT_OPEN_DATA_CONNECTION, "MLSD", null));
                return;
            }
            
            boolean failure = false;
            try {
                // parse argument
                ListArgument parsedArg = ListArgumentParser.parse(request.getArgument());
                
                FileFormater formater = new MLSTFileFormater((String[])session.getAttribute("MLST.types"));
                
                dataConnection.transferToClient(directoryLister.listFiles(parsedArg, session.getFileSystemView(), formater));
            }
            catch(SocketException ex) {
                log.debug("Socket exception during data transfer", ex);
                failure = true;
                out.write(FtpReplyUtil.translate(session, FtpResponse.REPLY_426_CONNECTION_CLOSED_TRANSFER_ABORTED, "MLSD", null));
            }
            catch(IOException ex) {
                log.debug("IOException during data transfer", ex);
                failure = true;
                out.write(FtpReplyUtil.translate(session, FtpResponse.REPLY_551_REQUESTED_ACTION_ABORTED_PAGE_TYPE_UNKNOWN, "MLSD", null));
            } catch(IllegalArgumentException e) {
                log.debug("Illegal listing syntax: " + request.getArgument(), e);
                // if listing syntax error - send message
                out.write(FtpReplyUtil.translate(session, FtpResponse.REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS, "MLSD", null));
            }
            
            // if data transfer ok - send transfer complete message
            if(!failure) {
                out.write(FtpReplyUtil.translate(session, FtpResponse.REPLY_226_CLOSING_DATA_CONNECTION, "MLSD", null));
            }
        }
        finally {
            session.getFtpDataConnection().closeDataSocket();
        }
    }
}
