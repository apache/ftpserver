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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.ftpserver.test.TestUtil;
import org.apache.ftpserver.util.IoUtils;


public class RetrieveTest extends ClientTestTemplate {
    private static final String TEST_FILENAME = "test.txt";

    private static byte[] testData = null;
    
    /* (non-Javadoc)
     * @see org.apache.ftpserver.clienttests.ClientTestTemplate#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();

        testData = "TESTDATA".getBytes("UTF-8");
        
        client.login(ADMIN_USERNAME, ADMIN_PASSWORD);
        client.setRemoteVerificationEnabled(false);
        client.enterLocalPassiveMode();
    }


    private void writeDataToFile(File file, byte[] data) throws IOException {
        FileOutputStream fos = null;
        
        try{
            fos = new FileOutputStream(file);
            
            fos.write(data);
        } finally {
            IoUtils.close(fos);
        }
    }

    public void testRetrieve() throws Exception {
        File testFile = new File(ROOT_DIR, TEST_FILENAME);
        
        writeDataToFile(testFile, testData);

        assertTrue(testFile.exists());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        assertTrue(client.retrieveFile(TEST_FILENAME, baos));
        
        assertTrue(testFile.exists());
        TestUtil.assertArraysEqual(testData, baos.toByteArray());
    }

    public void testRetrieveWithPath() throws Exception {
        File dir = new File(ROOT_DIR, "foo/bar");
        dir.mkdirs();
        
        File testFile = new File(dir, TEST_FILENAME);
        
        writeDataToFile(testFile, testData);
        
        assertTrue(testFile.exists());
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        assertTrue(client.retrieveFile("foo/bar/" + TEST_FILENAME, baos));
        
        assertTrue(testFile.exists());
        TestUtil.assertArraysEqual(testData, baos.toByteArray());
    }

    public void testRetrieveNonExistingFile() throws Exception {
        File testFile = new File(ROOT_DIR, TEST_FILENAME);
        
        assertFalse(testFile.exists());
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        assertFalse(client.retrieveFile(TEST_FILENAME, baos));
    }
}
