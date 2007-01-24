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
import java.util.ArrayList;
import java.util.List;

import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.AuthorizationRequest;
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
 */

public
class BaseUser implements User, Serializable {
    
    private static final long serialVersionUID = -47371353779731294L;
    
    private String name        = null;
    private String password    = null;

    private int maxIdleTimeSec  = 0; // no limit

    private String homeDir    = null;
    private boolean isEnabled = true;

    private Authority[] authorities = new Authority[0];
    
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
        authorities = user.getAuthorities();
        maxIdleTimeSec = user.getMaxIdleTime();
        homeDir = user.getHomeDirectory();
        isEnabled = user.getEnabled();
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

    public Authority[] getAuthorities() {
        return authorities;
    }

    public void setAuthorities(Authority[] authorities) {
        this.authorities = authorities;
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
     * String representation.
     */
    public String toString() {
        return name;
    }  
    
    /**
     * @see User#authorize(AuthorizationRequest)
     */
    public AuthorizationRequest authorize(AuthorizationRequest request) {
        Authority[] authorities = getAuthorities();
        
        boolean someoneCouldAuthorize = false;
        for (int i = 0; i < authorities.length; i++) {
            Authority authority = authorities[i];
            
            if(authority.canAuthorize(request)) {
                someoneCouldAuthorize = true;
                
                request = authority.authorize(request);
                
                // authorization failed, return null
                if(request == null) {
                    return null;
                }
            }
            
        }
        
        if(someoneCouldAuthorize) {
            return request;
        } else {
            return null;
        }
    }

    public Authority[] getAuthorities(Class clazz) {
        List selected = new ArrayList();
        
        for (int i = 0; i < authorities.length; i++) {
            if(authorities[i].getClass().equals(clazz)) {
                selected.add(authorities[i]);
            }
        }
        
        return (Authority[]) selected.toArray(new Authority[0]);
    }
}
