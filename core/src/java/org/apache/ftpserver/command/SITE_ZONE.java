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
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.ftpserver.FtpSessionImpl;
import org.apache.ftpserver.FtpWriter;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.listener.Connection;

/**
 * Displays the FTP server timezone in RFC 822 format.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class SITE_ZONE extends AbstractCommand {

    private final static SimpleDateFormat TIMEZONE_FMT = new SimpleDateFormat("Z"); 

    /**
     * Execute command.
     */
    public void execute(Connection connection,
            FtpRequest request,
            FtpSessionImpl session, 
            FtpWriter out) throws IOException, FtpException {
  
        // reset state variables
        session.resetState();
        
        // send timezone data
        String timezone = TIMEZONE_FMT.format(new Date());
        out.write(200, timezone);
    }
}
