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
package org.apache.ftpserver.ip;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Collection;


/**
 * IP Restrictor interface
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */

public
interface IpRestrictorInterface {

    String ROLE = IpRestrictorInterface.class.getName();

    /**
     * Allow/ban the listed IPs flag.
     */
    boolean isAllowIp();

    /**
     * Reload data from store.
     */
    void reload() throws IOException;

    /**
     * Save data into store.
     */
    void save() throws IOException;

    /**
     * Check IP permission.
     */
    boolean hasPermission(InetAddress addr);

    /**
     * Clear all entries.
     */
    void clear();

    /**
     * Add new entry
     */
    void addEntry(String str);

    /**
     * Remove entry
     */
    void removeEntry(String str);

    /**
     * Get all entries
     */
    Collection getAllEntries();
}
