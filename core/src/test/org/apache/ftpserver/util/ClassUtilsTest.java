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

import java.net.InetAddress;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.net.ssl.X509KeyManager;

import junit.framework.TestCase;

import org.apache.ftpserver.config.PropertiesConfiguration;
import org.apache.ftpserver.ftplet.Configuration;
import org.apache.ftpserver.ssl.AliasKeyManager;

public class ClassUtilsTest extends TestCase {

	public void testImplementsInterface() {
		X509KeyManager km = new AliasKeyManager(null, null);
		
		assertTrue(ClassUtils.implementsInterface(km.getClass(), "javax.net.ssl.X509KeyManager"));
		assertFalse(ClassUtils.implementsInterface(km.getClass(), "foo"));
	}
	
    public void testNormalizePropertyName() {
        assertEquals("foo", ClassUtils.normalizePropertyName("foo"));
        assertEquals("fooBar", ClassUtils.normalizePropertyName("fooBar"));
        assertEquals("fooBar", ClassUtils.normalizePropertyName("foo-bar"));
    }

    public void testSetProperty() {
        MyBean bean = new MyBean();
        
        ClassUtils.setProperty(bean, "foo", "flopp");
        assertEquals("flopp", bean.getFoo());

        ClassUtils.setProperty(bean, "foo", "flipp");
        assertEquals("flipp", bean.getFoo());
        
        ClassUtils.setProperty(bean, "bar", "123");

        assertEquals(123, bean.getBar());
    }
    
    public void testSetCamelCasesProperty() {
        MyBean bean = new MyBean();
        
        ClassUtils.setProperty(bean, "camelCasedProp", "flopp");
        assertEquals("flopp", bean.getCamelCasedProp());
    }

    public void testSetDashedProperty() {
        MyBean bean = new MyBean();
        
        ClassUtils.setProperty(bean, "camel-cased-prop", "flopp");
        assertEquals("flopp", bean.getCamelCasedProp());
    }

    public void testSetPropertyWrongCast() {
        MyBean bean = new MyBean();
        
        try{
            ClassUtils.setProperty(bean, "bar", "flopp");
            fail("Must throw exception");
        } catch(RuntimeException e) {
            // ok
        }
    }

    public void testSetPropertyUnknownProperty() {
        MyBean bean = new MyBean();
        
        try{
            ClassUtils.setProperty(bean, "dummy", "flopp");
            fail("Must throw exception");
        } catch(RuntimeException e) {
            // ok
        }
    }
    
    public void testCreateSimpleBean() {
        Properties props = new Properties();
        props.setProperty("config.class", MyBean.class.getName());
        props.setProperty("config.foo", "flopp");
        props.setProperty("config.bar", "123");
        
        Configuration config = new PropertiesConfiguration(props);
        
        MyBean bean = (MyBean) ClassUtils.createBean(config, null);
        assertEquals("flopp", bean.getFoo());
        assertEquals(123, bean.getBar());
    }
    
    public void testCreateComplexBean() throws Exception {
        Properties props = new Properties();
        props.setProperty("config.class", MyOtherBean.class.getName());
        props.setProperty("config.baz", "1.2.3.4");
        props.setProperty("config.myBean.class", MyBean.class.getName());
        props.setProperty("config.myBean.foo", "flopp");
        props.setProperty("config.myBean.bar", "123");
        
        Configuration config = new PropertiesConfiguration(props);
        
        MyOtherBean otherBean = (MyOtherBean) ClassUtils.createBean(config, null);
        assertEquals(InetAddress.getByName("1.2.3.4"), otherBean.getBaz());
        
        MyBean bean = otherBean.getMyBean();
        
        assertEquals("flopp", bean.getFoo());
        assertEquals(123, bean.getBar());
    }

