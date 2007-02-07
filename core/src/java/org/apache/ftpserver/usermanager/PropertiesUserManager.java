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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ftpserver.ftplet.Authentication;
import org.apache.ftpserver.ftplet.AuthenticationFailedException;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.util.BaseProperties;
import org.apache.ftpserver.util.EncryptUtils;
import org.apache.ftpserver.util.IoUtils;


/**
 * Properties file based <code>UserManager</code> implementation. 
 * We use <code>user.properties</code> file to store user data.
 */
public
class PropertiesUserManager extends AbstractUserManager {

    private final static String DEPRECATED_PREFIX    = "FtpServer.user.";
    private final static String PREFIX    = "ftpserver.user.";

    private Log log;
    
    private BaseProperties userDataProp;
    private File           userDataFile = new File("./res/user.gen");
    private boolean        isPasswordEncrypt = true;
    private String         adminName = "admin";

    private boolean isConfigured = false;
    
    
    /**
     * Set the log factory.
     */
    public void setLogFactory(LogFactory factory) {
        log = factory.getInstance(getClass());
    } 
    
    /**
     * Set the file used to store and read users. Must be set before 
     * {@link #configure()} is called.
     * @param propFile A file containing users
     */
    public void setPropFile(File propFile) {
        if(isConfigured) {
            throw new IllegalStateException("Must be called before configure()");
        }
        
        this.userDataFile = propFile; 
    }
  
    /**
     * If true is returned, passwords will be stored as hashes rather 
     * than in clear text. Default is true.
     * @return True if passwords are stored as hashes.
     */
    public boolean isEncryptPassword() {
        return isPasswordEncrypt;
    }
    
    /**
     * If set to true, passwords will be stored as a 
     * hash to ensure that it can not be retrived from the
     * user file.
     * Must be set before {@link #configure()} is called.
     * @param encryptPassword True to store a hash of the passwords,
     *      false to store the passwords in clear text.
     */
    public void setEncryptPasswords(boolean encryptPassword) {
        if(isConfigured) {
            throw new IllegalStateException("Must be called before configure()");
        }
        
        this.isPasswordEncrypt = encryptPassword;
    }

    /**
     * @deprecated Use {@link #setEncryptPasswords(boolean)}
     */
    public void setPropPasswordEncrypt(boolean encryptPassword) {
        setEncryptPasswords(encryptPassword);
    }
    
    /**
     * Configure user manager.
     */
    public void configure() throws FtpException {
        try {
            isConfigured  = true;
            File dir = userDataFile.getParentFile();
            if( (!dir.exists()) && (!dir.mkdirs()) ) {
                String dirName = dir.getAbsolutePath();
                throw new IOException("Cannot create directory : " + dirName);
            }
            userDataFile.createNewFile();
            userDataProp = new BaseProperties(userDataFile);
            
            convertDeprecatedPropertyNames();
        }
        catch(IOException ex) {
            log.fatal("PropertiesUserManager.configure()", ex);
            throw new FtpException("PropertiesUserManager.configure()", ex);
        }
    }
    
    private void convertDeprecatedPropertyNames() throws FtpException {
        Enumeration keys = userDataProp.propertyNames();
        
        boolean doSave = false;
        
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            
            if(key.startsWith(DEPRECATED_PREFIX)) {
                String newKey = PREFIX + key.substring(DEPRECATED_PREFIX.length());
                userDataProp.setProperty(newKey, userDataProp.getProperty(key));
                userDataProp.remove(key);
                
                doSave = true;
            }
        }
        
