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

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.GeneralSecurityException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.apache.ftpserver.ftplet.DataConnection;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.interfaces.DataConnectionConfiguration;
import org.apache.ftpserver.interfaces.FtpIoSession;
import org.apache.ftpserver.interfaces.FtpServerContext;
import org.apache.ftpserver.ssl.ClientAuth;
import org.apache.ftpserver.ssl.SslConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * We can get the ftp data connection using this class.
 * It uses either PORT or PASV command.
 */
public class IODataConnectionFactory implements ServerDataConnectionFactory {
    
    private static final Logger LOG = LoggerFactory.getLogger(IODataConnectionFactory.class);
    
    private FtpServerContext    serverContext;
    private Socket        dataSoc;
    ServerSocket  servSoc;
    
    InetAddress  address;
    int          port    = 0;
    
    long requestTime = 0L;
    
    boolean passive   = false;
    
    boolean secure   = false;
    private boolean isZip    = false;

    InetAddress serverControlAddress;

    FtpIoSession session;
    
    public IODataConnectionFactory(FtpServerContext serverContext, FtpIoSession session) {
        this.session = session;
        this.serverContext = serverContext;
        
    }

    
    /**
     * Close data socket.
     */
    public synchronized void closeDataConnection() {
        
        // close client socket if any
        if(dataSoc != null) {
            try {
                dataSoc.close(); 
            } 
            catch(Exception ex) {
                LOG.warn("FtpDataConnection.closeDataSocket()", ex);
            }
            dataSoc = null;
        }
        
        // close server socket if any
        if (servSoc != null) {
            try {
               servSoc.close();
            }
            catch(Exception ex) {
                LOG.warn("FtpDataConnection.closeDataSocket()", ex);
            }
            
            FtpServerContext ctx = serverContext;
            
            if(ctx != null) {
                DataConnectionConfiguration dcc = session.getListener().getDataConnectionConfiguration();
                if(dcc != null) {
                    dcc.releasePassivePort(port);
                }
            }
            
            servSoc = null;
        }
        
        // reset request time
        requestTime = 0L;
    }
     
    /**
     * Port command.
     */
    public synchronized void initActiveDataConnection(InetSocketAddress address) {
        
        // close old sockets if any
        closeDataConnection();
        
        // set variables
        passive = false;
        this.address = address.getAddress();
        port = address.getPort();
        requestTime = System.currentTimeMillis();
    } 
    
    /**
     * Initiate a data connection in passive mode (server listening). 
     * It returns the success flag.
     */
    public synchronized InetSocketAddress initPassiveDataConnection() throws DataConnectionException {
        
        // close old sockets if any
        closeDataConnection(); 
        
        // get the passive port
        int passivePort = session.getListener().getDataConnectionConfiguration().requestPassivePort();
        if(passivePort == -1) {
            servSoc = null;
            throw new DataConnectionException("Cannot find an available passive port.");
        }
        
        // open passive server socket and get parameters
        try {
            DataConnectionConfiguration dataCfg = session.getListener().getDataConnectionConfiguration();
            address = dataCfg.getPassiveAddress();

            if(address == null) {
                address = serverControlAddress;
            }

            if(secure) {
                SslConfiguration ssl = dataCfg.getSSLConfiguration();
                if(ssl == null) {
                    throw new DataConnectionException("Data connection SSL required but not configured.");
                }
                servSoc = createServerSocket(ssl, address, passivePort);
                port = servSoc.getLocalPort();
                LOG.debug("SSL data connection created on " + address + ":" + port);
            }
            else {
                servSoc = new ServerSocket(passivePort, 1, address);
                port = servSoc.getLocalPort();
                LOG.debug("Data connection created on " + address + ":" + port);
            }
            servSoc.setSoTimeout(dataCfg.getMaxIdleTimeMillis());

            // set different state variables
            passive = true;
            requestTime = System.currentTimeMillis();
            
            return new InetSocketAddress(address, port);
        }
        catch(Exception ex) {
            servSoc = null;
            closeDataConnection();
            throw new DataConnectionException("FtpDataConnection.setPasvCommand()", ex);
        }
    }
     
