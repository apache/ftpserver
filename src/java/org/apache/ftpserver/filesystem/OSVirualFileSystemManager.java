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

import org.apache.ftpserver.ftplet.Configuration;
import org.apache.ftpserver.ftplet.FileSystemManager;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.Logger;
import org.apache.ftpserver.ftplet.User;

import java.io.File;

/**
 * This is a operating system based virtual root file system manager. 
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class OSVirualFileSystemManager implements FileSystemManager {  
    
    private Logger m_logger;
    private boolean m_createHome;
    
    /**
     * Set the logger object.
     */
    public void setLogger(Logger logger) {
        m_logger = logger;
    }
    
    /**
     * Configure the file system manager - does nothing.
     */
    public void configure(Configuration conf) throws FtpException {
        m_createHome = conf.getBoolean("create-home", false);
    }
    
    /**
     * Dispose the file system manager.
     */
    public void dispose() {
    }
    
    /**
     * Create the appropriate user file system view.
     */
    public FileSystemView createFileSystemView(User user) throws FtpException {
        
        // create home if does not exist
        if(m_createHome) {
            String homeDirStr = user.getHomeDirectory();
            File homeDir = new File(homeDirStr);
            if( (!homeDir.exists()) && (!homeDir.mkdirs()) ) {
                m_logger.warn("Cannot create user home :: " + homeDirStr);
                throw new FtpException("Cannot create user home :: " + homeDirStr);
            }
        }
        
        OSVirualFileSystemView fsView = new OSVirualFileSystemView(user, m_logger);
        return fsView;
    }
    
}
