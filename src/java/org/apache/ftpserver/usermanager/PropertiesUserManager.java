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

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Collections;
import java.util.Enumeration;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.ContextException;

import org.apache.avalon.phoenix.BlockContext;

import org.apache.ftpserver.util.IoUtils;
import org.apache.ftpserver.util.BaseProperties;
import org.apache.ftpserver.util.EncryptUtils;

/**
 * Properties file based <code>UserManager</code>
 * implementation. We use <code>user.properties</code> file
 * to store user data.
 *
 * @phoenix:block
 * @phoenix:service name="org.apache.ftpserver.usermanager.UserManagerInterface"
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public class PropertiesUserManager extends AbstractUserManager {

    private static final String PREFIX    = "FtpServer.user.";
    private static final String USER_PROP = "user.properties";

    private BaseProperties mUserData;
    private File       mUserDataFile;
    private boolean    mbEncrypt;

    private long mlLastModified;

    /**
     * Instantiate user manager - default constructor.
     *
     * @param cfg Ftp config object.
     */
    public PropertiesUserManager() throws Exception {
    }

    /**
     * Set application context
     */
    public void contextualize(Context context) throws ContextException {
        super.contextualize(context);
        try {
            mUserDataFile = new File(((BlockContext)context).getBaseDirectory(), USER_PROP)   ;
            mUserDataFile.createNewFile();
            mUserData = new BaseProperties(mUserDataFile);
            mlLastModified = mUserDataFile.lastModified();
            getLogger().info("Loaded user data file - " + mUserDataFile);
        }
        catch(IOException ex) {
            getLogger().error(ex.getMessage(), ex);
            throw new ContextException(ex.getMessage());
        }
    }

    /**
     * Set configuration
     */
    public void configure(Configuration conf) throws ConfigurationException {
        super.configure(conf);
        mbEncrypt = conf.getChild("encrypt").getValueAsBoolean(false);
    }


    /**
     * Save user data. Store the properties.
     */
    public synchronized void save(User usr) throws IOException {

       // null value check
       if(usr.getName() == null) {
           throw new NullPointerException("User name is null.");
       }
       String thisPrefix = PREFIX + usr.getName() + '.';

       // set other properties
       mUserData.setProperty(thisPrefix + User.ATTR_PASSWORD,          getPassword(usr));
       mUserData.setProperty(thisPrefix + User.ATTR_HOME,              usr.getVirtualDirectory().getRootDirectory());
       mUserData.setProperty(thisPrefix + User.ATTR_ENABLE,            usr.getEnabled());
       mUserData.setProperty(thisPrefix + User.ATTR_WRITE_PERM,        usr.getVirtualDirectory().getWritePermission());
       mUserData.setProperty(thisPrefix + User.ATTR_MAX_IDLE_TIME,     usr.getMaxIdleTime());
       mUserData.setProperty(thisPrefix + User.ATTR_MAX_UPLOAD_RATE,   usr.getMaxUploadRate());
       mUserData.setProperty(thisPrefix + User.ATTR_MAX_DOWNLOAD_RATE, usr.getMaxDownloadRate());

       // save user data
       FileOutputStream fos = null;
       try {
           fos = new FileOutputStream(mUserDataFile);
           mUserData.store(fos, "Generated file - don't edit (please)");
           mlLastModified = mUserDataFile.lastModified();
       }
       finally {
           IoUtils.close(fos);
       }
    }


    /**
     * Delete an user. Removes all this user entries from the properties.
     * After removing the corresponding from the properties, save the data.
     */
    public synchronized void delete(String usrName) throws IOException {

        // remove entries from properties
        String thisPrefix = PREFIX + usrName + '.';
        Enumeration propNames = mUserData.propertyNames();
        ArrayList remKeys = new ArrayList();
        while(propNames.hasMoreElements()) {
            String thisKey = propNames.nextElement().toString();
            if(thisKey.startsWith(thisPrefix)) {
                remKeys.add(thisKey);
            }
        }
        Iterator remKeysIt = remKeys.iterator();
        while (remKeysIt.hasNext()) {
            mUserData.remove(remKeysIt.next().toString());
        }

        // save user data
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mUserDataFile);
            mUserData.store(fos, "Generated file - don't edit (please)");
            mlLastModified = mUserDataFile.lastModified();
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
        String password = usr.getPassword();
        if (password != null) {
            if (mbEncrypt) {
                password = EncryptUtils.encryptMD5(password);
            }
        }
        else if ( doesExist(usr.getName()) ) {
            String key = PREFIX + usr.getName() + '.' + User.ATTR_PASSWORD;
            password = mUserData.getProperty(key, "");
        }

        if (password == null) {
            password = "";
        }

        return password;
    }


    /**
     * Get all user names.
     */
    public synchronized List getAllUserNames() {

        // get all user names
        String suffix = '.' + User.ATTR_HOME;
        ArrayList ulst = new ArrayList();
        Enumeration allKeys = mUserData.propertyNames();
        while(allKeys.hasMoreElements()) {
            String key = (String)allKeys.nextElement();
            if(key.endsWith(suffix)) {
                String name = key.substring(PREFIX.length());
                int endIndex = name.length() - suffix.length();
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
        User user = new User();
        user.setName(userName);
        user.setEnabled(mUserData.getBoolean(baseKey + User.ATTR_ENABLE, true));
        user.getVirtualDirectory().setRootDirectory( mUserData.getFile(baseKey + User.ATTR_HOME, new File("/")) );
        user.getVirtualDirectory().setWritePermission(mUserData.getBoolean(baseKey + User.ATTR_WRITE_PERM, false));
        user.setMaxIdleTime(mUserData.getInteger(baseKey + User.ATTR_MAX_IDLE_TIME, 0));
        user.setMaxUploadRate(mUserData.getInteger(baseKey + User.ATTR_MAX_UPLOAD_RATE, 0));
        user.setMaxDownloadRate(mUserData.getInteger(baseKey + User.ATTR_MAX_DOWNLOAD_RATE, 0));
        return user;
    }


    /**
     * User existance check
     */
    public synchronized boolean doesExist(String name) {
        String key = PREFIX + name + '.' + User.ATTR_HOME;
        return mUserData.containsKey(key);
    }


    /**
     * User authenticate method
     */
    public synchronized boolean authenticate(String user, String password) {
        String passVal = mUserData.getProperty(PREFIX + user + '.' + User.ATTR_PASSWORD);
        if (mbEncrypt) {
            password = EncryptUtils.encryptMD5(password);
        }
        return password.equals(passVal);
    }

    /**
     * Reload the user data if necessary
     */
    public synchronized void reload() throws Exception {
        long lastModified = mUserDataFile.lastModified();
        if (lastModified > mlLastModified) {
            FileInputStream fis = new FileInputStream(mUserDataFile);
            mUserData.load(fis);
            fis.close();
            mlLastModified = lastModified;
            getLogger().info("File modified - loading " + mUserDataFile.getAbsolutePath());
        }
    }

    /**
     * Close the user manager - remove existing entries.
     */
    public void dispose() {
        getLogger().info("Closing properties user manager...");
        if (mUserData != null) {
            mUserData.clear();
            mUserData = null;
        }
    }
}

