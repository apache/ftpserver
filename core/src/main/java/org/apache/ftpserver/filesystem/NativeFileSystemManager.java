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

import org.apache.ftpserver.ftplet.FileSystemManager;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Native file system manager. It uses the OS file system.
 */
public 
class NativeFileSystemManager implements FileSystemManager {

    private final Logger LOG = LoggerFactory.getLogger(NativeFileSystemManager.class);
    
    private boolean createHome;
    private boolean caseInsensitive;
    
    public boolean isCreateHome() {
		return createHome;
	}

	public void setCreateHome(boolean createHome) {
		this.createHome = createHome;
	}

	public boolean isCaseInsensitive() {
		return caseInsensitive;
	}

	public void setCaseInsensitive(boolean caseInsensitive) {
		this.caseInsensitive = caseInsensitive;
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
                LOG.warn("Not a directory :: " + homeDirStr);
                throw new FtpException("Not a directory :: " + homeDirStr);
            }
            if( (!homeDir.exists()) && (!homeDir.mkdirs()) ) {
                LOG.warn("Cannot create user home :: " + homeDirStr);
                throw new FtpException("Cannot create user home :: " + homeDirStr);
            }
        }
        
        FileSystemView fsView = new NativeFileSystemView(user, caseInsensitive);
        return fsView;
    }
    
}
