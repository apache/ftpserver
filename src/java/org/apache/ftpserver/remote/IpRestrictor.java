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

import java.io.IOException;
import java.net.InetAddress;
import java.util.Collection;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * IP Restrictor remotr adapter class. Used by admin GUI.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public
class IpRestrictor implements org.apache.ftpserver.remote.interfaces.IpRestrictorInterface{

    private org.apache.ftpserver.ip.IpRestrictorInterface mIpRestrictor;

    /**
     * Constructor - sets the actual ip restrictor object
     */
    public IpRestrictor(org.apache.ftpserver.ip.IpRestrictorInterface ipRestrictor) throws RemoteException {
        mIpRestrictor = ipRestrictor;
        UnicastRemoteObject.exportObject(this);
    }

    /**
     * Get the actual object.
     */
    public org.apache.ftpserver.ip.IpRestrictorInterface getActualObject() {
        return mIpRestrictor;
    }

    /**
     * Allow/ban IP flag
     */
    public boolean isAllowIp() {
        return mIpRestrictor.isAllowIp();
    }

    /**
     * Reload data from store.
     */
    public void reload() throws IOException {
        mIpRestrictor.reload();
    }

    /**
     * Save data into store.
     */
    public void save() throws IOException {
        mIpRestrictor.save();
    }

    /**
     * Check IP permission.
     */
    public boolean hasPermission(InetAddress addr) {
        return mIpRestrictor.hasPermission(addr);
    }

    /**
     * Clear all entries.
     */
    public void clear() {
        mIpRestrictor.clear();
    }

    /**
     * Add new entry
     */
    public void addEntry(String str) {
        mIpRestrictor.addEntry(str);
    }

    /**
     * Remove entry
     */
    public void removeEntry(String str) {
        mIpRestrictor.removeEntry(str);
    }

    /**
     * Get all entries
     */
    public Collection getAllEntries() {
        return mIpRestrictor.getAllEntries();
    }
}