    private ServerSocket createServerSocket(SslConfiguration ssl, InetAddress address2, int passivePort) throws IOException, GeneralSecurityException {
        // get server socket factory
        SSLContext ctx = ssl.getSSLContext();
        SSLServerSocketFactory ssocketFactory = ctx.getServerSocketFactory();
        
        // create server socket
        SSLServerSocket sslServerSocket = null;
        if(address2 == null) {
            sslServerSocket = (SSLServerSocket) ssocketFactory.createServerSocket(passivePort, 100);
        } else {
            sslServerSocket = (SSLServerSocket) ssocketFactory.createServerSocket(passivePort, 100, address2);
        }
        
        // initialize server socket
        if(ssl.getClientAuth() == ClientAuth.NEED) {
            sslServerSocket.setNeedClientAuth(true);
        } else if(ssl.getClientAuth() == ClientAuth.WANT) {
            sslServerSocket.setWantClientAuth(true);
        }

        
        if(ssl.getEnabledCipherSuites() != null) {
            sslServerSocket.setEnabledCipherSuites(ssl.getEnabledCipherSuites());
        }
        return sslServerSocket;
    }


    /* (non-Javadoc)
     * @see org.apache.ftpserver.FtpDataConnectionFactory2#getInetAddress()
     */
    public InetAddress getInetAddress() {
        return address;
    }
     
    /* (non-Javadoc)
     * @see org.apache.ftpserver.FtpDataConnectionFactory2#getPort()
     */
    public int getPort() {
        return port;
    }

    /* (non-Javadoc)
     * @see org.apache.ftpserver.FtpDataConnectionFactory2#openConnection()
     */
    public DataConnection openConnection() throws Exception {
        return new IODataConnection(createDataSocket(), session, this);
    }
    
    /**
     * Get the data socket. In case of error returns null.
     */
    private synchronized Socket createDataSocket() throws Exception {

        // get socket depending on the selection
        dataSoc = null;
        DataConnectionConfiguration dataConfig = session.getListener().getDataConnectionConfiguration();
        try {
            if(!passive) {
                int localPort = dataConfig.getActiveLocalPort();
                if(secure) {
                    SslConfiguration ssl = dataConfig.getSSLConfiguration();
                    if(ssl == null) {
                        throw new FtpException("Data connection SSL not configured");
                    }
                    if(localPort == 0) {
                        dataSoc = createSocket(ssl, address, port, null, localPort, false);
                    }
                    else {
                        InetAddress localAddr = dataConfig.getActiveLocalAddress();
                        dataSoc = createSocket(ssl, address, port, localAddr, localPort, false);
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
            } else {
                LOG.debug("Opening passive data connection");
                dataSoc = servSoc.accept();
                LOG.debug("Passive data connection opened");
            }
        }
        catch(Exception ex) {
            closeDataConnection();
            LOG.warn("FtpDataConnection.getDataSocket()", ex);
            throw ex;
        }
        
        // Make sure we initate the SSL handshake, or we'll
        // get an error if we turn out not to send any data
        // e.g. during the listing of an empty dir
        if(dataSoc instanceof SSLSocket) {
            ((SSLSocket)dataSoc).startHandshake();
        }
        
        return dataSoc;
    }
    
    private Socket createSocket(SslConfiguration ssl, InetAddress address2,
            int port2, InetAddress localAddress, int localPort, boolean clientMode) throws IOException, GeneralSecurityException {
        
        // get socket factory
        SSLContext ctx = ssl.getSSLContext();
        SSLSocketFactory socFactory = ctx.getSocketFactory();
        
        // create socket
        SSLSocket ssoc;
        if(localPort != 0) {
            ssoc = (SSLSocket)socFactory.createSocket(address2, port2);
        } else {
            ssoc = (SSLSocket)socFactory.createSocket(address2, port2, localAddress, localPort);
        }
        ssoc.setUseClientMode(clientMode);
        
        
        // initialize socket
        if(ssl.getEnabledCipherSuites() != null) {
            ssoc.setEnabledCipherSuites(ssl.getEnabledCipherSuites());
        }
        return ssoc;
    }


    /* (non-Javadoc)
     * @see org.apache.ftpserver.FtpDataConnectionFactory2#isSecure()
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
    
    /* (non-Javadoc)
     * @see org.apache.ftpserver.FtpDataConnectionFactory2#isZipMode()
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
        int maxIdleTime = session.getListener().getDataConnectionConfiguration().getMaxIdleTimeMillis();
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
        closeDataConnection();
    }

    /**
     * Sets the server's control address. 
     */
    public void setServerControlAddress(InetAddress serverControlAddress) {
        this.serverControlAddress = serverControlAddress;
    }
}
    
