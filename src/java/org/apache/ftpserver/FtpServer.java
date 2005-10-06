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

import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.ftpserver.config.PropertiesConfiguration;
import org.apache.ftpserver.config.XmlConfigurationHandler;
import org.apache.ftpserver.ftplet.Configuration;
import org.apache.ftpserver.ftplet.EmptyConfiguration;
import org.apache.ftpserver.interfaces.IConnection;
import org.apache.ftpserver.interfaces.IConnectionManager;
import org.apache.ftpserver.interfaces.IFtpConfig;
import org.apache.ftpserver.util.IoUtils;


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
        
    
    /////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////
    /**
     * Command line ftp server starting point.
     */
    public static void main(String args[]) {
        
        try {
        
            // get configuration
            Configuration config = getConfiguration(args);
            if(config == null) {
                return;
            }
            
            // create root configuration object
            IFtpConfig ftpConfig = new FtpConfigImpl(config);
            
            // start the server    
            FtpServer server = new FtpServer(ftpConfig);
            server.start();
            
            // add shutdown hook if possible
            addShutdownHook(server);
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * Add shutdown hook.
     */
    private static void addShutdownHook(final FtpServer engine) {
        
        // create shutdown hook
        Runnable shutdownHook = new Runnable() {
            public void run() {
                System.out.println("Stopping server...");
                engine.stop();
            }    
        };
        
        // add shutdown hook
        Runtime runtime = Runtime.getRuntime();
        runtime.addShutdownHook(new Thread(shutdownHook));
    }
    
    /**
     * Print the usage message.
     */
    private static void usage() {
        System.err.println("Usage: java org.apache.ftpserver.FtpServer <options>");
        System.err.println("  <options> := -default |");
        System.err.println("               -xml <XML configuration file> |");
        System.err.println("               -prop <properties configuration file>");
        System.out.println();
        System.out.println("There are three ways to start the FTP server.");
        System.out.println("    -default :: default configuration will be used.");
        System.out.println("    -xml     :: XML configuration will be used. User has to specify the file.");
        System.out.println("    -prop    :: properties configuration will be used. User has to specify the file.");
    }

    /**
     * Get the configuration object.
     */
    private static Configuration getConfiguration(String[] args) throws Exception {
        
        Configuration config = null;
        FileInputStream in = null;
        try {
            if( (args.length == 1) && args[0].equals("-default") ) {
                config = EmptyConfiguration.INSTANCE;
            }
            else if( (args.length == 2) && args[0].equals("-xml") ) {
                in = new FileInputStream(args[1]);
                XmlConfigurationHandler xmlHandler = new XmlConfigurationHandler(in);
                config = xmlHandler.parse();
            }
            else if( (args.length == 2) && args[0].equals("-prop") ) {
                in = new FileInputStream(args[1]);
                config = new PropertiesConfiguration(in);
            }
        }
        finally {
            IoUtils.close(in);
        }
        
        if(config == null) {
            usage();
        }
        
        return config;
    }
    
}
