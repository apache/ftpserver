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
import java.net.UnknownHostException;

import org.apache.ftpserver.interfaces.DataConnectionConfig;
import org.apache.ftpserver.ssl.SslConfiguration;

/**
 * Data connection configuration.
 */
public 
class DefaultDataConnectionConfig implements DataConnectionConfig {

    public static class Active {
        private boolean enable = true;
        private InetAddress localAddress;
        private int localPort = 0;
        private boolean ipCheck = false;
        
        public Active() {
            try {
                localAddress = InetAddress.getLocalHost();
            } catch (UnknownHostException e) {
                throw new FtpServerConfigurationException("Failed to resolve localhost", e);
            }
        }
        
        public boolean isEnable() {
            return enable;
        }
        public void setEnable(boolean enable) {
            this.enable = enable;
        }
        public boolean isIpCheck() {
            return ipCheck;
        }
        public void setIpCheck(boolean ipCheck) {
            this.ipCheck = ipCheck;
        }
        public InetAddress getLocalAddress() {
            return localAddress;
        }
        public void setLocalAddress(InetAddress localAddress) {
            this.localAddress = localAddress;
        }
        public int getLocalPort() {
            return localPort;
        }
        public void setLocalPort(int localPort) {
            this.localPort = localPort;
        }
    }
    
    public static class Passive  {
        private InetAddress address;
        private InetAddress externalAddress;
        private PassivePorts passivePorts =PassivePorts.parse("0");
        
        public InetAddress getAddress() {
            return address;
        }
        public void setAddress(InetAddress address) {
            this.address = address;
        }
        public PassivePorts getPassivePorts() {
            return passivePorts;
        }
        public void setPorts(String ports) {
            this.passivePorts = PassivePorts.parse(ports);
        }
        public InetAddress getExternalAddress() {
            return externalAddress;
        }
        public void setExternalAddress(InetAddress externalAddress) {
            this.externalAddress = externalAddress;
        }
    }
    
    private int maxIdleTimeMillis = 10000;
    
    private SslConfiguration ssl;
    
    public void setIdleTime(int idleTime) {
        // get the maximum idle time in millis
        maxIdleTimeMillis = idleTime * 1000;
    }
    
    private Active active = new Active();
    private Passive passive = new Passive();
    
    public void setActive(Active active) {
        this.active = active;
    }
    
    public void setPassive(Passive passive) {
        this.passive = passive;
    }
    
    public void setSsl(SslConfiguration ssl) {
        this.ssl = ssl;
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
        return active.isEnable();
    }
    
    /**
     * Check the PORT IP?
     */
    public boolean isActiveIpCheck() {
        return active.isIpCheck();
    }
    
    /**
     * Get the local address for active mode data transfer.
     */
    public InetAddress getActiveLocalAddress() {
        return active.getLocalAddress();
    }
    
    /**
     * Get the active local port number.
     */
    public int getActiveLocalPort() {
        return active.getLocalPort();
    }
    
    /**
     * Get passive host.
     */
    public InetAddress getPassiveAddress() {
        return passive.getAddress();
    }

    /**
     * Set the passive host
     * @param address The passive host
     */
    public void setPassiveAddress(InetAddress address) {
    	passive.setAddress(address);
    }
    
    /**
     * Get external passive host.
     */
    public InetAddress getPassiveExernalAddress() {
        return passive.getExternalAddress();
    }

    /**
     * Set the passive external host
     * @param address The passive external  host
     */
    public void setPassiveExernalAddress(InetAddress address) {
    	passive.setExternalAddress(address);
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
            dataPort = passive.getPassivePorts().reserveNextPort();

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
        passive.getPassivePorts().releasePort(port);

        notify();
    }
    
    /**
     * Get SSL component.
     */
    public SslConfiguration getSSL() {
        return ssl;
    }
}
