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

package org.apache.ftpserver.config.spring;

import java.io.File;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.command.impl.DefaultCommandFactory;
import org.apache.ftpserver.command.impl.HELP;
import org.apache.ftpserver.command.impl.STAT;
import org.apache.ftpserver.filesystem.NativeFileSystemManager;
import org.apache.ftpserver.listener.Listener;
import org.apache.ftpserver.listener.nio.NioListener;
import org.apache.ftpserver.ssl.impl.DefaultSslConfiguration;
import org.apache.mina.filter.firewall.Subnet;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.FileSystemResource;

/**
*
* @author The Apache MINA Project (dev@mina.apache.org)
* @version $Rev$, $Date$
*
*/
public class SpringConfigTest extends TestCase {

    public void test() throws Throwable {
        XmlBeanFactory factory = new XmlBeanFactory(new FileSystemResource(
                "src/test/resources/spring-config/config-spring-1.xml"));

        FtpServer server = (FtpServer) factory.getBean("server");

        assertEquals(500, server.getConnectionConfig().getMaxLogins());
        assertEquals(false, server.getConnectionConfig()
                .isAnonymousLoginEnabled());
        assertEquals(123, server.getConnectionConfig().getMaxAnonymousLogins());
        assertEquals(124, server.getConnectionConfig().getMaxLoginFailures());
        assertEquals(125, server.getConnectionConfig().getLoginFailureDelay());

        Map<String, Listener> listeners = server.getServerContext()
                .getListeners();
        assertEquals(3, listeners.size());

        Listener listener = listeners.get("listener0");
        assertNotNull(listener);
        assertTrue(listener instanceof NioListener);
        assertEquals(2222, ((NioListener) listener).getPort());
        assertEquals(InetAddress.getByName("1.2.3.4"), ((NioListener) listener)
                .getServerAddress());
        assertEquals(true, ((NioListener) listener)
                .getDataConnectionConfiguration().isActiveEnabled());
        assertEquals(InetAddress.getByName("1.2.3.4"), ((NioListener) listener)
                .getDataConnectionConfiguration().getActiveLocalAddress());
        assertEquals("123-125", ((NioListener) listener)
                .getDataConnectionConfiguration().getPassivePorts());

        List<Subnet> subnets = ((NioListener) listener).getBlockedSubnets();
        assertEquals(3, subnets.size());
        assertEquals(new Subnet(InetAddress.getByName("1.2.3.0"), 16), subnets
                .get(0));
        assertEquals(new Subnet(InetAddress.getByName("1.2.4.0"), 16), subnets
                .get(1));
        assertEquals(new Subnet(InetAddress.getByName("1.2.3.4"), 32), subnets
                .get(2));

        DefaultSslConfiguration ssl = (DefaultSslConfiguration) listener
                .getSslConfiguration();
        assertEquals(new File("/tmp/tmp.jks"), ssl.getKeystoreFile());
        assertEquals("password", ssl.getKeystorePassword());

        // make sure the data connection got the same config
        ssl = (DefaultSslConfiguration) listener
                .getDataConnectionConfiguration().getSslConfiguration();
        assertEquals(new File("/tmp/tmp.jks"), ssl.getKeystoreFile());
        assertEquals("password", ssl.getKeystorePassword());

        listener = listeners.get("listener1");
        assertNotNull(listener);
        assertTrue(listener instanceof NioListener);
        assertEquals(2223, ((NioListener) listener).getPort());

        listener = listeners.get("listener2");
        assertNotNull(listener);
        assertTrue(listener instanceof NioListener);
        assertEquals(2224, ((NioListener) listener).getPort());

        DefaultCommandFactory cf = (DefaultCommandFactory) server
                .getServerContext().getCommandFactory();
        assertEquals(2, cf.getCommandMap().size());
        assertTrue(cf.getCommand("FOO") instanceof HELP);
        assertTrue(cf.getCommand("FOO2") instanceof STAT);

        String[] languages = server.getServerContext().getMessageResource()
                .getAvailableLanguages();

        assertEquals(3, languages.length);
        assertEquals("se", languages[0]);
        assertEquals("no", languages[1]);
        assertEquals("da", languages[2]);
        
        NativeFileSystemManager fs = (NativeFileSystemManager) server.getFileSystem();
        assertTrue(fs.isCreateHome());
        assertTrue(fs.isCaseInsensitive());
    }
}
