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

package org.apache.ftpserver;

import java.io.File;
import java.io.StringWriter;
import java.util.Date;
import java.util.Formatter;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;

import org.apache.ftpserver.filesystem.NativeFileSystemView;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.test.OS;
import org.apache.ftpserver.test.TestUtil;
import org.apache.ftpserver.usermanager.BaseUser;
import org.apache.ftpserver.util.DateUtils;
import org.apache.ftpserver.util.IoUtils;

import sun.management.StringFlag;

public class DirectoryListerTest extends TestCase {
    private static final String DELIMITER = "\\r\\n";
    private static final String LIST_PATTERN_TEMPLATE = "^%sr--------\\s\\s\\s%s\\suser\\sgroup\\s+%s\\s%s\\s%s$";
    
    private static final File TEST_TMP_DIR = new File("test-tmp");
    protected static final File ROOT_DIR = new File(TEST_TMP_DIR, "ftproot");
    
    private static final File TEST_FILE1 = new File(ROOT_DIR, "test1.txt");

    private static final File TEST_DIR1 = new File(ROOT_DIR, "dir1");
    private static final File TEST_DIR2 = new File(ROOT_DIR, "dir2");

    private static final File TEST_FILE1_IN_DIR1 = new File(TEST_DIR1, "test3.txt");
    private static final File TEST_FILE2_IN_DIR1 = new File(TEST_DIR1, "test4.txt");
    private static final File TEST_DIR_IN_DIR1 = new File(TEST_DIR1, "dir3");

    private static final byte[] TEST_DATA = "TESTDATA".getBytes();
    
    private static final String[] MONTHS = new String[]{
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    };
    
    private DirectoryLister directoryLister;

    private FileSystemView fileSystemView;

    protected void setUp() throws Exception {
        BaseUser baseUser = new BaseUser();
        baseUser.setHomeDirectory(ROOT_DIR.getAbsolutePath());
        fileSystemView = new NativeFileSystemView(baseUser) {};
        directoryLister = new DirectoryLister(fileSystemView);
        
        assertTrue(ROOT_DIR.mkdirs());
        assertTrue(TEST_DIR1.mkdirs());
        assertTrue(TEST_DIR2.mkdirs());
        TestUtil.writeDataToFile(TEST_FILE1, TEST_DATA);
        TestUtil.writeDataToFile(TEST_FILE1_IN_DIR1, TEST_DATA);
        TEST_FILE2_IN_DIR1.createNewFile();
        assertTrue(TEST_DIR_IN_DIR1.mkdir());
    }

    /*
     * Test method for 'org.apache.ftpserver.DirectoryLister.parse(String)'
     */
    public void testParse() {
        assertEquals(true, directoryLister.parse("-ls /abcd"));
        assertEquals("/abcd", directoryLister.file);
        assertEquals(true, directoryLister.isDetailOption);
        assertEquals(false, directoryLister.isAllOption);

        assertEquals(true, directoryLister.parse("-ls /ab cd"));
        assertEquals("/ab cd", directoryLister.file);
        assertEquals(true, directoryLister.isDetailOption);
        assertEquals(false, directoryLister.isAllOption);
    }
    
    public void testLIST() throws Exception {
        StringWriter writer = new StringWriter();
        assertTrue(directoryLister.doLIST(TEST_DIR1.getName(), writer));
        
        String[] lines = writer.toString().split(DELIMITER);

        assertEquals(3, lines.length);
        assertLISTLine(lines[0], TEST_DIR_IN_DIR1);
        assertLISTLine(lines[1], TEST_FILE1_IN_DIR1);
        assertLISTLine(lines[2], TEST_FILE2_IN_DIR1);
    }
    
    public void testLISTOnFile() throws Exception {
        StringWriter writer = new StringWriter();
        assertTrue(directoryLister.doLIST(TEST_DIR1.getName() + "/" + TEST_FILE1_IN_DIR1.getName(), writer));
        
        String[] lines = writer.toString().split(DELIMITER);

        assertEquals(1, lines.length);
        assertLISTLine(lines[0], TEST_FILE1_IN_DIR1);
    }
    
    /**
     * Should show the exact same output
     */
    public void testLISTLong() throws Exception {
        StringWriter shortWriter = new StringWriter();
        StringWriter longWriter = new StringWriter();

        assertTrue(directoryLister.doLIST(TEST_DIR1.getName(), shortWriter));
        assertTrue(directoryLister.doLIST("-l " + TEST_DIR1.getName(), longWriter));
        
        assertEquals(shortWriter.toString(), longWriter.toString());
    }
    
    public void testLISTEmptyDir() throws Exception {
        StringWriter writer = new StringWriter();
        assertTrue(directoryLister.doLIST(TEST_DIR2.getName(), writer));

        assertEquals(0, writer.toString().trim().length());
    }

