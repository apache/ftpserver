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

package org.apache.ftpserver.commandline;

import java.io.FileInputStream;

import org.apache.ftpserver.ConfigurableFtpServerContext;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.config.PropertiesConfiguration;
import org.apache.ftpserver.config.XmlConfigurationHandler;
import org.apache.ftpserver.ftplet.Configuration;
import org.apache.ftpserver.ftplet.EmptyConfiguration;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.interfaces.FtpServerContext;
import org.apache.ftpserver.util.IoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Invokes FtpServer as a daemon, running in the background. 
 * Used for example for the Windows service.
 */
public class Daemon {

    private static final Logger LOG = LoggerFactory.getLogger(Daemon.class);
    
    private static FtpServer server;
    private static Object lock = new Object();
    
    public static void main(String[] args) throws Exception {
        try{
            if(server == null) {
                // get configuration
                Configuration config = getConfiguration(args);
                if(config == null) {
                    LOG.error("No configuration provided");
                    throw new FtpException("No configuration provided");
                }
    
                // create root configuration object
                FtpServerContext serverContext = new ConfigurableFtpServerContext(config);
    
                // start the server
                server = new FtpServer(serverContext);  
            }
            
            String command = "start";
            
            if(args != null && args.length > 0) {
                command = args[0];
            }
            
            
            if(command.equals("start")) {
                LOG.info("Starting FTP server daemon");
                server.start();
                
                synchronized (lock) {
                    lock.wait();
                }
            } else if(command.equals("stop")) {
                synchronized (lock) {
                    lock.notify();
                }
                LOG.info("Stopping FTP server daemon");
                server.stop();
            }
        } catch(Throwable t) {
            LOG.error("Daemon error", t);
        }
    }

    /**
     * Get the configuration object.
     */
    private static Configuration getConfiguration(String[] args) throws Exception {
    
        Configuration config = null;
        FileInputStream in = null;
        try {
            if(args == null || args.length < 2) {
                LOG.info("Using default configuration....");
                config = EmptyConfiguration.INSTANCE;
            }
            else if( (args.length == 2) && args[1].equals("-default") ) {
                LOG.info("Using default configuration....");
                config = EmptyConfiguration.INSTANCE;
            }
            else if( (args.length == 3) && args[1].equals("-xml") ) {
                LOG.info("Using xml configuration file " + args[2] + "...");
                in = new FileInputStream(args[2]);
                XmlConfigurationHandler xmlHandler = new XmlConfigurationHandler(in);
                config = xmlHandler.parse();
            }
            else if( (args.length == 3) && args[1].equals("-prop") ) {
                LOG.info("Using properties configuration file " + args[2] + "...");
                in = new FileInputStream(args[2]);
                config = new PropertiesConfiguration(in);
            } else {
                throw new FtpException("Invalid configuration option");
            }
        }
        finally {
            IoUtils.close(in);
        }
        
        return config;
    }
}
