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

import org.apache.ftpserver.impl.DefaultFtpServer;
import org.apache.ftpserver.usermanager.impl.ClearTextPasswordEncryptor;
import org.apache.ftpserver.usermanager.impl.Md5PasswordEncryptor;
import org.apache.ftpserver.usermanager.impl.PropertiesUserManager;
import org.apache.ftpserver.usermanager.impl.SaltedPasswordEncryptor;

/**
*
* @author The Apache MINA Project (dev@mina.apache.org)
* @version $Rev$, $Date$
*
*/
public class FileUserManagerConfigTest extends SpringConfigTestTemplate {

    private PropertiesUserManager createPropertiesUserManager(String config) {
        DefaultFtpServer server = createServer(config);

        return (PropertiesUserManager) server.getUserManager();
    }

    public void testFile() throws Throwable {
        PropertiesUserManager um = createPropertiesUserManager("<file-user-manager file=\"/tmp/foo.users\" />");
        assertEquals(new File("/tmp/foo.users"), um.getFile());
    }

    public void testMd5PasswordEncryptor() throws Throwable {
        PropertiesUserManager um = createPropertiesUserManager("<file-user-manager file=\"foo\" encrypt-passwords=\"md5\" />");

        assertTrue(um.getPasswordEncryptor() instanceof Md5PasswordEncryptor);
    }
    
    public void testTruePasswordEncryptor() throws Throwable {
        PropertiesUserManager um = createPropertiesUserManager("<file-user-manager file=\"foo\" encrypt-passwords=\"true\" />");

        assertTrue(um.getPasswordEncryptor() instanceof Md5PasswordEncryptor);
    }

    public void testNonePasswordEncryptor() throws Throwable {
        PropertiesUserManager um = createPropertiesUserManager("<file-user-manager file=\"foo\" encrypt-passwords=\"clear\" />");

        assertTrue(um.getPasswordEncryptor() instanceof ClearTextPasswordEncryptor);
    }

    public void testSaltedPasswordEncryptor() throws Throwable {
        PropertiesUserManager um = createPropertiesUserManager("<file-user-manager file=\"foo\" encrypt-passwords=\"salted\" />");

        assertTrue(um.getPasswordEncryptor() instanceof SaltedPasswordEncryptor);
    }
    
    public void testFalsePasswordEncryptor() throws Throwable {
        PropertiesUserManager um = createPropertiesUserManager("<file-user-manager file=\"foo\" encrypt-passwords=\"false\" />");

        assertTrue(um.getPasswordEncryptor() instanceof ClearTextPasswordEncryptor);
    }

}
