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

import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.ftpserver.test.TestUtil;


public class PasvTest extends ClientTestTemplate {
    
    protected boolean isConnectClient() {
        return false;
    }

    /**
     * This tests that the correct IP is returned, that is
     * the IP that the client has connected to.
     * 
     * Note that this test will only work if you got more than one 
     * NIC and the server is allowed to listen an all NICs
     */
    public void testPasvIp() throws Exception {
        String[] ips = TestUtil.getHostAddresses();
        
        for (int i = 0; i < ips.length; i++) {

            String ip = ips[i];
            String ftpIp = ip.replace('.', ',');
            
            try{
                client.connect(ip, port);
            } catch(FTPConnectionClosedException e) {
                // try again
                Thread.sleep(200);
                client.connect(ip, port);
            }
            client.login(ADMIN_USERNAME, ADMIN_PASSWORD);
            client.pasv();
            
            assertTrue(client.getReplyString().indexOf(ftpIp) > -1 );
            
            client.quit();
            client.disconnect();
        }
    }
}
