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
import org.apache.ftpserver.FtpWriter;
import org.apache.ftpserver.RequestHandler;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.interfaces.DataConnectionConfig;

/**
 * The EPRT command allows for the specification of an extended address
 * for the data connection.  The extended address MUST consist of the
 * network protocol as well as the network and transport addresses.  The
 * format of EPRT is:
 *
 * EPRT<space><d><net-prt><d><net-addr><d><tcp-port><d>
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class EPRT extends AbstractCommand {

    /**
     * Execute command.
     */
    public void execute(RequestHandler handler,
                        FtpRequest request, 
                        FtpSessionImpl session, 
                        FtpWriter out) throws IOException {
        
        // reset state variables
        session.resetState();
        
        // argument check
        String arg = request.getArgument();
        if(arg == null) {
            out.send(501, "EPRT", null);
            return;  
        }
        
        // is port enabled
        DataConnectionConfig dataCfg = handler.getServerContext().getDataConnectionConfig();
        if(!dataCfg.isActiveEnabled()) {
            out.send(510, "EPRT.disabled", null);
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
            out.send(510, "EPRT", null);
            return;
        }
        
        // get data server
        InetAddress dataAddr = null;
        try {
            dataAddr = InetAddress.getByName(host);
        }
        catch(UnknownHostException ex) {
            log.debug("Unknown host: " + host, ex);
            out.send(553, "EPRT.host", null);
            return;
        }
        
        // check IP
        if(dataCfg.isActiveIpCheck()) {
            InetAddress clientAddr = session.getRemoteAddress();
            if(!dataAddr.equals(clientAddr)) {
                out.send(510, "EPRT.mismatch", null);
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
            out.send(552, "EPRT.invalid", null); 
            return; 
        }
        
        session.getFtpDataConnection().setPortCommand(dataAddr, dataPort);
        out.send(200, "EPRT", null);
    }
}
