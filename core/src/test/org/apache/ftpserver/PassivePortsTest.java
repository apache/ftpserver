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


public class PassivePortsTest extends TestCase {

    public void testParseSingleValue() {
        PassivePorts ports = PassivePorts.parse("123");
        
        assertEquals(123, ports.reserveNextPort());
        assertEquals(-1, ports.reserveNextPort());
    }

    public void testParseListOfValues() {
        PassivePorts ports = PassivePorts.parse("123, 456,789");

        
        assertEquals(123, ports.reserveNextPort());
        assertEquals(456, ports.reserveNextPort());
        assertEquals(789, ports.reserveNextPort());
        assertEquals(-1, ports.reserveNextPort());
    }

    public void testParseRelease() {
        PassivePorts ports = PassivePorts.parse("123, 456,789");
        
        
        assertEquals(123, ports.reserveNextPort());
        assertEquals(456, ports.reserveNextPort());
        ports.releasePort(456);
        assertEquals(456, ports.reserveNextPort());
        
        assertEquals(789, ports.reserveNextPort());
        assertEquals(-1, ports.reserveNextPort());
    }
    
}
