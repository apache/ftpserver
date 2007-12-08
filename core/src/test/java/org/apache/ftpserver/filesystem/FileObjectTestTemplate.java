package org.apache.ftpserver.filesystem;


import junit.framework.TestCase;

import org.apache.ftpserver.ftplet.AuthorizationRequest;
import org.apache.ftpserver.ftplet.FileObject;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.usermanager.BaseUser;

public abstract class FileObjectTestTemplate extends TestCase {

    protected static final String FILE2_PATH = "/dir1/file2";
    protected static final String DIR1_PATH = "/dir1";
    protected static final String DIR1_WITH_SLASH_PATH = "/dir1/";
    protected static final String FILE1_PATH = "/file1";
    protected static final String FILE3_PATH = "/file3";
    
    protected static final User USER = new BaseUser() {
        public AuthorizationRequest authorize(AuthorizationRequest request) {
            return request;
        }
    };

    protected abstract FileObject createFileObject(String fileName, User user);
    
    
    public void testNullFileName() {
        try{
            createFileObject(null, USER);
            fail("Must throw IllegalArgumentException");
        } catch(IllegalArgumentException e) {
            // OK
        }
    }

    public void testWhiteSpaceFileName() {
        try{
            createFileObject(" \t", USER);
           fail("Must throw IllegalArgumentException");
        } catch(IllegalArgumentException e) {
            // OK
        } 
    }
    
    public void testEmptyFileName() {
        try{
            createFileObject("", USER);
            fail("Must throw IllegalArgumentException");
        } catch(IllegalArgumentException e) {
            // OK
        }
    }

    public void testNonLeadingSlash() {
        try{
            createFileObject("foo", USER);
            fail("Must throw IllegalArgumentException");
        } catch(IllegalArgumentException e) {
            // OK
        }
    }

    public void testFullName() {
        FileObject fileObject = createFileObject(FILE2_PATH, USER);
        assertEquals("/dir1/file2", fileObject.getFullName());
    
        fileObject = createFileObject("/dir1/", USER);
        assertEquals("/dir1", fileObject.getFullName());
    
        fileObject = createFileObject("/dir1", USER);
        assertEquals("/dir1", fileObject.getFullName());
    }

    public void testShortName() {
        FileObject fileObject = createFileObject("/dir1/file2", USER);
        assertEquals("file2", fileObject.getShortName());
    
        fileObject = createFileObject("/dir1/", USER);
        assertEquals("dir1", fileObject.getShortName());
    
        fileObject = createFileObject("/dir1", USER);
        assertEquals("dir1", fileObject.getShortName());
    }
    
    public void testListFilesInOrder() {
        FileObject root = createFileObject("/", USER);
        
        FileObject[] files = root.listFiles();
        assertEquals(3, files.length);
        assertEquals("dir1", files[0].getShortName());
        assertEquals("file1", files[1].getShortName());
        assertEquals("file3", files[2].getShortName());
    }

}