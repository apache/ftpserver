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

import org.apache.avalon.cornerstone.services.store.ObjectRepository;
import org.apache.avalon.cornerstone.services.store.Store;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.Serviceable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


/**
 *
 * @phoenix:block
 * @phoenix:service name="org.apache.ftpserver.usermanager.UserManagerInterface"
 *
 * File object repository based user manager.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public
class ObjStoreUserManager extends AbstractUserManager implements Serviceable, Configurable {

    protected Configuration mStoreConfig;
    protected Store mStore;
    protected ObjectRepository mObjectRepository;

    /**
     * Initialize object repository.
     */
    public void initialize() throws Exception {
        super.initialize();
        mObjectRepository = (ObjectRepository) mStore.select(mStoreConfig);
    }

    /**
     * Configure user manager
     */
    public void configure(Configuration conf) throws ConfigurationException {
        mStoreConfig = conf.getChild("repository");
        Configuration adminConf = conf.getChild("ftp-admin-name", false);
        mstAdminName = "admin";
        if(adminConf != null) {
            mstAdminName = adminConf.getValue(mstAdminName);
        }

    }

    /**
     * Get store manager.
     *
     * @phoenix:dependency name="org.apache.avalon.cornerstone.services.store.Store"
     */
    public void service(ServiceManager serviceManager) throws ServiceException {
        mStore = (Store) serviceManager.lookup(Store.class.getName());
    }

    /**
     * Save user object.
     */
    public synchronized void save(User usr) {
        if (usr.getName() == null) {
            throw new NullPointerException("User name is null.");
        }
        usr.setPassword(getPassword(usr));
        mObjectRepository.put(usr.getName(), usr);
    }

    /**
     * Delete an user. Removes all this user entries from the properties.
     * After removing the corresponding from the properties, save the data.
     */
    public synchronized void delete(String usrName) {
        mObjectRepository.remove(usrName);
    }

    /**
     * Get all user names.
     */
    public synchronized List getAllUserNames() {
        ArrayList usrList = new ArrayList();
        Iterator nameIt = mObjectRepository.list();
        while(nameIt.hasNext()) {
            usrList.add(nameIt.next());
        }

        Collections.sort(usrList);
        return usrList;
    }

    /**
     * Load user data.
     */
    public synchronized User getUserByName(String userName) {
        User user = null;
        if(doesExist(userName)) {
            user = (User)mObjectRepository.get(userName);
        }
        return user;
    }

    /**
     * User existance check
     */
    public synchronized boolean doesExist(String name) {
        return mObjectRepository.containsKey(name);
    }

    /**
     * Get user password. Returns the encrypted value.
     * If the password value is not null
     *    password = new password
     * else
     *   if user does exist
     *     password = old password
     *   else
     *     password = ""
     */
    private String getPassword(User usr) {
        String password = usr.getPassword();

        if ( (password == null) && doesExist(usr.getName()) ) {
            usr = getUserByName(usr.getName());
            password = usr.getPassword();
        }

        if (password == null) {
            password = "";
        }

        return password;
    }


    /**
     * User authenticate method
     */
    public boolean authenticate(String userName, String password) {
        User user = getUserByName(userName);
        if(user == null) {
            return false;
        }

        return password.equals(user.getPassword());
    }

    public void dispose() {

    }
}
