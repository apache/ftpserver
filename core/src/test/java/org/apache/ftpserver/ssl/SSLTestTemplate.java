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
import java.util.Properties;

import javax.net.ssl.SSLException;

import junit.framework.TestCase;

import org.apache.commons.net.ProtocolCommandEvent;
import org.apache.commons.net.ProtocolCommandListener;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.ftpserver.ConfigurableFtpServerContext;
import org.apache.ftpserver.DefaultDataConnectionConfig;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.config.PropertiesConfiguration;
import org.apache.ftpserver.interfaces.FtpServerContext;
import org.apache.ftpserver.listener.mina.MinaListener;
import org.apache.ftpserver.util.IoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SSLTestTemplate extends TestCase {

    private final Logger LOG = LoggerFactory.getLogger(SSLTestTemplate.class);
    
    private static final File USERS_FILE = new File(getBaseDir(), "src/test/resources/users.gen");
    protected static final File FTPCLIENT_KEYSTORE = new File(getBaseDir(), "src/test/resources/client.jks");
    protected static final char[] KEYSTORE_PASSWORD = "password".toCharArray();

    private static final File FTPSERVER_KEYSTORE = new File(getBaseDir(), "src/test/resources/ftpserver.jks");

    private static final int DEFAULT_PORT = 12322;

    protected static final String ADMIN_PASSWORD = "admin";

    protected static final String ADMIN_USERNAME = "admin";

    protected FtpServer server;

    protected int port = DEFAULT_PORT;

    private FtpServerContext context;

    protected FTPSClient client;

    private static final File TEST_TMP_DIR = new File("test-tmp");

    protected static final File ROOT_DIR = new File(TEST_TMP_DIR, "ftproot");

    public static File getBaseDir() {
        // check Maven system prop first and use if set
        String basedir = System.getProperty("basedir");
        if(basedir != null) {
            return new File(basedir);
        } else {
            return new File(".");
        }
    }
    
    protected Properties createConfig() {
        assertTrue(FTPSERVER_KEYSTORE.exists());
        
        Properties configProps = new Properties();
        configProps.setProperty("config.listeners.default.class", MinaListener.class.getName());
        configProps.setProperty("config.listeners.default.port", Integer
                .toString(port));
        configProps.setProperty("config.listeners.default.ssl.class",
                DefaultSsl.class.getName());
        configProps.setProperty("config.listeners.default.ssl.keystore-file",
                FTPSERVER_KEYSTORE.getAbsolutePath());
        configProps.setProperty("config.listeners.default.ssl.keystore-password",
                "password");
        configProps
                .setProperty("config.listeners.default.ssl.ssl-protocol", getAuthValue());
        configProps.setProperty(
                "config.listeners.default.ssl.client-authentication", getClientAuth());
        configProps.setProperty("config.listeners.default.ssl.key-password",
                "password");

        configProps.setProperty("config.listeners.default.data-connection.class",
                DefaultDataConnectionConfig.class.getName());
        configProps.setProperty("config.listeners.default.data-connection.ssl.class",
                DefaultSsl.class.getName());
        configProps.setProperty("config.listeners.default.data-connection.ssl.keystore-file",
                FTPSERVER_KEYSTORE.getAbsolutePath());
        configProps.setProperty("config.listeners.default.data-connection.ssl.keystore-password",
        "password");
        configProps.setProperty("config.listeners.default.data-connection.ssl.ssl-protocol",
                getAuthValue());
        configProps.setProperty(
                "config.listeners.default.data-connection.ssl.client-authentication", getClientAuth());
        configProps.setProperty("config.listeners.default.data-connection.ssl.key-password",
        "password");
        
        
        configProps.setProperty("config.user-manager.class",
                "org.apache.ftpserver.usermanager.PropertiesUserManager");
        configProps.setProperty("config.user-manager.admin", "admin");
        configProps.setProperty("config.user-manager.prop-password-encrypt",
                "false");
        configProps.setProperty("config.user-manager.prop-file",
                USERS_FILE.getAbsolutePath());
        configProps.setProperty("config.create-default-user", "false");


        return configProps;
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
        initDirs();

        initServer();

        initClientKeystores(); 

        
        connectClient();
    }

    /**
     * 
     */
    private void initClientKeystores() {
        assertTrue(FTPCLIENT_KEYSTORE.exists());
        
        System.setProperty("javax.net.ssl.keyStore", FTPCLIENT_KEYSTORE.getAbsolutePath());
        System.setProperty("javax.net.ssl.keyStorePassword", "password"); 

        
        System.setProperty("javax.net.ssl.trustStore", FTPCLIENT_KEYSTORE.getAbsolutePath());
        System.setProperty("javax.net.ssl.trustStorePassword", "password");

    }

    /**
     * @throws IOException
     */
    protected void initDirs() throws IOException {
        cleanTmpDirs();

        TEST_TMP_DIR.mkdirs();
        ROOT_DIR.mkdirs();
    }

    /**
     * @throws IOException
     * @throws Exception
     */
    protected void initServer() throws IOException, Exception {
        context = new ConfigurableFtpServerContext(new PropertiesConfiguration(createConfig()));
        server = new FtpServer(context);

        server.start();
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

    /**
     * @throws Exception
     */
    protected void connectClient() throws Exception {
        client = createFTPClient();
        client.addProtocolCommandListener(new ProtocolCommandListener() {

            public void protocolCommandSent(ProtocolCommandEvent event) {
                LOG.debug("> " + event.getMessage().trim());

            }

            public void protocolReplyReceived(ProtocolCommandEvent event) {
                LOG.debug("< " + event.getMessage().trim());
            }
        });

        doConnect();
    }

    protected void doConnect() throws Exception {
        int attempts = 0;
        
        Exception lastException = null;
        while(attempts < 5) {
            try {
                client.connect("localhost", port);
                lastException = null;
                break;
            } catch (SSLException e) {
                // try again
                lastException = e;
            } catch (FTPConnectionClosedException e) {
                // try again
                lastException = e;
            }
            
            System.out.println("Retrying!");
            Thread.sleep(500);
            attempts++;
        }
        
        if(lastException != null) {
        	lastException.printStackTrace();
            throw lastException;
        }
    }

    protected void cleanTmpDirs() throws IOException {
        if (TEST_TMP_DIR.exists()) {
            IoUtils.delete(TEST_TMP_DIR);
        }
    }

    protected void writeDataToFile(File file, byte[] data) throws IOException {
        FileOutputStream fos = null;

        try {
            fos = new FileOutputStream(file);

            fos.write(data);
        } finally {
            IoUtils.close(fos);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        
        client.disconnect();

        if (server != null) {
            server.stop();
        }

        cleanTmpDirs();
    }

}
