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
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.HashMap;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509KeyManager;

import org.apache.ftpserver.FtpServerConfigurationException;
import org.apache.ftpserver.util.ClassUtils;
import org.apache.ftpserver.util.IoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Ssl implementation. This class encapsulates all 
 * the SSL functionalities.
 */
public class DefaultSslConfiguration implements SslConfiguration {
    
    private final Logger LOG = LoggerFactory.getLogger(DefaultSslConfiguration.class);
    
    private File keystoreFile = new File("./res/.keystore");
    private String keystorePass;
    private String keystoreType = "JKS";
    private String keystoreAlgorithm = "SunX509";

    private File trustStoreFile;
    private String trustStorePass;
    private String trustStoreType = "JKS";
    private String trustStoreAlgorithm = "SunX509";
    
    private String sslProtocol = "TLS";
    private ClientAuth clientAuthReqd = ClientAuth.NONE;
    private String keyPass;
    private String keyAlias;

    private KeyManagerFactory keyManagerFactory;
    private TrustManagerFactory trustManagerFactory;
    
    private HashMap<String, SSLContext> sslContextMap;

    private String[] enabledCipherSuites;
    
    public File getKeystoreFile() {
        return keystoreFile;
    }
    
    public void setKeystoreFile(File keyStoreFile) {
        this.keystoreFile = keyStoreFile;
    }
    
    public String getKeystorePassword() {
        return keystorePass;
    }
    
    public void setKeystorePassword(String keystorePass) {
        this.keystorePass = keystorePass;
    }
    
    public String getKeystoreType() {
        return keystoreType;
    }
    
    public void setKeystoreType(String keystoreType) {
        this.keystoreType = keystoreType;
    }
    
    public String getKeystoreAlgorithm() {
        return keystoreAlgorithm;
    }
    
    public void setKeystoreAlgorithm(String keystoreAlgorithm) {
        this.keystoreAlgorithm = keystoreAlgorithm;
    
    }
    
    public String getSslProtocol() {
        return sslProtocol;
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
    
    public String getKeyPassword() {
        return keyPass;
    }
    
    public void setKeyPassword(String keyPass) {
        this.keyPass = keyPass;
    }
    
    public File getTruststoreFile() {
        return trustStoreFile;
    }
    
    public void setTruststoreFile(File trustStoreFile) {
        this.trustStoreFile = trustStoreFile;
    }
    
    public String getTruststorePassword() {
        return trustStorePass;
    }
    
    public void setTruststorePassword(String trustStorePass) {
        this.trustStorePass = trustStorePass;
    }
    
    public String getTruststoreType() {
        return trustStoreType;
    }
    
    public void setTruststoreType(String trustStoreType) {
        this.trustStoreType = trustStoreType;
    }
    
    public String getTruststoreAlgorithm() {
        return trustStoreAlgorithm;
    }
    
    public void setTruststoreAlgorithm(String trustStoreAlgorithm) {
        this.trustStoreAlgorithm = trustStoreAlgorithm;
    
    }

    private KeyStore loadStore(File storeFile, String storeType, String storePass) throws IOException, GeneralSecurityException {
        FileInputStream fin = null;
        try {
            fin = new FileInputStream(storeFile);
            KeyStore store = KeyStore.getInstance(storeType);
            store.load(fin, storePass.toCharArray());
            
            return store;
        }
        finally {
            IoUtils.close(fin);
        }
    }
    
    /**
     * Configure secure server related properties. 
     */
    public synchronized void init() {
        
        try {
            // initialize keystore
            KeyStore keyStore = loadStore(keystoreFile, keystoreType, keystorePass);
            
            KeyStore trustStore;
            if(trustStoreFile != null) {
                trustStore = loadStore(trustStoreFile, trustStoreType, trustStorePass);
            } else {
                trustStore = keyStore;
            }
            
            // initialize key manager factory
            keyManagerFactory = KeyManagerFactory.getInstance(keystoreAlgorithm);
            keyManagerFactory.init(keyStore, keyPass.toCharArray());
            
            // initialize trust manager factory
            trustManagerFactory = TrustManagerFactory.getInstance(trustStoreAlgorithm);
            trustManagerFactory.init(trustStore);
            
            // create ssl context map - the key is the 
            // SSL protocol and the value is SSLContext.
            sslContextMap = new HashMap<String, SSLContext>();
        }
        catch(Exception ex) {
            LOG.error("DefaultSsl.configure()", ex);
            throw new FtpServerConfigurationException("DefaultSsl.configure()", ex);
        }
    }
    
    private synchronized void lazyInit() {
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
        SSLContext ctx = sslContextMap.get(protocol);
        if(ctx != null) {
            return ctx;
        }
        
        // create SSLContext
        ctx = SSLContext.getInstance(protocol);
        
        KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();

        // wrap key managers to allow us to control their behavior (FTPSERVER-93)
        for (int i = 0; i < keyManagers.length; i++) {
          if(ClassUtils.extendsClass(keyManagers[i].getClass(), "javax.net.ssl.X509ExtendedKeyManager")) {
        	  keyManagers[i] = new ExtendedAliasKeyManager(keyManagers[i], keyAlias);
          } else if(keyManagers[i] instanceof X509KeyManager) {
        	  keyManagers[i] = new AliasKeyManager(keyManagers[i], keyAlias);
          }
        } 
        
        // create SSLContext
        ctx = SSLContext.getInstance(protocol);
        
        ctx.init(keyManagers, 
                 trustManagerFactory.getTrustManagers(), 
                 null);

        // store it in map
        sslContextMap.put(protocol, ctx);
        
        return ctx;
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

    /**
     * Get the server key alias to be used for SSL communication
     * @return The alias, or null if none is set
     */
    public String getKeyAlias() {
        return keyAlias;
    }

    /**
     * Set the alias for the key to be used for SSL communication.
     * If the specified key store contains multiple keys, this 
     * alias can be set to select a specific key.
     * @param keyAlias The alias to use, or null if JSSE should
     *          be allowed to choose the key.
     */
    public void setKeyAlias(String keyAlias) {
        this.keyAlias = keyAlias;
    }
}
