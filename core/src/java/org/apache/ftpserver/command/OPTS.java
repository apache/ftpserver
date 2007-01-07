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
import java.util.HashMap;

import org.apache.ftpserver.FtpSessionImpl;
import org.apache.ftpserver.FtpWriter;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.interfaces.Command;
import org.apache.ftpserver.interfaces.Connection;


/**
 * <code>OPTS&lt;SP&gt; <commandt> &lt;SP&gt; <option> &lt;CRLF&gt;</code><br>
 *
 * This command shall cause the server use optional features for the 
 * command specified.
 *
 * @author Birkir A. Barkarson
 */
public 
class OPTS extends AbstractCommand {

    private static final HashMap COMMAND_MAP = new HashMap(16);
    
    
    /**
     * Execute command.
     */
    public void execute(Connection connection,
                        FtpRequest request,
                        FtpSessionImpl session, 
                        FtpWriter out) throws IOException, FtpException {
        
        // reset state
        session.resetState();
        
        // no params
        String argument = request.getArgument();
        if(argument == null) {
            out.send(501, "OPTS", null);
            return;
        }
        
        // get request name
        int spaceIndex = argument.indexOf(' ');
        if(spaceIndex != -1) {
            argument = argument.substring(0, spaceIndex);
        }
        argument = argument.toUpperCase();
        
        // call appropriate command method
        String optsRequest = "OPTS_" + argument; 
        Command command = (Command)COMMAND_MAP.get( optsRequest );
        try {
            if(command != null) {
                command.execute(connection, request, session, out);
            }
            else {
                session.resetState();
                out.send(502, "OPTS.not.implemented", argument);
            }
        }
        catch(Exception ex) {
            log.warn("OPTS.execute()", ex);
            session.resetState();
            out.send(500, "OPTS", null);
        }
    }
    
    // initialize all the OPTS command handlers
    static {
        COMMAND_MAP.put("OPTS_MLST", new org.apache.ftpserver.command.OPTS_MLST());
        COMMAND_MAP.put("OPTS_UTF8", new org.apache.ftpserver.command.OPTS_UTF8());
    }
}
