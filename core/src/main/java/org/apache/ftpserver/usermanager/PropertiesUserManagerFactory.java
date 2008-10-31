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

import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.usermanager.impl.PropertiesUserManager;

/**
 * Factory for the properties file based <code>UserManager</code> implementation.
 *
 * @author The Apache MINA Project (dev@mina.apache.org)
 * @version $Rev: 689495 $, $Date: 2008-08-27 16:58:52 +0200 (Wed, 27 Aug 2008) $
 */
public class PropertiesUserManagerFactory implements UserManagerFactory {


    
    private String adminName = "admin";
    
    private File userDataFile;
    
    private PasswordEncryptor passwordEncryptor = new Md5PasswordEncryptor();

    /**
     * Creates a {@link PropertiesUserManager} instance based on the provided configuration
     */
    public UserManager createUserManager() {
        return new PropertiesUserManager(passwordEncryptor, userDataFile, adminName);
    }
    
    /**
     * Get the admin name.
     */
    public String getAdminName() {
        return adminName;
    }

    /**
     * Set the name to use as the administrator of the server. The default value
     * is "admin".
     * 
     * @param adminName
     *            The administrator user name
     */
    public void setAdminName(String adminName) {
        this.adminName = adminName;
    }
    
    /**
     * Retrieve the file used to load and store users
     * @return The file
     */
    public File getFile() {
        return userDataFile;
    }

    /**
     * Set the file used to store and read users. 
     * 
     * @param propFile
     *            A file containing users
     */
    public void setFile(File propFile) {
        this.userDataFile = propFile;
    }

    
    /**
     * Retrieve the password encryptor used by user managers created by this factory
     * @return The password encryptor. Default to {@link Md5PasswordEncryptor}
     *  if no other has been provided
     */    
    public PasswordEncryptor getPasswordEncryptor() {
        return passwordEncryptor;
    }


    /**
     * Set the password encryptor to use by user managers created by this factory
     * @param passwordEncryptor The password encryptor
     */
    public void setPasswordEncryptor(PasswordEncryptor passwordEncryptor) {
        this.passwordEncryptor = passwordEncryptor;
    }
}
