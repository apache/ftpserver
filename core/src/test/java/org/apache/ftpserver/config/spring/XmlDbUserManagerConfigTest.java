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

import junit.framework.TestCase;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.usermanager.DbUserManager;
import org.hsqldb.jdbc.jdbcDataSource;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.FileSystemResource;

/**
*
* @author The Apache MINA Project (dev@mina.apache.org)
* @version $Rev$, $Date$
*
*/
public class XmlDbUserManagerConfigTest extends TestCase {

    public void test() throws Throwable {
        XmlBeanFactory factory = new XmlBeanFactory(
                new FileSystemResource(
                        "src/test/resources/spring-config/config-spring-db-user-manager.xml"));

        FtpServer server = (FtpServer) factory.getBean("server");

        DbUserManager um = (DbUserManager) server.getServerContext()
                .getUserManager();
        assertTrue(um.getDataSource() instanceof jdbcDataSource);

        assertEquals("INSERT USER", um.getSqlUserInsert());
        assertEquals("UPDATE USER", um.getSqlUserUpdate());
        assertEquals("DELETE USER", um.getSqlUserDelete());
        assertEquals("SELECT USER", um.getSqlUserSelect());
        assertEquals("SELECT ALL USERS", um.getSqlUserSelectAll());
        assertEquals("IS ADMIN", um.getSqlUserAdmin());
        assertEquals("AUTHENTICATE", um.getSqlUserAuthenticate());

    }
}
