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
package org.apache.ftpserver.socketfactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.commons.logging.LogFactory;
import org.apache.ftpserver.config.PropertiesConfiguration;
import org.apache.ftpserver.ftplet.EmptyConfiguration;
import org.apache.ftpserver.ftplet.FtpException;

public class FtpSocketFactoryTest extends TestCase {

    private FtpSocketFactory ftpSocketFactory;

    private FtpSocketFactory ftpSocketFactory2;

    private Properties getProperties() {
        Properties properties = new Properties();
        ftpSocketFactory2 = new FtpSocketFactory();
        properties.setProperty("address", "localhost");
        properties.setProperty("port", "8021");
        return properties;
    }

    protected void setUp() throws Exception {
        super.setUp();
        ftpSocketFactory = new FtpSocketFactory();
        ftpSocketFactory.configure(EmptyConfiguration.INSTANCE);

        Properties properties = getProperties();
        ftpSocketFactory2.configure(new PropertiesConfiguration(properties));
    }

    protected void tearDown() throws Exception {
        ftpSocketFactory.dispose();
        ftpSocketFactory2.dispose();
    }

    /*
     * Test method for
     * 'org.apache.ftpserver.socketfactory.FtpSocketFactory.configure(Configuration)'
     */
    public void testConfigure() throws FtpException {
        ftpSocketFactory.configure(EmptyConfiguration.INSTANCE);
        ftpSocketFactory
                .configure(new PropertiesConfiguration(getProperties()));
    }

    /*
     * Test method for
     * 'org.apache.ftpserver.socketfactory.FtpSocketFactory.createServerSocket()'
     */
    public void testCreateServerSocket() throws Exception {
        testCreateServerSocket(ftpSocketFactory, 21);
        testCreateServerSocket(ftpSocketFactory2, 8021);
    }

    private void testCreateServerSocket(
            final FtpSocketFactory ftpSocketFactory, final int port)
            throws Exception, IOException {
        boolean freePort = false;
        try {
            final ServerSocket testSocket = new ServerSocket(port, 100);
            freePort = testSocket.isBound();
            testSocket.close();
        } catch (Exception exc) {
            // ok
            freePort = true;
        }
        if (freePort) {
            new Thread() {
                public void run() {
                    synchronized (this) {
                        try {
                            this.wait(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            fail(e.toString());
                        }
                    }
                    try {
                        Socket socket = new Socket();
                        socket.connect(new InetSocketAddress("localhost", 
                                 port));
                        socket.getInputStream();
                        socket.getOutputStream();
                        socket.close();
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                        fail(e.toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                        fail(e.toString());
                    }
                }
            }.start();
            ServerSocket serverSocket = ftpSocketFactory.createServerSocket();
            assertNotNull(serverSocket);
            serverSocket.accept();
        }
    }

    /*
     * Test method for
     * 'org.apache.ftpserver.socketfactory.FtpSocketFactory.dispose()'
     */
    public void testDispose() {
    }

    /*
     * Test method for
     * 'org.apache.ftpserver.socketfactory.FtpSocketFactory.getPort()'
     */
    public void testGetPort() {
        assertEquals(21, ftpSocketFactory.getPort());
        assertEquals(8021, ftpSocketFactory2.getPort());
    }

    /*
     * Test method for
     * 'org.apache.ftpserver.socketfactory.FtpSocketFactory.getServerAddress()'
     */
    public void testGetServerAddress() throws UnknownHostException {
        assertEquals(null, ftpSocketFactory.getServerAddress());
        assertEquals(InetAddress.getByName("localhost"), ftpSocketFactory2
                .getServerAddress());
    }

    /*
     * Test method for
     * 'org.apache.ftpserver.socketfactory.FtpSocketFactory.getSSL()'
     */
    public void testGetSSL() {
        assertNull(ftpSocketFactory.getSSL());
        assertNull(ftpSocketFactory2.getSSL());
    }

    /*
     * Test method for
     * 'org.apache.ftpserver.socketfactory.FtpSocketFactory.setLogFactory(LogFactory)'
     */
    public void testSetLogFactory() {
        ftpSocketFactory.setLogFactory(LogFactory.getFactory());
        ftpSocketFactory2.setLogFactory(LogFactory.getFactory());
    }

}