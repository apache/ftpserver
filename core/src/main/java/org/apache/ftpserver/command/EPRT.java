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
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import org.apache.ftpserver.ftplet.FtpReply;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.interfaces.DataConnectionConfig;
import org.apache.ftpserver.interfaces.FtpIoSession;
import org.apache.ftpserver.interfaces.FtpServerContext;
import org.apache.ftpserver.util.FtpReplyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private final Logger LOG = LoggerFactory.getLogger(EPRT.class);
    
    /**
     * Execute command.
     */
    public void execute(FtpIoSession session,
                        FtpServerContext context, 
                        FtpRequest request) throws IOException {
        
        // reset state variables
        session.resetState();
        
        // argument check
        String arg = request.getArgument();
        if(arg == null) {
            session.write(FtpReplyUtil.translate(session, request, context, FtpReply.REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS, "EPRT", null));
            return;  
        }
        
        // is port enabled
        DataConnectionConfig dataCfg = session.getListener().getDataConnectionConfig();
        if(!dataCfg.isActiveEnabled()) {
            session.write(FtpReplyUtil.translate(session, request, context, 510, "EPRT.disabled", null));
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
            LOG.debug("Exception parsing host and port: " + arg, ex);
            session.write(FtpReplyUtil.translate(session, request, context, 510, "EPRT", null));
            return;
        }
        
        // get data server
        InetAddress dataAddr = null;
        try {
            dataAddr = InetAddress.getByName(host);
        }
        catch(UnknownHostException ex) {
            LOG.debug("Unknown host: " + host, ex);
            session.write(FtpReplyUtil.translate(session, request, context, FtpReply.REPLY_553_REQUESTED_ACTION_NOT_TAKEN_FILE_NAME_NOT_ALLOWED, "EPRT.host", null));
            return;
        }
        
        // check IP
        if(dataCfg.isActiveIpCheck()) {
        	if(session.getRemoteAddress() instanceof InetSocketAddress) {
	            InetAddress clientAddr = ((InetSocketAddress)session.getRemoteAddress()).getAddress();
	            if(!dataAddr.equals(clientAddr)) {
	                session.write(FtpReplyUtil.translate(session, request, context, 510, "EPRT.mismatch", null));
	                return;
	            }
        	}
        }
        
        // get data server port
        int dataPort = 0;
        try {
            dataPort = Integer.parseInt(port);     
        }
        catch(NumberFormatException ex) {
            LOG.debug("Invalid port: " + port, ex);
            session.write(FtpReplyUtil.translate(session, request, context, FtpReply.REPLY_552_REQUESTED_FILE_ACTION_ABORTED_EXCEEDED_STORAGE, "EPRT.invalid", null)); 
            return; 
        }
        
        session.getDataConnection().initActiveDataConnection(new InetSocketAddress(dataAddr, dataPort));
        session.write(FtpReplyUtil.translate(session, request, context, FtpReply.REPLY_200_COMMAND_OKAY, "EPRT", null));
    }
}
