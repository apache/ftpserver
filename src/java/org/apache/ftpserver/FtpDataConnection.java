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

import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.interfaces.IFtpConfig;
import org.apache.ftpserver.interfaces.ISsl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;


/**
 * We can get the ftp data connection using this class.
 * It uses either PORT or PASV command.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public
class FtpDataConnection {
    
    private IFtpConfig    m_fconfig;
    private Socket        m_dataSoc;
    private ServerSocket  m_servSoc;
    
    private InetAddress  m_address = null;
    private int          m_port   = 0;
    
    private long m_requestTime = 0L;
    
    private boolean m_isPort   = false;
    private boolean m_isPasv   = false;
    
    private boolean m_isSecure = false;
    private boolean m_isZip    = false;
    
    
    /**
     * Default constructor.
     */
    public FtpDataConnection() {
    }

    /**
     * Set the ftp config.
     */
    public void setFtpConfig(IFtpConfig cfg) {
        m_fconfig = cfg;
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
                m_fconfig.getLogger().warn("FtpDataConnection.closeDataSocket()", ex);
            }
            m_dataSoc = null;
        }
        
        // close server socket if any
        if (m_servSoc != null) {
            try {
               m_servSoc.close();
            }
            catch(Exception ex) {
                m_fconfig.getLogger().warn("FtpDataConnection.closeDataSocket()", ex);
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
        int port = getPassivePort();
        if(port == -1) {
            m_fconfig.getLogger().warn("Cannot find an available passive port.");
            m_servSoc = null;
            return false;
        }
        
        // open passive server socket and get parameters
        boolean bRet = false;
        try {
            m_address = m_fconfig.getDataConnectionConfig().getPassiveAddress();
            if(m_isSecure) {
                ISsl ssl = m_fconfig.getDataConnectionConfig().getSSL();
                if(ssl == null) {
                    throw new FtpException("Data connection SSL not configured");
                }
                m_servSoc = ssl.createServerSocket(m_address, m_port);
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
            m_fconfig.getLogger().warn("FtpDataConnection.setPasvCommand()", ex);
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
    public synchronized Socket getDataSocket() throws IOException {
       
        // get socket depending on the selection
        if(m_isPort) {
            if(m_isSecure) {
                //ISsl ssl = mConfig.getDataConnectionConfig().getSSL();
                //if(ssl == null) {
                //  throw new IOException("Data connection SSL not configured");
                //}
                //mDataSoc = new Socket(mAddress, miPort);
                //mDataSoc = ssl.createSocket(mDataSoc, true);
            }
            else {
                m_dataSoc = new Socket(m_address, m_port);  
            }
        }
        else if(m_isPasv) {
            m_dataSoc = m_servSoc.accept();
        }

        return m_dataSoc;
    }
    
    /**
     * Get the passive port. Get it from the port pool.
     */
    private int getPassivePort() {
        return m_fconfig.getDataConnectionConfig().getPassivePort();
    }
    
    /**
     * Is secure?
     */
    public boolean isSecure() {
        return m_isSecure;
    }
    
    /**
     * Set secure.
     */
    public void setSecure(boolean secure) {
        m_isSecure = secure;
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
     * Dispose data connection
     */ 
    public void dispose() {
        closeDataSocket();
    }
}
    
