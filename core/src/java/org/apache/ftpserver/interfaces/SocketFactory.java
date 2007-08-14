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
import java.net.ServerSocket;

import org.apache.ftpserver.ssl.Ssl;


/**
 * This interface is responsible to create appropriate server socket.
 */
public
interface SocketFactory {
    
    /**
     * Create the server socket. 
     */
    ServerSocket createServerSocket() throws Exception;
    
    /**
     * Get server address.
     * 
     * @return {@link InetAddress} obtained from host address from
     *         {@link SocketFactory} configuration, or <code>null</code> if
     *         not specified.
     */
    InetAddress getServerAddress();
    
    /**
     * Get server port.
     */
    int getPort();
    
    /**
     * Get SSL component.
     */
    Ssl getSSL();
}
