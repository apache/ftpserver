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

package org.apache.ftpserver.config;

import java.util.Iterator;

import junit.framework.TestCase;

import org.apache.ftpserver.ftplet.Configuration;
import org.apache.ftpserver.ftplet.FtpException;

public abstract class ConfigurationTest extends TestCase {

    protected Configuration config;

    protected abstract Configuration createConfiguration() throws Exception;
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        config = createConfiguration();
    }

    public void testBaseConfigKeys() throws Exception {
        Iterator keys = config.getKeys();
        
        assertEquals("socket-factory", keys.next());
        assertEquals("empty", keys.next());
        assertFalse(keys.hasNext());
    }

    public void testIsEmpty() throws Exception {
        assertFalse(config.isEmpty());
        assertTrue(config.subset("empty").isEmpty());
    }

    public void testGetBoolean() throws Exception {
        Configuration config = this.config.subset("socket-factory");
        
        assertTrue(config.getBoolean("booltrue"));
        assertFalse(config.getBoolean("boolfalse"));
        
        // non boolean value, should return false
        assertFalse(config.getBoolean("address"));
        
        // missing parameter, must throw exception
        try{
            config.getBoolean("dummy");
            fail("Must throw FtpException");
        } catch(FtpException e) {
            // OK
        }

        assertTrue(config.getBoolean("dummy", true));
        assertTrue(config.getBoolean("booltrue", false));
    }
    
    public void testGetInt() throws Exception {
        Configuration config = this.config.subset("socket-factory");

        assertEquals(21, config.getInt("port"));
        assertEquals(123, config.getInt("dummy", 123));
        assertEquals(21, config.getInt("port", 123));
        
        // non integer value, must throw exception
        try{
            config.getInt("address");
            fail("Must throw FtpException");
        } catch(FtpException e) {
            // OK
        }

        // missing parameter, must throw exception
        try{
            config.getInt("dummy");
            fail("Must throw FtpException");
        } catch(FtpException e) {
            // OK
        }
    }

    public void testGetLong() throws Exception {
        Configuration config = this.config.subset("socket-factory");
        
        assertEquals(21, config.getLong("port"));
        assertEquals(123, config.getLong("dummy", 123));
        assertEquals(21, config.getLong("port", 123));
        
        // non long value, must throw exception
        try{
            config.getLong("address");
            fail("Must throw FtpException");
        } catch(FtpException e) {
            // OK
        }
        
        // missing parameter, must throw exception
        try{
            config.getLong("dummy");
            fail("Must throw FtpException");
        } catch(FtpException e) {
            // OK
        }
    }
    
    public void testGetString() throws Exception {
        Configuration config = this.config.subset("socket-factory");

        assertEquals("localhost", config.getString("address"));
        assertEquals("localhost", config.getString("address", "foo"));
        assertEquals("localhost", config.getString("dummy", "localhost"));
        
        // missing parameter, must throw exception
        try{
            config.getString("dummy");
            fail("Must throw FtpException");
        } catch(FtpException e) {
            // OK
        }
    }

    public void testGetDouble() throws Exception {
        Configuration config = this.config.subset("socket-factory");
        
        assertEquals(21D, config.getDouble("port"), 0.001);
        assertEquals(123D, config.getDouble("dummy", 123), 0.001);
        assertEquals(21D, config.getDouble("port", 123), 0.001);
        assertEquals(1.234, config.getDouble("double"), 0.001);
        
        // non double value, must throw exception
        try{
            config.getDouble("address");
            fail("Must throw FtpException");
        } catch(FtpException e) {
            // OK
        }
        
        // missing parameter, must throw exception
        try{
            config.getDouble("dummy");
            fail("Must throw FtpException");
        } catch(FtpException e) {
            // OK
        }
    }
    
    public void testSubset() throws Exception {
        Configuration subConfig = config.subset("socket-factory");
        assertEquals("localhost", subConfig.getString("address"));
        
        Configuration subSubConfig = subConfig.subset("ssl");
        assertEquals("TLS", subSubConfig.getString("ssl-protocol"));
    }

    public void testGetStringForSubConfig() {
        Configuration subConfig = config.subset("socket-factory");
        try{
            subConfig.getString("ssl");
            fail("Must throw FtpException");
        } catch(FtpException e) {
            // OK
        }
    }

}
