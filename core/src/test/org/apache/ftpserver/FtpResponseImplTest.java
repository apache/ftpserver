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


public class FtpResponseImplTest extends TestCase {

    public void testSingleLineToString() {
        DefaultFtpReply response = new DefaultFtpReply(123, "foo bar");
        
        assertEquals("123 foo bar\r\n", response.toString());
    }

    public void testNullToString() {
        DefaultFtpReply response = new DefaultFtpReply(123, null);
        
        assertEquals("123 \r\n", response.toString());
    }

    public void testMultipleLinesToString() {
        DefaultFtpReply response = new DefaultFtpReply(123, "foo\nbar\nbaz");
        
        assertEquals("123-foo\r\nbar\r\n123 baz\r\n", response.toString());
    }

    public void testMultipleLinesToString1() {
        DefaultFtpReply response = new DefaultFtpReply(123, "\nfoo\nbar\nbaz");
        
        assertEquals("123-\r\nfoo\r\nbar\r\n123 baz\r\n", response.toString());
    }
}
