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

package org.apache.ftpserver.ftplet;


/**
 * User manager interface.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
interface UserManager extends Component {

    /**
     * Get user by name.
     */
    User getUserByName(String login) throws FtpException;
    
    /**
     * Get all user names in the system.
     */
    String[] getAllUserNames() throws FtpException;
    
    /**
     * Delete the user from the system.
     * 
     * @throws UnsupportedOperationException
     *             if UserManager in read-only mode
     */
    void delete(String login) throws FtpException;
    
    /**
     * Save user. If a new user, create it else update the existing user.
     * 
     * @throws UnsupportedOperationException
     *             if UserManager in read-only mode
     */
    void save(User user) throws FtpException;
    
    /**
     * User existance check.
     */
    boolean doesExist(String login) throws FtpException;
    
    /**
     * Authenticate user
     */
    User authenticate(Authentication authentication) throws AuthenticationFailedException;
    
    /**
     * Get admin user name
     */
    String getAdminName() throws FtpException;
    
    /**
     * @return true if user with this login is administrator
     */
    boolean isAdmin(String login) throws FtpException;
}