    public void testCreateComplexBeanNoClassForSubBean() throws Exception {
        Properties props = new Properties();
        props.setProperty("config.class", MyOtherBean.class.getName());
        props.setProperty("config.baz", "1.2.3.4");
        props.setProperty("config.myBean.foo", "flopp");
        props.setProperty("config.myBean.bar", "123");
        
        Configuration config = new PropertiesConfiguration(props);
        
        MyOtherBean otherBean = (MyOtherBean) ClassUtils.createBean(config, null);
        assertEquals(InetAddress.getByName("1.2.3.4"), otherBean.getBaz());
        
        MyBean bean = otherBean.getMyBean();
        
        assertEquals("flopp", bean.getFoo());
        assertEquals(123, bean.getBar());
    }
    
    public void testCreateListBean() {
        Properties props = new Properties();
        props.setProperty("config.class", MyCollectionBean.class.getName());
        props.setProperty("config.list", "foo,bar, bar, flopp  ");
        
        Configuration config = new PropertiesConfiguration(props);
        
        MyCollectionBean bean = (MyCollectionBean) ClassUtils.createBean(config, null);
        
        Iterator iter = bean.getList().iterator();
        
        assertEquals("foo", iter.next());
        assertEquals("bar", iter.next());
        assertEquals("bar", iter.next());
        assertEquals("flopp", iter.next());
        assertFalse(iter.hasNext());
    }
    
    public void testCreateAdvancedListBean() {
        Properties props = new Properties();
        props.setProperty("config.class", MyCollectionBean.class.getName());
        props.setProperty("config.list.1.class", MyBean.class.getName());
        props.setProperty("config.list.1.foo", "foo1");
        props.setProperty("config.list.2.class", MyBean.class.getName());
        props.setProperty("config.list.2.foo", "foo2");
        props.setProperty("config.list.3.class", MyBean.class.getName());
        props.setProperty("config.list.3.foo", "foo3");
        
        Configuration config = new PropertiesConfiguration(props);
        
        MyCollectionBean bean = (MyCollectionBean) ClassUtils.createBean(config, null);
        
        Iterator iter = bean.getList().iterator();
        
        MyBean myBean1 = (MyBean) iter.next();
        assertEquals("foo1", myBean1.getFoo());

        MyBean myBean2 = (MyBean) iter.next();
        assertEquals("foo2", myBean2.getFoo());
        
        MyBean myBean3 = (MyBean) iter.next();
        assertEquals("foo3", myBean3.getFoo());
        
        assertFalse(iter.hasNext());
    }

    public void testCreateArrayBean() {
        Properties props = new Properties();
        props.setProperty("config.class", MyCollectionBean.class.getName());
        props.setProperty("config.array", "1,12, 123, 1234  ");
        
        Configuration config = new PropertiesConfiguration(props);
        
        MyCollectionBean bean = (MyCollectionBean) ClassUtils.createBean(config, null);
        
        int[] array = bean.getArray();
        
        assertEquals(4, array.length);
        assertEquals(1, array[0]);
        assertEquals(12, array[1]);
        assertEquals(123, array[2]);
        assertEquals(1234, array[3]);
    }
    
    public void testCreateAdvancedArrayBean() {
        Properties props = new Properties();
        props.setProperty("config.class", MyCollectionBean.class.getName());
        props.setProperty("config.myBeans.1.foo", "foo1");
        props.setProperty("config.myBeans.2.foo", "foo2");
        props.setProperty("config.myBeans.3.foo", "foo3");
        
        Configuration config = new PropertiesConfiguration(props);
        
        MyCollectionBean bean = (MyCollectionBean) ClassUtils.createBean(config, null);
        
        MyBean[] array = bean.getMyBeans();
        
        assertEquals(3, array.length);
        assertEquals("foo1", array[0].getFoo());
        assertEquals("foo2", array[1].getFoo());
        assertEquals("foo3", array[2].getFoo());
    }

    public void testCreateSubClassArrayBean() {
        Properties props = new Properties();
        props.setProperty("config.class", MyCollectionBean.class.getName());
        props.setProperty("config.myBeans.1.class", MySubBean.class.getName());
        props.setProperty("config.myBeans.1.foo", "foo1");
        
        Configuration config = new PropertiesConfiguration(props);
        
        MyCollectionBean bean = (MyCollectionBean) ClassUtils.createBean(config, null);
        
        MyBean[] array = bean.getMyBeans();
        
        assertEquals(1, array.length);
        assertTrue(array[0] instanceof MySubBean);
        assertEquals("foo1", array[0].getFoo());
    }

