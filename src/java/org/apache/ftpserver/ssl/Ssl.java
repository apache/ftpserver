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
package org.apache.ftpserver.ssl;

import org.apache.ftpserver.ftplet.Configuration;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.Logger;
import org.apache.ftpserver.interfaces.ISsl;
import org.apache.ftpserver.util.IoUtils;

import java.io.FileInputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.Security;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import sun.security.provider.Sun;

import com.sun.net.ssl.internal.ssl.Provider;


/**
 * ISsl implementation.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class Ssl implements ISsl {
    
    private Logger m_logger; 
    
    private String m_keystoreFile;
    private String m_keystorePass;
    private String m_keystoreType;
    private String m_keystoreProtocol;
    private String m_keystoreAlgorithm;
    private boolean m_clientAuthReqd;
    private String m_keyPass;

    private SSLContext m_sslContext;
    private SSLSocketFactory m_socketFactory;
    private SSLServerSocketFactory m_serverSocketFactory;
    
    /**
     * Set logger.
     */
    public void setLogger(Logger logger) {
        m_logger = logger;
    }
    
    /**
     * Configure secure server related properties. 
     */
    public void configure(Configuration conf) throws FtpException {
        
        try {
            
            // check JSSE installation
            Class.forName("com.sun.net.ssl.internal.ssl.Provider");
        }
        catch(Exception ex) {
            throw new FtpException("JSSE not found.");
        }
        
        try {
            
            // get configuration parameters
            m_keystoreFile      = conf.getString("keystore-file", "./res/.keystore");
            m_keystorePass      = conf.getString("keystore-password", "password");
            m_keystoreType      = conf.getString("keystore-type", "JKS");
            m_keystoreProtocol  = conf.getString("keystore-protocol", "TLS");
            m_keystoreAlgorithm = conf.getString("keystore-algorithm", "SunX509");
            m_clientAuthReqd    = conf.getBoolean("client-authentication", false);
            m_keyPass           = conf.getString("key-password", "password");
            
            // get SSL context
            m_sslContext = getSSLContext();
        }
        catch(Exception ex) {
            m_logger.warn("Ssl.configure()", ex);
            throw new FtpException("SecureSocketUtil.configure()", ex);
        }
    }
    
    /**
     * Get SSL context.
     */
    private SSLContext getSSLContext() throws Exception {
        
        // initialize keystore
        KeyStore keystore = null;
        FileInputStream fin = null;
        try {
            fin = new FileInputStream(m_keystoreFile);
            keystore = KeyStore.getInstance(m_keystoreType);
            keystore.load(fin, m_keystorePass.toCharArray());
        }
        finally {
            IoUtils.close(fin);
        }
        
        // create SSL context
        Security.addProvider(new Sun());
        Security.addProvider(new Provider());
        SSLContext sslContext = SSLContext.getInstance(m_keystoreProtocol);
        
        // initialize key manager factory
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(m_keystoreAlgorithm);
        keyManagerFactory.init(keystore, m_keyPass.toCharArray());
        
        // initialize trust manager factory
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(m_keystoreAlgorithm);
        tmf.init(keystore);
        
        // initialize SSL context
        sslContext.init(keyManagerFactory.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());
        return sslContext;
    }
    
    /**
     * Create secure server socket.
     */
    public ServerSocket createServerSocket(InetAddress addr, 
                                              int port) throws Exception {
        
        // get server socket factory
        if(m_serverSocketFactory == null) {
            m_serverSocketFactory = m_sslContext.getServerSocketFactory();
        }
        
        // create server socket
        SSLServerSocket serverSocket = null;
        if(addr == null) {
            serverSocket = (SSLServerSocket) m_serverSocketFactory.createServerSocket(port, 100);
        }
        else {
            serverSocket = (SSLServerSocket) m_serverSocketFactory.createServerSocket(port, 100, addr);
        }
        
        // initialize server socket
        String cipherSuites[] = serverSocket.getSupportedCipherSuites();
        serverSocket.setEnabledCipherSuites(cipherSuites);
        serverSocket.setNeedClientAuth(m_clientAuthReqd);
        return serverSocket;
    }
 
    /**
     * Create socket.
     */
    public Socket createSocket(Socket soc, boolean clientMode) throws Exception {
        
        // get socket factory
        if(m_socketFactory == null) {
            m_socketFactory = m_sslContext.getSocketFactory();
        }
        
        // create socket
        String host = soc.getInetAddress().getHostAddress();
        int port = soc.getLocalPort();
        SSLSocket ssoc = (SSLSocket)m_socketFactory.createSocket(soc, host, port, true);
        ssoc.setUseClientMode(clientMode);
        
        // initialize socket
        String cipherSuites[] = ssoc.getSupportedCipherSuites();
        ssoc.setEnabledCipherSuites(cipherSuites);
        ssoc.setNeedClientAuth(m_clientAuthReqd);
        
        return ssoc;
    }

    /**
     * Dispose.
     */
    public void dispose() {
    }
}
