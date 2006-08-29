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

package org.apache.ftpserver;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.interfaces.IDataConnectionConfig;
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
    
    private Log log;
    
    private IFtpConfig    fconfig;
    private Socket        dataSoc;
    private ServerSocket  servSoc;
    
    private InetAddress  address = null;
    private int          port    = 0;
    
    private long requestTime = 0L;
    
    private boolean isPort   = false;
    private boolean isPasv   = false;
    
    private boolean secure   = false;
    private boolean isZip    = false;
    
    
    /**
     * Set the ftp config.
     */
    public void setFtpConfig(IFtpConfig cfg) {
        fconfig = cfg;
        log = fconfig.getLogFactory().getInstance(getClass());
    }
    
    /**
     * Close data socket.
     */
    public synchronized void closeDataSocket() {
        
        // close client socket if any
        if(dataSoc != null) {
            try {
                dataSoc.close(); 
            } 
            catch(Exception ex) {
                log.warn("FtpDataConnection.closeDataSocket()", ex);
            }
            dataSoc = null;
        }
        
        // close server socket if any
        if (servSoc != null) {
            try {
               servSoc.close();
            }
            catch(Exception ex) {
                log.warn("FtpDataConnection.closeDataSocket()", ex);
            }
            fconfig.getDataConnectionConfig().releasePassivePort(port);
            servSoc = null;
        }
        
        // reset request time
        requestTime = 0L;
    }
     
    /**
     * Port command.
     */
    public synchronized void setPortCommand(InetAddress addr, int activePort) {
        
        // close old sockets if any
        closeDataSocket();
        
        // set variables
        isPort = true;
        isPasv = false;
        address = addr;
        port = activePort;
        requestTime = System.currentTimeMillis();
    } 
    
    /**
     * Passive command. It returns the success flag.
     */
    public synchronized boolean setPasvCommand() {
        
        // close old sockets if any
        closeDataSocket(); 
        
        // get the passive port
        int passivePort = fconfig.getDataConnectionConfig().getPassivePort();
        if(passivePort == -1) {
            log.warn("Cannot find an available passive port.");
            servSoc = null;
            return false;
        }
        
        // open passive server socket and get parameters
        boolean bRet = false;
        try {
            IDataConnectionConfig dataCfg = fconfig.getDataConnectionConfig();
            address = dataCfg.getPassiveAddress();
            if(secure) {
                ISsl ssl = dataCfg.getSSL();
                if(ssl == null) {
                    throw new FtpException("Data connection SSL not configured.");
                }
                servSoc = ssl.createServerSocket(null, address, passivePort);
            }
            else {
                servSoc = new ServerSocket(passivePort, 1, address);
            }
            servSoc.setSoTimeout(dataCfg.getMaxIdleTimeMillis());
            port = servSoc.getLocalPort();

            // set different state variables
            isPort = false;
            isPasv = true;
            bRet = true;
            requestTime = System.currentTimeMillis();
        }
        catch(Exception ex) {
            servSoc = null;
            closeDataSocket();
            log.warn("FtpDataConnection.setPasvCommand()", ex);
        }
        return bRet;
    }
     
    /**
     * Get client address.
     */
    public InetAddress getInetAddress() {
        return address;
    }
     
    /**
     * Get port number.
     */
    public int getPort() {
        return port;
    }

    /**
     * Get the data socket. In case of error returns null.
     */
    public synchronized Socket getDataSocket() {

        // get socket depending on the selection
        dataSoc = null;
        IDataConnectionConfig dataConfig = fconfig.getDataConnectionConfig();
        try {
            if(isPort) {
                int localPort = dataConfig.getActiveLocalPort();
                if(secure) {
                    ISsl ssl = dataConfig.getSSL();
                    if(ssl == null) {
                        throw new FtpException("Data connection SSL not configured");
                    }
                    if(localPort == 0) {
                        dataSoc = ssl.createSocket(null, address, port, false);
                    }
                    else {
                        InetAddress localAddr = dataConfig.getActiveLocalAddress();
                        dataSoc = ssl.createSocket(null, address, port, localAddr, localPort, false);
                    }
                }
                else {
                    if(localPort == 0) {
                        dataSoc = new Socket(address, port);  
                    }
                    else {
                        InetAddress localAddr = dataConfig.getActiveLocalAddress();
                        dataSoc = new Socket(address, port, localAddr, localPort);
                    }
                }
            }
            else if(isPasv) {
                dataSoc = servSoc.accept();
            }
        }
        catch(Exception ex) {
            closeDataSocket();
            log.warn("FtpDataConnection.getDataSocket()", ex);
        }
        
        return dataSoc;
    }
    
    /**
     * Is secure?
     */
    public boolean isSecure() {
        return secure;
    }
    
    /**
     * Set the security protocol.
     */
    public void setSecure(boolean secure) {
        this.secure = secure;
    }
    
    /**
     * Is zip mode?
     */
    public boolean isZipMode() {
        return isZip;
    }
    
    /**
     * Set zip mode.
     */
    public void setZipMode(boolean zip) {
        isZip = zip;
    }
    
    /**
     * Check the data connection idle status.
     */
    public synchronized boolean isTimeout(long currTime) {
        
        // data connection not requested - not a timeout
        if(requestTime == 0L) {
            return false;
        }
        
        // data connection active - not a timeout
        if(dataSoc != null) {
            return false;
        }
        
        // no idle time limit - not a timeout
        int maxIdleTime = fconfig.getDataConnectionConfig().getMaxIdleTimeMillis();
        if(maxIdleTime == 0) {
            return false;
        }
        
        // idle time is within limit - not a timeout
        if( (currTime - requestTime) < maxIdleTime ) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Dispose data connection - close all the sockets.
     */ 
    public void dispose() {
        closeDataSocket();
    }
}
    
