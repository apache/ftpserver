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
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.StringTokenizer;

import org.apache.ftpserver.FtpSessionImpl;
import org.apache.ftpserver.ftplet.FtpReplyOutput;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.ftplet.FtpReply;
import org.apache.ftpserver.interfaces.DataConnectionConfig;
import org.apache.ftpserver.listener.Connection;
import org.apache.ftpserver.util.FtpReplyUtil;

/**
 * <code>PORT &lt;SP&gt; <host-port> &lt;CRLF&gt;</code><br>
 *
 * The argument is a HOST-PORT specification for the data port
 * to be used in data connection.  There are defaults for both
 * the user and server data ports, and under normal
 * circumstances this command and its reply are not needed.  If
 * this command is used, the argument is the concatenation of a
 * 32-bit internet host address and a 16-bit TCP port address.
 * This address information is broken into 8-bit fields and the
 * value of each field is transmitted as a decimal number (in
 * character string representation).  The fields are separated
 * by commas.  A port command would be:
 *
 *   PORT h1,h2,h3,h4,p1,p2
 * 
 * where h1 is the high order 8 bits of the internet host address.
 */
public 
class PORT extends AbstractCommand {

    /**
     * Execute command.
     */
    public void execute(Connection connection,
                        FtpRequest request,
                        FtpSessionImpl session, 
                        FtpReplyOutput out) throws IOException {
        
        // reset state variables
        session.resetState();
        
        // argument check
        if(!request.hasArgument()) {
            out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS, "PORT", null));
            return;  
        }
        
        StringTokenizer st = new StringTokenizer(request.getArgument(), ",");
        if(st.countTokens() != 6) {
            out.write(FtpReplyUtil.translate(session, 510, "PORT", null));
            return;
        }
        
        // is port enabled
        DataConnectionConfig dataCfg = connection.getServerContext().getDataConnectionConfig();
        if(!dataCfg.isActiveEnabled()) {
            out.write(FtpReplyUtil.translate(session, 510, "PORT.disabled", null));
            return;
        } 
        
        // get data server
        String dataSrvName = st.nextToken() + '.' + st.nextToken() + '.' +
        st.nextToken() + '.' + st.nextToken();
        InetAddress dataAddr = null;
        try {
            dataAddr = InetAddress.getByName(dataSrvName);
        }
        catch(UnknownHostException ex) {
            log.debug("Unknown host: " + dataSrvName, ex);
            out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_553_REQUESTED_ACTION_NOT_TAKEN_FILE_NAME_NOT_ALLOWED, "PORT.host", null));
            return;
        }
        
        // check IP
        if(dataCfg.isActiveIpCheck()) {
            InetAddress clientAddr = session.getClientAddress();
            if(!dataAddr.equals(clientAddr)) {
                out.write(FtpReplyUtil.translate(session, 510, "PORT.mismatch", null));
                return;
            }
        }
        
        // get data server port
        int dataPort = 0;
        try {
            int hi = Integer.parseInt(st.nextToken());
            int lo = Integer.parseInt(st.nextToken());
            dataPort = (hi << 8) | lo;     
        }
        catch(NumberFormatException ex) {
            log.debug("Invalid data port: " + request.getArgument(), ex);
            out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_552_REQUESTED_FILE_ACTION_ABORTED_EXCEEDED_STORAGE, "PORT.invalid", null)); 
            return; 
        }
        
        session.getFtpDataConnection().setPortCommand(dataAddr, dataPort);
        out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_200_COMMAND_OKAY, "PORT", null));
    }
    
}
