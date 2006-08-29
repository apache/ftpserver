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

package org.apache.ftpserver.config;

import java.util.Properties;

import org.apache.ftpserver.ftplet.Configuration;

public class PropertiesConfigurationTest extends ConfigurationTestTemplate {

    protected Configuration createConfiguration() throws Exception {
        Properties properties = new Properties();
        properties.setProperty("config.socket-factory.address", "localhost");
        properties.setProperty("config.socket-factory.booltrue", "true");
        properties.setProperty("config.socket-factory.boolfalse", "false");
        properties.setProperty("config.socket-factory.port", "21");
        properties.setProperty("config.socket-factory.double", "1.234");
        properties.setProperty("config.socket-factory.ssl.ssl-protocol", "TLS");
        properties.setProperty("config.socket-factory.ssl.client-authentication", "false");
        properties.setProperty("config.empty", "");

        return new PropertiesConfiguration(properties);
    }
    

}
