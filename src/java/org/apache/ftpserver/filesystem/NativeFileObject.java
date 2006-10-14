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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.StringTokenizer;

import org.apache.ftpserver.ftplet.FileObject;

/**
 * This class wraps native file object. 
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class NativeFileObject implements FileObject {

    // the file name with respect to the user root.
    // The path separator character will be '/' and
    // it will always begin with '/'.
    private String fileName;
    
    private File file;
    private boolean writePermission;
    
    
    /**
     * Constructor.
     */
    protected NativeFileObject(String fileName, File file, boolean writePerm) {
        if(fileName == null) {
            throw new IllegalArgumentException("fileName can not be null");
        } 
        if(file == null) {
            throw new IllegalArgumentException("file can not be null");
        } 
        fileName = fileName.trim();
        if(fileName.length() == 0) {
            throw new IllegalArgumentException("fileName can not be empty");
        } else if(fileName.charAt(0) != '/') {
            throw new IllegalArgumentException("fileName must be an absolut path");
        }
        
        
        
        this.fileName = fileName;
        this.file = file;
        this.writePermission = writePerm;
    }
    
    /**
     * Get full name.
     */
    public String getFullName() {
        
        // strip the last '/' if necessary
        String fullName = fileName;
        int filelen = fullName.length();
        if( (filelen != 1)&& (fullName.charAt(filelen - 1) == '/') ) {
            fullName = fullName.substring(0, filelen - 1);
        }
        
        return fullName;
    }
    
    /**
     * Get short name.
     */
    public String getShortName() {
        
        // root - the short name will be '/'
        if(fileName.equals("/")) {
            return "/";
        }
        
        // strip the last '/'
        String shortName = fileName;
        int filelen = fileName.length();
        if(shortName.charAt(filelen - 1) == '/') {
            shortName = shortName.substring(0, filelen - 1);
        }
        
        // return from the last '/'
        int slashIndex = shortName.lastIndexOf('/');
        if(slashIndex != -1) {
            shortName = shortName.substring(slashIndex + 1);
        }
        return shortName;
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
        if( "/".equals(fileName) ) {
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
            File destFile = ((NativeFileObject)dest).file;
            
            if(destFile.exists()) {
                // renameTo behaves differently on different platforms
                // this check verifies that if the destination already exists, 
                // we fail
                retVal = false;
            } else {
                retVal = file.renameTo(destFile);
            }
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
     * Get the physical file object.
     */
    public File getPhysicalFile() {
        return file;
    }
    
    /**
     * List files. If not a directory or does not exist, null
     * will be returned. 
     */
    public FileObject[] listFiles() {
    	
    	// is a directory
    	if(!file.isDirectory()) {
    		return null;
    	}
    	
        // directory - return all the files
        File[] files = file.listFiles();
        if(files == null) {
            return null;
        }
        
        // get the virtual name of the base directory
        String virtualFileStr = getFullName();
        if(virtualFileStr.charAt(virtualFileStr.length() - 1) != '/') {
            virtualFileStr += '/';
        }
        
        // now return all the files under the directory
        FileObject[] virtualFiles = new FileObject[files.length];
        for(int i=0; i<files.length; ++i) {
            File fileObj = files[i];
            String fileName = virtualFileStr + fileObj.getName();
            virtualFiles[i] = new NativeFileObject(fileName, fileObj, writePermission);
        }
        return virtualFiles;
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
     * Normalize separate characher. Separate character should be '/' always.
     */
    public final static String normalizeSeparateChar(String pathName) {
       pathName = pathName.replace(File.separatorChar, '/');
       pathName = pathName.replace('\\', '/');
       return pathName;
    }
    
    /**
     * Get the physical canonical file name. It works like 
     * File.getCanonicalPath().
     * 
     * @param rootDir The root directory. 
     * 
     * @param currDir The current directory. It will always be with
     * respect to the root directory. 
     * 
     * @param fileName The input file name.
     * 
     * @return The return string will always begin with the root directory.
     * It will never be null.
     */
    public final static String getPhysicalName(String rootDir, 
                                               String currDir, 
                                               String fileName) {
        
        // get the starting directory
        rootDir = rootDir.trim();
        fileName = fileName.trim();
        
        rootDir = normalizeSeparateChar(rootDir);
        if(rootDir.charAt(rootDir.length() - 1) != '/') {
            rootDir += '/';
        }
        
        fileName = normalizeSeparateChar(fileName);
        String resArg;
        if(fileName.charAt(0) != '/') {
            if(currDir == null) {
                currDir = "/";
            }
            currDir = currDir.trim();
            if(currDir.length() == 0) {
                currDir = "/";
            }
            
            currDir = normalizeSeparateChar(currDir);
            
            if(currDir.charAt(0) != '/') {
                currDir = '/' + currDir;
            }
            if(currDir.charAt(currDir.length() - 1) != '/') {
                currDir += '/';
            }

            
            resArg = rootDir + currDir.substring(1);
        }
        else {
            resArg = rootDir;
        }
        
        // strip last '/'
        if(resArg.charAt(resArg.length() -1) == '/') {
            resArg = resArg.substring(0, resArg.length()-1);
        }
        
        // replace ., ~ and ..
        // in this loop resArg will never end with '/'
        StringTokenizer st = new StringTokenizer(fileName, "/");
        while(st.hasMoreTokens()) {
            String tok = st.nextToken().trim();
            
            // . => current directory
            if(tok.equals(".")) {
                continue;
            }
            
            // .. => parent directory (if not root)
            if(tok.equals("..")) {
                if(resArg.startsWith(rootDir)) {
                    int slashIndex = resArg.lastIndexOf('/');
                    if(slashIndex != -1) {
                        resArg = resArg.substring(0, slashIndex);
                    }
                }
                continue;
            }
            
            // ~ => home directory (in this case the root directory)
            if (tok.equals("~")) {
                resArg = rootDir.substring(0, rootDir.length()-1);
                continue;
            }
            
            resArg = resArg + '/' + tok;
        }
        
        // add last slash if necessary
        if( (resArg.length()) + 1 == rootDir.length() ) {
            resArg += '/';
        }
        
        // final check
        if ( !resArg.regionMatches(0, rootDir, 0, rootDir.length()) ) {
            resArg = rootDir;
        }
        
        return resArg;
    }
}
