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

package org.apache.ftpserver.clienttests;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.listener.nio.NioListener;
import org.apache.mina.filter.firewall.Subnet;

/**
*
* @author The Apache MINA Project (dev@mina.apache.org)
* @version $Rev$, $Date$
*
*/
public class SubnetBlacklistTest extends ClientTestTemplate {
    protected FtpServer createServer() throws Exception {
        FtpServer server = super.createServer();

        ListenerFactory factory = new ListenerFactory(server.getListener("default"));

        List<Subnet> blockedSubnets = new ArrayList<Subnet>();
        blockedSubnets.add(new Subnet(InetAddress.getByName("localhost"), 32));

        factory.setBlockedSubnets(blockedSubnets);

        server.addListener("default", factory.createListener());
        
        return server;
    }

    protected boolean isConnectClient() {
        return false;
    }

    public void testConnect() throws Exception {
        try {
            client.connect("localhost", port);
            fail("Must throw");
        } catch (FTPConnectionClosedException e) {
            // OK
        }
    }
}
