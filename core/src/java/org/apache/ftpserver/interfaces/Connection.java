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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.apache.ftpserver.ftplet.FtpSession;

/**
 * This is the connection request handler interface.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public
interface Connection extends Runnable {

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
     * TODO: Limit exception type.
     * TODO: Rename without create (indicates factory)
     */
    void createSecureSocket(String type) throws Exception;

    long transfer(InputStream bis, OutputStream bos, int maxRate) throws IOException;

    Socket getControlSocket();
}
 
