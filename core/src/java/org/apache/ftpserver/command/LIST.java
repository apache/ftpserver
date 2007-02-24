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
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpReply;
import org.apache.ftpserver.ftplet.FtpReplyOutput;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.listener.Connection;
import org.apache.ftpserver.listing.DirectoryLister;
import org.apache.ftpserver.listing.LISTFileFormater;
import org.apache.ftpserver.listing.ListArgument;
import org.apache.ftpserver.listing.ListArgumentParser;
import org.apache.ftpserver.util.FtpReplyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 */
public 
class LIST extends AbstractCommand {
    
    private static final Logger LOG = LoggerFactory.getLogger(LIST.class);
    
    private static final LISTFileFormater LIST_FILE_FORMATER = new LISTFileFormater();
    private DirectoryLister directoryLister = new DirectoryLister();
    
    /**
     * Execute command.
     */
    public void execute(Connection connection,
                        FtpRequest request, 
                        FtpSessionImpl session, 
                        FtpReplyOutput out) throws IOException, FtpException {
        
        try {
        
            // reset state variables
            session.resetState();
            
            // get data connection
            out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_150_FILE_STATUS_OKAY, "LIST", null));

            FtpDataConnection dataConnection;
            try {
                dataConnection = session.getFtpDataConnection().openConnection();
            } catch (Exception e) {
                LOG.debug("Exception getting the output data stream", e);
                out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_425_CANT_OPEN_DATA_CONNECTION, "LIST", null));
                return;
            }
            
            
            // transfer listing data
            boolean failure = false;
            
            try {
                // parse argument
                ListArgument parsedArg = ListArgumentParser.parse(request.getArgument());
                
                dataConnection.transferToClient(directoryLister.listFiles(parsedArg, session.getFileSystemView(), LIST_FILE_FORMATER));
            }
            catch(SocketException ex) {
                LOG.debug("Socket exception during list transfer", ex);
                failure = true;
                out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_426_CONNECTION_CLOSED_TRANSFER_ABORTED, "LIST", null));
            }
            catch(IOException ex) {
                LOG.debug("IOException during list transfer", ex);
                failure = true;
                out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_551_REQUESTED_ACTION_ABORTED_PAGE_TYPE_UNKNOWN, "LIST", null));
            } catch(IllegalArgumentException e) {
                LOG.debug("Illegal list syntax: " + request.getArgument(), e);
                // if listing syntax error - send message
                out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS, "LIST", null));
            }
            
            // if data transfer ok - send transfer complete message
            if(!failure) {
                out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_226_CLOSING_DATA_CONNECTION, "LIST", null));
            }
        }
        finally {
            session.getFtpDataConnection().closeDataSocket();
        }
    }

}
