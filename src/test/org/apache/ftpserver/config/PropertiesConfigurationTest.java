package org.apache.ftpserver.config;

import java.util.Iterator;
import java.util.Properties;

import junit.framework.TestCase;

public class PropertiesConfigurationTest extends TestCase {

    public void testPropertiesConfigurationProperties() {
        Properties a = new Properties();
        a.setProperty("config.abc", "xyz");

        PropertiesConfiguration configuration = new PropertiesConfiguration(a);
        Iterator keys = configuration.getKeys();

        assertTrue(keys.hasNext());
        assertEquals("abc", keys.next());
    }

}
