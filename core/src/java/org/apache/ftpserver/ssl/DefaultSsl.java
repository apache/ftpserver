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

package org.apache.ftpserver.ssl;

import java.io.File;
import java.io.FileInputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.HashMap;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.apache.ftpserver.FtpServerConfigurationException;
import org.apache.ftpserver.util.IoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Ssl implementation. This class encapsulates all 
 * the SSL functionalities.
 */
public class DefaultSsl implements Ssl {
    
    private final Logger LOG = LoggerFactory.getLogger(DefaultSsl.class);
    
    private File keystoreFile = new File("./res/.keystore");
    private String keystorePass = "password";   // TODO should we really default this value?
    private String keystoreType = "JKS";
    private String keystoreAlgorithm = "SunX509";
    
    private String sslProtocol = "TLS";
    private ClientAuth clientAuthReqd = ClientAuth.NONE;
    private String keyPass = "password";   // TODO should we really default this value?

    private KeyStore keyStore;
    private KeyManagerFactory keyManagerFactory;
    private TrustManagerFactory trustManagerFactory;
    
    private HashMap sslContextMap;

    private String[] enabledCipherSuites;
    
    public void setKeystoreFile(File keyStoreFile) {
        this.keystoreFile = keyStoreFile;
    }
    
    public void setKeystorePassword(String keystorePass) {
        this.keystorePass = keystorePass;
    }
    
    public void setKeystoreType(String keystoreType) {
        this.keystoreType = keystoreType;
    }
    
    public void setKeystoreAlgorithm(String keystoreAlgorithm) {
        this.keystoreAlgorithm = keystoreAlgorithm;
    }
    
    public void setSslProtocol(String sslProtocol) {
        this.sslProtocol = sslProtocol;
    }
    
    public void setClientAuthentication(String clientAuthReqd) {
        if("true".equalsIgnoreCase(clientAuthReqd) 
                || "yes".equalsIgnoreCase(clientAuthReqd)) {
            this.clientAuthReqd = ClientAuth.NEED;
        } else if("want".equalsIgnoreCase(clientAuthReqd)) {
            this.clientAuthReqd = ClientAuth.WANT;
        } else {
            this.clientAuthReqd = ClientAuth.NONE;
        }
    }
    
    public void setKeyPassword(String keyPass) {
        this.keyPass = keyPass;
    }
    
    
    /**
     * Configure secure server related properties. 
     */
    public synchronized void init() {
        
        try {
            // initialize keystore
            FileInputStream fin = null;
            try {
                fin = new FileInputStream(keystoreFile);
                keyStore = KeyStore.getInstance(keystoreType);
                keyStore.load(fin, keystorePass.toCharArray());
            }
            finally {
                IoUtils.close(fin);
            }
            
            // initialize key manager factory
            keyManagerFactory = KeyManagerFactory.getInstance(keystoreAlgorithm);
            keyManagerFactory.init(keyStore, keyPass.toCharArray());
            
            // initialize trust manager factory
            trustManagerFactory = TrustManagerFactory.getInstance(keystoreAlgorithm);
            trustManagerFactory.init(keyStore);
            
            // create ssl context map - the key is the 
            // SSL protocol and the value is SSLContext.
            sslContextMap = new HashMap();
        }
        catch(Exception ex) {
            LOG.error("DefaultSsl.configure()", ex);
            throw new FtpServerConfigurationException("DefaultSsl.configure()", ex);
        }
    }
    
    private void lazyInit() {
        if(keyManagerFactory == null) {
            init();
        }
    }
    
    /**
     * Get SSL Context.
     */
    public synchronized SSLContext getSSLContext(String protocol) throws GeneralSecurityException {
        lazyInit();
        
        // null value check
        if(protocol == null) {
            protocol = sslProtocol;
        }
        
        // if already stored - return it
        SSLContext ctx = (SSLContext)sslContextMap.get(protocol);
        if(ctx != null) {
            return ctx;
        }
        
        // create SSLContext
        ctx = SSLContext.getInstance(protocol);
        ctx.init(keyManagerFactory.getKeyManagers(), 
                 trustManagerFactory.getTrustManagers(), 
                 null);

        // store it in map
        sslContextMap.put(protocol, ctx);
        
        return ctx;
    }
    
    /**
     * Dispose - does nothing.
     */
    public void dispose() {
    }

    public ClientAuth getClientAuth() {
        return clientAuthReqd;
    }

    public SSLContext getSSLContext() throws GeneralSecurityException {
        return getSSLContext(sslProtocol);
    }

    public String[] getEnabledCipherSuites() {
        return enabledCipherSuites;
    }
    
    public void setEnabledCipherSuites(String[] enabledCipherSuites) {
        this.enabledCipherSuites = enabledCipherSuites;
    }
}
