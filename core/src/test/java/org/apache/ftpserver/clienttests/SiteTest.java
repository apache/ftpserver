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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.net.ftp.FTPReply;

/**
*
* @author The Apache MINA Project (dev@mina.apache.org)
* @version $Rev$, $Date$
*
*/
public class SiteTest extends ClientTestTemplate {

    public void testSiteDescUser() throws Exception {
        client.login(ADMIN_USERNAME, ADMIN_PASSWORD);

        client.sendCommand("SITE DESCUSER admin");
        String[] siteReplies = client.getReplyString().split("\r\n");

        assertEquals("200-", siteReplies[0]);
        assertEquals("userid          : admin", siteReplies[1]);
        assertEquals("userpassword    : ********", siteReplies[2]);
        assertEquals("homedirectory   : ./test-tmp/ftproot", siteReplies[3]);
        assertEquals("writepermission : true", siteReplies[4]);
        assertEquals("enableflag      : true", siteReplies[5]);
        assertEquals("idletime        : 0", siteReplies[6]);
        assertEquals("uploadrate      : 0", siteReplies[7]);
        assertEquals("200 downloadrate    : 0", siteReplies[8]);
    }

    public void testAnonNotAllowed() throws Exception {
        client.login(ANONYMOUS_USERNAME, ANONYMOUS_PASSWORD);

        assertTrue(FTPReply.isNegativePermanent(client.sendCommand("SITE DESCUSER admin")));
    }

    public void testSiteWho() throws Exception {
        client.login(ADMIN_USERNAME, ADMIN_PASSWORD);

        client.sendCommand("SITE WHO");
        String[] siteReplies = client.getReplyString().split("\r\n");

        assertEquals("200-", siteReplies[0]);
        
        String timestampPattern = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}";
        System.out.println(">>" + siteReplies[1] + "<<");
        
        String pattern = "200 admin           127.0.0.1       " + timestampPattern + " " + timestampPattern + " "; 
        
        assertTrue(Pattern.matches(pattern, siteReplies[1]));
    }


    
}
