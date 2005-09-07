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
package org.apache.ftpserver.socketfactory;

import org.apache.ftpserver.ftplet.Configuration;
import org.apache.ftpserver.ftplet.FtpException;

import java.net.InetAddress;
import java.net.ServerSocket;


/**
 * It is used in case of implicit SSL connection.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class SSLFtpSocketFactory extends FtpSocketFactory {        
    
    /**
     * Configure secure server related properties. 
     */
    public void configure(Configuration conf) throws FtpException {
        super.configure(conf);
        if(getSSL() == null) {
            throw new FtpException("SSL component not configured.");
        }
    }
    
    /**
     * Create secure server socket.
     */
    public ServerSocket createServerSocket() throws Exception {
        InetAddress addr = getServerAddress();
        int port = getPort();
        return getSSL().createServerSocket(addr, port);
    }
}