    public void testCreateLongArrayBean() {
        Properties props = new Properties();
        props.setProperty("config.class", MyCollectionBean.class.getName());
        
        for(int i = 1; i<13; i++) {
            props.setProperty("config.array." + i, Integer.toString(i));
        }
        
        Configuration config = new PropertiesConfiguration(props);
        
        MyCollectionBean bean = (MyCollectionBean) ClassUtils.createBean(config, null);
        
        int[] array = bean.getArray();
        
        assertEquals(12, array.length);
        for(int i = 0; i<12; i++) {
            assertEquals(i+1, array[i]);
        }
    }
    
    public void testCreateMapBean() {
        Properties props = new Properties();
        props.setProperty("config.class", MyMapBean.class.getName());
        props.setProperty("config.map.foo1", "bar1");
        props.setProperty("config.map.foo2", "bar2");
        props.setProperty("config.map.foo3", "bar3");
        props.setProperty("config.map.foo4", "bar4");
        
        Configuration config = new PropertiesConfiguration(props);
        
        MyMapBean bean = (MyMapBean) ClassUtils.createBean(config, null);
        
        Map map = bean.getMap();
        
        assertEquals(4, map.size());
        assertEquals("bar1", map.get("foo1"));
        assertEquals("bar2", map.get("foo2"));
        assertEquals("bar3", map.get("foo3"));
        assertEquals("bar4", map.get("foo4"));
    }
    
    public void testCreateMap() {
        Properties props = new Properties();
        props.setProperty("config.foo1.class", MyBean.class.getName());
        props.setProperty("config.foo1.foo", "bar1");
        props.setProperty("config.foo2.class", MyBean.class.getName());
        props.setProperty("config.foo2.foo", "bar2");
        props.setProperty("config.foo3.class", MyBean.class.getName());
        props.setProperty("config.foo3.foo", "bar3");
        props.setProperty("config.foo4.class", MyBean.class.getName());
        props.setProperty("config.foo4.foo", "bar4");
        
        Configuration config = new PropertiesConfiguration(props);
        
        Map map = ClassUtils.createMap(config);
        
        for(int i = 1; i<5; i++) {
            MyBean bean = (MyBean) map.get("foo" + i);
            assertEquals("bar" + i, bean.getFoo());
            
        }
    }
 
    public static class MyCollectionBean {
        private List list;
        private int[] array;
        private MyBean[] myBeans;

        public MyBean[] getMyBeans() {
            return myBeans;
        }
        public void setMyBeans(MyBean[] myBeans) {
            this.myBeans = myBeans;
        }
        public int[] getArray() {
            return array;
        }
        public void setArray(int[] array) {
            this.array = array;
        }
        public List getList() {
            return list;
        }
        public void setList(List list) {
            this.list = list;
        }
    }
    
    public static class MyMapBean {
        private Map map;

        public Map getMap() {
            return map;
        }

        public void setMap(Map map) {
            this.map = map;
        }

    }
    
    public static class MyBean {
        private String foo;
        private int bar;
        private String camelCasedProp;
        
        public int getBar() {
            return bar;
        }
        public void setBar(int bar) {
            this.bar = bar;
        }
        public String getFoo() {
            return foo;
        }
        public void setFoo(String foo) {
            this.foo = foo;
        }
        public String getCamelCasedProp() {
            return camelCasedProp;
        }
        public void setCamelCasedProp(String camelCasedProp) {
            this.camelCasedProp = camelCasedProp;
        }
    }
    
    public static class MySubBean extends MyBean {}
    
    public static class MyOtherBean {
        private MyBean myBean;
        private InetAddress baz;
        public InetAddress getBaz() {
            return baz;
        }
        public void setBaz(InetAddress baz) {
            this.baz = baz;
        }
        public MyBean getMyBean() {
            return myBean;
        }
        public void setMyBean(MyBean myBean) {
            this.myBean = myBean;
        }
    }
    
}