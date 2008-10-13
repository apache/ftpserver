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

package org.apache.ftpserver.listener.nio;

import java.net.InetAddress;
import java.util.Collections;
import java.util.List;

import org.apache.ftpserver.DataConnectionConfiguration;
import org.apache.ftpserver.impl.DefaultDataConnectionConfiguration;
import org.apache.ftpserver.listener.Listener;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.ssl.SslConfiguration;
import org.apache.mina.filter.firewall.Subnet;

/**
 * Common base class for listener implementations
 *
 * @author The Apache MINA Project (dev@mina.apache.org)
 * @version $Rev$, $Date$
 */
public abstract class AbstractListener implements Listener {

    private InetAddress serverAddress;

    private int port = 21;

    private SslConfiguration ssl;

    private boolean implicitSsl = false;
    
    private int idleTimeout;
    
    private List<InetAddress> blockedAddresses;

    private List<Subnet> blockedSubnets;

    private DataConnectionConfiguration dataConnectionConfig;

    /**
     * Constructor for internal use, do not use directly. Instead use {@link ListenerFactory}
     */
    public AbstractListener(InetAddress serverAddress, int port, boolean implicitSsl, 
            SslConfiguration sslConfiguration, DataConnectionConfiguration dataConnectionConfig,
            int idleTimeout, List<InetAddress> blockedAddresses, List<Subnet> blockedSubnets) {
        this.serverAddress = serverAddress;
        this.port = port;
        this.implicitSsl = implicitSsl;
        this.dataConnectionConfig = dataConnectionConfig;
        this.ssl = sslConfiguration;
        this.idleTimeout = idleTimeout;
        
        if(blockedAddresses != null) {
            this.blockedAddresses = Collections.unmodifiableList(blockedAddresses);
        }
        if(blockedSubnets != null) {
            this.blockedSubnets = Collections.unmodifiableList(blockedSubnets);
        }
        
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isImplicitSsl() {
        return implicitSsl;
    }

    /**
     * {@inheritDoc}
     */
    public int getPort() {
        return port;
    }

    /**
     * Used internally to update the port after binding
     * @param port
     */
    protected void setPort(int port) {
        this.port = port;
    }
    
    /**
     * {@inheritDoc}
     */
    public InetAddress getServerAddress() {
        return serverAddress;
    }

    /**
     * {@inheritDoc}
     */
    public SslConfiguration getSslConfiguration() {
        return ssl;
    }

    /**
     * {@inheritDoc}
     */
    public DataConnectionConfiguration getDataConnectionConfiguration() {
        return dataConnectionConfig;
    }

    /**
     * Get the number of seconds during which no network activity 
     * is allowed before a session is closed due to inactivity.  
     * @return The idle time out
     */
    public int getIdleTimeout() {
        return idleTimeout;
    }

    /**
     * Retrives the {@link InetAddress} for which this listener blocks
     * connections
     * 
     * @return The list of {@link InetAddress}es
     */
    public List<InetAddress> getBlockedAddresses() {
        return blockedAddresses;
    }

    /**
     * Retrieves the {@link Subnet}s for this listener blocks connections
     * 
     * @return The list of {@link Subnet}s
     */
    public List<Subnet> getBlockedSubnets() {
        return blockedSubnets;
    }
}
