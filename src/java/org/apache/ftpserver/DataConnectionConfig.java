// $Id$
/*
 * Copyright 2004 The Apache Software Foundation
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
 */
package org.apache.ftpserver;

import java.net.InetAddress;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ftpserver.ftplet.Configuration;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.interfaces.IDataConnectionConfig;
import org.apache.ftpserver.interfaces.ISsl;

/**
 * Data connection configuration.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class DataConnectionConfig implements IDataConnectionConfig {

    private Log log;
    private LogFactory logFactory;
    
    private boolean activeEnable;
    private boolean activeIpCheck;
    private InetAddress activeLocalAddress;
    private int activeLocalPort;
    
    private InetAddress passiveAddress;
    private int passivePorts[][];
    
    private ISsl ssl;

    
    /**
     * Set the log factory. 
     */
    public void setLogFactory(LogFactory factory) {
        logFactory = factory;
        log = logFactory.getInstance(getClass());
    }
    
    /**
     * Configure the data connection config object.
     */
    public void configure(Configuration conf) throws FtpException {
        
        try {
            
            // get the active data connection parameters
            Configuration activeConf = conf.subset("active");
            activeEnable = activeConf.getBoolean("enable", true);
            if(activeEnable) {
                String portAddress = activeConf.getString("local-address", null);
                if(portAddress == null) {
                    activeLocalAddress = InetAddress.getLocalHost();
                }
                else {
                    activeLocalAddress = InetAddress.getByName(portAddress);
                }
                
                activeLocalPort = activeConf.getInt("local-port", 0);
                activeIpCheck = activeConf.getBoolean("ip-check", false);
            }
            
            // get the passive data connection parameters
            Configuration passiveConf = conf.subset("passive");
            
            String pasvAddress = passiveConf.getString("address", null);
            if(pasvAddress == null) {
                passiveAddress = InetAddress.getLocalHost();
            }
            else {
                passiveAddress = InetAddress.getByName(pasvAddress);
            }
            
            String pasvPorts = passiveConf.getString("ports", "0");
            StringTokenizer st = new StringTokenizer(pasvPorts, " ,;\t\n\r\f");
            passivePorts = new int[st.countTokens()][2];
            for(int i=0; i<passivePorts.length; i++) {
                passivePorts[i][0] = Integer.parseInt(st.nextToken());
                passivePorts[i][1] = 0;
            }
            
            // get SSL parameters if available 
            Configuration sslConf = conf.subset("ssl");
            if(!sslConf.isEmpty()) {
                ssl = (ISsl)Class.forName("org.apache.ftpserver.ssl.Ssl").newInstance();
                ssl.setLogFactory(logFactory);
                ssl.configure(sslConf);
            }
        }
        catch(FtpException ex) {
            throw ex;
        }
        catch(Exception ex) {
            log.error("DataConnectionConfig.configure()", ex);
            throw new FtpException("DataConnectionConfig.configure()", ex);
        }
    }

    /**
     * Is PORT enabled?
     */
    public boolean isActiveEnabled() {
        return activeEnable;
    }
    
    /**
     * Check the PORT IP?
     */
    public boolean isActiveIpCheck() {
        return activeIpCheck;
    }
    
    /**
     * Get the local address for active mode data transfer.
     */
    public InetAddress getActiveLocalAddress() {
        return activeLocalAddress;
    }
    
    /**
     * Get the active local port number.
     */
    public int getActiveLocalPort() {
        return activeLocalPort;
    }
    
    /**
     * Get passive host.
     */
    public InetAddress getPassiveAddress() {
        return passiveAddress;
    }
    
    /**
     * Get passive data port. Data port number zero (0) means that 
     * any available port will be used.
     */
    public synchronized int getPassivePort() {        
        int dataPort = -1;
        int loopTimes = 2;
        Thread currThread = Thread.currentThread();
        
        while( (dataPort==-1) && (--loopTimes >= 0)  && (!currThread.isInterrupted()) ) {

            // search for a free port            
            for(int i=0; i<passivePorts.length; i++) {
                if(passivePorts[i][1] == 0) {
                    if(passivePorts[i][0] != 0) {
                        passivePorts[i][1] = 1;
                    }
                    dataPort = passivePorts[i][0];
                    break;
                }
            }

            // no available free port - wait for the release notification
            if(dataPort == -1) {
                try {
                    wait();
                }
                catch(InterruptedException ex) {
                }
            }

        }
        return dataPort;
    }

    /**
     * Release data port
     */
    public synchronized void releasePassivePort(int port) {
        for(int i=0; i<passivePorts.length; i++) {
            if(passivePorts[i][0] == port) {
                passivePorts[i][1] = 0;
                break;
            }
        }
        notify();
    }
    
    /**
     * Get SSL component.
     */
    public ISsl getSSL() {
        return ssl;
    }
    
    /**
     * Dispose it.
     */
    public void dispose() {
    }
}
