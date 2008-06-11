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

package org.apache.ftpserver.listener;

import java.net.InetAddress;

import org.apache.ftpserver.DefaultDataConnectionConfiguration;
import org.apache.ftpserver.interfaces.DataConnectionConfiguration;
import org.apache.ftpserver.ssl.SslConfiguration;


/**
 * Common base class for listener implementations
 */
public abstract class AbstractListener implements Listener {
    
    private InetAddress serverAddress;
    private int port = 21;
    private SslConfiguration ssl;
    private boolean implicitSsl = false;
    private DataConnectionConfiguration dataConnectionConfig = new DefaultDataConnectionConfiguration();
    
    /**
     * {@inheritDoc}
     */
    public boolean isImplicitSsl() {
        return implicitSsl;
    }

    /**
     * {@inheritDoc}
     */
    public void setImplicitSsl(boolean implicitSsl) {
        this.implicitSsl = implicitSsl;
    }

    /**
     * {@inheritDoc}
     */
    public int getPort() {
        return port;
    }

    /**
     * {@inheritDoc}
     */
    public void setPort(int port) {
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
    public void setServerAddress(InetAddress serverAddress) {
        this.serverAddress = serverAddress;
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
    public void setSslConfiguration(SslConfiguration ssl) {
        this.ssl = ssl;
    }

    /**
     * {@inheritDoc}
     */
    public DataConnectionConfiguration getDataConnectionConfiguration() {
        return dataConnectionConfig;
    }
    
    /**
     * {@inheritDoc}
     */
    public void setDataConnectionConfiguration(DataConnectionConfiguration dataConnectionConfig) {
        this.dataConnectionConfig = dataConnectionConfig;
    }
}
