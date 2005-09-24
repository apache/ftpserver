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

import org.apache.ftpserver.ftplet.Configuration;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.Logger;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.util.BaseProperties;
import org.apache.ftpserver.util.EncryptUtils;
import org.apache.ftpserver.util.IoUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;


/**
 * Properties file based <code>UserManager</code> implementation. 
 * We use <code>user.properties</code> file to store user data.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public
class PropertiesUserManager implements UserManager {

    private final static String PREFIX    = "FtpServer.user.";

    private BaseProperties m_userDataProp;
    private File           m_userDataFile;
    private boolean        m_isPasswordEncrypt;
    private String         m_adminName;
    
    private Logger m_logger;
    
    /**
     * Set logger.
     */
    public void setLogger(Logger logger) {
        m_logger = logger;
    }
    
    /**
     * Configure user manager.
     */
    public void configure(Configuration config) throws FtpException {
        try {
            m_userDataFile = new File(config.getString("prop-file", "./res/user.gen"));
            File dir = m_userDataFile.getParentFile();
            if( (!dir.exists()) && (!dir.mkdirs()) ) {
                String dirName = dir.getAbsolutePath();
                throw new IOException("Cannot create directory : " + dirName);
            }
            m_userDataFile.createNewFile();
            m_userDataProp = new BaseProperties(m_userDataFile);
            
            m_isPasswordEncrypt = config.getBoolean("prop-password-encrypt", true);
            m_adminName = config.getString("admin", "admin");
        }
        catch(IOException ex) {
            m_logger.error("PropertiesUserManager.configure()", ex);
            throw new FtpException("PropertiesUserManager.configure()", ex);
        }
    }

    /**
     * Get the admin name.
     */
    public String getAdminName() {
        return m_adminName;
    }
    
    /**
     * @return true if user with this login is administrator
     */
    public boolean isAdmin(String login) throws FtpException {
        return m_adminName.equals(login);
    }
    
    /**
     * Save user data. Store the properties.
     */
    public synchronized void save(User usr) throws FtpException {
        
       // null value check
       if(usr.getName() == null) {
           throw new NullPointerException("User name is null.");
       }
       String thisPrefix = PREFIX + usr.getName() + '.';
       
       // set other properties
       m_userDataProp.setProperty(thisPrefix + BaseUser.ATTR_PASSWORD,          getPassword(usr));
       m_userDataProp.setProperty(thisPrefix + BaseUser.ATTR_HOME,              usr.getHomeDirectory());
       m_userDataProp.setProperty(thisPrefix + BaseUser.ATTR_ENABLE,            usr.getEnabled());
       m_userDataProp.setProperty(thisPrefix + BaseUser.ATTR_WRITE_PERM,        usr.getWritePermission());
       m_userDataProp.setProperty(thisPrefix + BaseUser.ATTR_MAX_IDLE_TIME,     usr.getMaxIdleTime());
       m_userDataProp.setProperty(thisPrefix + BaseUser.ATTR_MAX_UPLOAD_RATE,   usr.getMaxUploadRate());
       m_userDataProp.setProperty(thisPrefix + BaseUser.ATTR_MAX_DOWNLOAD_RATE, usr.getMaxDownloadRate());
   
       // save user data
       FileOutputStream fos = null;
       try {
           fos = new FileOutputStream(m_userDataFile);
           m_userDataProp.store(fos, "Generated file - don't edit (please)");
       }
       catch(IOException ex) {
           m_logger.error("PropertiesUserManager.save()", ex);
           throw new FtpException("PropertiesUserManager.save()", ex);
       }
       finally {
           IoUtils.close(fos);
       }
    }
     
    /**
     * Delete an user. Removes all this user entries from the properties.
     * After removing the corresponding from the properties, save the data.
     */
    public synchronized void delete(String usrName) throws FtpException {
        
        // remove entries from properties
        String thisPrefix = PREFIX + usrName + '.';
        Enumeration propNames = m_userDataProp.propertyNames();
        ArrayList remKeys = new ArrayList();
        while(propNames.hasMoreElements()) {
            String thisKey = propNames.nextElement().toString();
            if(thisKey.startsWith(thisPrefix)) {
                remKeys.add(thisKey);
            }
        }
        Iterator remKeysIt = remKeys.iterator();
        while (remKeysIt.hasNext()) {
            m_userDataProp.remove(remKeysIt.next().toString());
        }
        
        // save user data
        FileOutputStream fos = null;
        try {    
            fos = new FileOutputStream(m_userDataFile);
            m_userDataProp.store(fos, "Generated file - don't edit (please)");
        }
        catch(IOException ex) {
            m_logger.error("PropertiesUserManager.delete()", ex);
            throw new FtpException("PropertiesUserManager.delete()", ex);
        }
        finally {
            IoUtils.close(fos);
        }
    }
    
    /**
     * Get user password. Returns the encrypted value.
     * <pre>
     * If the password value is not null
     *    password = new password 
     * else 
     *   if user does exist
     *     password = old password
     *   else 
     *     password = ""
     * </pre>
     */
    private String getPassword(User usr) {
        String name = usr.getName();
        String password = usr.getPassword();
        
        if(password != null) {
            if (m_isPasswordEncrypt) {
                password = EncryptUtils.encryptMD5(password);
            }
        }
        else {
            String blankPassword = "";
            if(m_isPasswordEncrypt) {
                blankPassword = EncryptUtils.encryptMD5("");
            }
            
            if( doesExist(name) ) {
                String key = PREFIX + name + '.' + BaseUser.ATTR_PASSWORD;
                password = m_userDataProp.getProperty(key, blankPassword);
            }
            else {
                password = blankPassword;
            }
        }
        return password;
    } 
    
    /**
     * Get all user names.
     */
    public synchronized Collection getAllUserNames() {

        // get all user names
        String suffix = '.' + BaseUser.ATTR_HOME;
        ArrayList ulst = new ArrayList();
        Enumeration allKeys = m_userDataProp.propertyNames();
        int prefixlen = PREFIX.length();
        int suffixlen = suffix.length();
        while(allKeys.hasMoreElements()) {
            String key = (String)allKeys.nextElement();
            if(key.endsWith(suffix)) {
                String name = key.substring(prefixlen);
                int endIndex = name.length() - suffixlen;
                name = name.substring(0, endIndex);
                ulst.add(name);
            }
        }
        
        Collections.sort(ulst);
        return ulst;
    }

    /**
     * Load user data.
     */
    public synchronized User getUserByName(String userName) {
        
        if (!doesExist(userName)) {
            return null;
        }
        
        String baseKey = PREFIX + userName + '.';
        BaseUser user = new BaseUser();
        user.setName(userName);
        user.setEnabled(m_userDataProp.getBoolean(baseKey + BaseUser.ATTR_ENABLE, true));
        user.setHomeDirectory( m_userDataProp.getProperty(baseKey + BaseUser.ATTR_HOME, "/") );
        user.setWritePermission(m_userDataProp.getBoolean(baseKey + BaseUser.ATTR_WRITE_PERM, false));
        user.setMaxIdleTime(m_userDataProp.getInteger(baseKey + BaseUser.ATTR_MAX_IDLE_TIME, 0));
        user.setMaxUploadRate(m_userDataProp.getInteger(baseKey + BaseUser.ATTR_MAX_UPLOAD_RATE, 0));
        user.setMaxDownloadRate(m_userDataProp.getInteger(baseKey + BaseUser.ATTR_MAX_DOWNLOAD_RATE, 0));
        return user;
    }
    
    /**
     * User existance check
     */
    public synchronized boolean doesExist(String name) {
        String key = PREFIX + name + '.' + BaseUser.ATTR_HOME;
        return m_userDataProp.containsKey(key);
    }
    
    /**
     * User authenticate method
     */
    public synchronized boolean authenticate(String user, String password) {
        if(password == null) {
            password = "";
        }
        
        String passVal = m_userDataProp.getProperty(PREFIX + user + '.' + BaseUser.ATTR_PASSWORD);
        if (m_isPasswordEncrypt) {
            password = EncryptUtils.encryptMD5(password);
        }
        return password.equals(passVal);
    }
        
    /**
     * Close the user manager - remove existing entries.
     */
    public synchronized void dispose() {
        if (m_userDataProp != null) {
            m_userDataProp.clear();
            m_userDataProp = null;
        }
    }
}

