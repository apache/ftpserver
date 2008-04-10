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
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

public class ClassUtilsTest extends TestCase {

	public void testImplementsInterface() {
		assertTrue(ClassUtils.extendsClass(MySubBean.class, MyBean.class.getName()));
		assertFalse(ClassUtils.extendsClass(MySubBean.class, "foo"));
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
    
    public static class MyCollectionBean {
        private List<?> list;
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
        public List<?> getList() {
            return list;
        }
        public void setList(List<?> list) {
            this.list = list;
        }
    }
    
    public static class MyMapBean {
        private Map<?, ?> map;

        public Map<?, ?> getMap() {
            return map;
        }

        public void setMap(Map<?, ?> map) {
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