    /**
     * This test only works on Linux as there is no way
     * of creating a hidden file in Java on Windows
     */
    public void testLISTHiddenFileNotShown() throws Exception {
        if(!OS.isFamilyUnix()) {
            return;
        }
        
        File hiddenFile = new File(TEST_DIR1, ".hidden.txt");
        hiddenFile.createNewFile();
        
        StringWriter writer = new StringWriter();
        assertTrue(directoryLister.doLIST(TEST_DIR1.getName(), writer));
        
        String[] lines = writer.toString().split(DELIMITER);
        
        assertEquals(3, lines.length);
        assertLISTLine(lines[0], TEST_DIR_IN_DIR1);
        assertLISTLine(lines[1], TEST_FILE1_IN_DIR1);
        assertLISTLine(lines[2], TEST_FILE2_IN_DIR1);
    }
    
    /**
     * This test only works on Linux as there is no way
     * of creating a hidden file in Java on Windows
     */
    public void testLISTHiddenFileShown() throws Exception {
        if(!OS.isFamilyUnix()) {
            return;
        }
        
        File hiddenFile = new File(TEST_DIR1, ".hidden.txt");
        hiddenFile.createNewFile();
        
        StringWriter writer = new StringWriter();
        assertTrue(directoryLister.doLIST("-a " + TEST_DIR1.getName(), writer));
        
        String[] lines = writer.toString().split(DELIMITER);
        
        assertEquals(4, lines.length);
        assertLISTLine(lines[0], TEST_DIR_IN_DIR1);
        assertLISTLine(lines[1], hiddenFile);
        assertLISTLine(lines[2], TEST_FILE1_IN_DIR1);
        assertLISTLine(lines[3], TEST_FILE2_IN_DIR1);
    }

    
    private void assertLISTLine(String actual, File file) {
        String[] args = new String[5];
        args[0] = file.isDirectory() ? "d" : "-";
        args[1] = file.isDirectory() ? "3" : "1";
        args[2] = Long.toString(file.length());
        args[3] = DateUtils.getUnixDate(file.lastModified()).replace(" ", "\\s");
        args[4] = file.getName();
       
        String pattern = String.format(LIST_PATTERN_TEMPLATE, args);
       
        assertTrue(Pattern.matches(pattern, actual));
    }
    
    public void testMLSD() throws Exception {
        StringWriter writer = new StringWriter();
        directoryLister.doMLSD(TEST_DIR1.getName(), writer);
        
        String[] lines = writer.toString().split(DELIMITER);
        
        assertEquals(3, lines.length);
        assertMLSTLine(lines[0], TEST_DIR_IN_DIR1);
        assertMLSTLine(lines[1], TEST_FILE1_IN_DIR1);
        assertMLSTLine(lines[2], TEST_FILE2_IN_DIR1);
        
    }
    
    public void testMLST() throws Exception {
        StringWriter writer = new StringWriter();
        directoryLister.doMLST(TEST_FILE1.getName(), writer);
        
        assertMLSTLine(writer.toString(), TEST_FILE1);
    }
    
    private void assertMLSTLine(String actual, File file) { 
        StringTokenizer tokenizer = new StringTokenizer(actual, "=;");
        
        int tokenCount = tokenizer.countTokens();
        
        for(int i = 1; i<tokenCount; i = i+2) {
            String key = tokenizer.nextToken();
            String value = tokenizer.nextToken();
            
            if(key.equals("Size")) {
                assertEquals(Long.toString(file.length()), value);
            } else if(key.equals("Modify")) {
                    assertEquals(DateUtils.getFtpDate(file.lastModified()), value);
            } else if(key.equals("Type")) {
                if(file.isDirectory()) {
                    assertEquals("dir", value);
                } else {
                    assertEquals("file", value);
                }
            } else {
                fail("Unknown key: " + key);
            }
        }
        
        assertEquals(" " + file.getName(), tokenizer.nextElement());
    }
    
    public void testNLST() throws Exception {
        StringWriter writer = new StringWriter();
        directoryLister.doNLST(TEST_DIR1.getName(), writer);
        
        String[] lines = writer.toString().split(DELIMITER);
        
        assertEquals(3, lines.length);
        assertEquals(TEST_DIR_IN_DIR1.getName(),   lines[0]);
        assertEquals(TEST_FILE1_IN_DIR1.getName(), lines[1]);
        assertEquals(TEST_FILE2_IN_DIR1.getName(), lines[2]);
    }

    public void testNLSTOnFile() throws Exception {
        StringWriter writer = new StringWriter();
        directoryLister.doNLST(TEST_DIR1.getName() + "/" + TEST_FILE1_IN_DIR1.getName(), writer);
        
        String[] lines = writer.toString().split(DELIMITER);
        
        assertEquals(TEST_FILE1_IN_DIR1.getName(),   lines[0]);
    }

    /**
     * Should show the same output as LIST
     */
    public void testNLSTLong() throws Exception {
        StringWriter listWriter = new StringWriter();
        StringWriter nlstWriter = new StringWriter();

        assertTrue(directoryLister.doLIST(TEST_DIR1.getName(), listWriter));
        assertTrue(directoryLister.doNLST("-l " + TEST_DIR1.getName(), nlstWriter));
        
        assertEquals(listWriter.toString(), nlstWriter.toString());
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        if(TEST_TMP_DIR.exists()) {
            IoUtils.delete(TEST_TMP_DIR);
        }
    }
}