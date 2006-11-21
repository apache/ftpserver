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


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.ftpserver.interfaces.ServerFtpConfig;

/**
 * This is the starting point of all the servers. It invokes a new listener
 * thread. <code>Server</code> implementation is used to create the server
 * socket and handle client connection.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public class FtpServer {

    private Thread runner;

    private ServerFtpConfig ftpConfig;

    private Log log;

    private boolean suspended;

    private List listeners = new ArrayList();

    /**
     * Constructor. Set the server object.
     */
    public FtpServer(ServerFtpConfig ftpConfig) {
        this.ftpConfig = ftpConfig;
        log = this.ftpConfig.getLogFactory().getInstance(getClass());

        // for now just create one 
        listeners.add(new DefaultListener(ftpConfig));
    }

    /**
     * Start the server. Open a new listener thread.
     */
    public void start() throws Exception {
        for (Iterator iter = listeners.iterator(); iter.hasNext();) {
            Listener listener = (Listener) iter.next();
            
            listener.start();
        }
        
        System.out.println("Server ready :: Apache FTP Server");
        log.info("------- Apache FTP Server started ------");

    }
    
    /**
     * Stop the server. Stop the listener thread.
     */
    public void stop() {

        // stop all listeners
        for (Iterator iter = listeners.iterator(); iter.hasNext();) {
            Listener listener = (Listener) iter.next();
            
            listener.stop();
        }

        // release server resources
        if (ftpConfig != null) {
            ftpConfig.dispose();
            ftpConfig = null;
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
     * Get the root server configuration object.
     */
    public ServerFtpConfig getFtpConfig() {
        return ftpConfig;
    }
}
