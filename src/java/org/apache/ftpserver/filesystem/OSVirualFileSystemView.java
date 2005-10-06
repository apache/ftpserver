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
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ftpserver.ftplet.FileObject;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.User;


/**
 * Get the user file system view.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class OSVirualFileSystemView implements FileSystemView {

    private Log m_log;
    
    // root directory will be always absolute (canonical name) and
    // the path separator will be always '/'. The root directory
    // will always end with '/'.
    private String m_rootName;
    
    private FileObject m_currDir;
    private boolean m_hasWritePermission;
    
    /**
     * Constructor - set the user object.
     */
    public OSVirualFileSystemView(User user, LogFactory factory) throws FtpException {
        
        m_log = factory.getInstance(getClass());
        try {
            File root = new File(user.getHomeDirectory());
            m_rootName = root.getCanonicalPath();
            m_rootName = OSVirtualFileObject.normalizeSeparateChar(m_rootName);
            if( !m_rootName.endsWith("/") ) {
                m_rootName += '/';
            }
            
            m_hasWritePermission = user.getWritePermission();
            m_currDir = new OSVirtualFileObject(root, m_rootName, m_hasWritePermission);
        }
        catch(IOException ex) {
            m_log.warn("OSVirualFileSystemView.OSVirualFileSystemView()", ex);
            throw new FtpException("OSVirualFileSystemView.OSVirualFileSystemView()", ex);
        }
    }

    /**
     * Get the user root directory.
     */
    public FileObject getRootDirectory() throws FtpException {
        return new OSVirtualFileObject(new File(m_rootName), m_rootName, m_hasWritePermission);
    }
    
    /**
     * Get current directory.
     */
    public FileObject getCurrentDirectory() throws FtpException {
        return m_currDir;
    }
    
    /**
     * Change current directory.
     */
    public boolean changeDirectory(String dir) throws FtpException {
        FileObject dirObj = getFileObject(dir);
        boolean retVal = false;
        if(dirObj.isDirectory()) {
            m_currDir = dirObj;
            retVal = true;
        }
        return retVal;
    }
    
    /**
     * Get the file object.
     */
    public FileObject getFileObject(String fileStr) throws FtpException {
        try {
            
            // get the physical file object
            fileStr = OSVirtualFileObject.normalizeSeparateChar(fileStr);
            File physicalFile = null;
            if(fileStr.startsWith("/")) {
                physicalFile = new File(m_rootName, fileStr.substring(1));
            }
            else {
                File physicalCurrDir = ((OSVirtualFileObject)m_currDir).getPhysicalFile();
                physicalFile = new File(physicalCurrDir, fileStr);
            }

            // get full name of the file
            String physicalFileStr = physicalFile.getCanonicalPath();
            physicalFileStr = OSVirtualFileObject.normalizeSeparateChar(physicalFileStr);

            // not under the virtual root - not available
            FileObject virtualFile = null;
            if(m_rootName.regionMatches(0, physicalFileStr, 0, m_rootName.length() - 1)) {
                virtualFile = new OSVirtualFileObject(physicalFile, m_rootName, m_hasWritePermission);
            }
            return virtualFile;
        }
        catch(IOException ex) {
            m_log.warn("OSVirtualFileSystemView.getFileObject()", ex);
            throw new FtpException("OSVirtualFileSystemView.getFileObject()", ex);
        }
    }
    
    /**
     * List file objects
     */
    public FileObject[] listFiles(String file) throws FtpException {
        
        // get file object
        FileObject virtualFile = getFileObject(file);
        if(virtualFile == null) {
            return null;
        }
        File physicalFile = ((OSVirtualFileObject)virtualFile).getPhysicalFile();
        
        // does not exist - return null
        if(!physicalFile.exists()) {
            return null;
        } 
        
        // file - return a single element array 
        if(physicalFile.isFile()) {
            return new FileObject[] {
                           new OSVirtualFileObject(physicalFile, m_rootName, m_hasWritePermission)
                       };
        }
        
        // directory - return all the files
        File[] physicalFiles = physicalFile.listFiles();
        if(physicalFiles == null) {
            return null;
        }
        
        // now return all the files under the directory
        FileObject[] virtualFiles = new FileObject[physicalFiles.length];
        for(int i=0; i<physicalFiles.length; ++i) {
            virtualFiles[i] = new OSVirtualFileObject(physicalFiles[i], m_rootName, m_hasWritePermission);
        }
        return virtualFiles;
    }
}
