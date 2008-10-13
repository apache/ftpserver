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

import org.apache.ftpserver.impl.DefaultDataConnectionConfiguration;
import org.apache.ftpserver.impl.PassivePorts;
import org.apache.ftpserver.ssl.SslConfiguration;

/**
 * Data connection factory
 *
 * @author The Apache MINA Project (dev@mina.apache.org)
 * @version $Rev: 701863 $, $Date: 2008-10-05 21:25:50 +0200 (Sun, 05 Oct 2008) $
 */
public class DataConnectionConfigurationFactory {

    public void setIdleTime(int idleTime) {
        this.idleTime = idleTime;
    }

    public void setActiveEnabled(boolean activeEnabled) {
        this.activeEnabled = activeEnabled;
    }

    public void setActiveLocalAddress(InetAddress activeLocalAddress) {
        this.activeLocalAddress = activeLocalAddress;
    }

    public void setActiveLocalPort(int activeLocalPort) {
        this.activeLocalPort = activeLocalPort;
    }

    public void setActiveIpCheck(boolean activeIpCheck) {
        this.activeIpCheck = activeIpCheck;
    }

    public void setPassiveAddress(InetAddress passiveAddress) {
        this.passiveAddress = passiveAddress;
    }

    // maximum idle time in seconds
    private int idleTime = 300;
    private SslConfiguration ssl;

    private boolean activeEnabled = true;
    private InetAddress activeLocalAddress;
    private int activeLocalPort = 0;
    private boolean activeIpCheck = false;
    
    private InetAddress passiveAddress;
    private InetAddress passiveExternalAddress;
    private PassivePorts passivePorts = new PassivePorts(new int[] { 0 });

    public DataConnectionConfigurationFactory() {
        try {
            activeLocalAddress = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            throw new FtpServerConfigurationException(
                    "Failed to resolve localhost", e);
        }
    }
    
    public DataConnectionConfiguration createDataConnectionConfiguration() {
        return new DefaultDataConnectionConfiguration(idleTime,
                ssl, activeEnabled, activeIpCheck,
                activeLocalAddress, activeLocalPort,
                passiveAddress, passivePorts,
                passiveExternalAddress);
    }
    
    /**
     * Get the maximum idle time in seconds.
     */
    public int getIdleTime() {
        return idleTime;
    }

    /**
     * Is PORT enabled?
     */
    public boolean isActiveEnabled() {
        return activeEnabled;
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
     * Get external passive host.
     */
    public InetAddress getPassiveExernalAddress() {
        return passiveExternalAddress;
    }

    public void setPassiveExernalAddress(InetAddress passiveExternalAddress) {
        this.passiveExternalAddress = passiveExternalAddress;
    }
    
    /**
     * Get passive data port. Data port number zero (0) means that any available
     * port will be used.
     */
    public synchronized int requestPassivePort() {
        int dataPort = -1;
        int loopTimes = 2;
        Thread currThread = Thread.currentThread();

        while ((dataPort == -1) && (--loopTimes >= 0)
                && (!currThread.isInterrupted())) {

            // search for a free port
            dataPort = passivePorts.reserveNextPort();

            // no available free port - wait for the release notification
            if (dataPort == -1) {
                try {
                    wait();
                } catch (InterruptedException ex) {
                }
            }
        }
        return dataPort;
    }

    /**
     * Retrive the passive ports configured for this data connection
     * 
     * @return The String of passive ports
     */
    public String getPassivePorts() {
        return passivePorts.toString();
    }

    public void setPassivePorts(String passivePorts) {
        this.passivePorts = new PassivePorts(passivePorts);
    }

    
    /**
     * Release data port
     */
    public synchronized void releasePassivePort(final int port) {
        passivePorts.releasePort(port);

        notify();
    }

    /**
     * Get SSL component.
     */
    public SslConfiguration getSslConfiguration() {
        return ssl;
    }
    
    public void setSslConfiguration(SslConfiguration ssl) {
        this.ssl = ssl;
    }
}
