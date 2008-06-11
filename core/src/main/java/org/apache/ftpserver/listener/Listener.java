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
import java.util.Set;

import org.apache.ftpserver.interfaces.DataConnectionConfiguration;
import org.apache.ftpserver.interfaces.FtpIoSession;
import org.apache.ftpserver.interfaces.FtpServerContext;
import org.apache.ftpserver.ssl.SslConfiguration;


/**
 * Interface for the component responsible for waiting for incoming
 * socket requests and kicking off {@link FtpIoSession}s 
 *
 */
public interface Listener {
    
    /**
     * Start the listener, will initiate the listener waiting
     * on the socket.
     * The method should not return until the listener has
     * started accepting socket requests.
     * 
     * @throws Exception On error during start up
     */
    void start(FtpServerContext serverContext) throws Exception;

    /**
     * Stop the listener, it should no longer except socket requests.
     * The method should not return until the listener has stopped
     * accepting socket requests.
     */
    void stop();

    /**
     * Checks if the listener is currently started.
     * 
     * @return True if the listener is started
     */
    boolean isStopped();

    /**
     * Temporarily stops the listener from accepting socket requests.
     * Resume the listener by using the {@link #resume()} method.
     * The method should not return until the listener has stopped 
     * accepting socket requests.
     */
    void suspend();

    /**
     * Resumes a suspended listener. 
     * The method should not return until the listener has
     * started accepting socket requests.
     */
    void resume();

    /**
     * Checks if the listener is currently suspended
     * @return True if the listener is suspended
     */
    boolean isSuspended();
    
    /**
     * Returns the currently active sessions for this listener.
     * If no sessions are active, an empty {@link Set} would be returned.
     * @return The currently active sessions
     */
    Set<FtpIoSession> getActiveSessions();

    /**
     * Is this listener in SSL mode automatically or must
     * the client explicitly request to use SSL
     * @return true is the listener is automatically in SSL mode, false otherwise
     */
    boolean isImplicitSsl();

    /**
     * Should this listener be in SSL mode automatically or must
     * the client explicitly request to use SSL
     * @param implicitSsl true is the listener should automatically be in SSL mode, false otherwise
     */
    void setImplicitSsl(boolean implicitSsl);

    /**
     * Get the {@link SslConfiguration} used for this listener
     * @return The current {@link SslConfiguration}
     */
    SslConfiguration getSslConfiguration();
    
    /**
     * Set the {@link SslConfiguration} used for this listener
     * @param sslConfiguration The {@link SslConfiguration}
     */
    void setSslConfiguration(SslConfiguration sslConfiguration);
    
    /**
     * Get the port on which this listener is waiting for requests.
     * For listeners where the port is automatically assigned, this 
     * will return the bound port.
     * @return The port
     */
    int getPort();

    /**
     * Set the port on which this listener will accept requests. Or set to 
     * 0 (zero) is the port should be automatically assigned
     * @param port The port to use.
     */
    void setPort(int port);

    /**
     * Get the {@link InetAddress} used for binding the local socket. Defaults
     * to null, that is, the server binds to all available network interfaces
     * @return The local socket {@link InetAddress}, if set
     */
    InetAddress getServerAddress();

    /**
     * Set the {@link InetAddress} used for binding the local socket. Defaults
     * to null, that is, the server binds to all available network interfaces
     * @param serverAddress The local socket {@link InetAddress}
     */
    void setServerAddress(InetAddress serverAddress);

    /**
     * Get configuration for data connections made within this listener
     * @return The data connection configuration
     */
    DataConnectionConfiguration getDataConnectionConfiguration();

    /**
     * Set configuration for data connections made within this listener
     * @param dataConnectionConfig The data connection configuration 
     */
    void setDataConnectionConfiguration(DataConnectionConfiguration dataConnectionConfig);
}