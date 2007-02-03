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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.ftpserver.config.PropertiesConfiguration;
import org.apache.ftpserver.ftplet.Configuration;

public class ClassUtilsTest extends TestCase {

    public void testSetProperty() {
        MyBean bean = new MyBean();
        
        ClassUtils.setProperty(bean, "foo", "flopp");
        assertEquals("flopp", bean.getFoo());

        ClassUtils.setProperty(bean, "foo", "flipp");
        assertEquals("flipp", bean.getFoo());
        
        ClassUtils.setProperty(bean, "bar", "123");

        assertEquals(123, bean.getBar());
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
    
    
    /////////////////////////////////
    // Test cast method
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

    public static class MyCollectionBean {
        private List list;
        private int[] array;

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
    }
    
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