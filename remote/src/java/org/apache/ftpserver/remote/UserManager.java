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
package org.apache.ftpserver.remote;

import org.apache.ftpserver.remote.interfaces.UserManagerInterface;
import org.apache.ftpserver.usermanager.User;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

/**
 * This is user manager remote adapter class. This is used by remote admin GUI.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public
class UserManager implements UserManagerInterface {

    private org.apache.ftpserver.usermanager.UserManagerInterface mUserManager;

    /**
     * Constructor - sets the actual user manager
     */
    public UserManager(org.apache.ftpserver.usermanager.UserManagerInterface userManager) throws RemoteException {
        mUserManager = userManager;
        UnicastRemoteObject.exportObject(this);
    }

    /**
     * Get the actual user manager
     */
    public org.apache.ftpserver.usermanager.UserManagerInterface getUserManager() {
        return mUserManager;
    }

    /**
     * Save the user. If a new user, create it else update the
     * existing user.
     */
    public void save(User user) throws Exception {
        mUserManager.save(user);
    }

    /**
     * Delete the user from the system.
     *
     * @param name name of the user to be deleted.
     */
    public void delete(String userName) throws Exception {
        mUserManager.delete(userName);
    }

    /**
     * Get user by name.
     */
    public User getUserByName(String name) {
        return mUserManager.getUserByName(name);
    }

    /**
     * Get all user names in the system.
     */
    public List getAllUserNames() {
        return mUserManager.getAllUserNames();
    }

    /**
     * User existance check.
     *
     * @param name user name
     */
    public boolean doesExist(String name) {
        return mUserManager.doesExist(name);
    }

    /**
     * Authenticate user
     */
    public boolean authenticate(String login, String password) {
        return mUserManager.authenticate(login, password);
    }

    /**
     * Load the user data again
     */
    public void reload() throws Exception {
        mUserManager.reload();
    }

    /**
     * Get admin name
     */
    public String getAdminName() {
        return mUserManager.getAdminName();
    }

}
