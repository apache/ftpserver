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

import java.io.IOException;
import java.net.InetAddress;
import java.util.Collection;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * IP Restrictor remotr interface. Used by admin GUI.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */

public
interface IpRestrictorInterface extends Remote {

    /**
     * Allow/ban IP flag
     */
    boolean isAllowIp() throws RemoteException;

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
    boolean hasPermission(final InetAddress addr) throws RemoteException;

    /**
     * Clear all entries.
     */
    void clear() throws RemoteException;

    /**
     * Add new entry
     */
    void addEntry(final String str) throws RemoteException;

    /**
     * Remove entry
     */
    void removeEntry(final String str) throws RemoteException;

    /**
     * Get all entries
     */
    Collection getAllEntries() throws RemoteException;
}
