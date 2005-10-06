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
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.ftpserver.Command;
import org.apache.ftpserver.FtpRequestImpl;
import org.apache.ftpserver.FtpWriter;
import org.apache.ftpserver.RequestHandler;
import org.apache.ftpserver.ftplet.FtpException;


/**
 * <code>OPTS&lt;SP&gt; <commandt> &lt;SP&gt; <option> &lt;CRLF&gt;</code><br>
 *
 * This command shall cause the server use optional features for the 
 * command specified.
 *
 * @author Birkir A. Barkarson
 */
public 
class OPTS implements Command {

    private static final HashMap COMMAND_MAP = new HashMap(16);
    
    
    /**
     * Execute command.
     */
    public void execute(RequestHandler handler,
                        FtpRequestImpl request, 
                        FtpWriter out) throws IOException, FtpException {
        
        // reset state
        request.resetState();
        
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
                command.execute(handler, request, out);
            }
            else {
                request.resetState();
                out.send(502, "OPTS.not.implemented", argument);
            }
        }
        catch(Exception ex) {
            Log log = handler.getConfig().getLogFactory().getInstance(getClass());
            log.warn("OPTS.execute()", ex);
            request.resetState();
            out.send(500, "OPTS", null);
        }
    }
    
    // initialize all the OPTS command handlers
    static {
        COMMAND_MAP.put("OPTS_MLST", new org.apache.ftpserver.command.OPTS_MLST());
        COMMAND_MAP.put("OPTS_UTF8", new org.apache.ftpserver.command.OPTS_UTF8());
    }
}
