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

/**
 * Abstract user manager class.
 *
 * @author Paul Hammant
 */
public
        abstract class AbstractUserManager
        implements UserManagerInterface {

    protected String mstAdminName;


    /**
     * Initialize - fourth step.
     */
    public void initialize() throws Exception {
    }


    /**
     * Reload user data - dummy implementation.
     */
    public void reload() throws UserManagerException {
    }

    /**
     * Get admin name
     */
    public String getAdminName() {
        return mstAdminName;
    }

    /**
     * Close user manager
     */
    public abstract void dispose();

}
