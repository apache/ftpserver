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
import org.apache.ftpserver.ftplet.FileObject;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpReplyOutput;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.ftplet.FtpReply;
import org.apache.ftpserver.listener.Connection;
import org.apache.ftpserver.util.FtpReplyUtil;

/**
 * <code>SIZE &lt;SP&gt; &lt;pathname&gt; &lt;CRLF&gt;</code><br>
 *
 * Returns the size of the file in bytes.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class SIZE extends AbstractCommand {
    
    /**
     * Execute command.
     */
    public void execute(Connection connection,
                        FtpRequest request,
                        FtpSessionImpl session, 
                        FtpReplyOutput out) throws IOException, FtpException {
        
        // reset state variables
        session.resetState();
        
        // argument check
        String fileName = request.getArgument();
        if(fileName == null) {
            out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS, "SIZE", null));
            return;  
        }
        
        // get file object
        FileObject file = null;
        try {
            file = session.getFileSystemView().getFileObject(fileName);
        }
        catch(Exception ex) {
            log.debug("Exception getting file object", ex);
        }
        if(file == null) {
            out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_550_REQUESTED_ACTION_NOT_TAKEN, "SIZE.missing", fileName));
            return;
        }
        
        // print file size
        fileName = file.getFullName();
        if(!file.doesExist()) {
            out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_550_REQUESTED_ACTION_NOT_TAKEN, "SIZE.missing", fileName));
        }
        else if(!file.isFile()) {
            out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_550_REQUESTED_ACTION_NOT_TAKEN, "SIZE.invalid", fileName));
        }
        else {
            String fileLen = String.valueOf(file.getSize());             
            out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_213_FILE_STATUS, "SIZE", fileLen));
        }
    } 

}
