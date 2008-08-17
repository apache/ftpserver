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

import junit.framework.TestCase;

import org.apache.commons.net.ProtocolCommandEvent;
import org.apache.commons.net.ProtocolCommandListener;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.ftpserver.DefaultFtpServerContext;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.listener.nio.NioListener;
import org.apache.ftpserver.test.TestUtil;
import org.apache.ftpserver.usermanager.PropertiesUserManager;
import org.apache.ftpserver.util.IoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
*
* @author The Apache MINA Project (dev@mina.apache.org)
* @version $Rev$, $Date$
*
*/
public abstract class ClientTestTemplate extends TestCase {

    private final Logger LOG = LoggerFactory
            .getLogger(ClientTestTemplate.class);

    protected static final String ADMIN_PASSWORD = "admin";

    protected static final String ADMIN_USERNAME = "admin";

    protected static final String ANONYMOUS_PASSWORD = "foo@bar.com";

    protected static final String ANONYMOUS_USERNAME = "anonymous";

    protected static final String TESTUSER2_USERNAME = "testuser2";

    protected static final String TESTUSER1_USERNAME = "testuser1";

    protected static final String TESTUSER_PASSWORD = "password";

    protected FtpServer server;

    protected int port = -1;

    protected FTPClient client;

    private static final File USERS_FILE = new File(TestUtil.getBaseDir(),
            "src/test/resources/users.properties");

    private static final File TEST_TMP_DIR = new File("test-tmp");

    protected static final File ROOT_DIR = new File(TEST_TMP_DIR, "ftproot");

    protected FtpServer createServer() throws Exception {
        assertTrue(USERS_FILE.getAbsolutePath() + " must exist", USERS_FILE
                .exists());

        DefaultFtpServerContext context = new DefaultFtpServerContext();

        NioListener listener = new NioListener();
        listener.setPort(port);
        context.setListener("default", listener);

        PropertiesUserManager userManager = new PropertiesUserManager();
        userManager.setAdminName("admin");
        userManager.setEncryptPasswords(false);
        userManager.setPropFile(USERS_FILE);
        userManager.configure();

        context.setUserManager(userManager);

        return new FtpServer(context);
    }

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        initDirs();

        initServer();

        connectClient();
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
        initPort();

        server = createServer();

        if (isStartServer()) {
            server.start();
        }
    }

    protected boolean isStartServer() {
        return true;
    }

    protected FTPClient createFTPClient() throws Exception {
        return new FTPClient();
    }

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

        if (isConnectClient()) {
            doConnect();
        }
    }

    protected void doConnect() throws Exception {
        try {
            client.connect("localhost", port);
        } catch (FTPConnectionClosedException e) {
            // try again
            Thread.sleep(200);
            client.connect("localhost", port);
        }
    }

    protected boolean isConnectClient() {
        return true;
    }

    /**
     * Attempts to find a free port or fallback to a default
     * 
     * @throws IOException
     * 
     * @throws IOException
     */
    private void initPort() throws IOException {
        if (port == -1) {
            port = TestUtil.findFreePort();
        }
    }

    protected void cleanTmpDirs() throws IOException {
        if (TEST_TMP_DIR.exists()) {
            IoUtils.delete(TEST_TMP_DIR);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        if (isConnectClient()) {
            try {
                client.quit();
            } catch (Exception e) {
                // ignore
            }
        }

        if (server != null) {
            server.stop();
        }

        cleanTmpDirs();
    }

}
