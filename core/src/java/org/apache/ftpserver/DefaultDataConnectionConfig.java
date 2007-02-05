/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */  

package org.apache.ftpserver;

import java.net.InetAddress;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ftpserver.ftplet.Component;
import org.apache.ftpserver.ftplet.Configuration;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.interfaces.DataConnectionConfig;
import org.apache.ftpserver.interfaces.Ssl;

/**
 * Data connection configuration.
 */
public 
class DefaultDataConnectionConfig implements DataConnectionConfig, Component {

    private Log log;
    private LogFactory logFactory;
    
    private int maxIdleTimeMillis;
    
    private boolean activeEnable;
    private boolean activeIpCheck;
    private InetAddress activeLocalAddress;
    private int activeLocalPort;
    
    private InetAddress passiveAddress;
    
    private Ssl ssl;
    
    private PassivePorts passivePorts;

    
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
            
            // get the maximum idle time in millis
            maxIdleTimeMillis = conf.getInt("idle-time", 10) * 1000;
            
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
                passiveAddress = null;
            }
            else {
                passiveAddress = InetAddress.getByName(pasvAddress);
            }
            
            String pasvPorts = passiveConf.getString("ports", "0");
            
            passivePorts = PassivePorts.parse(pasvPorts);
            
            // get SSL parameters if available 
            Configuration sslConf = conf.subset("ssl");
            if(!sslConf.isEmpty()) {
                ssl = (Ssl)Class.forName("org.apache.ftpserver.ssl.DefaultSsl").newInstance();
                ssl.setLogFactory(logFactory);
                ssl.configure(sslConf);
            }
        }
        catch(FtpException ex) {
            throw ex;
        }
        catch(Exception ex) {
            log.error("DefaultDataConnectionConfig.configure()", ex);
            throw new FtpException("DefaultDataConnectionConfig.configure()", ex);
        }
    }

    /**
     * Get the maximum idle time in millis.
     */
    public int getMaxIdleTimeMillis() {
        return maxIdleTimeMillis;
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
            dataPort = passivePorts.reserveNextPort();

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
        passivePorts.releasePort(port);

        notify();
    }
    
    /**
     * Get SSL component.
     */
    public Ssl getSSL() {
        return ssl;
    }
    
    /**
     * Dispose it.
     */
    public void dispose() {
    }
}
