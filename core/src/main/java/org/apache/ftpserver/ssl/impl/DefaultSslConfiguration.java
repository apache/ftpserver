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

package org.apache.ftpserver.ssl.impl;

import java.security.GeneralSecurityException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509KeyManager;

import org.apache.ftpserver.ssl.ClientAuth;
import org.apache.ftpserver.ssl.SslConfiguration;
import org.apache.ftpserver.ssl.SslConfigurationFactory;
import org.apache.ftpserver.util.ClassUtils;

/**
 * <strong>Internal class, do not use directly.</strong>
 * 
 * Used to configure the SSL settings for the control channel or the data
 * channel.
 * 
 * <strong><strong>Internal class, do not use directly.</strong></strong>
 *
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 */
public class DefaultSslConfiguration implements SslConfiguration {

    private KeyManagerFactory keyManagerFactory;

    private TrustManagerFactory trustManagerFactory;

    private String sslProtocol = "TLS";

    private ClientAuth clientAuth = ClientAuth.NONE;

    private String keyAlias;

    private String[] enabledCipherSuites;

    /**
     * Internal constructor, do not use directly. Instead, use {@link SslConfigurationFactory}
     */
    public DefaultSslConfiguration(KeyManagerFactory keyManagerFactory,
            TrustManagerFactory trustManagerFactory, ClientAuth clientAuthReqd,
            String sslProtocol, String[] enabledCipherSuites, String keyAlias) {
        super();
        this.clientAuth = clientAuthReqd;
        this.enabledCipherSuites = enabledCipherSuites;
        this.keyAlias = keyAlias;
        this.keyManagerFactory = keyManagerFactory;
        this.sslProtocol = sslProtocol;
        this.trustManagerFactory = trustManagerFactory;
    }

    /**
     * @see SslConfiguration#getSSLContext(String)
     */
    public SSLContext getSSLContext(String protocol)
            throws GeneralSecurityException {

        // null value check
        if (protocol == null) {
            protocol = sslProtocol;
        }

        KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();

        // wrap key managers to allow us to control their behavior
        // (FTPSERVER-93)
        for (int i = 0; i < keyManagers.length; i++) {
            if (ClassUtils.extendsClass(keyManagers[i].getClass(),
                    "javax.net.ssl.X509ExtendedKeyManager")) {
                keyManagers[i] = new ExtendedAliasKeyManager(keyManagers[i],
                        keyAlias);
            } else if (keyManagers[i] instanceof X509KeyManager) {
                keyManagers[i] = new AliasKeyManager(keyManagers[i], keyAlias);
            }
        }

        // create SSLContext
        // TODO revisit if we need caching of contexts.
        SSLContext ctx = SSLContext.getInstance(protocol);

        ctx.init(keyManagers, trustManagerFactory.getTrustManagers(), null);

        return ctx;
    }

    /**
     * @see SslConfiguration#getClientAuth()
     */
    public ClientAuth getClientAuth() {
        return clientAuth;
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
        if (enabledCipherSuites != null) {
            return enabledCipherSuites.clone();
        } else {
            return null;
        }
    }
}
