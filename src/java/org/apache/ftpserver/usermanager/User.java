/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1997-2003 The Apache Software Foundation. All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *    "This product includes software developed by the
 *    Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software
 *    itself, if and wherever such third-party acknowledgments
 *    normally appear.
 *
 * 4. The names "Incubator", "FtpServer", and "Apache Software Foundation"
 *    must not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation. For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * $Id$
 */
package org.apache.ftpserver.usermanager;

import java.io.Serializable;
import java.net.InetAddress;
import java.rmi.server.UID;
import org.apache.ftpserver.util.VirtualDirectory;

/**
 * Generic user class. All the application specific user classes will
 * be derived from this.
 * <ul>
 *   <li>uid</li>
 *   <li>userpassword</li>
 *   <li>objectclass</li>
 *   <li>enableflag</li>
 *   <li>homedirectory</li>
 *   <li>writepermission</li>
 *   <li>idletime</li>
 *   <li>uploadrate</li>
 *   <li>downloadrate</li>
 * </ul>
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */

public
class User implements Serializable {

    /**
     * uid
     */
    public static final String ATTR_LOGIN             = "uid";
    
    /**
     * userpassword
     */
    public static final String ATTR_PASSWORD          = "userpassword";
    
    /**
     * homedirectory
     */
    public static final String ATTR_HOME              = "homedirectory";
    
    /**
     * writepermission
     */
    public static final String ATTR_WRITE_PERM        = "writepermission";
    
    /**
     * enableflag
     */
    public static final String ATTR_ENABLE            = "enableflag";
    
    /**
     * idletime
     */
    public static final String ATTR_MAX_IDLE_TIME     = "idletime";
    
    /**
     * uploadrate
     */
    public static final String ATTR_MAX_UPLOAD_RATE   = "uploadrate";
    
    /**
     * downloadrate
     */
    public static final String ATTR_MAX_DOWNLOAD_RATE = "downloadrate";

    private String mstUserName    = null;
    private String mstPassword    = null;

    private long mlIdleTime          = 0; // no limit
    private int  miUploadRateLimit   = 0; // no limit
    private int  miDownloadRateLimit = 0; // no limit

    private long mlLoginTime         = 0;
    private long mlLastAccessTime    = 0;

    private boolean mbEnabled        = true;

    private VirtualDirectory mUserDirectory = null;
    private String mstSessionId             = null;
    private InetAddress mClientAddress      = null;

    /**
     * Constructor, set session id and default virtual directory object.
     */
    public User() {
        mUserDirectory = new VirtualDirectory();
        mstSessionId = new UID().toString();
    }


    /**
     * Get the user name.
     */
    public String getName() {
        return mstUserName;
    }

    /**
     * Set user name.
     */
    public void setName(String name) {
        mstUserName = name;
    }


    /**
     * Get the user password.
     */
    public String getPassword() {
        return mstPassword;
    }

    /**
     * Set user password
     */
    public void setPassword(String pass) {
        mstPassword = pass;
    }


    /**
     * Get the maximum idle time in second.
     */
    public int getMaxIdleTime() {
        return (int)(mlIdleTime/1000);
    }

    /**
     * Set the maximum idle time in second.
     */
    public void setMaxIdleTime(int idleSec) {
        if(idleSec < 0L) {
            mlIdleTime = 0L;
        }
        mlIdleTime = idleSec * 1000L;
    }


    /**
     * Get the user enable status.
     */
    public boolean getEnabled() {
        return mbEnabled;
    }

    /**
     * Set the user enable status
     */
    public void setEnabled(boolean enb) {
        mbEnabled = enb;
    }


    /**
     * Get maximum user upload rate in bytes/sec.
     */
    public int getMaxUploadRate() {
        return miUploadRateLimit;
    }

    /**
     * Set user maximum upload rate limit.
     * Less than or equal to zero means no limit.
     */
    public void setMaxUploadRate(int rate) {
        miUploadRateLimit = rate;
    }


    /**
     * Get maximum user download rate in bytes/sec
     */
    public int getMaxDownloadRate() {
        return miDownloadRateLimit;
    }

    /**
     * Set user maximum download rate limit.
     * Less than or equal to zero means no limit.
     */
    public void setMaxDownloadRate(int rate) {
        miDownloadRateLimit = rate;
    }


    /**
     * Get client address
     */
    public InetAddress getClientAddress() {
       return mClientAddress;
    }

    /**
     * Set client address
     */
    public void setClientAddress(InetAddress clientAddress) {
       mClientAddress = clientAddress;
    }


    /**
     * get user filesystem view
     */
    public VirtualDirectory getVirtualDirectory() {
        return mUserDirectory;
    }

    /**
     * Get session id.
     */
    public String getSessionId() {
       return mstSessionId;
    }

    /**
     * Get user loglin time.
     */
    public long getLoginTime() {
       return mlLoginTime;
    }

    /**
     * Get last access time
     */
    public long getLastAccessTime() {
       return mlLastAccessTime;
    }

    /**
     * Check the user login status.
     */
    public boolean hasLoggedIn() {
        return mlLoginTime != 0;
    }

    /**
     * User login.
     */
    public void login() {
        mlLoginTime = System.currentTimeMillis();
        mlLastAccessTime = mlLoginTime;
    }

    /**
     * User logout
     */
    public void logout() {
        mlLoginTime = 0;
    }


    /**
      * Is an active user (is removable)?
      * Compares the last access time with the specified time.
      */
    public boolean isActive(long currTime) {
         boolean bActive = true;
         long maxIdleTime = getMaxIdleTime() * 1000; // milliseconds
         if(maxIdleTime != 0L) {
            long idleTime = currTime - mlLastAccessTime;
            bActive = maxIdleTime > idleTime;
         }
         return bActive;
    }

    /**
     * Is still active. Compares the last access time with the
     * current time.
     */
    public boolean isActive() {
        return isActive(System.currentTimeMillis());
    }

    /**
     * Hit user - update last access time
     */
    public void hitUser() {
       mlLastAccessTime = System.currentTimeMillis();
    }

    /**
     * Equality check.
     */
    public boolean equals(Object obj) {
        if (obj instanceof User) {
            return ((User)obj).mstSessionId.equals(mstSessionId);
        }
        return false;
    }

    /**
     * String representation
     */
    public String toString() {
        return mstUserName;
    }
}
