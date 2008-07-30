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
 * Used to configure the SSL settings for the control channel
 * or the data channel.
 */
public class DefaultSslConfiguration implements SslConfiguration {
    
    private final Logger LOG = LoggerFactory.getLogger(DefaultSslConfiguration.class);
    
    private File keystoreFile = new File("./res/.keystore");
    private String keystorePass;
    private String keystoreType = KeyStore.getDefaultType();
    private String keystoreAlgorithm = "SunX509";

    private File trustStoreFile;
    private String trustStorePass;
    private String trustStoreType = KeyStore.getDefaultType();
    private String trustStoreAlgorithm = "SunX509";
    
    private String sslProtocol = "TLS";
    private ClientAuth clientAuthReqd = ClientAuth.NONE;
    private String keyPass;
    private String keyAlias;

    private KeyManagerFactory keyManagerFactory;
    private TrustManagerFactory trustManagerFactory;
    
    private HashMap<String, SSLContext> sslContextMap = new HashMap<String, SSLContext>();

    private String[] enabledCipherSuites;
    
    /**
     * The key store file used by this configuration
     * @return The key store file
     */
    public File getKeystoreFile() {
        return keystoreFile;
    }
    
    /**
     * Set the key store file to be used by this configuration
     * @param keyStoreFile A path to an existing key store file
     */
    public void setKeystoreFile(File keyStoreFile) {
        this.keystoreFile = keyStoreFile;
    }
    
    /**
     * The password used to load the key store
     * @return The password
     */
    public String getKeystorePassword() {
        return keystorePass;
    }

    /**
     * Set the password used to load the key store 
     * @param keystorePass The password
     */
    public void setKeystorePassword(String keystorePass) {
        this.keystorePass = keystorePass;
    }

    /**
     * The key store type, defaults to @see {@link KeyStore#getDefaultType()}
     * @return The key store type
     */
    public String getKeystoreType() {
        return keystoreType;
    }
    
    /**
     * Set the key store type
     * @param keystoreType The key store type
     */
    public void setKeystoreType(String keystoreType) {
        this.keystoreType = keystoreType;
    }
    
    /**
     * The algorithm used to open the key store. 
     * 	 Defaults to "SunX509"
     * @return The key store algorithm
     */
    public String getKeystoreAlgorithm() {
        return keystoreAlgorithm;
    }
    
    /**
     * Override the key store algorithm used to
     *   open the key store
     * @param keystoreAlgorithm The key store algorithm
     */
    public void setKeystoreAlgorithm(String keystoreAlgorithm) {
        this.keystoreAlgorithm = keystoreAlgorithm;
    
    }
    
    /**
     * The SSL protocol used for this channel. 
     *   Supported values are "SSL" and "TLS". 
     *   Defaults to "TLS".
     * @return The SSL protocol
     */
    public String getSslProtocol() {
        return sslProtocol;
    }

    /**
     * Set the SSL protocol used for this channel. 
     *   Supported values are "SSL" and "TLS".
     *   Defaults to "TLS". 
     * @param sslProtocol The SSL protocol
     */
    public void setSslProtocol(String sslProtocol) {
        this.sslProtocol = sslProtocol;
    }
    
    /**
     * Set what client authentication level to use, supported
     * values are "yes" or "true" for required authentication, 
     * "want" for wanted authentication and "false" or "none" 
     * for no authentication. Defaults to "none".
     * @param clientAuthReqd The desired authentication level
     */
    public void setClientAuthentication(String clientAuthReqd) {
        if("true".equalsIgnoreCase(clientAuthReqd) 
                || "yes".equalsIgnoreCase(clientAuthReqd)
                || "need".equalsIgnoreCase(clientAuthReqd) ) {
            this.clientAuthReqd = ClientAuth.NEED;
        } else if("want".equalsIgnoreCase(clientAuthReqd)) {
            this.clientAuthReqd = ClientAuth.WANT;
        } else {
            this.clientAuthReqd = ClientAuth.NONE;
        }
    }
    
    /**
     * The password used to load the key
     * @return The password
     */
    public String getKeyPassword() {
        return keyPass;
    }
    
    /**
     * Set the password used to load the key 
     * @param keyPass The password
     */
    public void setKeyPassword(String keyPass) {
        this.keyPass = keyPass;
    }
    
    public File getTruststoreFile() {
        return trustStoreFile;
    }
    
    /**
     * Set the password used to load the trust store 
     * @param trustStoreFile The password
     */
    public void setTruststoreFile(File trustStoreFile) {
        this.trustStoreFile = trustStoreFile;
    }

    /**
     * The password used to load the trust store
     * @return The password
     */
    public String getTruststorePassword() {
        return trustStorePass;
    }

    /**
     * Set the password used to load the trust store 
     * @param trustStorePass The password
     */
    public void setTruststorePassword(String trustStorePass) {
        this.trustStorePass = trustStorePass;
    }
    
    /**
     * The trust store type, defaults to @see {@link KeyStore#getDefaultType()}
     * @return The trust store type
     */
    public String getTruststoreType() {
        return trustStoreType;
    }

    /**
     * Set the trust store type
     * @param keystoreType The trust store type
     */    
    public void setTruststoreType(String trustStoreType) {
        this.trustStoreType = trustStoreType;
    }
    
    /**
     * The algorithm used to open the trust store. 
     * 	 Defaults to "SunX509"
     * @return The trust store algorithm
     */
    public String getTruststoreAlgorithm() {
        return trustStoreAlgorithm;
    }
    
    /**
     * Override the trust store algorithm used to
     *   open the trust store
     * @param trustStoreAlgorithm The trust store algorithm
     */
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
            LOG.debug("Loading key store from \"{}\", using the key store type \"{}\"", keystoreFile.getAbsolutePath(), keystoreType);
            KeyStore keyStore = loadStore(keystoreFile, keystoreType, keystorePass);
            
            KeyStore trustStore;
            if(trustStoreFile != null) {
                LOG.debug("Loading trust store from \"{}\", using the key store type \"{}\"", trustStoreFile.getAbsolutePath(), trustStoreType);
                trustStore = loadStore(trustStoreFile, trustStoreType, trustStorePass);
            } else {
                trustStore = keyStore;
            }
            
            
            String keyPassToUse;
            if(keyPass == null) {
                keyPassToUse = keystorePass;
            } else {
                keyPassToUse = keyPass;
            }
            // initialize key manager factory
            keyManagerFactory = KeyManagerFactory.getInstance(keystoreAlgorithm);
            keyManagerFactory.init(keyStore, keyPassToUse.toCharArray());
            
            // initialize trust manager factory
            trustManagerFactory = TrustManagerFactory.getInstance(trustStoreAlgorithm);
            trustManagerFactory.init(trustStore);
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
     * @see SslConfiguration#getSSLContext(String)
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
    
    /**
     * @see SslConfiguration#getClientAuth()
     */
    public ClientAuth getClientAuth() {
        return clientAuthReqd;
    }

    /**
     * @see SslConfiguration#getSSLContext()
     */
    public SSLContext getSSLContext() throws GeneralSecurityException {
        return getSSLContext(sslProtocol);
    }

    /**
     * @see SslConfiguration#getEnabledCipherSuites()
     */
    public String[] getEnabledCipherSuites() {
        return enabledCipherSuites;
    }
    
    /**
     * Set the allowed cipher suites, note that
     *   the exact list of supported cipher suites
     *   differs between JRE implementations.
     * @param enabledCipherSuites
     */
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
