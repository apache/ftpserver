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
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.net.ftp.FTPSClient;
import org.apache.ftpserver.DefaultDataConnectionConfig;
import org.apache.ftpserver.DefaultFtpServerContext;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.clienttests.ClientTestTemplate;
import org.apache.ftpserver.listener.mina.MinaListener;
import org.apache.ftpserver.test.TestUtil;
import org.apache.ftpserver.util.IoUtils;

public abstract class SSLTestTemplate extends ClientTestTemplate {

    protected static final File FTPCLIENT_KEYSTORE = new File(TestUtil.getBaseDir(), "src/test/resources/client.jks");
    protected static final String KEYSTORE_PASSWORD = "password";

    private static final File FTPSERVER_KEYSTORE = new File(TestUtil.getBaseDir(), "src/test/resources/ftpserver.jks");

    protected FtpServer createServer() throws Exception {
        assertTrue(FTPSERVER_KEYSTORE.exists());

        FtpServer server = super.createServer();
        DefaultFtpServerContext context = (DefaultFtpServerContext) server.getServerContext();
        MinaListener listener = (MinaListener) context.getListener("default");
        
        listener.setImplicitSsl(useImplicit());
        
        DefaultSslConfiguration sslConfig = new DefaultSslConfiguration();
        sslConfig.setKeystoreFile(FTPSERVER_KEYSTORE);
        sslConfig.setKeystorePassword(KEYSTORE_PASSWORD);
        sslConfig.setSslProtocol(getAuthValue());
        sslConfig.setClientAuthentication(getClientAuth());
        sslConfig.setKeyPassword(KEYSTORE_PASSWORD);

        listener.setSsl(sslConfig);
        
        DefaultSslConfiguration dataSslConfig = new DefaultSslConfiguration();
        dataSslConfig.setKeystoreFile(FTPSERVER_KEYSTORE);
        dataSslConfig.setKeystorePassword("password");
        dataSslConfig.setSslProtocol(getAuthValue());
        dataSslConfig.setClientAuthentication(getClientAuth());
        dataSslConfig.setKeyPassword("password");
        
        DefaultDataConnectionConfig dataConfig = new DefaultDataConnectionConfig();
        dataConfig.setSsl(dataSslConfig);
        
        listener.setDataConnectionConfig(dataConfig);
        
        return server;
    }

    protected boolean useImplicit() {
        return false;
    }
    
    protected String getClientAuth() {
        return "false";
    }

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
    	initClientKeystores(); 

    	super.setUp();
    }

    /**
     * 
     */
    private void initClientKeystores() {
        assertTrue(FTPCLIENT_KEYSTORE.exists());
        
        System.setProperty("javax.net.ssl.keyStore", FTPCLIENT_KEYSTORE.getAbsolutePath());
        System.setProperty("javax.net.ssl.keyStorePassword", KEYSTORE_PASSWORD); 

        
        System.setProperty("javax.net.ssl.trustStore", FTPCLIENT_KEYSTORE.getAbsolutePath());
        System.setProperty("javax.net.ssl.trustStorePassword", KEYSTORE_PASSWORD);
    }

    protected FTPSClient createFTPClient() throws Exception {
        FTPSClient ftpsClient = new FTPSClient();
        
        String auth = getAuthValue();
        if(auth != null) {
            ftpsClient.setAuthValue(auth);
        }
        return ftpsClient;
    }

    protected abstract String getAuthValue();

    protected void writeDataToFile(File file, byte[] data) throws IOException {
        FileOutputStream fos = null;

        try {
            fos = new FileOutputStream(file);

            fos.write(data);
        } finally {
            IoUtils.close(fos);
        }
    }


}
