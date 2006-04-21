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

import org.apache.commons.logging.Log;
import org.apache.ftpserver.FtpRequestImpl;
import org.apache.ftpserver.FtpWriter;
import org.apache.ftpserver.RequestHandler;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.interfaces.ICommand;
import org.apache.ftpserver.interfaces.IFtpConfig;

/**
 * This server supports explicit SSL support.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class AUTH implements ICommand {
    

    /**
     * Execute command
     */
    public void execute(RequestHandler handler,
                        FtpRequestImpl request, 
                        FtpWriter out) throws IOException, FtpException {
        
        // reset state variables
        request.resetState();
        
        // argument check
        if(!request.hasArgument()) {
            out.send(501, "AUTH", null);
            return;  
        }
        
        // check SSL configuration
        IFtpConfig fconfig = handler.getConfig();
        Log log = fconfig.getLogFactory().getInstance(getClass());
        if(fconfig.getSocketFactory().getSSL() == null) {
            out.send(431, "AUTH", null);
            return;
        }
        
        // check parameter
        String authType = request.getArgument().toUpperCase();
        if(authType.equals("SSL")) {
            out.send(234, "AUTH.SSL", null);
            try {
                handler.createSecureSocket("SSL");
            }
            catch(FtpException ex) {
                throw ex;
            }
            catch(Exception ex) {
                log.warn("AUTH.execute()", ex);
                throw new FtpException("AUTH.execute()", ex);
            }
        }
        else if(authType.equals("TLS")) {
            out.send(234, "AUTH.TLS", null);
            try {
                handler.createSecureSocket("TLS");
            }
            catch(FtpException ex) {
                throw ex;
            }
            catch(Exception ex) {
                log.warn("AUTH.execute()", ex);
                throw new FtpException("AUTH.execute()", ex);
            }
        }
        else {
            out.send(502, "AUTH", null);
        }
    }
}