        if(doSave) {
            saveUserData();
        }
    }

    /**
     * Get the admin name.
     */
    public String getAdminName() {
        return adminName;
    }
    
    /**
     * Set the name to use as the administrator of the server.
     * The default value is "admin".
     * @param adminName The administrator user name
     */
    public void setAdminName(String adminName) {
        this.adminName = adminName;
    }

    /**
     * Set the name to use as the administrator of the server
     * @param adminName The administrator user name
     * @deprecated Use {@link #setAdminName(String)} instead
     */
    public void setAdmin(String adminName) {
        this.adminName = adminName;
    }
    
    /**
     * @return true if user with this login is administrator
     */
    public boolean isAdmin(String login) throws FtpException {
        return adminName.equals(login);
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
       userDataProp.setProperty(thisPrefix + ATTR_PASSWORD,          getPassword(usr));
       
       String home = usr.getHomeDirectory();
       if(home == null) {
           home = "/";
       }
       userDataProp.setProperty(thisPrefix + ATTR_HOME,              home);
       userDataProp.setProperty(thisPrefix + ATTR_ENABLE,            usr.getEnabled());
       userDataProp.setProperty(thisPrefix + ATTR_WRITE_PERM,        usr.authorize(new WriteRequest()) != null);
       userDataProp.setProperty(thisPrefix + ATTR_MAX_IDLE_TIME,     usr.getMaxIdleTime());
       
       TransferRateRequest transferRateRequest = new TransferRateRequest();
       transferRateRequest = (TransferRateRequest) usr.authorize(transferRateRequest);
       
       if(transferRateRequest != null) {
           userDataProp.setProperty(thisPrefix + ATTR_MAX_UPLOAD_RATE,   
                   transferRateRequest.getMaxUploadRate());
           userDataProp.setProperty(thisPrefix + ATTR_MAX_DOWNLOAD_RATE, 
                   transferRateRequest.getMaxDownloadRate());
       } else {
           userDataProp.remove(thisPrefix + ATTR_MAX_UPLOAD_RATE);
           userDataProp.remove(thisPrefix + ATTR_MAX_DOWNLOAD_RATE);       
       }
       
       // request that always will succeed
       ConcurrentLoginRequest concurrentLoginRequest = new ConcurrentLoginRequest(0, 0);
       concurrentLoginRequest = (ConcurrentLoginRequest) usr.authorize(concurrentLoginRequest);
       
       if(concurrentLoginRequest != null) {
           userDataProp.setProperty(thisPrefix + ATTR_MAX_LOGIN_NUMBER, 
                   concurrentLoginRequest.getMaxConcurrentLogins());
           userDataProp.setProperty(thisPrefix + ATTR_MAX_LOGIN_PER_IP, 
                   concurrentLoginRequest.getMaxConcurrentLoginsPerIP());
       } else {
           userDataProp.remove(thisPrefix + ATTR_MAX_LOGIN_NUMBER);
           userDataProp.remove(thisPrefix + ATTR_MAX_LOGIN_PER_IP);   
       }
       
       saveUserData();
    }

    /**
     * @throws FtpException
     */
    private void saveUserData() throws FtpException {
        // save user data
           FileOutputStream fos = null;
           try {
               fos = new FileOutputStream(userDataFile);
               userDataProp.store(fos, "Generated file - don't edit (please)");
           }
           catch(IOException ex) {
               log.error("Failed saving user data", ex);
               throw new FtpException("Failed saving user data", ex);
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
        Enumeration propNames = userDataProp.propertyNames();
        ArrayList remKeys = new ArrayList();
        while(propNames.hasMoreElements()) {
            String thisKey = propNames.nextElement().toString();
            if(thisKey.startsWith(thisPrefix)) {
                remKeys.add(thisKey);
            }
        }
        Iterator remKeysIt = remKeys.iterator();
        while (remKeysIt.hasNext()) {
            userDataProp.remove(remKeysIt.next().toString());
        }
        
        saveUserData();
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
            if (isPasswordEncrypt) {
                password = EncryptUtils.encryptMD5(password);
            }
        }
        else {
            String blankPassword = "";
            if(isPasswordEncrypt) {
                blankPassword = EncryptUtils.encryptMD5("");
            }
            
            if( doesExist(name) ) {
                String key = PREFIX + name + '.' + ATTR_PASSWORD;
                password = userDataProp.getProperty(key, blankPassword);
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
    public synchronized String[] getAllUserNames() {

        // get all user names
        String suffix = '.' + ATTR_HOME;
        ArrayList ulst = new ArrayList();
        Enumeration allKeys = userDataProp.propertyNames();
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
        return (String[]) ulst.toArray(new String[0]);
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
        user.setEnabled(userDataProp.getBoolean(baseKey + ATTR_ENABLE, true));
        user.setHomeDirectory( userDataProp.getProperty(baseKey + ATTR_HOME, "/") );
        
        List authorities = new ArrayList();
        
        if(userDataProp.getBoolean(baseKey + ATTR_WRITE_PERM, false)) {
            authorities.add(new WritePermission());
        }
        
        int maxLogin = userDataProp.getInteger(baseKey + ATTR_MAX_LOGIN_NUMBER, 0);
        int maxLoginPerIP = userDataProp.getInteger(baseKey + ATTR_MAX_LOGIN_PER_IP, 0);
        
        authorities.add(new ConcurrentLoginPermission(maxLogin, maxLoginPerIP));

        int uploadRate = userDataProp.getInteger(baseKey + ATTR_MAX_UPLOAD_RATE, 0);
        int downloadRate = userDataProp.getInteger(baseKey + ATTR_MAX_DOWNLOAD_RATE, 0);
        
        authorities.add(new TransferRatePermission(downloadRate, uploadRate));
        
        user.setAuthorities((Authority[]) authorities.toArray(new Authority[0]));
        
        user.setMaxIdleTime(userDataProp.getInteger(baseKey + ATTR_MAX_IDLE_TIME, 0));

        return user;
    }
    
    /**
     * User existance check
     */
    public synchronized boolean doesExist(String name) {
        String key = PREFIX + name + '.' + ATTR_HOME;
        return userDataProp.containsKey(key);
    }
    
    /**
     * User authenticate method
     */
    public synchronized User authenticate(Authentication authentication) throws AuthenticationFailedException {
        
        if(authentication instanceof UsernamePasswordAuthentication) {
            UsernamePasswordAuthentication upauth = (UsernamePasswordAuthentication) authentication;
            
            String user = upauth.getUsername(); 
            String password = upauth.getPassword(); 
        
            if(user == null) {
                throw new AuthenticationFailedException("Authentication failed");
            }
            
            if(password == null) {
                password = "";
            }
            
            String passVal = userDataProp.getProperty(PREFIX + user + '.' + ATTR_PASSWORD);
            if (isPasswordEncrypt) {
                password = EncryptUtils.encryptMD5(password);
            }
            if(password.equals(passVal)) {
                return getUserByName(user);
            } else {
                throw new AuthenticationFailedException("Authentication failed");
            }
            
        } else if(authentication instanceof AnonymousAuthentication) {
            if(doesExist("anonymous")) {
                return getUserByName("anonymous");
            } else {
                throw new AuthenticationFailedException("Authentication failed");
            }
        } else {
            throw new IllegalArgumentException("Authentication not supported by this user manager");
        }
    }
        
    /**
     * Close the user manager - remove existing entries.
     */
    public synchronized void dispose() {
        if (userDataProp != null) {
            userDataProp.clear();
            userDataProp = null;
        }
    }
}

