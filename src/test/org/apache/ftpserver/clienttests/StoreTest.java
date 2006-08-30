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

import java.io.ByteArrayInputStream;
import java.io.File;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.ftpserver.test.TestUtil;


public class StoreTest extends ClientTestTemplate {
    private static final String TEST_FILENAME = "test.txt";
    private static final String DEFAULT_UNIQUE_FILENAME = "ftp.dat";

    private static final String ADMIN_PASSWORD = "admin";
    private static final String ADMIN_USERNAME = "admin";
    
    private static byte[] testData = null;
    
    /* (non-Javadoc)
     * @see org.apache.ftpserver.clienttests.ClientTestTemplate#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();

        testData = "TESTDATA".getBytes("UTF-8");
        
        client.login(ADMIN_USERNAME, ADMIN_PASSWORD);
    }



    public void testStore() throws Exception {
        File testFile = new File(rootDir, TEST_FILENAME);

        assertTrue(client.storeFile(TEST_FILENAME, new ByteArrayInputStream(testData)));
        
        assertTrue(testFile.exists());
        TestUtil.assertFileEqual(testData, testFile);
    }

    public void testStoreEmptyFile() throws Exception {
        File testFile = new File(rootDir, TEST_FILENAME);
        
        assertTrue(client.storeFile(TEST_FILENAME, new ByteArrayInputStream(new byte[0])));
        
        assertTrue(testFile.exists());
        assertEquals(0, testFile.length());
    }

    public void testStoreWithExistingFile() throws Exception {
        File testFile = new File(rootDir, TEST_FILENAME);
        testFile.createNewFile();
        
        assertTrue(testFile.exists());
        assertEquals(0, testFile.length());

        assertTrue(client.storeFile(TEST_FILENAME, new ByteArrayInputStream(testData)));
        
        assertTrue(testFile.exists());
        TestUtil.assertFileEqual(testData, testFile);
    }

    public void testStoreWithPath() throws Exception {
        File dir = new File(rootDir, "foo/bar");
        dir.mkdirs();
        File testFile = new File(dir, TEST_FILENAME);
        
        assertTrue(client.storeFile("foo/bar/" + TEST_FILENAME, new ByteArrayInputStream(testData)));
        
        assertTrue(testFile.exists());
        TestUtil.assertFileEqual(testData, testFile);
    }

    public void testStoreWithNonExistingPath() throws Exception {
        File testFile = new File(rootDir, TEST_FILENAME);

        assertFalse(client.storeFile("foo/bar/" + TEST_FILENAME, new ByteArrayInputStream(testData)));

        assertFalse(testFile.exists());
    }

    public void testStoreWithoutWriteAccess() throws Exception {
        File testFile = new File(rootDir, TEST_FILENAME);
        
        client.rein();
        client.login("anonymous", "foo@bar.com");
        
        assertFalse(client.storeFile(TEST_FILENAME, new ByteArrayInputStream(testData)));
        assertFalse(testFile.exists());
    }
    
    public void testStoreUniqueWithNoDirectory() throws Exception {

        assertTrue(client.storeUniqueFile(new ByteArrayInputStream(testData)));
        
        doAssertOfUniqueFile(client, rootDir);
    }

    public void testStoreUniqueWithDirectory() throws Exception {
        File dir = new File(rootDir, "foo/bar");
        dir.mkdirs();

        assertTrue(client.storeUniqueFile("foo/bar", new ByteArrayInputStream(testData)));
        
        doAssertOfUniqueFile(client, rootDir);
    }

    public void testStoreUniqueWithDirectoryWithTrailingSlash() throws Exception {
        File dir = new File(rootDir, "foo/bar");
        dir.mkdirs();
        
        assertTrue(client.storeUniqueFile("foo/bar/", new ByteArrayInputStream(testData)));
        
        doAssertOfUniqueFile(client, rootDir);
    }



    /**
     * @throws Exception
     */
    private void doAssertOfUniqueFile(FTPClient client, File dir) throws Exception {
        assertTrue(client.completePendingCommand());
        
        String reply = client.getReplyString();
        String generatedFileName = reply.substring(5, reply.indexOf(':'));
        File testFile = new File(dir, generatedFileName);

        assertTrue(testFile.exists());
        TestUtil.assertFileEqual(testData, testFile);
    }
}
