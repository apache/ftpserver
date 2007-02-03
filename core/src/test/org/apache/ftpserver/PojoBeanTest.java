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

import java.util.Properties;

import junit.framework.TestCase;

import org.apache.commons.logging.LogFactory;
import org.apache.ftpserver.config.PropertiesConfiguration;
import org.apache.ftpserver.ftplet.Configuration;
import org.apache.ftpserver.ftplet.FtpException;


public class PojoBeanTest extends TestCase {

    public static class MockPojo {

        public Configuration config;
        public boolean configured = false;
        public boolean disposed = false;
        public LogFactory logFactory;
        
        public String foo;
        public int bar;
        
        public void setBar(int bar) {
            this.bar = bar;
        }

        public void setFoo(String foo) {
            this.foo = foo;
        }

        public void configure() throws FtpException {
            logFactory.getInstance(MockPojo.class).debug("test");
            
            configured = true;
        }

        public void dispose() {
            disposed = true;
        }

        public void setLogFactory(LogFactory logFactory) {
            this.logFactory = logFactory;
        }
    }
    
    public void testLifecycle() throws Exception {
        Properties props = new Properties();
        props.setProperty("config.class", MockPojo.class.getName());
        props.setProperty("config.foo", "hello");
        props.setProperty("config.bar", "123");

        PropertiesConfiguration config = new PropertiesConfiguration(props);
        
        LogFactory logFactory = LogFactory.getFactory();
        
        PojoBean bean = (PojoBean) Bean.createBean(config, null, logFactory);
        
        MockPojo pojo = (MockPojo) bean.initBean();
        assertEquals("hello", pojo.foo);
        assertEquals(123, pojo.bar);
        assertFalse(pojo.disposed);
        assertTrue(pojo.configured);
        assertSame(logFactory, pojo.logFactory);

        bean.destroyBean();
        assertTrue(pojo.disposed);
        assertNull(bean.getBean());
    }
    
    /*public void testLifecycleDefaultClass() throws Exception {
        Properties props = new Properties();
        props.setProperty("config.foo", "bar");
        PropertiesConfiguration config = new PropertiesConfiguration(props);
        
        
        ComponentBean bean = (ComponentBean) Bean.createBean(config, MockComponent.class.getName(), null);
        
        MockComponent component = (MockComponent) bean.initBean();
        assertEquals("bar", component.config.getString("foo"));
        assertFalse(component.disposed);
        
        bean.destroyBean();
        assertTrue(component.disposed);
    }*/

}
