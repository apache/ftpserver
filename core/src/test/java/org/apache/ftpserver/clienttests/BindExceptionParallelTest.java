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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.ftpserver.ConnectionConfigFactory;
import org.apache.ftpserver.DataConnectionConfigurationFactory;
import org.apache.ftpserver.test.TestUtil;

/**
*
* From FTPSERVER-250
*
* @author The Apache MINA Project (dev@mina.apache.org)*
*/
public class BindExceptionParallelTest extends ClientTestTemplate {
    private static final int NUMBER_OF_CLIENTS = 2;
    private List<FTPClient> clients;
    
    private int port = TestUtil.findFreePort(2020);

    public BindExceptionParallelTest() throws IOException {
        // default cstr
    }
    
    @Override
    protected FTPClient createFTPClient() throws Exception {
        FTPClient c = super.createFTPClient();
        c.setDataTimeout(1000);
        return c;
    }

    @Override
    protected void connectClient() throws Exception {
        clients = new ArrayList<FTPClient>();

        for (int i = 0; i < NUMBER_OF_CLIENTS; i++) {
            super.connectClient();
            client.login(ADMIN_USERNAME, ADMIN_PASSWORD);
            clients.add(client);
        }

        client = null;
    }

    @Override
    protected ConnectionConfigFactory createConnectionConfigFactory() {
        ConnectionConfigFactory factory = super.createConnectionConfigFactory();
        factory.setMaxLogins(0);
        return factory;
    }

    @Override
    protected DataConnectionConfigurationFactory createDataConnectionConfigurationFactory() {
        DataConnectionConfigurationFactory factory = super.createDataConnectionConfigurationFactory();
        factory.setActiveLocalPort(port);
        factory.setActiveLocalAddress("localhost");
        return factory;
    }

    public void testParallelExecution() throws Exception {
        final BindExceptionTestFailStatus testFailStatus = new BindExceptionTestFailStatus();
        for (int i = 0; i < NUMBER_OF_CLIENTS; i++) {
            final int c = i;
            new Thread("client" + (i + 1)) {
                @Override
                public void run() {
                    try {
                        System.out.println(" -- " + getName() + " before command");
                        System.out.println("--> " + getName() + " " + Arrays.asList(clients.get(c).listFiles()));
                        System.out.println(" -- " + getName() + " command ok");
                    } catch (IOException e) {
                        System.err.println(" xx " + getName() + " command ko");
                        e.printStackTrace();
                        testFailStatus.failed = true;
                    }
                }
            }.start();
        }

        // make time to threads to finish its work
        Thread.sleep(3000);

        assertFalse(testFailStatus.failed);
    }

    @Override
    protected void tearDown() throws Exception {
        for (int i = 1; i < NUMBER_OF_CLIENTS; i++)
            if (isConnectClient()) {
                try {
                    clients.get(i).quit();
                } catch (Exception e) {
                    // ignore
                }
            }
        client = clients.get(0);
        super.tearDown();
    }

    private static final class BindExceptionTestFailStatus {
        public boolean failed = false;
    }
}