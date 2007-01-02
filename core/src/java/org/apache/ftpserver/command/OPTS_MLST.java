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
import java.util.StringTokenizer;

import org.apache.ftpserver.FtpSessionImpl;
import org.apache.ftpserver.FtpWriter;
import org.apache.ftpserver.RequestHandler;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpRequest;

/**
 * Client-Server listing negotation.
 * Instruct the server what listing types to include in
 * machine directory/file listings.
 * 
 * @author Birkir A. Barkarson
 */
public 
class OPTS_MLST extends AbstractCommand {
    
    private final static String[] AVAILABLE_TYPES = {
        "Size",
        "Modify",
        "Type",
        "Perm"
    };
    
    /**
     * Execute command.
     */
    public void execute(RequestHandler handler,
                        FtpRequest request, 
                        FtpSessionImpl session, 
                        FtpWriter out) throws IOException, FtpException {
        
        // reset state
        session.resetState();
        
        // get the listing types
        String argument = request.getArgument();
        int spIndex = argument.indexOf(' ');
        if(spIndex == -1) {
            out.send(503, "OPTS.MLST", null);
            return;
        }
        String listTypes = argument.substring(spIndex + 1);
        
        // parse all the type tokens
        StringTokenizer st = new StringTokenizer(listTypes, ";");
        String types[] = new String[st.countTokens()];
        for(int i=0; i<types.length; ++i) {
            types[i] = st.nextToken();
        }
        
        // set the list types
        String[] validatedTypes = validateSelectedTypes(types);
        if(validatedTypes != null) {
            session.setAttribute("MLST.types", validatedTypes);
            out.send(200, "OPTS.MLST", listTypes);
        }
        else {
            out.send(501, "OPTS.MLST", listTypes);
        }
    }
    
    private String[] validateSelectedTypes(String types[]) {
        
        // ignore null types
        if(types == null) {
            return null;
        }
        
        // check all the types
        for(int i=0; i<types.length; ++i) {
            boolean bMatch = false;
            for(int j=0; j<AVAILABLE_TYPES.length; ++j) {
                if(AVAILABLE_TYPES[j].equals(types[i])) {
                    bMatch = true;
                    break;
                }
            }
            if(!bMatch) {
                return null;
            }
        }
        
        // set the user types
        String[] selectedTypes = new String[types.length];
        System.arraycopy(types, 0, selectedTypes, 0, types.length);
        return selectedTypes;
    }
}
