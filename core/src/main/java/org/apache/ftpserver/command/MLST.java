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

import org.apache.ftpserver.ftplet.FileObject;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpReply;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.interfaces.FtpIoSession;
import org.apache.ftpserver.interfaces.FtpServerContext;
import org.apache.ftpserver.listing.FileFormater;
import org.apache.ftpserver.listing.ListArgument;
import org.apache.ftpserver.listing.ListArgumentParser;
import org.apache.ftpserver.listing.MLSTFileFormater;
import org.apache.ftpserver.util.FtpReplyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <code>MLST &lt;SP&gt; &lt;pathname&gt; &lt;CRLF&gt;</code><br>
 *
 * Returns info on the file over the control connection.
 */
public 
class MLST extends AbstractCommand {

    private final Logger LOG = LoggerFactory.getLogger(MLST.class);
    
    /**
     * Execute command.
     */
    public void execute(final FtpIoSession session,
            final FtpServerContext context,
            final FtpRequest request) throws IOException {
        
        // reset state variables
        session.resetState();
        
//      parse argument
        ListArgument parsedArg = ListArgumentParser.parse(request.getArgument());
        
        FileObject file = null;
        try {
            file = session.getFileSystemView().getFileObject(parsedArg.getFile());
            if(file != null && file.doesExist()) {
                FileFormater formater = new MLSTFileFormater((String[])session.getAttribute("MLST.types"));
                session.write(FtpReplyUtil.translate(session, request, context, FtpReply.REPLY_250_REQUESTED_FILE_ACTION_OKAY, "MLST", formater.format(file)));
            } else {            
                session.write(FtpReplyUtil.translate(session, request, context, FtpReply.REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS, "MLST", null));
            }
        }
        catch(FtpException ex) {
            LOG.debug("Exception sending the file listing", ex);
            session.write(FtpReplyUtil.translate(session, request, context, FtpReply.REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS, "MLST", null));
        }     
    }   
}
