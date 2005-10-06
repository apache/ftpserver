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

import org.apache.ftpserver.ftplet.FileObject;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.User;

/**
 * File system view based on native file system. Here the root directory
 * will be user virtual root (/).
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class NativeFileSystemView implements FileSystemView {
    
    // the root directory will always end with '/'.
    private String m_rootDir;
    
    // the first and the last character will always be '/'
    // It is always with respect to the root directory.
    private String m_currDir;
    
    private boolean m_writePermission;
    
    
    /**
     * Constructor - set the user object.
     */
    protected NativeFileSystemView(User user) throws FtpException {
        
        // add last '/' if necessary
        String rootDir = user.getHomeDirectory();
        rootDir = NativeFileObject.normalizeSeparateChar(rootDir);
        if(!rootDir.endsWith("/")) {
            rootDir += '/';
        }
        m_rootDir = rootDir;
        
        m_writePermission = user.getWritePermission();
        m_currDir = "/";
    }
    
    /**
     * Get the root directory.
     */
    public FileObject getRootDirectory() {
        return new NativeFileObject("/", new File(m_rootDir), m_writePermission);
    }
    
    /**
     * Get the current directory.
     */
    public FileObject getCurrentDirectory() {
        FileObject fileObj = null;
        if(m_currDir.equals("/")) {
            fileObj = new NativeFileObject("/", new File(m_rootDir), m_writePermission); 
        }
        else {
            File file = new File(m_rootDir, m_currDir.substring(1));
            fileObj = new NativeFileObject(m_currDir, file, m_writePermission);
            
        }
        return fileObj;
    }
    
    /**
     * Get file object.
     */
    public FileObject getFileObject(String file) {
        
        // get actual file object
        String physicalName = NativeFileObject.getPhysicalName(m_rootDir, m_currDir, file);
        File fileObj = new File(physicalName);
        
        // strip the root directory and return
        String userFileName = physicalName.substring(m_rootDir.length() - 1);
        return new NativeFileObject(userFileName, fileObj, m_writePermission);
    }
    
    /**
     * Change directory.
     */
    public boolean changeDirectory(String dir) {
        
        // not a directory - return false
        dir = NativeFileObject.getPhysicalName(m_rootDir, m_currDir, dir);
        File dirObj = new File(dir); 
        if(!dirObj.isDirectory()) {
            return false;
        }
        
        // strip user root and add last '/' if necessary
        dir = dir.substring(m_rootDir.length() - 1);
        if(dir.charAt(dir.length() - 1) != '/') {
            dir = dir + '/';
        }
        
        m_currDir = dir;
        return true;
    } 
    
    /**
     * List files.
     */
    public FileObject[] listFiles(String str) {
        
        // get the physical file object
        FileObject virtualFile = getFileObject(str);
        File physicalFile = ((NativeFileObject)virtualFile).getPhysicalFile();
        
        // does not exist - return null
        if(!physicalFile.exists()) {
            return null;
        } 
        
        // file - return a single element array 
        if(physicalFile.isFile()) {
            return new FileObject[] { virtualFile };
        }
        
        // directory - return all the files
        File[] physicalFiles = physicalFile.listFiles();
        if(physicalFiles == null) {
            return null;
        }
        
        // get the virtual name of the base directory
        String virtualFileStr = virtualFile.getFullName();
        if(virtualFileStr.charAt(virtualFileStr.length() - 1) != '/') {
            virtualFileStr += '/';
        }
        
        // now return all the files under the directory
        FileObject[] virtualFiles = new FileObject[physicalFiles.length];
        for(int i=0; i<physicalFiles.length; ++i) {
            File fileObj = physicalFiles[i];
            String fileName = virtualFileStr + fileObj.getName();
            virtualFiles[i] = new NativeFileObject(fileName, fileObj, m_writePermission);
        }
        return virtualFiles;
    }
}
