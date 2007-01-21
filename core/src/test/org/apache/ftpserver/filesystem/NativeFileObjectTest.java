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

package org.apache.ftpserver.filesystem;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.apache.ftpserver.ftplet.AuthorizationRequest;
import org.apache.ftpserver.usermanager.BaseUser;
import org.apache.ftpserver.util.IoUtils;

public class NativeFileObjectTest extends TestCase {

    private static final File TEST_TMP_DIR = new File("test-tmp");
    private static final File ROOT_DIR = new File(TEST_TMP_DIR, "ftproot");
    private static final File TEST_DIR1 = new File(ROOT_DIR, "dir1");
    private static final File TEST_FILE1 = new File(ROOT_DIR, "file1");
    private static final File TEST_FILE2_IN_DIR1 = new File(TEST_DIR1, "file2");

    private static final String ROOT_DIR_PATH = ROOT_DIR.getAbsolutePath()
            .replace(File.separatorChar, '/');

    private static final String FULL_PATH = ROOT_DIR_PATH + "/"
            + TEST_DIR1.getName() + "/" + TEST_FILE2_IN_DIR1.getName();

    private static final String FULL_PATH_NO_CURRDIR = ROOT_DIR_PATH + "/"
            + TEST_FILE2_IN_DIR1.getName();

    public static class AlwaysAuthorizedUser extends BaseUser {

        public AuthorizationRequest authorize(AuthorizationRequest request) {
            return request;
        }
        
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        initDirs();

        TEST_DIR1.mkdirs();
        TEST_FILE1.createNewFile();
        TEST_FILE2_IN_DIR1.createNewFile();
    }

    public void testGetPhysicalName() {

        assertEquals(FULL_PATH, NativeFileObject.getPhysicalName(ROOT_DIR_PATH
                + "/", "/" + TEST_DIR1.getName() + "/", TEST_FILE2_IN_DIR1
                .getName()));
        assertEquals("No trailing slash on rootDir", FULL_PATH,
                NativeFileObject.getPhysicalName(ROOT_DIR_PATH, "/"
                        + TEST_DIR1.getName() + "/", TEST_FILE2_IN_DIR1
                        .getName()));
        assertEquals("No leading slash on currDir", FULL_PATH,
                NativeFileObject.getPhysicalName(ROOT_DIR_PATH + "/", TEST_DIR1
                        .getName()
                        + "/", TEST_FILE2_IN_DIR1.getName()));
        assertEquals("No trailing slash on currDir", FULL_PATH,
                NativeFileObject.getPhysicalName(ROOT_DIR_PATH + "/", "/"
                        + TEST_DIR1.getName(), TEST_FILE2_IN_DIR1.getName()));
        assertEquals("No slashes on currDir", FULL_PATH, NativeFileObject
                .getPhysicalName(ROOT_DIR_PATH + "/", TEST_DIR1.getName(),
                        TEST_FILE2_IN_DIR1.getName()));
        assertEquals("Backslashes in rootDir", FULL_PATH, NativeFileObject
                .getPhysicalName(ROOT_DIR.getAbsolutePath() + "/", "/"
                        + TEST_DIR1.getName() + "/", TEST_FILE2_IN_DIR1
                        .getName()));
        assertEquals("Null currDir", FULL_PATH_NO_CURRDIR, NativeFileObject
                .getPhysicalName(ROOT_DIR.getAbsolutePath() + "/", null,
                        TEST_FILE2_IN_DIR1.getName()));
        assertEquals("Empty currDir", FULL_PATH_NO_CURRDIR, NativeFileObject
                .getPhysicalName(ROOT_DIR.getAbsolutePath() + "/", "",
                        TEST_FILE2_IN_DIR1.getName()));
        assertEquals("Absolut fileName in root", FULL_PATH_NO_CURRDIR,
                NativeFileObject.getPhysicalName(ROOT_DIR.getAbsolutePath()
                        + "/", TEST_DIR1.getName(), "/"
                        + TEST_FILE2_IN_DIR1.getName()));
        assertEquals("Absolut fileName in dir1", FULL_PATH, NativeFileObject
                .getPhysicalName(ROOT_DIR.getAbsolutePath() + "/", null, "/"
                        + TEST_DIR1.getName() + "/"
                        + TEST_FILE2_IN_DIR1.getName()));

        assertEquals(". in currDir", FULL_PATH, NativeFileObject
                .getPhysicalName(ROOT_DIR.getAbsolutePath(), TEST_DIR1
                        .getName()
                        + "/./", "/" + TEST_DIR1.getName() + "/"
                        + TEST_FILE2_IN_DIR1.getName()));

    }

