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

package org.apache.ftpserver.usermanager;

import java.io.Serializable;

import org.apache.ftpserver.ftplet.User;

/**
 * Generic user class. 
 * The user attributes are:
 * <ul>
 *   <li>uid</li>
 *   <li>userpassword</li>
 *   <li>enableflag</li>
 *   <li>homedirectory</li>
 *   <li>writepermission</li>
 *   <li>idletime</li>
 *   <li>uploadrate</li>
 *   <li>downloadrate</li>
 * </ul>
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */

public
class BaseUser implements User, Serializable {
    
    private static final long serialVersionUID = -47371353779731294L;
    
    private String name        = null;
    private String password    = null;

    private int maxIdleTimeSec  = 0; // no limit
    private int maxUploadRate   = 0; // no limit
    private int maxDownloadRate = 0; // no limit
    
    private int maxLoginNumber = 0; //no limit
    private int maxLoginPerIP = 0; //no limit

    private boolean hasWritePermission;
    
    private String homeDir    = null;
    private boolean isEnabled = true;
    
    /**
     * Default constructor.
     */
    public BaseUser() {
    }
    
    /**
     * Copy constructor.
     */
    public BaseUser(User user) {
        name = user.getName();
        password = user.getPassword();
        maxIdleTimeSec = user.getMaxIdleTime();
        maxUploadRate = user.getMaxUploadRate();
        maxDownloadRate = user.getMaxDownloadRate();
        hasWritePermission = user.getWritePermission();
        homeDir = user.getHomeDirectory();
        isEnabled = user.getEnabled();
        maxLoginNumber = user.getMaxLoginNumber();
        maxLoginPerIP = user.getMaxLoginPerIP();
    }
    
    /**
     * Get the user name.
     */
    public String getName() {
        return name;
    }
        
    /**
     * Set user name.
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Get the user password.
     */
    public String getPassword() {
        return password;
    }
    
    /**
     * Set user password.
     */
    public void setPassword(String pass) {
        password = pass;
    }

    /**
     * Get the maximum idle time in second.
     */
    public int getMaxIdleTime() {
        return maxIdleTimeSec;
    }

    /**
     * Set the maximum idle time in second.
     */
    public void setMaxIdleTime(int idleSec) {
        maxIdleTimeSec = idleSec;
        if(maxIdleTimeSec < 0) {
            maxIdleTimeSec = 0;
        }
    }

    /**
     * Get the user enable status.
     */
    public boolean getEnabled() {
        return isEnabled;
    }
    
    /**
     * Set the user enable status.
     */
    public void setEnabled(boolean enb) {
        isEnabled = enb;
    }

    /**
     * Get maximum user upload rate in bytes/sec.
     */
    public int getMaxUploadRate() {
        return maxUploadRate;
    }
    
    /**
     * Set user maximum upload rate limit.
     * Less than or equal to zero means no limit.
     */
    public void setMaxUploadRate(int rate) {
        maxUploadRate = rate;
    }
    
    /**
     * Get maximum user download rate in bytes/sec.
     */
    public int getMaxDownloadRate() {
        return maxDownloadRate;
    }
    
    /**
     * Set user maximum download rate limit.
     * Less than or equal to zero means no limit.
     */
    public void setMaxDownloadRate(int rate) {
        maxDownloadRate = rate;
    }
    
    /**
     * Get the user home directory.
     */
    public String getHomeDirectory() {
        return homeDir;
    }

    /**
     * Set the user home directory.
     */
    public void setHomeDirectory(String home) {
        homeDir = home;
    }
    
    /**
     *Get maximum login number allowed for this user account
     */
    public int getMaxLoginNumber() {
      return maxLoginNumber;
    }

    /**
     * set this user's allowed maximum login number.
     * Less than or equal to zero means no limit
     */
    public void setMaxLoginNumber(int loginNumber) {
      maxLoginNumber = loginNumber;
    }
    
    /**
     *Get maximum login number allowed from the same IP address when using this user account
     */
    public int getMaxLoginPerIP() {
      return maxLoginPerIP;
    }
    
    /**
     * Set the allowed maximum login number from the same IP address and logged as this user account
     * Less than or equal to zero means no limit
     */
    public void setMaxLoginPerIP(int loginPerIP) {
      maxLoginPerIP = loginPerIP;
    }
        
    /**
     * Get write permission.
     */
    public boolean getWritePermission() {
        return hasWritePermission;
    }
    
    /**
     * Set write permission.
     */
    public void setWritePermission(boolean writePerm) {
        hasWritePermission = writePerm;
    } 

    /** 
     * String representation.
     */
    public String toString() {
        return name;
    }    
}
