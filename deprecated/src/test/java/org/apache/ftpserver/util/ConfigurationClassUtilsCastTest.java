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

public class ConfigurationClassUtilsCastTest extends TestCase {
    public void testCastToInt() {
        assertEquals(new Integer(123), ConfigurationClassUtils.cast(Integer.TYPE, "123"));
        assertEquals(new Integer(123), ConfigurationClassUtils.cast(Integer.class, "123"));
        
        try {
            ConfigurationClassUtils.cast(Integer.class, "foo");
            fail("Must throw exception");
        } catch(NumberFormatException e) {
            // ok
        }
    }
    
    public void testCastToLong() {
        assertEquals(new Long(123), ConfigurationClassUtils.cast(Long.TYPE, "123"));
        assertEquals(new Long(123), ConfigurationClassUtils.cast(Long.class, "123"));
        
        try {
            ConfigurationClassUtils.cast(Long.class, "foo");
            fail("Must throw exception");
        } catch(NumberFormatException e) {
            // ok
        }
    }
    
    public void testCastToFloat() {
        assertEquals(new Float(123), ConfigurationClassUtils.cast(Float.TYPE, "123"));
        assertEquals(new Float(123), ConfigurationClassUtils.cast(Float.class, "123"));
        assertEquals(new Float(1.23), ConfigurationClassUtils.cast(Float.TYPE, "1.23"));
        assertEquals(new Float(1.23), ConfigurationClassUtils.cast(Float.class, "1.23"));
        
        try {
            ConfigurationClassUtils.cast(Float.class, "foo");
            fail("Must throw exception");
        } catch(NumberFormatException e) {
            // ok
        }
    }

    public void testCastToDouble() {
        assertEquals(new Double(123), ConfigurationClassUtils.cast(Double.TYPE, "123"));
        assertEquals(new Double(123), ConfigurationClassUtils.cast(Double.class, "123"));
        assertEquals(new Double(1.23), ConfigurationClassUtils.cast(Double.TYPE, "1.23"));
        assertEquals(new Double(1.23), ConfigurationClassUtils.cast(Double.class, "1.23"));
        
        try {
            ConfigurationClassUtils.cast(Double.class, "foo");
            fail("Must throw exception");
        } catch(NumberFormatException e) {
            // ok
        }
    }
    
    public void testCastToByte() {
        assertEquals(new Byte("3"), ConfigurationClassUtils.cast(Byte.TYPE, "3"));
        assertEquals(new Byte("3"), ConfigurationClassUtils.cast(Byte.class, "3"));

        try {
            ConfigurationClassUtils.cast(Byte.class, "foo");
            fail("Must throw exception");
        } catch(NumberFormatException e) {
            // ok
        }
    }
    
    public void testCastToBigDecimal() {
        assertEquals(new BigDecimal("1.23"), ConfigurationClassUtils.cast(BigDecimal.class, "1.23"));
        
        try {
            ConfigurationClassUtils.cast(BigDecimal.class, "foo");
            fail("Must throw exception");
        } catch(NumberFormatException e) {
            // ok
        }
    }
    
    public void testCastToBigInteger() {
        assertEquals(new BigInteger("123"), ConfigurationClassUtils.cast(BigInteger.class, "123"));
        
        try {
            ConfigurationClassUtils.cast(BigInteger.class, "foo");
            fail("Must throw exception");
        } catch(NumberFormatException e) {
            // ok
        }
    }

    public void testCastToChar() {
        assertEquals(new Character('a'), ConfigurationClassUtils.cast(Character.TYPE, "a"));
        assertEquals(new Character('a'), ConfigurationClassUtils.cast(Character.class, "a"));
        
        try {
            ConfigurationClassUtils.cast(Character.class, "foo");
            fail("Must throw exception");
        } catch(RuntimeException e) {
            // ok
        }
    }

    public void testCastToBoolean() {
        assertEquals(Boolean.TRUE, ConfigurationClassUtils.cast(Boolean.TYPE, "true"));
        assertEquals(Boolean.TRUE, ConfigurationClassUtils.cast(Boolean.class, "true"));
        assertEquals(Boolean.FALSE, ConfigurationClassUtils.cast(Boolean.TYPE, "false"));
        assertEquals(Boolean.FALSE, ConfigurationClassUtils.cast(Boolean.class, "false"));
        assertEquals(Boolean.FALSE, ConfigurationClassUtils.cast(Boolean.class, "foo"));
    }
    
    public void testCastToURL() throws Exception {
        assertEquals(new URL("http://localhost"), ConfigurationClassUtils.cast(URL.class, "http://localhost"));
        
        try {
            ConfigurationClassUtils.cast(URL.class, "foo://foo://foo");
            fail("Must throw exception");
        } catch(RuntimeException e) {
            // ok
        }
    }
    
    public void testCastToFile() throws Exception {
        assertEquals(new File("foo"), ConfigurationClassUtils.cast(File.class, "foo"));
    }
    
    public void testCastToInetAddress() throws Exception {
        assertEquals(InetAddress.getByName("localhost"), ConfigurationClassUtils.cast(InetAddress.class, "localhost"));
        assertEquals(InetAddress.getByName("1.2.3.4"), ConfigurationClassUtils.cast(InetAddress.class, "1.2.3.4"));
        
        try {
            ConfigurationClassUtils.cast(InetAddress.class, "1.2.3.4.5");
            fail("Must throw exception");
        } catch(RuntimeException e) {
            // ok
        }
    }
}