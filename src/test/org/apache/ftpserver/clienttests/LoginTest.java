/*
 * Copyright 2006 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ftpserver.clienttests;

import org.apache.commons.net.ftp.FTPReply;

public class LoginTest extends ClientTestTemplate {
    public void testLogin() throws Exception {
        assertTrue(client.login("admin", "admin"));
    }
    
    public void testLoginIncorrectPassword() throws Exception {
        assertFalse(client.login("admin", "foo"));
    }

    public void testReLogin() throws Exception {
        assertFalse(client.login("admin", "foo"));
        assertTrue(client.login("admin", "admin"));
    }

    public void testReLoginWithOnlyPass() throws Exception {
        assertFalse(client.login("admin", "foo"));
        
        int reply = client.pass("admin");
        assertTrue(FTPReply.isNegativePermanent(reply));
    }

    public void testLoginUnknownUser() throws Exception {
        assertFalse(client.login("foo", "foo"));
    }


}
