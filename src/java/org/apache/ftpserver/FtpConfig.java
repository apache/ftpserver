/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1997-2003 The Apache Software Foundation. All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *    "This product includes software developed by the
 *    Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software
 *    itself, if and wherever such third-party acknowledgments
 *    normally appear.
 *
 * 4. The names "Incubator", "FtpServer", and "Apache Software Foundation"
 *    must not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation. For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * $Id$
 */
package org.apache.ftpserver;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.StringTokenizer;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.ftpserver.ip.IpRestrictorInterface;
import org.apache.ftpserver.remote.RemoteHandler;
import org.apache.ftpserver.usermanager.UserManagerInterface;
import org.apache.ftpserver.util.AsyncMessageQueue;
import org.apache.avalon.phoenix.BlockContext;

/**
 * Ftp configuration class. It has all ftp server configuration
 * parameters. This is not hot-editable. parameters will be loaded
 * once during server startup. We can add our own config parameters.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public
class FtpConfig {

    private FtpStatus  mStatus                  = null;
    private ConnectionService mConService       = null;
    private IpRestrictorInterface mIpRestrictor = null;
    private UserManagerInterface mUserManager   = null;

    private InetAddress mServerAddress          = null;
    private InetAddress mSelfAddress            = null;

    private Configuration mConf                 = null;
    private BlockContext mContext               = null;
    private Logger mLogger                      = null;

    private FtpStatistics mStatistics           = null;

    private RemoteHandler mRemoteHandler        = null;

    private int miServerPort;
    private int miDataPort[][];
    private int miRmiPort;
    private int miMaxLogin;
    private int miAnonLogin;
    private int miPollInterval;
    private int miDefaultIdle;
    private boolean mbAnonAllowed;
    private boolean mbCreateHome;
    private boolean mbRemoteAdminAllowed;
    private File mDefaultRoot;
    private AsyncMessageQueue mQueue;

    /**
     * Default constructor - first step.
     */
    public FtpConfig() throws IOException {
        mStatus = new FtpStatus();
        mQueue  = new AsyncMessageQueue();
        mQueue.setMaxSize(4096);
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
    public void setContext(BlockContext ctx) {
        mContext = ctx;
    }

    /**
     * Set component manager - fourth step.
     * TODO - case for more blocks here? - PJH
     */
    public void setServiceManager(ServiceManager serviceManager) throws ServiceException {
        mIpRestrictor = (IpRestrictorInterface)serviceManager.lookup(IpRestrictorInterface.ROLE);
        mUserManager  = (UserManagerInterface)serviceManager.lookup(UserManagerInterface.ROLE);
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

        // get autometic user home creation flag
        mbCreateHome = false;
        tmpConf = conf.getChild("create-user-home", false);
        if(tmpConf != null) {
            mbCreateHome = tmpConf.getValueAsBoolean(mbCreateHome);
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

        mStatistics = new FtpStatistics(this);
        mConService = new ConnectionService(this);
        if (mbRemoteAdminAllowed) {
            mRemoteHandler = new RemoteHandler(this);
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
     * Get server port.
     */
    public int getServerPort()  {
        return miServerPort;
    }

    /**
     * Get context
     */
    public BlockContext getContext() {
        return mContext;
    }

    /**
     * Get configuration
     */
    public Configuration getConfiguration() {
        return mConf;
    }

    /**
     * Get server bind address.
     */
    public InetAddress getServerAddress() {
        return mServerAddress;
    }

    /**
     * Get self address
     */
    public InetAddress getSelfAddress() {
        return mSelfAddress;
    }

    /**
     * Check annonymous login support.
     */
    public boolean isAnonymousLoginAllowed() {
        return mbAnonAllowed;
    }

    /**
     * Get ftp status resource.
     */
    public FtpStatus getStatus() {
        return mStatus;
    }

    /**
     * Get connection service.
     */
    public ConnectionService getConnectionService() {
        return mConService;
    }

    /**
     * Get user manager.
     */
    public UserManagerInterface getUserManager() {
        return mUserManager;
    }

    /**
     * Get maximum number of connections.
     */
    public int getMaxConnections() {
        return miMaxLogin;
    }

    /**
     * Get maximum number of anonymous connections.
     */
    public int getMaxAnonymousLogins() {
        if(!isAnonymousLoginAllowed()) {
            return 0;
        }
        return miAnonLogin;
    }

    /**
     * Get poll interval in seconds.
     */
    public int getSchedulerInterval() {
        return miPollInterval;
    }

    /**
     * Get default idle time in seconds.
     */
    public int getDefaultIdleTime() {
        return miDefaultIdle;
    }

    /**
     * Get default root directory
     */
    public File getDefaultRoot() {
        return mDefaultRoot;
    }

    /**
     * Create user home directory if not exist during login
     */
    public boolean isCreateHome() {
        return mbCreateHome;
    }

    /**
     * Get rmi port
     */
    public int getRemoteAdminPort() {
        return miRmiPort;
    }

    /**
     * Is remote admin allowed
     */
    public boolean isRemoteAdminAllowed() {
        return mbRemoteAdminAllowed;
    }

    /**
     * Get base directory
     */
    public File getBaseDirectory() {
        return mContext.getBaseDirectory();
    }

    /**
     * Get IP restrictor object.
     */
    public IpRestrictorInterface getIpRestrictor() {
        return mIpRestrictor;
    }

    /**
     * Get global statistics object.
     */
    public FtpStatistics getStatistics() {
        return mStatistics;
    }

    /**
     * Get message queue
     */
    public AsyncMessageQueue getMessageQueue() {
        return mQueue;
    }


    /**
     * Get the system name.
     */
    public String getSystemName() {
        String systemName = System.getProperty("os.name");
        if(systemName == null) {
            systemName = "UNKNOWN";
        }
        else {
            systemName = systemName.toUpperCase();
            systemName = systemName.replace(' ', '-');
        }
        return systemName;
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













