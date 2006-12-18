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

package org.apache.ftpserver.perftest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Hashtable;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.ftpserver.ConfigurableFtpServerContext;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.config.PropertiesConfiguration;
import org.apache.ftpserver.interfaces.FtpServerContext;
import org.apache.ftpserver.util.IoUtils;

public abstract class PerformanceTestTemplate extends TestCase {

    public static class ClientRunner implements Runnable {

        private FTPClient client;

        private ClientTask task;

        private int repeats;

        public ClientRunner(FTPClient client, ClientTask task, int repeats) {
            this.client = client;
            this.task = task;
            this.repeats = repeats;
        }

        public void run() {
            for (int round = 0; round < repeats; round++) {
                try {
                    task.doWithClient(client);
                } catch (Exception e) {
                    // activeThreads.remove(Thread.currentThread());
                    // throw new RuntimeException(e);
                    e.printStackTrace();
                }
            }
            activeThreads.remove(Thread.currentThread());
        }
    }

    public static interface ClientTask {
        public void doWithClient(FTPClient client) throws Exception;
    }

    public static final int DEFAULT_PORT = 12321;

    protected static final String ADMIN_PASSWORD = "admin";

    protected static final String ADMIN_USERNAME = "admin";

    protected static final String ANONYMOUS_PASSWORD = "foo@bar.com";

    protected static final String ANONYMOUS_USERNAME = "anonymous";

    protected static final String TESTUSER2_USERNAME = "testuser2";

    protected static Hashtable activeThreads = new Hashtable();

    protected static final String TESTUSER1_USERNAME = "testuser1";

    protected static final String TESTUSER_PASSWORD = "password";

    protected FtpServer server;

    protected int port = DEFAULT_PORT;

    private FtpServerContext serverContext;

    private long startTime;

    private static final File USERS_FILE = new File(getBaseDir(),
            "src/test/users.gen");

    private static final File TEST_TMP_DIR = new File("test-tmp");

    protected static final File ROOT_DIR = new File(TEST_TMP_DIR, "ftproot");

    public static File getBaseDir() {
        // check Maven system prop first and use if set
        String basedir = System.getProperty("basedir");
        if (basedir != null) {
            return new File(basedir);
        } else {
            return new File(".");
        }
    }

    protected Properties createConfig() {
        return createDefaultConfig();
    }

    protected Properties createDefaultConfig() {
        assertTrue(USERS_FILE.getAbsolutePath() + " must exist", USERS_FILE
                .exists());

        Properties configProps = new Properties();
        configProps.setProperty("serverContext.socket-factory.port", Integer
                .toString(port));
        configProps.setProperty("serverContext.user-manager.class",
                "org.apache.ftpserver.usermanager.PropertiesUserManager");
        configProps.setProperty("serverContext.user-manager.admin", "admin");
        configProps.setProperty("serverContext.user-manager.prop-password-encrypt",
                "false");
        configProps.setProperty("serverContext.user-manager.prop-file", USERS_FILE
                .getAbsolutePath());
        configProps.setProperty("serverContext.create-default-user", "false");
        configProps.setProperty("serverContext.connection-manager.max-connection", "0");
        configProps.setProperty("serverContext.connection-manager.max-login", "0");

        return configProps;
    }

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        initDirs();

        initServer();
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
        serverContext = new ConfigurableFtpServerContext(new PropertiesConfiguration(createConfig()));
        server = new FtpServer(serverContext);

        server.start();
        Thread.sleep(200);
    }

    protected void cleanTmpDirs() throws IOException {
        if (TEST_TMP_DIR.exists()) {
            IoUtils.delete(TEST_TMP_DIR);
        }
    }

    /**
     * Ugly but working way of knowing when the test is complete
     */
    protected void waitForTestRun() throws InterruptedException {
        while (true) {
            Thread.sleep(20);

            if (activeThreads.size() == 0) {
                break;
            }
        }
    }

    protected void doClients(ClientTask task, int noOfClients, int repeats) {
        for (int i = 0; i < noOfClients; i++) {
            FTPClient client = new FTPClient();

            String clientId = "client" + i;
            client.addProtocolCommandListener(new FtpClientLogger(clientId));

            Thread clientThread = new Thread(new ClientRunner(client, task,
                    repeats), clientId);
            activeThreads.put(clientThread, client);
            clientThread.start();
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

    protected void endMeasureTime(String testName, int numberOfClients,
            int numberOfRepeats) {
        StringBuffer msg = new StringBuffer();
        msg.append(testName).append(" took ")
            .append(new Date().getTime() - startTime)
            .append(" ms to complete. No of clients: ")
            .append(numberOfClients)
            .append(", number of repeats: ")
            .append(numberOfRepeats);

        System.out.println(msg.toString());
    }

    protected void startMeasureTime() {
        startTime = new Date().getTime();
    }

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        if (server != null) {
            server.stop();
        }

        cleanTmpDirs();
    }

}
