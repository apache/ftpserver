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

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.net.InetAddress;

import org.apache.ftpserver.remote.interfaces.FtpConfigInterface;
import org.apache.ftpserver.remote.interfaces.IpRestrictorInterface;
import org.apache.ftpserver.remote.interfaces.ConnectionServiceInterface;
import org.apache.ftpserver.remote.interfaces.UserManagerInterface;
import org.apache.ftpserver.remote.interfaces.FtpStatisticsInterface;

/**
 * Ftp configuration remote adapter. It is used by remote admin GUI.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public
class FtpConfig implements FtpConfigInterface {

    private org.apache.ftpserver.core.AbstractFtpConfig mConfig;

    private IpRestrictor mIpRestrictor;
    private UserManager mUserManager;
    private ConnectionService mConService;
    private FtpStatistics mStatistics;

    /**
     * Constructor - sets the actual config object.
     */
    public FtpConfig(org.apache.ftpserver.core.AbstractFtpConfig config) throws RemoteException {
        mConfig = config;
        mIpRestrictor = new IpRestrictor(config.getIpRestrictor());
        mUserManager  = new UserManager(config.getUserManager());
        mConService   = new ConnectionService(config.getConnectionService());
        mStatistics   = new FtpStatistics(config.getStatistics());

        UnicastRemoteObject.exportObject(this);
    }

    /**
     * Get config
     */
    public org.apache.ftpserver.core.AbstractFtpConfig getConfig() {
        return mConfig;
    }

    /**
     * Get user manager
     */
    public UserManagerInterface getUserManager() {
        return mUserManager;
    }

    /**
     * Get IP restrictor object.
     */
    public IpRestrictorInterface getIpRestrictor() {
        return mIpRestrictor;
    }

    /**
     * Get server bind address.
     */
    public InetAddress getServerAddress() {
        return mConfig.getServerAddress();
    }

    /**
     * Get address string
     */
    public String getAddressString() {
        return mConfig.getSelfAddress().toString();
    }

    /**
     * Get server port.
     */
    public int getServerPort() {
        return mConfig.getServerPort();
    }

    /**
     * Check annonymous login support.
     */
    public boolean isAnonymousLoginAllowed() {
        return mConfig.isAnonymousLoginAllowed();
    }

    /**
     * Get the connection handler
     */
    public ConnectionServiceInterface getConnectionService() {
        return mConService;
    }

    /**
     * Get maximum number of connections.
     */
    public int getMaxConnections() {
        return mConfig.getMaxConnections();
    }

    /**
     * Get maximum number of anonymous connections.
     */
    public int getMaxAnonymousLogins() {
        return mConfig.getMaxAnonymousLogins();
    }

    /**
     * Get poll interval in seconds.
     */
    public int getSchedulerInterval() {
        return mConfig.getSchedulerInterval();
    }

    /**
     * Get default idle time in seconds.
     */
    public int getDefaultIdleTime() {
        return mConfig.getDefaultIdleTime();
    }

    /**
     * Get default root directory
     */
    public String getDefaultRoot() {
        return mConfig.getDefaultRoot().getAbsolutePath();
    }

    /**
     * Get global statistics object.
     */
    public FtpStatisticsInterface getStatistics() {
        return mStatistics;
    }

    /**
     * Get rmi port
     */
    public int getRemoteAdminPort() {
        return mConfig.getRemoteAdminPort();
    }

    /**
     * Is remote admin allowed
     */
    public boolean isRemoteAdminAllowed() {
        return mConfig.isRemoteAdminAllowed();
    }

    /**
     * Get base directory
     */
    public String getBaseDirectory() {
        return mConfig.getBaseDirectory().getAbsolutePath();
    }

}

