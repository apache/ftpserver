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
package org.apache.ftpserver.core;

import org.apache.ftpserver.UserManagerException;
import org.apache.ftpserver.ip.IpRestrictorInterface;
import org.apache.ftpserver.remote.RemoteHandlerFactory;
import org.apache.ftpserver.usermanager.UserManagerInterface;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.StringTokenizer;

/**
 * Ftp configuration class. It has all ftp server configuration
 * parameters. This is not hot-editable. parameters will be loaded
 * once during server startup. We can add our own config parameters.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public
class FtpConfigBean extends AbstractFtpConfig {

    public FtpConfigBean(IpRestrictorInterface ipRestrictorInterface, UserManagerInterface userManagerInterface, RemoteHandlerFactory remoteHandlerFactory) throws IOException, UserManagerException {
        mUserManager = userManagerInterface;
        mIpRestrictor = ipRestrictorInterface;
        mRemoteHandlerFactory = remoteHandlerFactory;
        miServerPort = 21;
        mDefaultRoot = new File("/");
        miDefaultIdle = 300;
        mbRemoteAdminAllowed = true;
        miRmiPort = java.rmi.registry.Registry.REGISTRY_PORT;
        miPollInterval = 120;
        miAnonLogin = 10;
        mbAnonAllowed = true;
        miMaxLogin = 20;
        setDefaultDataPool("0");
        setConfiguration();
    }

    public void setServerHost(String serverHost) throws UnknownHostException {
        if (serverHost != null) {
            mServerAddress = InetAddress.getByName(serverHost);
        }
    }

    public void setServerPort(int port) {
        miServerPort = port;
    }

    /**
     * Set configuration - fifth step.
     */
    public void setConfiguration() throws UnknownHostException, UserManagerException, RemoteException {

        // get host addresses
        if (mSelfAddress == null) {
            mSelfAddress = InetAddress.getLocalHost();
        }
        if (mServerAddress == null) {
            mServerAddress = mSelfAddress;
        }

        //TODO - not Avalon monitors
        mStatistics = new FtpStatistics(this, new AvalonFileMonitor(null));
        mConService = new ConnectionService(this, new AvalonConnectionMonitor(null));
        if (mbRemoteAdminAllowed) {
            mRemoteHandler = mRemoteHandlerFactory.createRemoteHandler(this,
                    new AvalonRemoteHandlerMonitor(null));
        }
    }

    private void setDefaultDataPool(String dataPort) {
        // get data port number
        StringTokenizer st = new StringTokenizer(dataPort, ", \t\n\r\f");
        miDataPort = new int[st.countTokens()][2];
        for (int i = 0; i < miDataPort.length; i++) {
            miDataPort[i][0] = Integer.parseInt(st.nextToken());
            miDataPort[i][1] = 0;
        }
    }

    public void setDefaultRoot(String defaultRoot) {
        mDefaultRoot = new File(defaultRoot);
    }

    public void setDefaultIdle(int idleTime) {
        miDefaultIdle = idleTime;
    }

    public void setCreateUserHome(boolean setit) {
        mbCreateHome = setit;
    }

    public void setCreateUsers(boolean setit) {
        mbCreateUsers = setit;
    }

    public void setRemoteAdminAllowed(boolean allowed) {
        mbRemoteAdminAllowed = allowed;
    }

    public void setRmiPort(int port) {
        miRmiPort = port;
    }

    public void setPollInterval(int interval) {
        miPollInterval = interval;
    }

    public void setMaxAnonLogins(int max) {
        miAnonLogin = max;
    }

    public void setAnonLoginsAllowed(boolean allowed) {
        mbAnonAllowed = allowed;
    }

    public void setMaxLogin(int max) {
        miMaxLogin = max;
    }

    /**
     * Get data port. Data port number zero (0) means that
     * any available port will be used.
     */
    public int getDataPort() {
        synchronized (miDataPort) {
            int dataPort = -1;
            int loopTimes = 2;
            Thread currThread = Thread.currentThread();

            while ((dataPort == -1) && (--loopTimes >= 0) && (!currThread.isInterrupted())) {

                // search for a free port
                for (int i = 0; i < miDataPort.length; i++) {
                    if (miDataPort[i][1] == 0) {
                        if (miDataPort[i][0] != 0) {
                            miDataPort[i][1] = 1;
                        }
                        dataPort = miDataPort[i][0];
                        break;
                    }
                }

                // no available free port - wait for the release notification
                if (dataPort == -1) {
                    try {
                        miDataPort.wait();
                    } catch (InterruptedException ex) {
                    }
                }

            }
            return dataPort;
        }
    }

    /**
     * Release data port
     */
    public void releaseDataPort(int port) {
        synchronized (miDataPort) {
            for (int i = 0; i < miDataPort.length; i++) {
                if (miDataPort[i][0] == port) {
                    miDataPort[i][1] = 0;
                    break;
                }
            }
            miDataPort.notify();
        }
    }


    /**
     * Close this config and all the related resources. Ftp server
     * <code>FtpServer.stop()</code> method will call this method.
     */
    public void dispose() {

        // close remote handler
        if (mRemoteHandler != null) {
            mRemoteHandler.dispose();
            mRemoteHandler = null;
        }

        // close connection service
        if (mConService != null) {
            mConService.dispose();
            mConService = null;
        }

        // close message queue
        if (mQueue != null) {
            mQueue.stop();
            mQueue = null;
        }
    }

}