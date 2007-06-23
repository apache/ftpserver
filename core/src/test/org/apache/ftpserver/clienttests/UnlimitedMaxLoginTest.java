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

import java.util.Properties;

import org.apache.commons.net.ftp.FTPConnectionClosedException;


public class UnlimitedMaxLoginTest extends ClientTestTemplate {
    private static final String UNKNOWN_USERNAME = "foo";
    private static final String UNKNOWN_PASSWORD = "bar";

    
    
    protected Properties createConfig() {
        Properties config = super.createConfig();
        
        config.setProperty("config.connection-manager.max-login-failures", "0");
        return config;
    }



    public void testLogin() throws Exception {
        // must never be disconnected
        assertFalse(client.login(UNKNOWN_USERNAME, UNKNOWN_PASSWORD));
        assertFalse(client.login(UNKNOWN_USERNAME, UNKNOWN_PASSWORD));
        assertFalse(client.login(UNKNOWN_USERNAME, UNKNOWN_PASSWORD));
        assertFalse(client.login(UNKNOWN_USERNAME, UNKNOWN_PASSWORD));
        assertFalse(client.login(UNKNOWN_USERNAME, UNKNOWN_PASSWORD));
        assertFalse(client.login(UNKNOWN_USERNAME, UNKNOWN_PASSWORD));
        assertFalse(client.login(UNKNOWN_USERNAME, UNKNOWN_PASSWORD));
    }
}
