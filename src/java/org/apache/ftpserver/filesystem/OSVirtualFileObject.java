// $Id$
/*
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ftpserver.filesystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import org.apache.ftpserver.ftplet.FileObject;

/**
 * Operating system file.  
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public
class OSVirtualFileObject implements FileObject {

    private File file;
    private boolean writePermission;
    
    // root directory will be always absolute (canonical name) and
    // the path separator will be always '/'. The root directory
    // will always end with '/'.
    private String rootDir;
    
    
    /**
     * Protected constructor - used only in the 
     * <code>OSVirtualFileSystemView</code>.
     */
    protected OSVirtualFileObject(File file, String rootDir, boolean writePerm) {
        this.file = file;
        this.rootDir = rootDir;
        writePermission = writePerm;
    }

    /**
     * Get the fully qualified name. Here the name will be 
     * always with respect to the user root. If the file is 
     * a directory, the last character will never be '/' except 
     * the root directory where '/' will be returned. The first 
     * character will always be '/'.
     */
    public String getFullName() {
        String virtualFileStr = null;
        try {
            String physicalFileStr = file.getCanonicalPath();
            physicalFileStr = normalizeSeparateChar(physicalFileStr);
            virtualFileStr = physicalFileStr.substring(rootDir.length() - 1);
            
            if(file.isDirectory()) {
                if(virtualFileStr.equals("")) {
                    virtualFileStr = "/";
                }
                else if( (!virtualFileStr.equals("/")) && virtualFileStr.endsWith("/") ) {
                    virtualFileStr = virtualFileStr.substring(0, virtualFileStr.length() - 1);
                }
            }
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        return virtualFileStr;
    }
    
    /**
     * Get the file short name.
     */
    public String getShortName() {
        return file.getName();
    }
    
    /**
     * Is a hidden file?
     */
    public boolean isHidden() {
        return file.isHidden();
    }
     
    /**
     * Is it a directory?
     */
    public boolean isDirectory() {
        return file.isDirectory();
    }
    
    /**
     * Is it a file?
     */
    public boolean isFile() {
        return file.isFile();
    }
    
    /**
     * Does this file exists?
     */
    public boolean doesExist() {
        return file.exists();
    }
    
    /**
     * Get file size.
     */
    public long getSize() {
        return file.length();
    }
    
    /**
     * Get file owner.
     */
    public String getOwnerName() {
        return "user";
    }
    
    /**
     * Get group name
     */
    public String getGroupName() {
        return "group";
    }
    
    /**
     * Get link count
     */
    public int getLinkCount() {
        return file.isDirectory() ? 3 : 1;
    }
    
    /**
     * Get last modified time.
     */ 
    public long getLastModified() {
        return file.lastModified();
    }
    
    /**
     * Check read permission.
     */
    public boolean hasReadPermission() {
        return file.canRead();
    }
    
    /**
     * Chech file write permission.
     */
    public boolean hasWritePermission() {
        if(!writePermission) {
            return false;
        }
        
        if(file.exists()) {
            return file.canWrite();
        }
        return true;
    }
    
    /**
     * Has delete permission.
     */
    public boolean hasDeletePermission() {
        
        // root cannot be deleted
        if( "/".equals(getFullName()) ) {
            return false;
        }
        
        return hasWritePermission();
    }
    
    /**
     * Delete file.
     */
    public boolean delete() {
        boolean retVal = false;
        if( hasDeletePermission() ) {
            retVal = file.delete();
        }
        return retVal;
    }
    
    /**
     * Move file object.
     */
    public boolean move(FileObject dest) {
        boolean retVal = false;
        if(dest.hasWritePermission() && hasReadPermission()) {
            File destFile = ((OSVirtualFileObject)dest).file;
            retVal = file.renameTo(destFile);
        }
        return retVal;
    }
    
    /**
     * Create directory.
     */
    public boolean mkdir() {
        boolean retVal = false;
        if(hasWritePermission()) {
            retVal = file.mkdirs();
        }
        return retVal;
    }
    
    /**
     * Create output stream for writing.
     */
    public OutputStream createOutputStream(boolean append) throws IOException {
        
        // permission check
        if(!hasWritePermission()) {
            throw new IOException("No write permission : " + file.getName());
        }
        
        // create output stream
        OutputStream out = null;
        if(append && file.exists()) {
            RandomAccessFile raf = new RandomAccessFile(file, "rw");
            raf.seek(raf.length());
            out = new FileOutputStream(raf.getFD());
        }
        else {
            out = new FileOutputStream(file);
        }
        
        return out;
    }
    
    /**
     * Create output stream for writing.
     */
    public OutputStream createOutputStream(long offset) throws IOException {
        
        // permission check
        if(!hasWritePermission()) {
            throw new IOException("No write permission : " + file.getName());
        }
        
        // create output stream
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        raf.setLength(offset);
        raf.seek(offset);
        return new FileOutputStream(raf.getFD());
    }
    
    /**
     * Create input stream for reading.
     */
    public InputStream createInputStream(long offset) throws IOException {
        
        // permission check
        if(!hasReadPermission()) {
            throw new IOException("No read permission : " + file.getName());
        }
        
        // move to the appropriate offset and create input stream
        RandomAccessFile raf = new RandomAccessFile(file, "r");
        raf.seek(offset);
        return new FileInputStream(raf.getFD());
    }
    
    /**
     * Get the physical file.
     */
    File getPhysicalFile() {
        return file;
    }
    
    /**
     * Normalize separate characher. Separate character should be '/' always.
     */
    public final static String normalizeSeparateChar(String pathName) {   
       pathName = pathName.replace(File.separatorChar, '/');
       pathName = pathName.replace('\\', '/');
       return pathName;
    }
}
