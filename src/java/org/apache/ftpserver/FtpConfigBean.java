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

import org.apache.ftpserver.ip.IpRestrictorInterface;
import org.apache.ftpserver.remote.RemoteHandler;
import org.apache.ftpserver.usermanager.UserManagerInterface;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.StringTokenizer;
import java.rmi.RemoteException;

/**
 * Ftp configuration class. It has all ftp server configuration
 * parameters. This is not hot-editable. parameters will be loaded
 * once during server startup. We can add our own config parameters.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public
class FtpConfigBean extends AbstractFtpConfig {

    public FtpConfigBean(IpRestrictorInterface ipRestrictorInterface, UserManagerInterface userManagerInterface) throws IOException, UserManagerException {
        mUserManager = userManagerInterface;
        mIpRestrictor = ipRestrictorInterface;
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
        if(mServerAddress == null) {
            mServerAddress = mSelfAddress;
        }

        //TODO - not Avalon monitors
        mStatistics = new FtpStatistics(this, new AvalonFileMonitor(null));
        mConService = new ConnectionService(this, new AvalonConnectionMonitor(null));
        if (mbRemoteAdminAllowed) {
            mRemoteHandler = new RemoteHandler(this, new AvalonFtpRemoteHandlerMonitor(null));
        }
    }

    private void setDefaultDataPool(String dataPort) {
        // get data port number
        StringTokenizer st = new StringTokenizer(dataPort, ", \t\n\r\f");
        miDataPort = new int[st.countTokens()][2];
        for(int i=0; i<miDataPort.length; i++) {
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