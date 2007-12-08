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

import java.io.File;
import java.io.FileInputStream;

import org.apache.ftpserver.ftplet.Configuration;
import org.apache.ftpserver.test.TestUtil;

public class XmlConfigurationTest extends ConfigurationTestTemplate {

    protected Configuration createConfiguration() throws Exception {
        File xmlFile = new File(TestUtil.getBaseDir(), "src/test/resources/XmlConfigurationHandler-test.xml");
        FileInputStream fis = new FileInputStream(xmlFile);
        XmlConfigurationHandler handler = new XmlConfigurationHandler(fis);
        
        return handler.parse();
        
    }
}
