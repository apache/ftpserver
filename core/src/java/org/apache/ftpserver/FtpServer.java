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

package org.apache.ftpserver;


import org.apache.commons.logging.Log;
import org.apache.ftpserver.interfaces.FtpServerContext;
import org.apache.ftpserver.listener.Listener;

/**
 * This is the starting point of all the servers. It invokes a new listener
 * thread. <code>Server</code> implementation is used to create the server
 * socket and handle client connection.
 */
public class FtpServer {

    private Thread runner;

    private FtpServerContext serverContext = new DefaultFtpServerContext();

    private Log log;

    private boolean suspended;

    /**
     * Default constructor.
     * @throws Exception 
     */
    public FtpServer() throws Exception {
        log = this.serverContext.getLogFactory().getInstance(getClass());
    }

    /**
     * Constructor. Set the server context.
     * @throws Exception 
     */
    public FtpServer(FtpServerContext serverContext) throws Exception {
        this.serverContext = serverContext;
        log = this.serverContext.getLogFactory().getInstance(getClass());
    }

    /**
     * Start the server. Open a new listener thread.
     */
    public void start() throws Exception {
        Listener[] listeners = serverContext.getListeners(); 
        for (int i = 0; i<listeners.length; i++) {
            listeners[i].start(serverContext);
        }
        
        System.out.println("Server ready :: Apache FTP Server");
        log.info("------- Apache FTP Server started ------");

    }
    
    /**
     * Stop the server. Stop the listener thread.
     */
    public void stop() {

        // stop all listeners
        Listener[] listeners = serverContext.getListeners(); 
        for (int i = 0; i<listeners.length; i++) {
            listeners[i].stop();
        }


        // release server resources
        if (serverContext != null) {
            serverContext.dispose();
            serverContext = null;
        }

    }

    /**
     * Get the server status.
     */
    public boolean isStopped() {
        return runner == null;
    }

    /**
     * Suspend further requests
     */
    public void suspend() {
        suspended = true;
    }

    /**
     * Resume the server handler
     */
    public void resume() {
        suspended = false;
    }

    /**
     * Is the server suspended
     */
    public boolean isSuspended() {
        return suspended;
    }

    /**
     * Get the root server context.
     */
    public FtpServerContext getServerContext() {
        return serverContext;
    }
}
