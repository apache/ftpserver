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

package org.apache.ftpserver.filesystem.nativefs.impl;

import java.io.File;
import java.io.IOException;

import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.util.IoUtils;

/**
*
* @author <a href="http://mina.apache.org">Apache MINA Project</a>*
*/
public class NativeFileSystemViewTest extends FileSystemViewTemplate {

    private static final File TEST_TMP_DIR = new File("test-tmp");

    private static final File ROOT_DIR = new File(TEST_TMP_DIR, "ftproot");

    private static final File TEST_DIR1 = new File(ROOT_DIR, DIR1_NAME);

    protected void setUp() throws Exception {
        initDirs();

        TEST_DIR1.mkdirs();

        user.setHomeDirectory(ROOT_DIR.getAbsolutePath());
    }

    public void testConstructor() throws FtpException {
        NativeFileSystemView view = new NativeFileSystemView(user);
        assertEquals("/", view.getWorkingDirectory().getAbsolutePath());
    }

    public void testConstructorWithNullUser() throws FtpException {
        try {
            new NativeFileSystemView(null);
            fail("Must throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // OK
        }
    }

    public void testConstructorWithNullHomeDir() throws FtpException {
        user.setHomeDirectory(null);
        try {
            new NativeFileSystemView(user);
            fail("Must throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // OK
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        cleanTmpDirs();
    }

    /**
     * @throws IOException
     */
    protected void initDirs() throws IOException {
        cleanTmpDirs();

        TEST_TMP_DIR.mkdirs();
        ROOT_DIR.mkdirs();
    }

    protected void cleanTmpDirs() throws IOException {
        if (TEST_TMP_DIR.exists()) {
            IoUtils.delete(TEST_TMP_DIR);
        }
    }

}
