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

import org.apache.ftpserver.ftplet.Ftplet;
import org.apache.ftpserver.ftplet.FtpletContext;
import org.apache.ftpserver.listener.ConnectionManager;
import org.apache.ftpserver.listener.Listener;

/**
 * This is basically <code>org.apache.ftpserver.ftplet.FtpletContext</code> with added
 * connection manager, message resource functionalities.
 */
public 
interface FtpServerContext extends FtpletContext {

    /**
     * Get connection manager.
     */
    ConnectionManager getConnectionManager();
    
    /**
     * Get message resource.
     */
    MessageResource getMessageResource();
    
    /**
     * Get IP restrictor.
     */
    IpRestrictor getIpRestrictor();
    
    /**
     * Get ftplet container.
     */
    Ftplet getFtpletContainer();
    
    Listener getListener(String name);

    Listener[] getAllListeners();
    
    /**
     * Get the command factory.
     */
    CommandFactory getCommandFactory();
    
    /**
     * Release all components.
     */
    void dispose();
}
