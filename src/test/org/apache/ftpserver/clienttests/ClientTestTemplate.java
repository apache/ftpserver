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

package org.apache.ftpserver.clienttests;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Properties;

import org.apache.commons.net.ProtocolCommandEvent;
import org.apache.commons.net.ProtocolCommandListener;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.ftpserver.FtpConfigImpl;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.config.PropertiesConfiguration;
import org.apache.ftpserver.interfaces.IFtpConfig;
import org.apache.ftpserver.util.IoUtils;
import org.apache.log4j.Logger;

import junit.framework.TestCase;

public abstract class ClientTestTemplate extends TestCase {

    private static final Logger log = Logger.getLogger(ClientTestTemplate.class);
    
    protected static final String ADMIN_PASSWORD = "admin";
    protected static final String ADMIN_USERNAME = "admin";
    protected static final String ANONYMOUS_PASSWORD = "foo@bar.com";
    protected static final String ANONYMOUS_USERNAME = "anonymous";
    protected static final String TESTUSER2_USERNAME = "testuser2";
    protected static final String TESTUSER1_USERNAME = "testuser1";
    protected static final String TESTUSER_PASSWORD = "password";

    
    private static final int FALLBACK_PORT = 12321;

    private FtpServer server;

    protected int port = -1;

    private IFtpConfig config;

    protected FTPClient client;

    private static final File TEST_TMP_DIR = new File("test-tmp");
    protected static File ROOT_DIR = new File(TEST_TMP_DIR, "ftproot");
    
    protected Properties createConfig() {
        return createDefaultConfig();
    }

    protected Properties createDefaultConfig() {
        Properties configProps = new Properties();
        configProps.setProperty("config.socket-factory.port", Integer
                .toString(port));
        configProps.setProperty("config.user-manager.class",
                "org.apache.ftpserver.usermanager.PropertiesUserManager");
        configProps.setProperty("config.user-manager.admin", "admin");
        configProps.setProperty("config.user-manager.prop-password-encrypt", "false");
        configProps.setProperty("config.user-manager.prop-file",
                "src/test/users.gen");
        configProps.setProperty("config.create-default-user", "false");

        return configProps;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        cleanTmpDirs();
        
        TEST_TMP_DIR.mkdirs();
        ROOT_DIR.mkdirs();
        
        initPort();

        config = new FtpConfigImpl(new PropertiesConfiguration(createConfig()));
        server = new FtpServer(config);
        server.start();

        client = new FTPClient();
        client.addProtocolCommandListener(new ProtocolCommandListener(){

            public void protocolCommandSent(ProtocolCommandEvent event) {
                log.debug("> " + event.getMessage().trim());
                
            }

            public void protocolReplyReceived(ProtocolCommandEvent event) {
                log.debug("< " + event.getMessage().trim());
            }});
        
        try{
            client.connect("localhost", port);
        } catch(FTPConnectionClosedException e) {
            // tryu again
            client.connect("localhost", port);
        }
    }

    /**
     * Attempts to find a free port or fallback to a default
     * 
     * @throws IOException
     */
    private void initPort() {
        if (port == -1) {
            ServerSocket tmpSocket = null;
            try {
                tmpSocket = new ServerSocket();
                tmpSocket.bind(null);
                port = tmpSocket.getLocalPort();
            } catch (IOException e) {
                port = FALLBACK_PORT;
            } finally {
                if (tmpSocket != null) {
                    try {
                        tmpSocket.close();
                    } catch (IOException e) {
                        // ignore
                    }
                }
            }
        }
    }

    private void cleanTmpDirs() throws IOException {
        IoUtils.delete(TEST_TMP_DIR);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        server.stop();
        
        cleanTmpDirs();
    }

}
