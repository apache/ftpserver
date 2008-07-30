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

import org.apache.ftpserver.interfaces.DataConnectionConfiguration;
import org.apache.ftpserver.ssl.SslConfiguration;

/**
 * Data connection configuration.
 */
public 
class DefaultDataConnectionConfiguration implements DataConnectionConfiguration {

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
        private PassivePorts passivePorts = new PassivePorts(new int[]{0});
        
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
            this.passivePorts = new PassivePorts(ports);
        }
        public InetAddress getExternalAddress() {
            return externalAddress;
        }
        public void setExternalAddress(InetAddress externalAddress) {
            this.externalAddress = externalAddress;
        }
    }
    
    // maximum idle time in seconds
    private int idleTime = 300;
    
    private SslConfiguration ssl;
    
    /**
     * Get the maximum idle time in seconds.
     */
    public int getIdleTime() {
        return idleTime;
    }
    
    public void setIdleTime(int idleTime) {
        this.idleTime = idleTime;
    }
    
    private Active active = new Active();
    private Passive passive = new Passive();
    
    public void setActive(Active active) {
        this.active = active;
    }
    
    public void setPassive(Passive passive) {
        this.passive = passive;
    }
    
    public void setSslConfiguration(SslConfiguration ssl) {
        this.ssl = ssl;
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
    public synchronized int requestPassivePort() {        
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
     * Retrive the passive ports configured for this data connection
     * @return The String of passive ports
     */
    public String getPassivePorts() {
        return passive.passivePorts.toString();
    }

    /**
     * Set the passive ports allowed for this data connection. 
     * @param passivePorts A string consisting of port numbers 
     *  separated by commas. It can also include ranged. For example:
     *  <p>22,23,24</p>
     *  <p>22-24,28</p>
     */
    public void setPassivePorts(String passivePorts) {
        passive.passivePorts = new PassivePorts(passivePorts);
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
    public SslConfiguration getSslConfiguration() {
        return ssl;
    }
}
