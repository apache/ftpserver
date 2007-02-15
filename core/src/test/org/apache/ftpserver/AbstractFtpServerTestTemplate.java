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

import java.net.InetAddress;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.ftpserver.config.PropertiesConfiguration;
import org.apache.ftpserver.interfaces.FtpServerContext;
import org.apache.ftpserver.interfaces.SocketFactory;
import org.apache.ftpserver.test.TestUtil;

/**
 * Abstract test case class, which starts and shutdown FtpServer.
 */
public abstract class AbstractFtpServerTestTemplate extends TestCase {

    private FtpServer ftpServer;

    private InetAddress serverAddress;

    private int serverPort;

    protected final FtpServer getFtpServer() {
        return ftpServer;
    }

    protected final InetAddress getServerAddress() {
        return serverAddress;
    }

    protected final int getServerPort() {
        return serverPort;
    }

    protected Properties createConfig() {
        Properties configProps = new Properties();
        configProps.setProperty("config.listeners.default.port", Integer
                .toString(serverPort));

        return configProps;
    }
    
    protected void setUp() throws Exception {
        super.setUp();

        this.serverPort = TestUtil.findFreePort();
        
        // create root configuration object
        final FtpServerContext serverContext = new ConfigurableFtpServerContext(new PropertiesConfiguration(createConfig()));

        // start the server
        this.ftpServer = new FtpServer(serverContext);
        this.ftpServer.start();

        final SocketFactory socketFactory = serverContext.getSocketFactory();
        this.serverAddress = socketFactory.getServerAddress() != null ? socketFactory
                .getServerAddress()
                : InetAddress.getByName(null);
    }

    protected void tearDown() throws Exception {
        ftpServer.stop();
        super.tearDown();
    }

}
