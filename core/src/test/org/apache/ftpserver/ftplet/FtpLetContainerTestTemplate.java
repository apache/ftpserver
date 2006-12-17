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

package org.apache.ftpserver.ftplet;

import junit.framework.TestCase;


public abstract class FtpLetContainerTestTemplate extends TestCase {

    private FtpletContainer container = createFtpletContainer();
    
    public void testAddAndGetFtplet() {
        MockFtplet ftplet1 = new MockFtplet();
        MockFtplet ftplet2 = new MockFtplet();
        
        assertNull(container.getFtplet("ftplet1"));
        assertNull(container.getFtplet("ftplet2"));
        
        container.addFtplet("ftplet1", ftplet1);
        container.addFtplet("ftplet2", ftplet2);
        
        assertSame(ftplet1, container.getFtplet("ftplet1"));
        assertSame(ftplet2, container.getFtplet("ftplet2"));
    }
    
    public void testAddFtpletWithDuplicateName() {
        MockFtplet ftplet1 = new MockFtplet();
        MockFtplet ftplet2 = new MockFtplet();
        
        assertNull(container.getFtplet("ftplet1"));
        
        container.addFtplet("ftplet1", ftplet1);
        
        try {
            container.addFtplet("ftplet1", ftplet2);
            fail("IllegalArgumentException must be thrown");
        } catch(IllegalArgumentException e) {
            // ok
        }
        
        assertSame(ftplet1, container.getFtplet("ftplet1"));
    }

    public void testRemoveFtplet() {
        MockFtplet ftplet1 = new MockFtplet();
        MockFtplet ftplet2 = new MockFtplet();
        
        container.addFtplet("ftplet1", ftplet1);
        container.addFtplet("ftplet2", ftplet2);
        
        assertSame(ftplet1, container.getFtplet("ftplet1"));
        assertSame(ftplet2, container.getFtplet("ftplet2"));
        
        assertSame(ftplet1, container.removeFtplet("ftplet1"));
        
        assertNull(container.getFtplet("ftplet1"));
        assertSame(ftplet2, container.getFtplet("ftplet2"));
        
        assertNull(container.removeFtplet("ftplet1"));
    }
    
    protected abstract FtpletContainer createFtpletContainer();

}
