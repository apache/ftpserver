package org.apache.ftpserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.ftpserver.interfaces.Connection;
import org.apache.ftpserver.interfaces.ConnectionManager;
import org.apache.ftpserver.interfaces.ServerFtpConfig;

public class DefaultListener implements Listener, Runnable {

    private Log log;
    
    private ServerFtpConfig ftpConfig;

    private ServerSocket serverSocket;

    private Thread listenerThread;

    private boolean suspended = false;

    public DefaultListener(ServerFtpConfig ftpConfig) {
        this.ftpConfig = ftpConfig;
        
        log = ftpConfig.getLogFactory().getInstance(getClass());
    }

    public void start() throws Exception {
        serverSocket = ftpConfig.getSocketFactory().createServerSocket();

        listenerThread = new Thread(this);
        listenerThread.start();

    }

    public void run() {
        if(serverSocket == null) {
            throw new IllegalStateException("start() must be called before run()");
        }
        
        log.info("Listener started on port " + serverSocket.getLocalPort());

        // ftpConfig might be null if stop has been called
        if (ftpConfig == null) {
            return;
        }

        ConnectionManager conManager = ftpConfig.getConnectionManager();
        
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

                Connection connection = new RequestHandler(ftpConfig, soc);
                conManager.newConnection(connection);
            } catch (Exception ex) {
                return;
            }
        }
    }

    public synchronized void stop() {
        // close server socket
        if (serverSocket != null) {

            try {
                serverSocket.close();
            } catch (IOException ex) {
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

    public boolean isStopped() {
        return listenerThread == null;

    }

    public boolean isSuspended() {
        return suspended;
    }

    public void resume() {
        suspended = false;
    }

    public void suspend() {
        suspended = true;
    }
}