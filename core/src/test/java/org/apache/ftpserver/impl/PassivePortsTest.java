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

package org.apache.ftpserver.impl;

import junit.framework.TestCase;

/**
*
* @author <a href="http://mina.apache.org">Apache MINA Project</a>*
*/
public class PassivePortsTest extends TestCase {

    public void testParseSingleValue() {
        PassivePorts ports = new PassivePorts("123", false);

        assertEquals(123, ports.reserveNextPort());
        assertEquals(-1, ports.reserveNextPort());
    }

    public void testParseMaxValue() {
        PassivePorts ports = new PassivePorts("65535", false);

        assertEquals(65535, ports.reserveNextPort());
        assertEquals(-1, ports.reserveNextPort());
    }

    public void testParseMinValue() {
        PassivePorts ports = new PassivePorts("0", false);

        assertEquals(0, ports.reserveNextPort());
        assertEquals(0, ports.reserveNextPort());
        assertEquals(0, ports.reserveNextPort());
        assertEquals(0, ports.reserveNextPort());
        // should return 0 forever
    }

    public void testParseTooLargeValue() {
        try {
            new PassivePorts("65536", false);
            fail("Must fail due to too high port number");
        } catch (IllegalArgumentException e) {
            // ok
        }
    }

    public void testParseNonNumericValue() {
        try {
            new PassivePorts("foo", false);
            fail("Must fail due to non numerical port number");
        } catch (IllegalArgumentException e) {
            // ok
        }
    }

    public void testParseListOfValues() {
        PassivePorts ports = new PassivePorts("123, 456,\t\n789", false);

        assertEquals(123, ports.reserveNextPort());
        assertEquals(456, ports.reserveNextPort());
        assertEquals(789, ports.reserveNextPort());
        assertEquals(-1, ports.reserveNextPort());
    }

    public void testParseListOfValuesOrder() {
        PassivePorts ports = new PassivePorts("123, 789, 456", false);

        assertEquals(123, ports.reserveNextPort());
        assertEquals(789, ports.reserveNextPort());
        assertEquals(456, ports.reserveNextPort());
        assertEquals(-1, ports.reserveNextPort());
    }

    public void testParseListOfValuesDuplicate() {
        PassivePorts ports = new PassivePorts("123, 789, 456, 789", false);

        assertEquals(123, ports.reserveNextPort());
        assertEquals(789, ports.reserveNextPort());
        assertEquals(456, ports.reserveNextPort());
        assertEquals(-1, ports.reserveNextPort());
    }

    public void testParseSimpleRange() {
        PassivePorts ports = new PassivePorts("123-125", false);

        assertEquals(123, ports.reserveNextPort());
        assertEquals(124, ports.reserveNextPort());
        assertEquals(125, ports.reserveNextPort());
        assertEquals(-1, ports.reserveNextPort());
    }

    public void testParseMultipleRanges() {
        PassivePorts ports = new PassivePorts("123-125, 127-128, 130-132", false);

        assertEquals(123, ports.reserveNextPort());
        assertEquals(124, ports.reserveNextPort());
        assertEquals(125, ports.reserveNextPort());
        assertEquals(127, ports.reserveNextPort());
        assertEquals(128, ports.reserveNextPort());
        assertEquals(130, ports.reserveNextPort());
        assertEquals(131, ports.reserveNextPort());
        assertEquals(132, ports.reserveNextPort());
        assertEquals(-1, ports.reserveNextPort());
    }

    public void testParseMixedRangeAndSingle() {
        PassivePorts ports = new PassivePorts("123-125, 126, 128-129", false);

        assertEquals(123, ports.reserveNextPort());
        assertEquals(124, ports.reserveNextPort());
        assertEquals(125, ports.reserveNextPort());
        assertEquals(126, ports.reserveNextPort());
        assertEquals(128, ports.reserveNextPort());
        assertEquals(129, ports.reserveNextPort());
        assertEquals(-1, ports.reserveNextPort());
    }

    public void testParseOverlapingRanges() {
        PassivePorts ports = new PassivePorts("123-125, 124-126", false);

        assertEquals(123, ports.reserveNextPort());
        assertEquals(124, ports.reserveNextPort());
        assertEquals(125, ports.reserveNextPort());
        assertEquals(126, ports.reserveNextPort());
        assertEquals(-1, ports.reserveNextPort());
    }

    public void testParseOverlapingRangesorder() {
        PassivePorts ports = new PassivePorts("124-126, 123-125", false);

        assertEquals(124, ports.reserveNextPort());
        assertEquals(125, ports.reserveNextPort());
        assertEquals(126, ports.reserveNextPort());
        assertEquals(123, ports.reserveNextPort());
        assertEquals(-1, ports.reserveNextPort());
    }

    public void testParseOpenLowerRange() {
        PassivePorts ports = new PassivePorts("9, -3", false);

        assertEquals(9, ports.reserveNextPort());
        assertEquals(1, ports.reserveNextPort());
        assertEquals(2, ports.reserveNextPort());
        assertEquals(3, ports.reserveNextPort());
        assertEquals(-1, ports.reserveNextPort());
    }

    public void testParseOpenUpperRange() {
        PassivePorts ports = new PassivePorts("65533-", false);

        assertEquals(65533, ports.reserveNextPort());
        assertEquals(65534, ports.reserveNextPort());
        assertEquals(65535, ports.reserveNextPort());
        assertEquals(-1, ports.reserveNextPort());
    }

    public void testParseOpenUpperRange3() {
        PassivePorts ports = new PassivePorts("65533-, 65532-", false);

        assertEquals(65533, ports.reserveNextPort());
        assertEquals(65534, ports.reserveNextPort());
        assertEquals(65535, ports.reserveNextPort());
        assertEquals(65532, ports.reserveNextPort());
        assertEquals(-1, ports.reserveNextPort());
    }

    public void testParseOpenUpperRange2() {
        PassivePorts ports = new PassivePorts("65533-, 1", false);

        assertEquals(65533, ports.reserveNextPort());
        assertEquals(65534, ports.reserveNextPort());
        assertEquals(65535, ports.reserveNextPort());
        assertEquals(1, ports.reserveNextPort());
        assertEquals(-1, ports.reserveNextPort());
    }

    public void testParseRelease() {
        PassivePorts ports = new PassivePorts("123, 456,789", false);

        assertEquals(123, ports.reserveNextPort());
        assertEquals(456, ports.reserveNextPort());
        ports.releasePort(456);
        assertEquals(456, ports.reserveNextPort());

        assertEquals(789, ports.reserveNextPort());
        assertEquals(-1, ports.reserveNextPort());
    }

}
