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
    
    public static final String ATTR_LOGIN             = "uid";
    public static final String ATTR_PASSWORD          = "userpassword";
    public static final String ATTR_HOME              = "homedirectory";
    public static final String ATTR_WRITE_PERM        = "writepermission";
    public static final String ATTR_ENABLE            = "enableflag";
    public static final String ATTR_MAX_IDLE_TIME     = "idletime";
    public static final String ATTR_MAX_UPLOAD_RATE   = "uploadrate";
    public static final String ATTR_MAX_DOWNLOAD_RATE = "downloadrate";
    
    private String m_name        = null;
    private String m_password    = null;

    private int m_maxIdleTimeSec  = 0; // no limit
    private int m_maxUploadRate   = 0; // no limit
    private int m_maxDownloadRate = 0; // no limit

    private boolean m_hasWritePermission;
    
    private String m_homeDir    = null;
    private boolean m_isEnabled = true;
    
    /**
     * Default constructor.
     */
    public BaseUser() {
    }
    
    /**
     * Copy constructor.
     */
    public BaseUser(User user) {
        m_name = user.getName();
        m_password = user.getPassword();
        m_maxIdleTimeSec = user.getMaxIdleTime();
        m_maxUploadRate = user.getMaxUploadRate();
        m_maxDownloadRate = user.getMaxDownloadRate();
        m_hasWritePermission = user.getWritePermission();
        m_homeDir = user.getHomeDirectory();
        m_isEnabled = user.getEnabled();
    }
    
    /**
     * Get the user name.
     */
    public String getName() {
        return m_name;
    }
        
    /**
     * Set user name.
     */
    public void setName(String name) {
        m_name = name;
    }
    
    /**
     * Get the user password.
     */
    public String getPassword() {
        return m_password;
    }
    
    /**
     * Set user password.
     */
    public void setPassword(String pass) {
        m_password = pass;
    }

    /**
     * Get the maximum idle time in second.
     */
    public int getMaxIdleTime() {
        return m_maxIdleTimeSec;
    }

    /**
     * Set the maximum idle time in second.
     */
    public void setMaxIdleTime(int idleSec) {
        m_maxIdleTimeSec = idleSec;
        if(m_maxIdleTimeSec < 0) {
            m_maxIdleTimeSec = 0;
        }
    }

    /**
     * Get the user enable status.
     */
    public boolean getEnabled() {
        return m_isEnabled;
    }
    
    /**
     * Set the user enable status.
     */
    public void setEnabled(boolean enb) {
        m_isEnabled = enb;
    }

    /**
     * Get maximum user upload rate in bytes/sec.
     */
    public int getMaxUploadRate() {
        return m_maxUploadRate;
    }
    
    /**
     * Set user maximum upload rate limit.
     * Less than or equal to zero means no limit.
     */
    public void setMaxUploadRate(int rate) {
        m_maxUploadRate = rate;
    }
    
    /**
     * Get maximum user download rate in bytes/sec.
     */
    public int getMaxDownloadRate() {
        return m_maxDownloadRate;
    }
    
    /**
     * Set user maximum download rate limit.
     * Less than or equal to zero means no limit.
     */
    public void setMaxDownloadRate(int rate) {
        m_maxDownloadRate = rate;
    }
    
    /**
     * Get the user home directory.
     */
    public String getHomeDirectory() {
        return m_homeDir;
    }

    /**
     * Set the user home directory.
     */
    public void setHomeDirectory(String home) {
        m_homeDir = home;
    } 
        
    /**
     * Get write permission.
     */
    public boolean getWritePermission() {
        return m_hasWritePermission;
    }
    
    /**
     * Set write permission.
     */
    public void setWritePermission(boolean writePerm) {
        m_hasWritePermission = writePerm;
    } 

    /** 
     * String representation.
     */
    public String toString() {
        return m_name;
    }    
}
