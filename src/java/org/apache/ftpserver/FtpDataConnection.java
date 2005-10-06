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
package org.apache.ftpserver;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.interfaces.IFtpConfig;
import org.apache.ftpserver.interfaces.ISsl;


/**
 * We can get the ftp data connection using this class.
 * It uses either PORT or PASV command.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public
class FtpDataConnection {
    
    private Log m_log;
    
    private IFtpConfig    m_fconfig;
    private Socket        m_dataSoc;
    private ServerSocket  m_servSoc;
    
    private InetAddress  m_address = null;
    private int          m_port   = 0;
    
    private long m_requestTime = 0L;
    
    private boolean m_isPort   = false;
    private boolean m_isPasv   = false;
    
    private boolean m_secure   = false;
    private boolean m_isZip    = false;
    
    
    /**
     * Set the ftp config.
     */
    public void setFtpConfig(IFtpConfig cfg) {
        m_fconfig = cfg;
        m_log = m_fconfig.getLogFactory().getInstance(getClass());
    }
    
    /**
     * Close data socket.
     */
    public synchronized void closeDataSocket() {
        
        // close client socket if any
        if(m_dataSoc != null) {
            try {
                m_dataSoc.close(); 
            } 
            catch(Exception ex) {
                m_log.warn("FtpDataConnection.closeDataSocket()", ex);
            }
            m_dataSoc = null;
        }
        
        // close server socket if any
        if (m_servSoc != null) {
            try {
               m_servSoc.close();
            }
            catch(Exception ex) {
                m_log.warn("FtpDataConnection.closeDataSocket()", ex);
            }
            m_fconfig.getDataConnectionConfig().releasePassivePort(m_port);
            m_servSoc = null;
        }
        
        // reset request time
        m_requestTime = 0L;
    }
     
    /**
     * Port command.
     */
    public synchronized void setPortCommand(InetAddress addr, int port) {
        
        // close old sockets if any
        closeDataSocket();
        
        // set variables
        m_isPort = true;
        m_isPasv = false;
        m_address = addr;
        m_port = port;
        m_requestTime = System.currentTimeMillis();
    } 
    
    /**
     * Passive command. It returns the success flag.
     */
    public synchronized boolean setPasvCommand() {
        
        // close old sockets if any
        closeDataSocket(); 
        
        // get the passive port
        int port = m_fconfig.getDataConnectionConfig().getPassivePort();
        if(port == -1) {
            m_log.warn("Cannot find an available passive port.");
            m_servSoc = null;
            return false;
        }
        
        // open passive server socket and get parameters
        boolean bRet = false;
        try {
            m_address = m_fconfig.getDataConnectionConfig().getPassiveAddress();
            if(m_secure) {
                ISsl ssl = m_fconfig.getDataConnectionConfig().getSSL();
                if(ssl == null) {
                    throw new FtpException("Data connection SSL not configured.");
                }
                m_servSoc = ssl.createServerSocket(null, m_address, m_port);
            }
            else {
                m_servSoc = new ServerSocket(port, 1, m_address);   
            }
            m_port = m_servSoc.getLocalPort();          

            // set different state variables
            m_isPort = false;
            m_isPasv = true;
            bRet = true;
            m_requestTime = System.currentTimeMillis();
        }
        catch(Exception ex) {
            m_servSoc = null;
            m_log.warn("FtpDataConnection.setPasvCommand()", ex);
        }
        return bRet;
    }
     
    /**
     * Get client address.
     */
    public InetAddress getInetAddress() {
        return m_address;
    }
     
    /**
     * Get port number.
     */
    public int getPort() {
        return m_port;
    }

    /**
     * Get the data socket. In case of error returns null.
     */
    public synchronized Socket getDataSocket() {

        // get socket depending on the selection
        m_dataSoc = null;
        try {
            if(m_isPort) {
                if(m_secure) {
                    ISsl ssl = m_fconfig.getDataConnectionConfig().getSSL();
                    if(ssl == null) {
                        throw new FtpException("Data connection SSL not configured");
                    }
                    m_dataSoc = ssl.createSocket(null, m_address, m_port, false);
                }
                else {
                    m_dataSoc = new Socket(m_address, m_port);  
                }
            }
            else if(m_isPasv) {
                m_dataSoc = m_servSoc.accept();
            }
        }
        catch(Exception ex) {
            m_log.warn("FtpDataConnection.getDataSocket()", ex);
        }
        
        return m_dataSoc;
    }
    
    /**
     * Is secure?
     */
    public boolean isSecure() {
        return m_secure;
    }
    
    /**
     * Set the security protocol.
     */
    public void setSecure(boolean secure) {
        m_secure = secure;
    }
    
    /**
     * Is zip mode?
     */
    public boolean isZipMode() {
        return m_isZip;
    }
    
    /**
     * Set zip mode.
     */
    public void setZipMode(boolean zip) {
        m_isZip = zip;
    }
    
    /**
     * Is the data connection active?
     */
    public boolean isActive() {
        return m_dataSoc != null;
    }
    
    /**
     * Get the request time.
     */
    public long getRequestTime() {
        return m_requestTime;
    }
    
    /**
     * Dispose data connection - close all the sockets.
     */ 
    public void dispose() {
        closeDataSocket();
    }
}
    
