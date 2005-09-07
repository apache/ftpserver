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

import org.apache.ftpserver.ftplet.FileObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

/**
 * Operating system file.  
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public
class OSVirtualFileObject implements FileObject {

    private File m_file;
    private String m_rootDir;
    private boolean m_writePermission;
    
    /**
     * Protected constructor - used only in the <code>OSVirtualFileSystemView</code>.
     */
    protected OSVirtualFileObject(File file, String rootDir, boolean writePerm) {
        m_file = file;
        m_rootDir = rootDir;
        m_writePermission = writePerm;
    }

    /**
     * Get the fully qualified name.
     */
    public String getFullName() {
        String virtualFileStr = null;
        try {
            String physicalFileStr = m_file.getCanonicalPath();
            physicalFileStr = normalizeSeparateChar(physicalFileStr);
            virtualFileStr = physicalFileStr.substring(m_rootDir.length() - 1);
            
            if(m_file.isDirectory()) {
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
        return m_file.getName();
    }
    
    /**
     * Is a hidden file?
     */
    public boolean isHidden() {
        return m_file.isHidden();
    }
     
    /**
     * Is it a directory?
     */
    public boolean isDirectory() {
        return m_file.isDirectory();
    }
    
    /**
     * Is it a file?
     */
    public boolean isFile() {
        return m_file.isFile();
    }
    
    /**
     * Does this file exists?
     */
    public boolean doesExist() {
        return m_file.exists();
    }
    
    /**
     * Get file size.
     */
    public long getSize() {
        return m_file.length();
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
        return m_file.isDirectory() ? 3 : 1;
    }
    
    /**
     * Get last modified time.
     */ 
    public long getLastModified() {
        return m_file.lastModified();
    }
    
    /**
     * Check read permission.
     */
    public boolean hasReadPermission() {
        return m_file.canRead();
    }
    
    /**
     * Chech file write permission.
     */
    public boolean hasWritePermission() {
        if(!m_writePermission) {
            return false;
        }
        
        if(m_file.exists()) {
            return m_file.canWrite();
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
            retVal = m_file.delete();
        }
        return retVal;
    }
    
    /**
     * Move file object.
     */
    public boolean move(FileObject dest) {
        boolean retVal = false;
        if(dest.hasWritePermission() && hasReadPermission()) {
            File destFile = ((OSVirtualFileObject)dest).m_file;
            retVal = m_file.renameTo(destFile);
        }
        return retVal;
    }
    
    /**
     * Create directory.
     */
    public boolean mkdir() {
        boolean retVal = false;
        if(hasWritePermission()) {
            retVal = m_file.mkdirs();
        }
        return retVal;
    }
    
    /**
     * Create output stream for writing.
     */
    public OutputStream createOutputStream(boolean append) throws IOException {
        
        // permission check
        if(!hasWritePermission()) {
            throw new IOException("No write permission : " + m_file.getName());
        }
        
        // create output stream
        OutputStream out = null;
        if(append && m_file.exists()) {
            RandomAccessFile raf = new RandomAccessFile(m_file, "rw");
            raf.seek(raf.length());
            out = new FileOutputStream(raf.getFD());
        }
        else {
            out = new FileOutputStream(m_file);
        }
        
        return out;
    }
    
    /**
     * Create output stream for writing.
     */
    public OutputStream createOutputStream(long offset) throws IOException {
        
        // permission check
        if(!hasWritePermission()) {
            throw new IOException("No write permission : " + m_file.getName());
        }
        
        // create output stream
        RandomAccessFile raf = new RandomAccessFile(m_file, "rw");
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
            throw new IOException("No read permission : " + m_file.getName());
        }
        
        // move to the appropriate offset and create input stream
        RandomAccessFile raf = new RandomAccessFile(m_file, "r");
        raf.seek(offset);
        return new FileInputStream(raf.getFD());
    }
    
    /**
     * Get the physical file.
     */
    File getPhysicalFile() {
        return m_file;
    }
    
    /**
     * Normalize separate characher. Separate character should be '/' always.
     */
    public static String normalizeSeparateChar(String pathName) {   
       pathName = pathName.replace(File.separatorChar, '/');
       pathName = pathName.replace('\\', '/');
       return pathName;
    }
}
