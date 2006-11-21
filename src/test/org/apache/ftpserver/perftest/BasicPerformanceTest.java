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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

import org.apache.commons.net.ftp.FTPClient;

public class BasicPerformanceTest extends PerformanceTestTemplate {

    private static final File TEST_FILE = new File(ROOT_DIR, "test.txt");

    private static final byte[] TEST_DATA = "TESTDATA".getBytes();
    
    private static final int NUMBER_OF_CLIENTS = 9;
    private static final int NUMBER_OF_REPEATS = 50;
    
    public void testNoop() throws Exception {
        startMeasureTime();
        
        doClients(new ClientTask() {

            public void doWithClient(FTPClient client) throws Exception {
                client.connect("localhost", DEFAULT_PORT);
                client.login(ADMIN_USERNAME, ADMIN_PASSWORD);

                client.noop();
                
                client.quit();
                client.disconnect();
            }
        }, NUMBER_OF_CLIENTS, 50);
        
        waitForTestRun();
        
        endMeasureTime("NOOP", NUMBER_OF_CLIENTS, NUMBER_OF_REPEATS);
    }
    
    public void testRetrive() throws Exception {
        writeDataToFile(TEST_FILE, TEST_DATA);

        startMeasureTime();
        
        doClients(new ClientTask() {

            public void doWithClient(FTPClient client) throws Exception {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                client.connect("localhost", DEFAULT_PORT);
                client.login(ADMIN_USERNAME, ADMIN_PASSWORD);

                client.retrieveFile(TEST_FILE.getName(), baos);
                
                client.quit();
                client.disconnect();

                baos.close();
            }
        }, NUMBER_OF_CLIENTS, 50);
        
        waitForTestRun();
        
        endMeasureTime("RETR", NUMBER_OF_CLIENTS, NUMBER_OF_REPEATS);
    }



    public void testStore() throws Exception {
        final ByteArrayInputStream bais = new ByteArrayInputStream(TEST_DATA);

        startMeasureTime();
        
        doClients(new ClientTask() {

            public void doWithClient(FTPClient client) throws Exception {
                client.connect("localhost", DEFAULT_PORT);
                client.login(ADMIN_USERNAME, ADMIN_PASSWORD);

                client.storeFile(TEST_FILE.getName(), bais);

                client.quit();
                client.disconnect();

            }
        }, 9, 50);

        waitForTestRun();
        
        endMeasureTime("STOR", NUMBER_OF_CLIENTS, NUMBER_OF_REPEATS);
        
        bais.close();
    }

}
