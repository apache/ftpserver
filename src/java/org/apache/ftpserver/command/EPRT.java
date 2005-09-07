// $Id$
/*
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ftpserver.command;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.ftpserver.Command;
import org.apache.ftpserver.FtpRequestImpl;
import org.apache.ftpserver.FtpWriter;
import org.apache.ftpserver.RequestHandler;

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
class EPRT implements Command {

    /**
     * Execute command.
     */
    public void execute(RequestHandler handler,
                        FtpRequestImpl request, 
                        FtpWriter out) throws IOException {
        
        // reset state variables
        request.resetState();
        
        // argument check
        String arg = request.getArgument();
        if(arg == null) {
            out.send(501, "EPRT", null);
            return;  
        }
        
        // is port enabled
        if(!handler.getConfig().getDataConnectionConfig().isPortEnabled()) {
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
            out.send(510, "EPRT", null);
            return;
        }
        
        // get data server
        InetAddress dataAddr = null;
        try {
            dataAddr = InetAddress.getByName(host);
        }
        catch(UnknownHostException ex) {
            out.send(553, "EPRT.host.unknown", null);
            return;
        }
        
        // check IP
        if(handler.getConfig().getDataConnectionConfig().isPortIpCheck()) {
            InetAddress clientAddr = handler.getRequest().getRemoteAddress();
            if(!dataAddr.equals(clientAddr)) {
                out.send(510, "EPRT.IP.mismatch", null);
                return;
            }
        }
        
        // get data server port
        int dataPort = 0;
        try {
            dataPort = Integer.parseInt(port);     
        }
        catch(NumberFormatException ex) {
            out.send(552, "EPRT.number.valid", null); 
            return; 
        }
        
        request.getFtpDataConnection().setPortCommand(dataAddr, dataPort);
        out.send(200, "EPRT", null);
    }
}
