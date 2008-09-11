package org.apache.ftpserver.examples;

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

import java.io.File;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.ssl.impl.DefaultSslConfiguration;

public class EmbeddingFtpServer {

    public static void main(String[] args) throws Exception {
        FtpServer server = new FtpServer();
        // set the port of the default listener
        server.getListener("default").setPort(2221);

        // define SSL configuration
        DefaultSslConfiguration ssl = new DefaultSslConfiguration();
        ssl.setKeystoreFile(new File("src/test/resources/ftpserver.jks"));
        ssl.setKeystorePassword("password");

        // set the SSL configuration for the listener
        server.getListener("default").setSslConfiguration(ssl);
        server.getListener("default").setImplicitSsl(true);

        // start the server
        server.start();
    }
}
