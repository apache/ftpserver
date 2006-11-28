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

import org.apache.ftpserver.ftplet.Component;

/**
 * Data connection configuration interface.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
interface DataConnectionConfig extends Component {

    /**
     * Get the maximum idle time in millis.
     */
    int getMaxIdleTimeMillis();
    
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
     * Get passive port.
     */
    int getPassivePort();
    
    /**
     * Release passive port.
     */
    void releasePassivePort(int port);
    
    /**
     * Get SSL component.
     */
    Ssl getSSL();
}
