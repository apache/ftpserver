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

package org.apache.ftpserver.impl;

import org.apache.ftpserver.DataConnectionConfiguration;
import org.apache.ftpserver.DataConnectionConfigurationFactory;
import org.apache.ftpserver.ssl.SslConfiguration;

/**
 * <strong>Internal class, do not use directly.</strong>
 * 
 * Data connection configuration.
 *
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 */
public class DefaultDataConnectionConfiguration implements
        DataConnectionConfiguration {

    // maximum idle time in seconds
    private int idleTime;
    private SslConfiguration ssl;

    private boolean activeEnabled;
    private String activeLocalAddress;
    private int activeLocalPort;
    private boolean activeIpCheck;
    
    private String passiveAddress;
    private String passiveExternalAddress;
    //private PassivePorts passivePorts;
    private PassivePortResolver passivePortResolver;

    private final boolean implicitSsl;

    /**
     * Internal constructor, do not use directly. Use {@link DataConnectionConfigurationFactory} instead.
     */
    public DefaultDataConnectionConfiguration(int idleTime,
            SslConfiguration ssl, boolean activeEnabled, boolean activeIpCheck,
            String activeLocalAddress, int activeLocalPort,
            String passiveAddress, PassivePortResolver passivePortsResolver,
            String passiveExternalAddress, boolean implicitSsl) {
        this.idleTime = idleTime;
        this.ssl = ssl;
        this.activeEnabled = activeEnabled;
        this.activeIpCheck = activeIpCheck;
        this.activeLocalAddress = activeLocalAddress;
        this.activeLocalPort = activeLocalPort;
        this.passiveAddress = passiveAddress;
        this.passivePortResolver = passivePortsResolver;
        this.passiveExternalAddress = passiveExternalAddress;
        this.implicitSsl = implicitSsl;
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
    public String getActiveLocalAddress() {
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
    public String getPassiveAddress() {
        return passiveAddress;
    }

    /**
     * Get external passive host.
     */
    public String getPassiveExernalAddress()  {
    	return passiveExternalAddress;
    	}

    /**
     * Get passive data port. Data port number zero (0) means that any available
     * port will be used.
     */
    public synchronized int requestPassivePort(FtpIoSession session) {
    	return passivePortResolver.requestPassivePort(session);
    }

    /**
     * Retrive the passive ports configured for this data connection
     * 
     * @return The String of passive ports
     */
    public String getPassivePorts() {
        return passivePortResolver.toString();
    }

    /**
     * Release data port
     */
    public synchronized void releasePassivePort(final FtpIoSession session, final int port) {
        passivePortResolver.releasePassivePort(session,port);
    }

    /**
     * Get SSL component.
     */
    public SslConfiguration getSslConfiguration() {
        return ssl;
    }

    /**
     * @see org.apache.ftpserver.DataConnectionConfiguration#isImplicitSsl()
     */
    public boolean isImplicitSsl() {
        return implicitSsl;
    }
}
