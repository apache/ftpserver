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

package org.apache.ftpserver;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.ftpserver.ip.IpRestrictorInterface;
import org.apache.ftpserver.remote.RemoteHandlerFactory;
import org.apache.ftpserver.usermanager.UserManagerInterface;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.StringTokenizer;

/**
 * Ftp configuration class. It has all ftp server configuration
 * parameters. This is not hot-editable. parameters will be loaded
 * once during server startup. We can add our own config parameters.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public
class AvalonFtpConfig extends AbstractFtpConfig {

    protected Configuration mConf                 = null;
    protected Context mContext                    = null;
    protected RemoteHandlerFactory mRemoteHandlerFactory = null;
    protected Logger mLogger                      = null;

    public AvalonFtpConfig() throws IOException {
    }

    /**
     * Set logger - second step.
     */
    public void setLogger(Logger logger) {
        mLogger = logger;
    }

    /**
     * Set context - third step.
     */
    public void setContext(Context ctx) {
        mContext = ctx;
    }

    /**
     * Set component manager - fourth step.
     * TODO - case for more blocks here? - PJH
     */
    public void setServiceManager(ServiceManager serviceManager) throws ServiceException {
        mIpRestrictor = (IpRestrictorInterface)serviceManager.lookup(IpRestrictorInterface.ROLE);
        mUserManager  = (UserManagerInterface)serviceManager.lookup(UserManagerInterface.ROLE);
        mRemoteHandlerFactory = (RemoteHandlerFactory) serviceManager.lookup(RemoteHandlerFactory.ROLE);
    }


    /**
     * Set configuration - fifth step.
     */
    public void setConfiguration(Configuration conf) throws Exception {
        mConf = conf;
        Configuration tmpConf = null;

        // get server address
        tmpConf = conf.getChild("server-host", false);
        if(tmpConf != null) {
            mServerAddress = InetAddress.getByName(tmpConf.getValue());
        }

        // get self address
        tmpConf = conf.getChild("self-host", false);
        if(tmpConf != null) {
            mSelfAddress = InetAddress.getByName(tmpConf.getValue());
        }

        // get server port
        miServerPort = 21;
        tmpConf = conf.getChild("ftp-port", false);
        if(tmpConf != null) {
            miServerPort = tmpConf.getValueAsInteger(miServerPort);
        }

        // get maximum number of connections
        miMaxLogin = 20;
        tmpConf = conf.getChild("max-connection", false);
        if(tmpConf != null) {
            miMaxLogin = tmpConf.getValueAsInteger(miMaxLogin);
        }

        // get anonymous login allow flag
        mbAnonAllowed = true;
        tmpConf = conf.getChild("anonymous-login-allowed", false);
        if(tmpConf != null) {
            mbAnonAllowed = tmpConf.getValueAsBoolean(mbAnonAllowed);
        }

        // get maximum number of anonymous connections
        miAnonLogin = 10;
        tmpConf = conf.getChild("anonymous-max-connection", false);
        if(tmpConf != null) {
            miAnonLogin = tmpConf.getValueAsInteger(miAnonLogin);
        }

        // get scheduler interval
        miPollInterval = 120;
        tmpConf = conf.getChild("poll-interval", false);
        if(tmpConf != null) {
            miPollInterval = tmpConf.getValueAsInteger(miPollInterval);
        }

        // get rmi port
        miRmiPort = java.rmi.registry.Registry.REGISTRY_PORT;
        tmpConf = conf.getChild("remote-admin-port", false);
        if(tmpConf != null) {
            miRmiPort = tmpConf.getValueAsInteger(miRmiPort);
        }

        // get remote admin allow flag
        mbRemoteAdminAllowed = true;
        tmpConf = conf.getChild("remote-admin-allowed", false);
        if(tmpConf != null) {
            mbRemoteAdminAllowed = tmpConf.getValueAsBoolean(mbRemoteAdminAllowed);
        }

        // get automatic user home creation flag
        mbCreateHome = false;
        tmpConf = conf.getChild("create-user-home", false);
        if(tmpConf != null) {
            mbCreateHome = tmpConf.getValueAsBoolean(mbCreateHome);
        }

        // get automatic users creation flag
        mbCreateUsers = true;
        tmpConf = conf.getChild("create-users", false);
        if(tmpConf != null) {
            mbCreateUsers = tmpConf.getValueAsBoolean(mbCreateUsers);
        }

        // get default idle time
        miDefaultIdle = 300;
        tmpConf = conf.getChild("default-idle-time", false);
        if(tmpConf != null) {
            miDefaultIdle = tmpConf.getValueAsInteger(miDefaultIdle);
        }

        // get default root
        String defaultRoot = "/";
        tmpConf = conf.getChild("default-user-root", false);
        if(tmpConf != null) {
            defaultRoot = tmpConf.getValue(defaultRoot);
        }
        mDefaultRoot = new File(defaultRoot);

        // get data port number
        String dataPort = "0";
        tmpConf = conf.getChild("data-port-pool", false);
        if(tmpConf != null) {
            dataPort = tmpConf.getValue(dataPort);
        }
        StringTokenizer st = new StringTokenizer(dataPort, ", \t\n\r\f");
        miDataPort = new int[st.countTokens()][2];
        for(int i=0; i<miDataPort.length; i++) {
            miDataPort[i][0] = Integer.parseInt(st.nextToken());
            miDataPort[i][1] = 0;
        }

        // get host addresses
        if (mSelfAddress == null) {
            mSelfAddress = InetAddress.getLocalHost();
        }
        if(mServerAddress == null) {
            mServerAddress = mSelfAddress;
        }

        mStatistics = new FtpStatistics(this, new AvalonFileMonitor(getLogger()));
        mConService = new ConnectionService(this, new AvalonConnectionMonitor(getLogger()));
        if (mbRemoteAdminAllowed) {
            mRemoteHandler = mRemoteHandlerFactory.createRemoteHandler(this,
                    new AvalonRemoteHandlerMonitor(getLogger()));
        }
    }

    /**
     * Get data port. Data port number zero (0) means that
     * any available port will be used.
     */
    public int getDataPort() {
        synchronized(miDataPort) {
            int dataPort = -1;
            int loopTimes = 2;
            Thread currThread = Thread.currentThread();

            while( (dataPort==-1) && (--loopTimes >= 0)  && (!currThread.isInterrupted()) ) {

                // search for a free port
                for(int i=0; i<miDataPort.length; i++) {
                    if(miDataPort[i][1] == 0) {
                        if(miDataPort[i][0] != 0) {
                            miDataPort[i][1] = 1;
                        }
                        dataPort = miDataPort[i][0];
                        break;
                    }
                }

                // no available free port - wait for the release notification
                if(dataPort == -1) {
                    try {
                        miDataPort.wait();
                    }
                    catch(InterruptedException ex) {
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
        synchronized(miDataPort) {
            for(int i=0; i<miDataPort.length; i++) {
                if(miDataPort[i][0] == port) {
                    miDataPort[i][1] = 0;
                    break;
                }
            }
            miDataPort.notify();
        }
    }

    /**
     * Get logger
     */
    public Logger getLogger() {
        return mLogger;
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
         if(mConService != null) {
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













