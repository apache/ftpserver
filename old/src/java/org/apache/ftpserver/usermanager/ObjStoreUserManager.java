/* ====================================================================
 * Copyright 2002 - 2004
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
 *
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
