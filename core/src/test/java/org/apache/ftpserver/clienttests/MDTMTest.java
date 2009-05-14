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
import java.text.SimpleDateFormat;
import java.util.Date;

/**
*
* @author The Apache MINA Project (dev@mina.apache.org)
*
*/
public class MDTMTest extends ClientTestTemplate {
    private static final SimpleDateFormat FTP_DATE_FORMAT = new SimpleDateFormat(
            "yyyyMMddHHmmss.SSS");

    private static final File TEST_FILE1 = new File(ROOT_DIR, "test1.txt");

    private static final File TEST_DIR1 = new File(ROOT_DIR, "dir1");

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.ftpserver.clienttests.ClientTestTemplate#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();

        client.login(ADMIN_USERNAME, ADMIN_PASSWORD);
    }

    public void testMDTMForFile() throws Exception {
        assertFalse(TEST_FILE1.exists());
        assertTrue(TEST_FILE1.createNewFile());

        Date expected = new Date(TEST_FILE1.lastModified());
        assertEquals(213, client.sendCommand("MDTM " + TEST_FILE1.getName()));

        Date actual = FTP_DATE_FORMAT.parse(client.getReplyString()
                .substring(4).trim());
        assertEquals(expected, actual);
    }

    public void testMDTMForDir() throws Exception {
        assertFalse(TEST_DIR1.exists());
        assertTrue(TEST_DIR1.mkdirs());

        Date expected = new Date(TEST_DIR1.lastModified());
        assertEquals(213, client.sendCommand("MDTM " + TEST_DIR1.getName()));

        Date actual = FTP_DATE_FORMAT.parse(client.getReplyString()
                .substring(4).trim());
        assertEquals(expected, actual);
    }

    public void testMDTMForNonExistingFile() throws Exception {
        assertEquals(550, client.sendCommand("MDTM " + TEST_FILE1.getName()));
    }

    public void testMDTMWithNoFileName() throws Exception {
        assertEquals(501, client.sendCommand("MDTM"));
    }
}
