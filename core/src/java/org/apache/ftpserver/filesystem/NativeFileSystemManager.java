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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ftpserver.ftplet.Component;
import org.apache.ftpserver.ftplet.Configuration;
import org.apache.ftpserver.ftplet.FileSystemManager;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.User;

/**
 * Native file system manager. It uses the OS file system.
 */
public 
class NativeFileSystemManager implements FileSystemManager, Component {

    private Log log;
    private boolean createHome;
    private boolean caseInsensitive;
    
    
    /**
     * Set the log factory.
     */
    public void setLogFactory(LogFactory factory) {
        log = factory.getInstance(getClass());
    }
    
    /**
     * Configure the file system manager - does nothing.
     */
    public void configure(Configuration conf) throws FtpException {
        createHome  = conf.getBoolean("create-home", false); 
        caseInsensitive  = conf.getBoolean("case-insensitive", false); 
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
        if(createHome) {
            String homeDirStr = user.getHomeDirectory();
            File homeDir = new File(homeDirStr);
            if(homeDir.isFile()) {
                log.warn("Not a directory :: " + homeDirStr);
                throw new FtpException("Not a directory :: " + homeDirStr);
            }
            if( (!homeDir.exists()) && (!homeDir.mkdirs()) ) {
                log.warn("Cannot create user home :: " + homeDirStr);
                throw new FtpException("Cannot create user home :: " + homeDirStr);
            }
        }
        
        FileSystemView fsView = new NativeFileSystemView(user, caseInsensitive);
        return fsView;
    }
    
}
