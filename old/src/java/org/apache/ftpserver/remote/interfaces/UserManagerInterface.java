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
package org.apache.ftpserver.remote.interfaces;

import java.util.List;
import java.rmi.Remote;
import java.rmi.RemoteException;
import org.apache.ftpserver.usermanager.User;

/**
 * This is user manager remote interface. This is used by remote admin GUI.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public
interface UserManagerInterface extends Remote {

    /**
     * Save the user. If a new user, create it else update the
     * existing user.
     */
    void save(final User user) throws Exception;

    /**
     * Delete the user from the system.
     *
     * @param name name of the user to be deleted.
     */
    void delete(final String userName) throws Exception;

    /**
     * Get user by name.
     */
    User getUserByName(final String name) throws RemoteException;

    /**
     * Get all user names in the system.
     */
    List getAllUserNames() throws RemoteException;

    /**
     * User existance check.
     *
     * @param name user name
     */
    boolean doesExist(final String name) throws RemoteException;

    /**
     * Authenticate user
     */
    boolean authenticate(final String login, final String password) throws RemoteException;

    /**
     * Load the user data again
     */
    void reload() throws Exception;

    /**
     * Get admin user name
     */
    String getAdminName() throws RemoteException;

}
