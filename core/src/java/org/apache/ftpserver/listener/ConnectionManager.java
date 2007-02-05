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

import java.util.List;

import org.apache.ftpserver.ftplet.Component;

/**
 * It manages all the ftp connections.
 */
public
interface ConnectionManager {

    /**
     * Get maximum number of connections.
     */
    int getMaxConnections();
     
    /**
     * Get maximum number of logins.
     */
    int getMaxLogins();
     
    
    /**
     * Is anonymous login enabled?
     */
    boolean isAnonymousLoginEnabled();
    
    
    /**
     * Get maximum anonymous logins
     */
    int getMaxAnonymousLogins();

    
    /**
     * Get all request handlers.
     */
    List getAllConnections();
    
    /**
     * Establish a new connection channel.
     */
    void newConnection(Connection connection); 
    
    
    /**
     * Update connection.
     */
    void updateConnection(Connection connection);
    
    
    /**
     * Close a connection.
     */
    void closeConnection(Connection connection);
    
    
    /**
     * Close all connections.
     */
    void closeAllConnections();
    
    
    /**
     * Set connection manager observer.
     */
    void setObserver(ConnectionManagerObserver observer);
} 
