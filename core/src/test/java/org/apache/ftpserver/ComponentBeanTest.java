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

import org.apache.ftpserver.config.PropertiesConfiguration;
import org.apache.ftpserver.ftplet.Component;
import org.apache.ftpserver.ftplet.Configuration;
import org.apache.ftpserver.ftplet.FtpException;


public class ComponentBeanTest extends TestCase {

    public static class MockComponent implements Component {

        public Configuration config;
        public boolean disposed = false;
        
        public void configure(Configuration config) throws FtpException {
            this.config = config;
        }

        public void dispose() {
            disposed = true;
        }
        
    }
    
    public void testLifecycle() throws Exception {
        Properties props = new Properties();
        props.setProperty("config.class", MockComponent.class.getName());
        props.setProperty("config.foo", "bar");
        PropertiesConfiguration config = new PropertiesConfiguration(props);
        
        ComponentBean bean = (ComponentBean) Bean.createBean(config, null);
        
        MockComponent component = (MockComponent) bean.initBean();
        assertEquals("bar", component.config.getString("foo"));
        assertFalse(component.disposed);

        bean.destroyBean();
        assertTrue(component.disposed);
        assertNull(bean.getBean());

    }
    
    public void testLifecycleDefaultClass() throws Exception {
        Properties props = new Properties();
        props.setProperty("config.foo", "bar");
        PropertiesConfiguration config = new PropertiesConfiguration(props);
        
        
        ComponentBean bean = (ComponentBean) Bean.createBean(config, MockComponent.class.getName());
        
        MockComponent component = (MockComponent) bean.initBean();
        assertEquals("bar", component.config.getString("foo"));
        assertFalse(component.disposed);
        
        bean.destroyBean();
        assertTrue(component.disposed);
        assertNull(bean.getBean());

    }

}
