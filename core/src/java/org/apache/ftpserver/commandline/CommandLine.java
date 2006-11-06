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

import org.apache.ftpserver.FtpConfigImpl;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.config.PropertiesConfiguration;
import org.apache.ftpserver.config.XmlConfigurationHandler;
import org.apache.ftpserver.ftplet.Configuration;
import org.apache.ftpserver.ftplet.EmptyConfiguration;
import org.apache.ftpserver.interfaces.ServerFtpConfig;
import org.apache.ftpserver.util.IoUtils;

/**
 * This class is the starting point for the FtpServer when it is started
 * using the command line mode.
 *
 * There are three ways to start the FTP server. The one to be used must be
 * specified as the first command line parameter when starting the application.
 * <p>
 * The allowed values are:
 * <ul>
 *   <li>-default :: default configuration will be used.</li>
 *   <li>-xml :: XML configuration will be used. User has to specify the file.</li>
 *   <li>-prop :: properties configuration will be used. User has to specify the file.</li>
 * </ul>
 * If you do not specify any parameter, default configuration will be used. 
 * 
 * @author Luis Sanabria
 */
public 
class CommandLine {

    /**
     * The pourpose of this class is to allow the final user to start the
     * FtpServer application. Because of that it has only <code>static</code>
     * methods and cannot be instanced.
     */
    private CommandLine() {
    }

    /**
     * This method is the FtpServer starting poing when running by using the
     * command line mode.
     *
     * @param args The first element of this array must specify the kind of
     *             configuration to be used to start the server.
     */
    public static void main(String args[]) {

        try {

            // get configuration
            Configuration config = getConfiguration(args);
            if(config == null) {
                return;
            }

            // create root configuration object
            ServerFtpConfig ftpConfig = new FtpConfigImpl(config);

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
        System.err.println("Usage: java org.apache.ftpserver.commandline.CommandLine [<options>]");
        System.err.println("  <options> := -default |");
        System.err.println("               -xml <XML configuration file> |");
        System.err.println("               -prop <properties configuration file>");
        System.out.println();
        System.out.println("There are three ways to start the FTP server.");
        System.out.println("    -default :: default configuration will be used.");
        System.out.println("    -xml     :: XML configuration will be used. User has to specify the file.");
        System.out.println("    -prop    :: properties configuration will be used. User has to specify the file.");
        System.out.println();
        System.out.println("In case of no option, default configuration will be used.");
    }

    /**
     * Get the configuration object.
     */
    private static Configuration getConfiguration(String[] args) throws Exception {

        Configuration config = null;
        FileInputStream in = null;
        try {
            if(args.length == 0) {
                System.out.println("Using default configuration....");
                config = EmptyConfiguration.INSTANCE;
            }
            else if( (args.length == 1) && args[0].equals("-default") ) {
                System.out.println("Using default configuration....");
                config = EmptyConfiguration.INSTANCE;
            }
            else if( (args.length == 2) && args[0].equals("-xml") ) {
                System.out.println("Using xml configuration file " + args[1] + "...");
                in = new FileInputStream(args[1]);
                XmlConfigurationHandler xmlHandler = new XmlConfigurationHandler(in);
                config = xmlHandler.parse();
            }
            else if( (args.length == 2) && args[0].equals("-prop") ) {
                System.out.println("Using properties configuration file " + args[1] + "...");
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