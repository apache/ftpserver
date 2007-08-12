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
    
    protected static final User USER = new BaseUser() {
        public AuthorizationRequest authorize(AuthorizationRequest request) {
            return request;
        }
    };

    public FileObjectTestTemplate() {
        super();
    }

    public FileObjectTestTemplate(String name) {
        super(name);
    }

    protected abstract FileObject createFile(String fileName, User user);
    
    
    public void testNullFileName() {
        try{
            createFile(null, USER);
            fail("Must throw IllegalArgumentException");
        } catch(IllegalArgumentException e) {
            // OK
        }
    }

    public void testWhiteSpaceFileName() {
        try{
            createFile(" \t", USER);
           fail("Must throw IllegalArgumentException");
        } catch(IllegalArgumentException e) {
            // OK
        } 
    }
    
    public void testEmptyFileName() {
        try{
            createFile("", USER);
            fail("Must throw IllegalArgumentException");
        } catch(IllegalArgumentException e) {
            // OK
        }
    }

    public void testNonLeadingSlash() {
        try{
            createFile("foo", USER);
            fail("Must throw IllegalArgumentException");
        } catch(IllegalArgumentException e) {
            // OK
        }
    }

    public void testFullName() {
        FileObject fileObject = createFile(FILE2_PATH, USER);
        assertEquals("/dir1/file2", fileObject.getFullName());
    
        fileObject = createFile("/dir1/", USER);
        assertEquals("/dir1", fileObject.getFullName());
    
        fileObject = createFile("/dir1", USER);
        assertEquals("/dir1", fileObject.getFullName());
    }

    public void testShortName() {
        FileObject fileObject = createFile("/dir1/file2", USER);
        assertEquals("file2", fileObject.getShortName());
    
        fileObject = createFile("/dir1/", USER);
        assertEquals("dir1", fileObject.getShortName());
    
        fileObject = createFile("/dir1", USER);
        assertEquals("dir1", fileObject.getShortName());
    }

}