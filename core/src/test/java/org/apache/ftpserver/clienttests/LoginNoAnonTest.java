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

import org.apache.ftpserver.DefaultConnectionConfig;


public class LoginNoAnonTest extends ClientTestTemplate {

    /* (non-Javadoc)
     * @see org.apache.ftpserver.clienttests.ClientTestTemplate#createConfig()
     */
    protected Properties createConfig() {
        Properties config = super.createDefaultConfig();

        config.setProperty("config.connection-config.class", DefaultConnectionConfig.class.getName());
        config.setProperty("config.connection-config.anonymous-login-enabled", "false");

        return config;
    }



    public void testLoginWithAnon() throws Exception {
        assertFalse(client.login(ANONYMOUS_USERNAME, ANONYMOUS_PASSWORD));
    }

}
