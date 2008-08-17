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

package org.apache.ftpserver;

import junit.framework.TestCase;

/**
*
* @author The Apache MINA Project (dev@mina.apache.org)
* @version $Rev$, $Date$
*
*/
public class FtpRequestImplTest extends TestCase {

    public void testCommandOnly() {
        FtpRequestImpl request = new FtpRequestImpl("foo");

        assertEquals("foo", request.getRequestLine());
        assertEquals("FOO", request.getCommand());
        assertFalse(request.hasArgument());
        assertNull(request.getArgument());
    }

    public void testCommandWithLeadingWhitespace() {
        FtpRequestImpl request = new FtpRequestImpl("\rfoo");

        assertEquals("foo", request.getRequestLine());
        assertEquals("FOO", request.getCommand());
        assertFalse(request.hasArgument());
        assertNull(request.getArgument());
    }

    public void testCommandWithTrailingWhitespace() {
        FtpRequestImpl request = new FtpRequestImpl("foo\r");

        assertEquals("foo", request.getRequestLine());
        assertEquals("FOO", request.getCommand());
        assertFalse(request.hasArgument());
        assertNull(request.getArgument());
    }

    public void testCommandAndSingleArgument() {
        FtpRequestImpl request = new FtpRequestImpl("foo bar");

        assertEquals("foo bar", request.getRequestLine());
        assertEquals("FOO", request.getCommand());
        assertTrue(request.hasArgument());
        assertEquals("bar", request.getArgument());
    }

    public void testCommandAndMultipleArguments() {
        FtpRequestImpl request = new FtpRequestImpl("foo bar baz");

        assertEquals("foo bar baz", request.getRequestLine());
        assertEquals("FOO", request.getCommand());
        assertTrue(request.hasArgument());
        assertEquals("bar baz", request.getArgument());
    }
}
