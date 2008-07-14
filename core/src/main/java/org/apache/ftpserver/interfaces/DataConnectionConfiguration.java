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

package org.apache.ftpserver.interfaces;

import java.net.InetAddress;

import org.apache.ftpserver.ssl.SslConfiguration;

/**
 * Data connection configuration interface.
 */
public 
interface DataConnectionConfiguration {

    /**
     * Get the maximum idle time in seconds.
     */
    int getIdleTime();
    
    /**
     * Is active data connection enabled?
     */
    boolean isActiveEnabled();
    
    /**
     * Check the PORT IP with the client IP?
     */
    boolean isActiveIpCheck();
    
    /**
     * Get the active data connection local host.
     */
    InetAddress getActiveLocalAddress();
    
    /**
     * Get the active data connection local port.
     */
    int getActiveLocalPort(); 
    
    /**
     * Get passive server address. null, if not set in the configuration.
     */
    InetAddress getPassiveAddress();
    
    /**
     * Get the passive address that will be returned to clients on the
     * PASV command.
     * @return The passive address to be returned to clients, null if not
     *      configured.
     */
    InetAddress getPassiveExernalAddress();
    
    /**
     * Set the passive ports to be used for data connections. 
     * Ports can be defined as single ports, closed or open ranges. 
     * Multiple definitions can be separated by commas, for example:
     * <ul>
     *   <li>2300 : only use port 2300 as the passive port</li>
     *   <li>2300-2399 : use all ports in the range</li>
     *   <li>2300- : use all ports larger than 2300</li>
     *   <li>2300, 2305, 2400- : use 2300 or 2305 or any port larger than 2400</li>
     * </ul>
     * 
     * Defaults to using any available port
     * @return The passive ports string
     */
    String getPassivePorts();
    
    /**
     * Set the allowed passive ports. 
     * @see DataConnectionConfiguration#getPassivePorts() for details on the allowed format.
     * If set to null, the passive port with be assigned from any available port 
     * @param passivePorts The passive ports to use for this data connection
     */
    void setPassivePorts(String passivePorts);

    
    /**
     * Request a passive port
     */
    int requestPassivePort();
    
    /**
     * Release passive port.
     */
    void releasePassivePort(int port);
    
    /**
     * Get SSL configuration for this data connection.
     */
    SslConfiguration getSSLConfiguration();
}