    public void testGetPhysicalNameWithRelative() {
        assertEquals(".. in fileName", FULL_PATH_NO_CURRDIR, NativeFileObject
                .getPhysicalName(ROOT_DIR.getAbsolutePath(), TEST_DIR1
                        .getName(), "/../" + TEST_FILE2_IN_DIR1.getName()));
        assertEquals(".. beyond rootDir", FULL_PATH_NO_CURRDIR,
                NativeFileObject.getPhysicalName(ROOT_DIR.getAbsolutePath(),
                        TEST_DIR1.getName(), "/../../"
                                + TEST_FILE2_IN_DIR1.getName()));
    }

    public void testGetPhysicalNameWithTilde() {
        assertEquals(FULL_PATH_NO_CURRDIR, NativeFileObject.getPhysicalName(
                ROOT_DIR.getAbsolutePath(), TEST_DIR1.getName(), "/~/"
                        + TEST_FILE2_IN_DIR1.getName()));
    }

    public void testGetPhysicalNameCaseInsensitive() {
        assertEquals(FULL_PATH, NativeFileObject.getPhysicalName(
                ROOT_DIR.getAbsolutePath(), TEST_DIR1.getName(), TEST_FILE2_IN_DIR1.getName().toUpperCase(), true));
        
        
    }

    
    public void testConstructorWithNullFileName() {
        try{
            new NativeFileObject(null, TEST_FILE2_IN_DIR1, new AlwaysAuthorizedUser());
            fail("Must throw IllegalArgumentException");
        } catch(IllegalArgumentException e) {
            // OK
        }
    }

    public void testEmptyFileName() {
        try{
            new NativeFileObject("", TEST_FILE2_IN_DIR1, new AlwaysAuthorizedUser());
            fail("Must throw IllegalArgumentException");
        } catch(IllegalArgumentException e) {
            // OK
        }
    }

    public void testNonLeadingSlash() {
        try{
            new NativeFileObject("foo", TEST_FILE2_IN_DIR1, new AlwaysAuthorizedUser());
            fail("Must throw IllegalArgumentException");
        } catch(IllegalArgumentException e) {
            // OK
        }
    }

    public void testWhiteSpaceFileName() {
        try{
            new NativeFileObject(" \t", TEST_FILE2_IN_DIR1, new AlwaysAuthorizedUser());
            fail("Must throw IllegalArgumentException");
        } catch(IllegalArgumentException e) {
            // OK
        } 
    }

    public void testConstructorWithNullFile() {
        try{
            new NativeFileObject("foo", null, new AlwaysAuthorizedUser());
            fail("Must throw IllegalArgumentException");
        } catch(IllegalArgumentException e) {
            // OK
        }
    }

    
    public void testFullName() {
        NativeFileObject fileObject = new NativeFileObject("/dir1/file2",
                TEST_FILE2_IN_DIR1, new AlwaysAuthorizedUser());
        assertEquals("/dir1/file2", fileObject.getFullName());

        fileObject = new NativeFileObject("/dir1/", TEST_DIR1, new AlwaysAuthorizedUser());
        assertEquals("/dir1", fileObject.getFullName());

        fileObject = new NativeFileObject("/dir1", TEST_DIR1, new AlwaysAuthorizedUser());
        assertEquals("/dir1", fileObject.getFullName());
    }

    public void testShortName() {
        NativeFileObject fileObject = new NativeFileObject("/dir1/file2",
                TEST_FILE2_IN_DIR1, new AlwaysAuthorizedUser());
        assertEquals("file2", fileObject.getShortName());

        fileObject = new NativeFileObject("/dir1/", TEST_DIR1, new AlwaysAuthorizedUser());
        assertEquals("dir1", fileObject.getShortName());

        fileObject = new NativeFileObject("/dir1", TEST_DIR1, new AlwaysAuthorizedUser());
        assertEquals("dir1", fileObject.getShortName());
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
