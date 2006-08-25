/*
 * Copyright 2006 The Apache Software Foundation
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

package org.apache.ftpserver.clienttests;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Properties;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.ftpserver.FtpConfigImpl;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.config.PropertiesConfiguration;
import org.apache.ftpserver.interfaces.IFtpConfig;

import junit.framework.TestCase;

public abstract class ClientTestTemplate extends TestCase {

    private static final int FALLBACK_PORT = 12321;

    private FtpServer server;

    private int port = -1;
    private IFtpConfig config;

    protected FTPClient client;

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        initPort();

        Properties configProps = new Properties();
        configProps.setProperty("config.socket-factory.port", Integer
                .toString(port));

        config = new FtpConfigImpl(new PropertiesConfiguration(configProps));
        server = new FtpServer(config);
        server.start();

        client = new FTPClient();
        client.connect("localhost", port);
    }
    


    /**
     * Attempts to find a free port or fallback to a default
     * 
     * @throws IOException
     */
    private void initPort() {
        if (port == -1) {
            ServerSocket tmpSocket = null;
            try {
                tmpSocket = new ServerSocket();
                tmpSocket.bind(null);
                port = tmpSocket.getLocalPort();
            } catch (IOException e) {
                port = FALLBACK_PORT;
            } finally {
                if (tmpSocket != null) {
                    try {
                        tmpSocket.close();
                    } catch (IOException e) {
                        // ignore
                    }
                }
            }
        }
    }


    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        server.stop();
    }
    
}
