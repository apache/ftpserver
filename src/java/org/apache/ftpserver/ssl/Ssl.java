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

import java.io.FileInputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.HashMap;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.apache.ftpserver.ftplet.Configuration;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.Logger;
import org.apache.ftpserver.interfaces.ISsl;
import org.apache.ftpserver.util.IoUtils;


/**
 * ISsl implementation. This class encapsulates all 
 * the SSL functionalities.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class Ssl implements ISsl {
    
    private Logger m_logger; 
    
    private String m_keystoreFile;
    private String m_keystorePass;
    private String m_keystoreType;
    private String m_keystoreAlgorithm;
    
    private String m_sslProtocol;
    private boolean m_clientAuthReqd;
    private String m_keyPass;

    private KeyStore m_keyStore;
    private KeyManagerFactory m_keyManagerFactory;
    private TrustManagerFactory m_trustManagerFactory;
    
    private HashMap m_sslContextMap;
    
    
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
            
            // get configuration parameters
            m_keystoreFile      = conf.getString("keystore-file", "./res/.keystore");
            m_keystorePass      = conf.getString("keystore-password", "password");
            m_keystoreType      = conf.getString("keystore-type", "JKS");
            m_keystoreAlgorithm = conf.getString("keystore-algorithm", "SunX509");
            m_sslProtocol       = conf.getString("ssl-protocol", "TLS");
            m_clientAuthReqd    = conf.getBoolean("client-authentication", false);
            m_keyPass           = conf.getString("key-password", "password");
            
            // initialize keystore
            FileInputStream fin = null;
            try {
                fin = new FileInputStream(m_keystoreFile);
                m_keyStore = KeyStore.getInstance(m_keystoreType);
                m_keyStore.load(fin, m_keystorePass.toCharArray());
            }
            finally {
                IoUtils.close(fin);
            }
            
            // initialize key manager factory
            m_keyManagerFactory = KeyManagerFactory.getInstance(m_keystoreAlgorithm);
            m_keyManagerFactory.init(m_keyStore, m_keyPass.toCharArray());
            
            // initialize trust manager factory
            m_trustManagerFactory = TrustManagerFactory.getInstance(m_keystoreAlgorithm);
            m_trustManagerFactory.init(m_keyStore);
            
            // create ssl context map - the key is the 
            // SSL protocol and the value is SSLContext.
            m_sslContextMap = new HashMap();
        }
        catch(Exception ex) {
            m_logger.warn("Ssl.configure()", ex);
            throw new FtpException("Ssl.configure()", ex);
        }
    }
    
    /**
     * Get SSL Context.
     */
    private synchronized SSLContext getSSLContext(String protocol) throws Exception {
        
        // null value check
        if(protocol == null) {
            protocol = m_sslProtocol;
        }
        
        // if already stored - return it
        SSLContext ctx = (SSLContext)m_sslContextMap.get(protocol);
        if(ctx != null) {
            return ctx;
        }
        
        // create new secure random object
        SecureRandom random = new SecureRandom();
        random.nextInt();
        
        // create SSLContext
        ctx = SSLContext.getInstance(protocol);
        ctx.init(m_keyManagerFactory.getKeyManagers(), 
                 m_trustManagerFactory.getTrustManagers(), 
                 random);

        // store it in map
        m_sslContextMap.put(protocol, ctx);
        return ctx;
    }

    /**
     * Create secure server socket.
     */
    public ServerSocket createServerSocket(String protocol,
                                           InetAddress addr, 
                                           int port) throws Exception {

        // get server socket factory
        SSLContext ctx = getSSLContext(protocol);
        SSLServerSocketFactory ssocketFactory = ctx.getServerSocketFactory();
        
        // create server socket
        SSLServerSocket serverSocket = null;
        if(addr == null) {
            serverSocket = (SSLServerSocket) ssocketFactory.createServerSocket(port, 100);
        }
        else {
            serverSocket = (SSLServerSocket) ssocketFactory.createServerSocket(port, 100, addr);
        }
        
        // initialize server socket
        String cipherSuites[] = serverSocket.getSupportedCipherSuites();
        serverSocket.setEnabledCipherSuites(cipherSuites);
        serverSocket.setNeedClientAuth(m_clientAuthReqd);
        return serverSocket;
    }
 
    /**
     * Returns a socket layered over an existing socket.
     */
    public Socket createSocket(String protocol,
                               Socket soc, 
                               boolean clientMode) throws Exception {
        
        // already wrapped - no need to do anything
        if(soc instanceof SSLSocket) {
            return soc;
        }
        
        // get socket factory
        SSLContext ctx = getSSLContext(protocol);
        SSLSocketFactory socFactory = ctx.getSocketFactory();
        
        // create socket
        String host = soc.getInetAddress().getHostAddress();
        int port = soc.getLocalPort();
        SSLSocket ssoc = (SSLSocket)socFactory.createSocket(soc, host, port, true);
        ssoc.setUseClientMode(clientMode);
        
        // initialize socket
        String cipherSuites[] = ssoc.getSupportedCipherSuites();
        ssoc.setEnabledCipherSuites(cipherSuites);
        ssoc.setNeedClientAuth(m_clientAuthReqd);
        
        return ssoc;
    }

    /**
     * Create a secure socket.
     */
    public Socket createSocket(String protocol,
                               InetAddress addr, 
                               int port,
                               boolean clientMode) throws Exception {

        // get socket factory
        SSLContext ctx = getSSLContext(protocol);
        SSLSocketFactory socFactory = ctx.getSocketFactory();
        
        // create socket
        SSLSocket ssoc = (SSLSocket)socFactory.createSocket(addr, port);
        ssoc.setUseClientMode(clientMode);
        
        // initialize socket
        String cipherSuites[] = ssoc.getSupportedCipherSuites();
        ssoc.setEnabledCipherSuites(cipherSuites);
        return ssoc;
    } 
    
    /**
     * Dispose - does nothing.
     */
    public void dispose() {
    }
}
