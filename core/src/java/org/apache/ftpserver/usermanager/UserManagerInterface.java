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

import org.apache.ftpserver.UserManagerException;

import java.util.List;

/**
 * This is user manager interface. All the user manager classes
 * implement this interface. If we want to add a new user manager,
 * we have to implement this class.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public
interface UserManagerInterface {

    String ROLE = UserManagerInterface.class.getName();

    /**
     * Save the user. If a new user, create it else update the existing user.
     *
     * @throws UnsupportedOperationException if this operation is not supported by user manager.
     */
    void save(User user) throws UserManagerException;

    /**
     * Delete the user from the system.
     *
     * @param userName name of the user to be deleted.
     * @throws UnsupportedOperationException if this operation is not supported by user manager.
     */
    void delete(String userName) throws UserManagerException;

    /**
     * Get user by name.
     */
    User getUserByName(String name);

    /**
     * Get all user names in the system.
     */
    List getAllUserNames();

    /**
     * User existance check.
     *
     * @param name user name
     */
    boolean doesExist(String name);

    /**
     * Authenticate user
     */
    boolean authenticate(String login, String password);

    /**
     * Load the user data again
     */
    void reload() throws UserManagerException;

    /**
     * Get admin user name
     */
    String getAdminName();
}
