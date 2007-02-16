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

import org.apache.ftpserver.FtpSessionImpl;
import org.apache.ftpserver.ftplet.FtpReply;
import org.apache.ftpserver.ftplet.FtpReplyOutput;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.interfaces.DataConnectionConfig;
import org.apache.ftpserver.listener.Connection;
import org.apache.ftpserver.util.FtpReplyUtil;

/**
 * The EPRT command allows for the specification of an extended address
 * for the data connection.  The extended address MUST consist of the
 * network protocol as well as the network and transport addresses.  The
 * format of EPRT is:
 *
 * EPRT<space><d><net-prt><d><net-addr><d><tcp-port><d>
 */
public 
class EPRT extends AbstractCommand {

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
        String arg = request.getArgument();
        if(arg == null) {
            out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS, "EPRT", null));
            return;  
        }
        
        // is port enabled
        DataConnectionConfig dataCfg = session.getListener().getDataConnectionConfig();
        if(!dataCfg.isActiveEnabled()) {
            out.write(FtpReplyUtil.translate(session, 510, "EPRT.disabled", null));
            return;
        }
        
        // parse argument
        String host = null;
        String port = null;
        try {
            char delim = arg.charAt(0);
            int lastDelimIdx = arg.indexOf(delim, 3);
            host = arg.substring(3, lastDelimIdx);
            port = arg.substring(lastDelimIdx+1, arg.length() - 1);
        }
        catch(Exception ex) {
            log.debug("Exception parsing host and port: " + arg, ex);
            out.write(FtpReplyUtil.translate(session, 510, "EPRT", null));
            return;
        }
        
        // get data server
        InetAddress dataAddr = null;
        try {
            dataAddr = InetAddress.getByName(host);
        }
        catch(UnknownHostException ex) {
            log.debug("Unknown host: " + host, ex);
            out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_553_REQUESTED_ACTION_NOT_TAKEN_FILE_NAME_NOT_ALLOWED, "EPRT.host", null));
            return;
        }
        
        // check IP
        if(dataCfg.isActiveIpCheck()) {
            InetAddress clientAddr = session.getClientAddress();
            if(!dataAddr.equals(clientAddr)) {
                out.write(FtpReplyUtil.translate(session, 510, "EPRT.mismatch", null));
                return;
            }
        }
        
        // get data server port
        int dataPort = 0;
        try {
            dataPort = Integer.parseInt(port);     
        }
        catch(NumberFormatException ex) {
            log.debug("Invalid port: " + port, ex);
            out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_552_REQUESTED_FILE_ACTION_ABORTED_EXCEEDED_STORAGE, "EPRT.invalid", null)); 
            return; 
        }
        
        session.getFtpDataConnection().setPortCommand(dataAddr, dataPort);
        out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_200_COMMAND_OKAY, "EPRT", null));
    }
}
