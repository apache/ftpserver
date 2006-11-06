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

import java.util.Properties;

import junit.framework.TestCase;

import org.apache.ftpserver.config.PropertiesConfiguration;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.ftplet.UserManager;

public abstract class UserManagerTestTemplate extends TestCase {

    protected UserManager userManager;

    protected abstract UserManager createUserManager() throws Exception;
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        userManager = createUserManager();
    }

    /**
     * @return
     */
    protected abstract Properties createConfig();

    public void testAuthenticate() throws Exception {
        assertTrue(userManager.authenticate("user1", "pw1"));
    }

    public void testAuthenticateWrongPassword() throws Exception {
        assertFalse(userManager.authenticate("user1", "foo"));
    }

    public void testAuthenticateUnknownUser() throws Exception {
        assertFalse(userManager.authenticate("foo", "foo"));
    }

    public void testDoesExist() throws Exception {
        assertTrue(userManager.doesExist("user1"));
        assertTrue(userManager.doesExist("user2"));
        assertFalse(userManager.doesExist("foo"));
    }

    public void testGetAdminName() throws Exception {
        assertEquals("admin", userManager.getAdminName());
    }

    public void testIsAdmin() throws Exception {
        assertTrue(userManager.isAdmin("admin"));
        assertFalse(userManager.isAdmin("user1"));
        assertFalse(userManager.isAdmin("foo"));
    }

    public void testDelete() throws Exception {
        assertTrue(userManager.doesExist("user1"));
        assertTrue(userManager.doesExist("user2"));
        userManager.delete("user1");
        assertFalse(userManager.doesExist("user1"));
        assertTrue(userManager.doesExist("user2"));
        userManager.delete("user2");
        assertFalse(userManager.doesExist("user1"));
        assertFalse(userManager.doesExist("user2"));
    }

    public void testDeleteNonExistingUser() throws Exception {
        // silent failure
        userManager.delete("foo");
    }

    public void testGetUserByNameWithDefaultValues() throws Exception {
        User user = userManager.getUserByName("user1");

        assertEquals("user1", user.getName());
        assertNull("Password must not be set", user.getPassword());
        assertEquals("home", user.getHomeDirectory());
        assertEquals(0, user.getMaxDownloadRate());
        assertEquals(0, user.getMaxIdleTime());
        assertEquals(0, user.getMaxLoginNumber());
        assertEquals(0, user.getMaxLoginPerIP());
        assertEquals(0, user.getMaxUploadRate());
        assertFalse(user.getWritePermission());
        assertTrue(user.getEnabled());
    }

    public void testGetUserByName() throws Exception {
        User user = userManager.getUserByName("user2");
        
        assertEquals("user2", user.getName());
        assertNull("Password must not be set", user.getPassword());
        assertEquals("home", user.getHomeDirectory());
        assertEquals(1, user.getMaxDownloadRate());
        assertEquals(2, user.getMaxIdleTime());
        assertEquals(3, user.getMaxLoginNumber());
        assertEquals(4, user.getMaxLoginPerIP());
        assertEquals(5, user.getMaxUploadRate());
        assertTrue(user.getWritePermission());
        assertFalse(user.getEnabled());
    }

    public void testGetUserByNameWithUnknownUser() throws Exception {
        assertNull(userManager.getUserByName("foo"));
    }

    public void testSave() throws Exception {
        BaseUser user = new BaseUser();
        user.setName("newuser");
        user.setPassword("newpw");
        user.setHomeDirectory("newhome");
        user.setEnabled(false);
        user.setMaxDownloadRate(1);
        user.setMaxIdleTime(2);
        user.setMaxLoginNumber(3);
        user.setMaxLoginPerIP(4);
        user.setMaxUploadRate(5);

        user.setWritePermission(true);
        userManager.save(user);
        
        UserManager newUserManager = new PropertiesUserManager();
        newUserManager.configure(new PropertiesConfiguration(createConfig()));

        
        User actualUser = newUserManager.getUserByName("newuser");
        
        assertEquals(user.getName(), actualUser.getName());
        assertNull(actualUser.getPassword());
        assertEquals(user.getHomeDirectory(), actualUser.getHomeDirectory());
        assertEquals(user.getEnabled(), actualUser.getEnabled());
        assertEquals(user.getWritePermission(), actualUser.getWritePermission());
        assertEquals(user.getMaxDownloadRate(), actualUser.getMaxDownloadRate());
        assertEquals(user.getMaxIdleTime(), actualUser.getMaxIdleTime());
        assertEquals(user.getMaxLoginNumber(), actualUser.getMaxLoginNumber());
        assertEquals(user.getMaxLoginPerIP(), actualUser.getMaxLoginPerIP());
        assertEquals(user.getMaxUploadRate(), actualUser.getMaxUploadRate());
    }

    public void testSaveWithExistingUser() throws Exception {
        BaseUser user = new BaseUser();
        user.setName("user2");
        user.setHomeDirectory("newhome");
        userManager.save(user);
        
        User actualUser = userManager.getUserByName("user2");
        
        assertEquals("user2", actualUser.getName());
        assertNull(actualUser.getPassword());
        assertEquals("newhome", actualUser.getHomeDirectory());
        assertEquals(0, actualUser.getMaxDownloadRate());
        assertEquals(0, actualUser.getMaxIdleTime());
        assertEquals(0, actualUser.getMaxLoginNumber());
        assertEquals(0, actualUser.getMaxLoginPerIP());
        assertEquals(0, actualUser.getMaxUploadRate());
        assertFalse(actualUser.getWritePermission());
        assertTrue(actualUser.getEnabled());
    }

    public void testSaveWithDefaultValues() throws Exception {
        BaseUser user = new BaseUser();
        user.setName("newuser");
        user.setPassword("newpw");
        userManager.save(user);
        
        UserManager newUserManager = new PropertiesUserManager();
        newUserManager.configure(new PropertiesConfiguration(createConfig()));
        
        User actualUser = newUserManager.getUserByName("newuser");
        
        assertEquals(user.getName(), actualUser.getName());
        assertNull(actualUser.getPassword());
        assertEquals("/", actualUser.getHomeDirectory());
        assertEquals(true, actualUser.getEnabled());
        assertEquals(false, actualUser.getWritePermission());
        assertEquals(0, actualUser.getMaxDownloadRate());
        assertEquals(0, actualUser.getMaxIdleTime());
        assertEquals(0, actualUser.getMaxLoginNumber());
        assertEquals(0, actualUser.getMaxLoginPerIP());
        assertEquals(0, actualUser.getMaxUploadRate());
    }
}
