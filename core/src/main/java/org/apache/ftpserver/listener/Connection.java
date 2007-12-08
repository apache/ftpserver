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

import org.apache.ftpserver.ftplet.FtpSession;
import org.apache.ftpserver.interfaces.FtpServerContext;
import org.apache.ftpserver.interfaces.FtpServerSession;

/**
 * This is the connection request handler interface.
 */
public
interface Connection {

    /**
     * Get current session.
     */
    FtpSession getSession();
    
    /**
     * Close handler.
     */
    void close();
            
    /**
     * Set connection observer.
     */
    void setObserver(ConnectionObserver observer);
    
    /**
     * Return the server context
     * @return The servet context
     */
    FtpServerContext getServerContext();

    /**
     * Secure the control socket
     * @param type The type of security to use, i.e. SSL or TLS
     * @throws Exception
     */
    void beforeSecureControlChannel(FtpServerSession session, String type) throws Exception;

    void afterSecureControlChannel(FtpServerSession session, String type) throws Exception;
}
 
