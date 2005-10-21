// $Id$
/*
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

    private Thread m_runner;
    private ServerSocket m_serverSocket;
    private IFtpConfig m_ftpConfig;
    private Log m_log;
    private boolean m_suspended;
    

    /**
     * Constructor. Set the server object.
     */
    public FtpServer(IFtpConfig ftpConfig) {
        m_ftpConfig = ftpConfig;
        m_log = m_ftpConfig.getLogFactory().getInstance(getClass());
    }

    /**
     * Start the server. Open a new listener thread.
     */
    public void start() throws Exception {
        if (m_runner == null) {
            m_serverSocket = m_ftpConfig.getSocketFactory().createServerSocket();            
            m_runner = new Thread(this);
            m_runner.start();
            System.out.println("Server ready :: Apache FTP Server");
            m_log.info("------- Apache FTP Server started ------");
        }
    }

    /**
     * Stop the server. Stop the listener thread.
     */
    public void stop() {
        
        // first interrupt the server engine thread
        if (m_runner != null) {
            m_runner.interrupt();
        }

        // close server socket
        if (m_serverSocket != null) {
            try {
                m_serverSocket.close();
            }
            catch(IOException ex){
            }
            m_serverSocket = null;
        }  
         
        // release server resources
        if (m_ftpConfig != null) {
            m_ftpConfig.dispose();
            m_ftpConfig = null;
        }

        // wait for the runner thread to terminate
        if( (m_runner != null) && m_runner.isAlive() ) {
            try {
                m_runner.join();
            }
            catch(InterruptedException ex) {
            }
            m_runner = null;
        }
    }

    /**
     * Get the server status.
     */
    public boolean isStopped() {
        return m_runner == null;
    }

    /**
     * Suspend further requests
     */
    public void suspend() {
        m_suspended = true;
    }

    /**
     * Resume the server handler
     */
    public void resume() {
        m_suspended = false;
    }

    /**
     * Is the server suspended
     */
    public boolean isSuspended() {
        return m_suspended;
    }

    /**
     * Listen for client requests.
     */
    public void run() {
        IConnectionManager conManager = m_ftpConfig.getConnectionManager();
        while (m_runner != null) {
            try {
                
                // closed - return
                if(m_serverSocket == null) {
                    return;
                }
                
                // accept new connection .. if suspended 
                // close immediately.
                Socket soc = m_serverSocket.accept();
                if(m_suspended) {
                    try {
                        soc.close();
                    }
                    catch(Exception ex) {
                    }
                    continue;
                }
                
                IConnection connection = new RequestHandler(m_ftpConfig, soc);
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
        return m_ftpConfig;
    }
}
