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

import org.apache.ftpserver.DefaultDataConnectionConfig;
import org.apache.ftpserver.interfaces.DataConnectionConfig;
import org.apache.ftpserver.interfaces.Ssl;


/**
 * Common base class for listener implementations
 */
public abstract class AbstractListener implements Listener {
    
    private InetAddress serverAddress;
    private int port = 21;
    private Ssl ssl;
    private boolean implicitSsl = false;
    private DataConnectionConfig dataConnectionConfig = new DefaultDataConnectionConfig();
    
    public boolean isImplicitSsl() {
        return implicitSsl;
    }
    public void setImplicitSsl(boolean implicitSsl) {
        this.implicitSsl = implicitSsl;
    }
    public int getPort() {
        return port;
    }
    public void setPort(int port) {
        this.port = port;
    }
    public InetAddress getServerAddress() {
        return serverAddress;
    }
    public void setServerAddress(InetAddress serverAddress) {
        this.serverAddress = serverAddress;
    }
    public void setAddress(InetAddress serverAddress) {
        this.serverAddress = serverAddress;
    }
    public Ssl getSsl() {
        return ssl;
    }
    public void setSsl(Ssl ssl) {
        this.ssl = ssl;
    }
    public DataConnectionConfig getDataConnectionConfig() {
        return dataConnectionConfig;
    }
    public void setDataConnectionConfig(DataConnectionConfig dataConnectionConfig) {
        this.dataConnectionConfig = dataConnectionConfig;
    }
    public void setDataConnection(DataConnectionConfig dataConnectionConfig) {
        setDataConnectionConfig(dataConnectionConfig);
    }
}
