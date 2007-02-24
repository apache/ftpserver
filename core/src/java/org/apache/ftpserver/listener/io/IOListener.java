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

package org.apache.ftpserver.listener.io;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import org.apache.ftpserver.interfaces.FtpServerContext;
import org.apache.ftpserver.listener.AbstractListener;
import org.apache.ftpserver.listener.Connection;
import org.apache.ftpserver.listener.ConnectionManager;
import org.apache.ftpserver.listener.Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default {@link Listener} implementation.
 *
 */
public class IOListener extends AbstractListener implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(IOListener.class);

    private FtpServerContext serverContext;

    private ServerSocket serverSocket;

    private Thread listenerThread;

    private boolean suspended = false;

    /**
     * @see Listener#start(FtpServerContext)
     */
    public void start(FtpServerContext serverContext) throws Exception {
        this.serverContext = serverContext;
        
        serverSocket = createServerSocket();

        listenerThread = new Thread(this);
        listenerThread.start();

    }
    
    /**
     * Create server socket.
     */
    private ServerSocket createServerSocket() throws Exception { 
        ServerSocket ssocket = null;
        
        if(isImplicitSsl()) {
            ssocket = getSsl().createServerSocket(null, getServerAddress(), getPort());
        } else  {
            if(getServerAddress() == null) {
                ssocket = new ServerSocket(getPort(), 100);
            } else {
                ssocket = new ServerSocket(getPort(), 100, getServerAddress());
            }
        }
        
        return ssocket;
    }

    /**
     * The main thread method for the listener
     */
    public void run() {
        if(serverSocket == null) {
            throw new IllegalStateException("start() must be called before run()");
        }
        
        LOG.info("Listener started on port " + serverSocket.getLocalPort());

        // serverContext might be null if stop has been called
        if (serverContext == null) {
            return;
        }

        ConnectionManager conManager = serverContext.getConnectionManager();
        
        while (true) {
            try {

                // closed - return
                if (serverSocket == null) {
                    return;
                }

                // accept new connection .. if suspended
                // close immediately.
                Socket soc = serverSocket.accept();

                if (suspended) {
                    try {
                        soc.close();
                    } catch (Exception ex) {
                        // ignore
                    }
                    continue;
                }

                Connection connection = new IOConnection(serverContext, soc, this);
                conManager.newConnection(connection);
            } catch (SocketException ex) {
                return;
            } catch (Exception ex) {
                LOG.debug("Listener ending", ex);
                return;
            }
        }
    }

    /**
     * @see Listener#stop()
     */
    public synchronized void stop() {
        // close server socket
        if (serverSocket != null) {

            try {
                serverSocket.close();
            } catch (IOException ex) {
                // ignore
            }
            serverSocket = null;
        }

        listenerThread.interrupt();

        // wait for the runner thread to terminate
        if (listenerThread != null && listenerThread.isAlive()) {

            try {
                listenerThread.join();
            } catch (InterruptedException ex) {
            }
            listenerThread = null;
        }
    }

    /**
     * @see Listener#isStopped()
     */
    public boolean isStopped() {
        return listenerThread == null;

    }

    /**
     * @see Listener#isSuspended()
     */
    public boolean isSuspended() {
        return suspended;
    }

    /**
     * @see Listener#resume()
     */
    public void resume() {
        suspended = false;
    }

    /**
     * @see Listener#suspend()
     */
    public void suspend() {
        suspended = true;
    }
}