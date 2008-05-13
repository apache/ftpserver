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


import java.util.Map;

import org.apache.ftpserver.interfaces.FtpServerContext;
import org.apache.ftpserver.listener.Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the starting point of all the servers. It invokes a new listener
 * thread. <code>Server</code> implementation is used to create the server
 * socket and handle client connection.
 */
public class FtpServer {

    private final Logger LOG = LoggerFactory.getLogger(FtpServer.class);
    
    
    private FtpServerContext serverContext;

    private boolean suspended;

    private boolean started = false;

    /**
     * Creates a server with the default configuration
     * @throws Exception 
     */
    public FtpServer() throws Exception {
        serverContext = new DefaultFtpServerContext();
    }

    /**
     * Constructor. Set the server context.
     * @throws Exception 
     */
    public FtpServer(FtpServerContext serverContext) throws Exception {
        this.serverContext = serverContext;
    }

    /**
     * Start the server. Open a new listener thread.
     */
    public void start() throws Exception {
        Map<String, Listener> listeners = serverContext.getListeners(); 
        for (Listener listener : listeners.values()) {
            listener.start(serverContext);
        }

        started = true;
        
        LOG.info("FTP server started");

    }
    
    /**
     * Stop the server. Stop the listener thread.
     */
    public void stop() {
    	if(!started || serverContext == null) {
    		// we have already been stopped, ignore
    		return;
    	}
    	
        // stop all listeners
    	Map<String, Listener> listeners = serverContext.getListeners();
        for (Listener listener : listeners.values()) {
            listener.stop();
        }

        // release server resources
        if (serverContext != null) {
            serverContext.dispose();
            serverContext = null;
        }

        started = false;
    }

    /**
     * Get the server status.
     */
    public boolean isStopped() {
        return !started;
    }

    /**
     * Suspend further requests
     */
    public void suspend() {
        // stop all listeners
        Map<String, Listener> listeners = serverContext.getListeners();
        for (Listener listener : listeners.values()) {
            listener.suspend();
        }
        
        suspended = true;
    }

    /**
     * Resume the server handler
     */
    public void resume() {
        Map<String, Listener> listeners = serverContext.getListeners();
        for (Listener listener : listeners.values()) {
            listener.resume();
        }

        
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

	public void setServerContext(FtpServerContext serverContext) {
		this.serverContext = serverContext;
	}
}
