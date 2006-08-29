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

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.ftpserver.interfaces.IConnection;
import org.apache.ftpserver.interfaces.IConnectionManager;
import org.apache.ftpserver.interfaces.IFtpConfig;


/**
 * This is the starting point of all the servers. It invokes a new listener
 * thread. <code>Server</code> implementation is used to create the server 
 * socket and handle client connection.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class FtpServer implements Runnable {

    private Thread runner;
    private ServerSocket serverSocket;
    private IFtpConfig ftpConfig;
    private Log log;
    private boolean suspended;
    

    /**
     * Constructor. Set the server object.
     */
    public FtpServer(IFtpConfig ftpConfig) {
        this.ftpConfig = ftpConfig;
        log = this.ftpConfig.getLogFactory().getInstance(getClass());
    }

    /**
     * Start the server. Open a new listener thread.
     */
    public void start() throws Exception {
        if (runner == null) {
            serverSocket = ftpConfig.getSocketFactory().createServerSocket();            
            runner = new Thread(this);
            runner.start();
            System.out.println("Server ready :: Apache FTP Server");
            log.info("------- Apache FTP Server started ------");
        }
    }

    /**
     * Stop the server. Stop the listener thread.
     */
    public void stop() {
        
        // first interrupt the server engine thread
        if (runner != null) {
            runner.interrupt();
        }

        // close server socket
        if (serverSocket != null) {
            try {
                serverSocket.close();
            }
            catch(IOException ex){
            }
            serverSocket = null;
        }  

        
        // wait for the runner thread to terminate
        if( (runner != null) && runner.isAlive() ) {
            try {
                runner.join();
            }
            catch(InterruptedException ex) {
            }
            runner = null;
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
     * Listen for client requests.
     */
    public void run() {
        // ftpConfig might be null if stop has been called
        if(ftpConfig == null) {
            return;
        }
        
        IConnectionManager conManager = ftpConfig.getConnectionManager();
        while (runner != null) {
            try {
                
                // closed - return
                if(serverSocket == null) {
                    return;
                }
                
                // accept new connection .. if suspended 
                // close immediately.
                Socket soc = serverSocket.accept();
                if(suspended) {
                    try {
                        soc.close();
                    }
                    catch(Exception ex) {
                    }
                    continue;
                }
                
                IConnection connection = new RequestHandler(ftpConfig, soc);
                conManager.newConnection(connection);
            }
            catch (Exception ex) {
                return;
            }
        }
    }
    
    /**
     * Get the root server configuration object. 
     */
    public IFtpConfig getFtpConfig() {
        return ftpConfig;
    }
}
