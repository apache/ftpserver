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

import java.io.ByteArrayInputStream;
import java.io.File;

import javax.net.ssl.SSLServerSocketFactory;

import org.apache.commons.net.ftp.FTPReply;


public abstract class ExplicitSecurityTestTemplate extends SSLTestTemplate {

    private static final File TEST_FILE1 = new File(ROOT_DIR, "test1.txt");
    private static final File TEST_FILE2 = new File(ROOT_DIR, "test2.txt");
    private static final byte[] TEST_DATA = "TESTDATA".getBytes();
    
    protected void setUp() throws Exception {
        super.setUp();

        client.login(ADMIN_USERNAME, ADMIN_PASSWORD);
    }

    /**
     * Tests that we can send command over the command channel.
     * This is, in fact already tested by login in setup but 
     * an explicit test is good anyways.
     */
    public void testCommandChannel() throws Exception {
        assertTrue(FTPReply.isPositiveCompletion(client.noop()));
    }

    public void testStoreWithProtPInPassiveMode() throws Exception {
        client.setRemoteVerificationEnabled(false);
        client.enterLocalPassiveMode();
        
        client.execPROT("P");
        
        client.storeFile(TEST_FILE1.getName(), new ByteArrayInputStream(TEST_DATA));
        
        assertTrue(TEST_FILE1.exists());
        assertEquals(TEST_DATA.length, TEST_FILE1.length());
    }

    public void testStoreWithProtPAndReturnToProtCInPassiveMode() throws Exception {
        client.setRemoteVerificationEnabled(false);
        client.enterLocalPassiveMode();
        
        client.execPROT("P");
        
        client.storeFile(TEST_FILE1.getName(), new ByteArrayInputStream(TEST_DATA));
        
        assertTrue(TEST_FILE1.exists());
        assertEquals(TEST_DATA.length, TEST_FILE1.length());

        client.execPROT("C");
        
        client.storeFile(TEST_FILE2.getName(), new ByteArrayInputStream(TEST_DATA));
        
        assertTrue(TEST_FILE2.exists());
        assertEquals(TEST_DATA.length, TEST_FILE2.length());
    }

    public void testStoreWithProtPInActiveMode() throws Exception {
        // needed due to bug in commons-net
        client.setServerSocketFactory(SSLServerSocketFactory.getDefault());

        client.execPROT("P");
        
        client.storeFile(TEST_FILE1.getName(), new ByteArrayInputStream(TEST_DATA));
        
        assertTrue(TEST_FILE1.exists());
        assertEquals(TEST_DATA.length, TEST_FILE1.length());
    }

    public void testStoreWithProtPAndReturnToProtCInActiveMode() throws Exception {
        // needed due to bug in commons-net
        client.setServerSocketFactory(SSLServerSocketFactory.getDefault());
        
        client.execPROT("P");
        
        client.storeFile(TEST_FILE1.getName(), new ByteArrayInputStream(TEST_DATA));
        
        assertTrue(TEST_FILE1.exists());
        assertEquals(TEST_DATA.length, TEST_FILE1.length());

        // needed due to bug in commons-net
        client.setServerSocketFactory(null);
        
        client.execPROT("C");
        
        client.storeFile(TEST_FILE2.getName(), new ByteArrayInputStream(TEST_DATA));
        
        assertTrue(TEST_FILE2.exists());
        assertEquals(TEST_DATA.length, TEST_FILE2.length());
    }
}
