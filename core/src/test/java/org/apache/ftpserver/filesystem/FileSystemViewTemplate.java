package org.apache.ftpserver.filesystem;

import org.apache.ftpserver.usermanager.BaseUser;

import junit.framework.TestCase;

public abstract class FileSystemViewTemplate extends TestCase {

    protected static final String DIR1_NAME = "dir1";
   
    protected BaseUser user = new BaseUser();

    
    public void testChangeDirectory() throws Exception {
        NativeFileSystemView view = new NativeFileSystemView(user);
        assertEquals("/", view.getCurrentDirectory().getFullName());

        assertTrue(view.changeDirectory(DIR1_NAME));
        assertEquals("/" + DIR1_NAME, view.getCurrentDirectory().getFullName());
        
        assertTrue(view.changeDirectory("."));
        assertEquals("/" + DIR1_NAME, view.getCurrentDirectory().getFullName());

        assertTrue(view.changeDirectory(".."));
        assertEquals("/", view.getCurrentDirectory().getFullName());

        assertTrue(view.changeDirectory("./" + DIR1_NAME));
        assertEquals("/" + DIR1_NAME, view.getCurrentDirectory().getFullName());

        assertTrue(view.changeDirectory("~"));
        assertEquals("/", view.getCurrentDirectory().getFullName());
    }

    public void testChangeDirectoryCaseInsensitive() throws Exception {
        NativeFileSystemView view = new NativeFileSystemView(user, true);
        assertEquals("/", view.getCurrentDirectory().getFullName());
        
        assertTrue(view.changeDirectory("/DIR1"));
        assertEquals("/dir1", view.getCurrentDirectory().getFullName());
        assertTrue(view.getCurrentDirectory().doesExist());
        
        assertTrue(view.changeDirectory("/dir1"));
        assertEquals("/dir1", view.getCurrentDirectory().getFullName());
        assertTrue(view.getCurrentDirectory().doesExist());

        assertTrue(view.changeDirectory("/DiR1"));
        assertEquals("/dir1", view.getCurrentDirectory().getFullName());
        assertTrue(view.getCurrentDirectory().doesExist());
    }

}