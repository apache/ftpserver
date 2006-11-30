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

import java.io.FileInputStream;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;

import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;


public class ClientAuthTest extends SSLTestTemplate {

    
    
    protected void setUp() throws Exception {
        super.setUp();

        client.login(ADMIN_USERNAME, ADMIN_PASSWORD);
    }

    /* (non-Javadoc)
     * @see org.apache.ftpserver.ssl.SSLTestTemplate#createFTPClient()
     */
    protected FTPSClient createFTPClient() throws Exception {
        FTPSClient client = new FTPSClient();
        client.setNeedClientAuth(true);
        
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream(FTPCLIENT_KEYSTORE), KEYSTORE_PASSWORD);
        
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, KEYSTORE_PASSWORD);

        client.setKeyManager(kmf.getKeyManagers()[0]);

        return client;
    }

    protected String getAuthValue() {
        return "TLS";
    }
    
    protected String getClientAuth() {
        return "true";
    }
    
    public void testCommandChannel() throws Exception {
        assertTrue(FTPReply.isPositiveCompletion(client.noop()));
    }

}
