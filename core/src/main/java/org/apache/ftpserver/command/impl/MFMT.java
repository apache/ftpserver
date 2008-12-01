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

package org.apache.ftpserver.command.impl;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.apache.ftpserver.command.AbstractCommand;
import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.ftpserver.ftplet.FtpReply;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.impl.FtpIoSession;
import org.apache.ftpserver.impl.FtpServerContext;
import org.apache.ftpserver.impl.LocalizedFtpReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command for changing the modified time of a file. 
 * <p>
 * Specified in the following document:
 * http://www.omz13.com/downloads/draft-somers-ftp-mfxx-00.html#anchor8
 * </p>
 * @author The Apache MINA Project (dev@mina.apache.org)
 * @version $Rev$, $Date$
 */
public class MFMT extends AbstractCommand {

    private final Logger LOG = LoggerFactory.getLogger(MFMT.class);

    /**
     * Execute command.
     */
    public void execute(final FtpIoSession session,
            final FtpServerContext context, final FtpRequest request)
            throws IOException {

        DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        
        // reset state variables
        session.resetState();

        String argument = request.getArgument();

        if (argument == null || argument.trim().length() == 0) {
            session
                    .write(LocalizedFtpReply
                            .translate(
                                    session,
                                    request,
                                    context,
                                    FtpReply.REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS,
                                    "MFMT.invalid", null));
            return;
        }
        
        String[] arguments = argument.split(" ");

        if(arguments.length != 2) {
            session
            .write(LocalizedFtpReply
                    .translate(
                            session,
                            request,
                            context,
                            FtpReply.REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS,
                            "MFMT.invalid", null));
            return;
        }
       
        String timestamp = arguments[0].trim();
        
        df.setLenient(false);
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        
        try {
            Date time = df.parse(timestamp);
            
            String fileName = arguments[1].trim();
            
            // get file object
            FtpFile file = null;
            
            try {
                file = session.getFileSystemView().getFile(fileName);
            } catch (Exception ex) {
                LOG.debug("Exception getting the file object: " + fileName, ex);
            }
            
            if (file == null || !file.doesExist()) {
                session
                .write(LocalizedFtpReply
                        .translate(
                                session,
                                request,
                                context,
                                FtpReply.REPLY_550_REQUESTED_ACTION_NOT_TAKEN,
                                "MFMT.filemissing", fileName));
                return;
            }
            
            // check file
            if (!file.isFile()) {
                session
                .write(LocalizedFtpReply
                        .translate(
                                session,
                                request,
                                context,
                                FtpReply.REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS,
                                "MFMT.invalid", null));
                return;
            }

            // all checks okay, lets go
            file.setLastModified(time.getTime());

            session
            .write(LocalizedFtpReply
                    .translate(
                            session,
                            request,
                            context,
                            FtpReply.REPLY_213_FILE_STATUS,
                            "MFMT", "ModifyTime=" + timestamp + " " + fileName));
            return;

        } catch (ParseException e) {
            session
            .write(LocalizedFtpReply
                    .translate(
                            session,
                            request,
                            context,
                            FtpReply.REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS,
                            "MFMT.invalid", null));
            return;
        }
        

    }
}
