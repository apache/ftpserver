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

package org.apache.ftpserver.util;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URL;

import junit.framework.TestCase;

public class ClassUtilsCastTest extends TestCase {
    public void testCastToInt() {
        assertEquals(new Integer(123), ClassUtils.cast(Integer.TYPE, "123"));
        assertEquals(new Integer(123), ClassUtils.cast(Integer.class, "123"));
        
        try {
            ClassUtils.cast(Integer.class, "foo");
            fail("Must throw exception");
        } catch(NumberFormatException e) {
            // ok
        }
    }
    
    public void testCastToLong() {
        assertEquals(new Long(123), ClassUtils.cast(Long.TYPE, "123"));
        assertEquals(new Long(123), ClassUtils.cast(Long.class, "123"));
        
        try {
            ClassUtils.cast(Long.class, "foo");
            fail("Must throw exception");
        } catch(NumberFormatException e) {
            // ok
        }
    }
    
    public void testCastToFloat() {
        assertEquals(new Float(123), ClassUtils.cast(Float.TYPE, "123"));
        assertEquals(new Float(123), ClassUtils.cast(Float.class, "123"));
        assertEquals(new Float(1.23), ClassUtils.cast(Float.TYPE, "1.23"));
        assertEquals(new Float(1.23), ClassUtils.cast(Float.class, "1.23"));
        
        try {
            ClassUtils.cast(Float.class, "foo");
            fail("Must throw exception");
        } catch(NumberFormatException e) {
            // ok
        }
    }

    public void testCastToDouble() {
        assertEquals(new Double(123), ClassUtils.cast(Double.TYPE, "123"));
        assertEquals(new Double(123), ClassUtils.cast(Double.class, "123"));
        assertEquals(new Double(1.23), ClassUtils.cast(Double.TYPE, "1.23"));
        assertEquals(new Double(1.23), ClassUtils.cast(Double.class, "1.23"));
        
        try {
            ClassUtils.cast(Double.class, "foo");
            fail("Must throw exception");
        } catch(NumberFormatException e) {
            // ok
        }
    }
    
    public void testCastToByte() {
        assertEquals(new Byte("3"), ClassUtils.cast(Byte.TYPE, "3"));
        assertEquals(new Byte("3"), ClassUtils.cast(Byte.class, "3"));

        try {
            ClassUtils.cast(Byte.class, "foo");
            fail("Must throw exception");
        } catch(NumberFormatException e) {
            // ok
        }
    }
    
    public void testCastToBigDecimal() {
        assertEquals(new BigDecimal("1.23"), ClassUtils.cast(BigDecimal.class, "1.23"));
        
        try {
            ClassUtils.cast(BigDecimal.class, "foo");
            fail("Must throw exception");
        } catch(NumberFormatException e) {
            // ok
        }
    }
    
    public void testCastToBigInteger() {
        assertEquals(new BigInteger("123"), ClassUtils.cast(BigInteger.class, "123"));
        
        try {
            ClassUtils.cast(BigInteger.class, "foo");
            fail("Must throw exception");
        } catch(NumberFormatException e) {
            // ok
        }
    }

    public void testCastToChar() {
        assertEquals(new Character('a'), ClassUtils.cast(Character.TYPE, "a"));
        assertEquals(new Character('a'), ClassUtils.cast(Character.class, "a"));
        
        try {
            ClassUtils.cast(Character.class, "foo");
            fail("Must throw exception");
        } catch(RuntimeException e) {
            // ok
        }
    }

    public void testCastToBoolean() {
        assertEquals(Boolean.TRUE, ClassUtils.cast(Boolean.TYPE, "true"));
        assertEquals(Boolean.TRUE, ClassUtils.cast(Boolean.class, "true"));
        assertEquals(Boolean.FALSE, ClassUtils.cast(Boolean.TYPE, "false"));
        assertEquals(Boolean.FALSE, ClassUtils.cast(Boolean.class, "false"));
        assertEquals(Boolean.FALSE, ClassUtils.cast(Boolean.class, "foo"));
    }
    
    public void testCastToURL() throws Exception {
        assertEquals(new URL("http://localhost"), ClassUtils.cast(URL.class, "http://localhost"));
        
        try {
            ClassUtils.cast(URL.class, "foo://foo://foo");
            fail("Must throw exception");
        } catch(RuntimeException e) {
            // ok
        }
    }
    
    public void testCastToFile() throws Exception {
        assertEquals(new File("foo"), ClassUtils.cast(File.class, "foo"));
    }
    
    public void testCastToInetAddress() throws Exception {
        assertEquals(InetAddress.getByName("localhost"), ClassUtils.cast(InetAddress.class, "localhost"));
        assertEquals(InetAddress.getByName("1.2.3.4"), ClassUtils.cast(InetAddress.class, "1.2.3.4"));
        
        try {
            ClassUtils.cast(InetAddress.class, "1.2.3.4.5");
            fail("Must throw exception");
        } catch(RuntimeException e) {
            // ok
        }
    }
}