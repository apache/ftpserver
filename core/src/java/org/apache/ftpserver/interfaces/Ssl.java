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
import java.net.Socket;
import java.security.GeneralSecurityException;

import javax.net.ssl.SSLContext;

/**
 * SSL interface.
 */
public 
interface Ssl {
    
    SSLContext getSSLContext() throws GeneralSecurityException;
    SSLContext getSSLContext(String protocol) throws GeneralSecurityException;
    
    boolean getClientAuthenticationRequired();
    
    
    /**
     * Create secure server socket.
     */
    ServerSocket createServerSocket(String protocol, 
                                    InetAddress addr, 
                                    int port) throws Exception;
    
    /**
     * Returns a socket layered over an existing socket.
     */
    Socket createSocket(String protocol,
                        Socket soc, 
                        boolean clientMode) throws Exception;
    
    /**
     * Create a secure socket.
     */
    Socket createSocket(String protocol,
                        InetAddress host, 
                        int port,
                        boolean clientMode) throws Exception;
    
    /**
     * Create a secure socket.
     */
    Socket createSocket(String protocol,
                        InetAddress host,
                        int port,
                        InetAddress localhost,
                        int localport,
                        boolean clientMode) throws Exception;
}
