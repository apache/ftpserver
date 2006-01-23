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

import java.net.InetAddress;
import java.net.ServerSocket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ftpserver.ftplet.Configuration;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.interfaces.ISocketFactory;
import org.apache.ftpserver.interfaces.ISsl;


/**
 * It creates standard FTP server socket.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class FtpSocketFactory implements ISocketFactory {
    
    private Log log;
    private LogFactory logFactory;
    
    private InetAddress serverAddress;
    private int port;
    private ISsl ssl;
    
    
    /**
     * Set the log factory.
     */
    public void setLogFactory(LogFactory factory) {
        logFactory = factory;
        log = logFactory.getInstance(getClass());
    }
    
    /**
     * Configure the server
     */
    public void configure(Configuration conf) throws FtpException {
        try {
            
            // get server address
            String serverAddress = conf.getString("address", null);
            if(serverAddress != null) {
                this.serverAddress = InetAddress.getByName(serverAddress);
            }
            
            // get server port
            port = conf.getInt("port", 21);
            
            // get certificate
            Configuration sslConf = conf.subset("ssl");
            if(!sslConf.isEmpty()) {
                ssl = (ISsl)Class.forName("org.apache.ftpserver.ssl.Ssl").newInstance();
                ssl.setLogFactory(logFactory);
                ssl.configure(sslConf);
            }
        }
        catch(FtpException ex) {
            throw ex;
        }
        catch(Exception ex) {
            log.fatal("FtpSocketFactory.configure()", ex);
            throw new FtpException("FtpSocketFactory.configure()", ex);
        }
    }
    
    /**
     * Create server socket.
     */
    public ServerSocket createServerSocket() throws Exception {     
        ServerSocket ssocket = null;
        if(serverAddress == null) {
            ssocket = new ServerSocket(port, 100);
        }
        else {
            ssocket = new ServerSocket(port, 100, serverAddress);
        }
        return ssocket;
    }
            
    /**
     * Get server address.
     */
    public InetAddress getServerAddress() {
        return serverAddress;
    }
    
    /**
     * Get port number.
     */
    public int getPort() {
        return port;
    }
        
    /**
     * Get SSL component.
     */
    public ISsl getSSL() {
        return ssl;
    }
    
    /**
     * Release all resources.
     */
    public void dispose() {
    } 
}
