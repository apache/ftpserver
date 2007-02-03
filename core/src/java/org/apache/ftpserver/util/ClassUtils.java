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

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.ftpserver.ftplet.Configuration;



public class ClassUtils {
 
    public static void setProperty(Object target, String propertyName, String propertyValue) {
        PropertyDescriptor setter = getDescriptor(target.getClass(), propertyName);

        setProperty(target, setter, propertyValue);
    }

    public static void setProperty(Object target, String propertyName, Object propertyValue) {
        PropertyDescriptor setter = getDescriptor(target.getClass(), propertyName);
        
        setProperty(target, setter, propertyValue);
    }
    
    private static void setProperty(Object target, PropertyDescriptor setter, Object castValue) {
        Method setterMethod = setter.getWriteMethod();
        
        if(setter != null && setterMethod != null) {
            try {
                setterMethod.invoke(target, new Object[]{castValue});
            } catch (Exception e) {
                throw new RuntimeException("Failed invoking setter " + setter.getDisplayName() + " on " + target, e);
            }
        } else {
            throw new RuntimeException("Property \"" + setter.getDisplayName() + "\" is not settable on class "+ target.getClass());
        }
        
    }
    
    private static void setProperty(Object target, PropertyDescriptor setter, String propertyValue) {
        Object castValue = ClassUtils.cast(setter.getPropertyType(), propertyValue);
        
        setProperty(target, setter, castValue);
    }
    
    private static PropertyDescriptor getDescriptor(Class clazz, String propertyName) {
        BeanInfo beanInfo;
        try {
            beanInfo = Introspector.getBeanInfo(clazz);
        } catch (IntrospectionException e) {
            throw new RuntimeException("Failed to introspect class: " + clazz);
        }
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        
        for (int i = 0; i < propertyDescriptors.length; i++) {
            PropertyDescriptor propertyDescriptor = propertyDescriptors[i];
            if(propertyDescriptor.getName().equals(propertyName)) {
                return propertyDescriptor;
            }
        }
        
        return null;
    }
        
    public static Object createBean(Configuration config, String defaultClass) {
        String className = config.getString("class", defaultClass);
        
        Class clazz;
        Object bean;
        try {
            clazz = Class.forName(className);
            bean = clazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create instance of class " + className, e);
        }
        
        
        Iterator keys = config.getKeys();
        
        while (keys.hasNext()) {
            String key = (String) keys.next();

            if(key.equals("class")) {
                continue;
            }
            
            Configuration subConfig = config.subset(key);
            
            Object value;
            PropertyDescriptor descriptor = getDescriptor(clazz, key);
            if(subConfig.isEmpty()) {
                // regular property
                value = cast(descriptor.getPropertyType(), config.getString(key, null));
            } else {
                if(Map.class.isAssignableFrom(descriptor.getPropertyType())) {
                    Map map = new HashMap();
                    
                    Iterator mapKeys = subConfig.getKeys();
                    
                    while (mapKeys.hasNext()) {
                        String mapKey = (String) mapKeys.next();
                        
                        map.put(mapKey, subConfig.getString(mapKey, null));
                    }
                    
                    value = map;
                    
                } else {
                    // create new bean
                    
                    value = createBean(subConfig, descriptor.getPropertyType().getName());
                }
                
            }

            setProperty(bean, descriptor, value);
        }
        
        
        return bean;
    }
    
    public static void invokeMethod(Object target, String methodName) {
        try {
            Method destroyMethod = target.getClass().getMethod(methodName, new Class[0]);
            destroyMethod.invoke(target, new Object[0]);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke method " + methodName + " on " + target);
        }
    }
    
    public static Object cast(Class clazz, String value) {
        
        Object castValue = null;
        int pos;
        
        if (clazz == String.class) {
            castValue = value;
        } else if (clazz == Boolean.TYPE || clazz == Boolean.class) {
            castValue = new Boolean(value);
        } else if (clazz == Byte.TYPE || clazz == Byte.class) {
            castValue = new Byte(value);
        } else if (
            (clazz == Character.TYPE || clazz == Character.class)
                && value.length() == 1) {
            castValue = new Character(value.charAt(0));
        } else if (clazz == Double.TYPE || clazz == Double.class) {
            castValue = new Double(value);
        } else if (clazz == Float.TYPE || clazz == Float.class) {
            castValue = new Float(value);
        } else if (clazz == Integer.TYPE || clazz == Integer.class) {
            castValue = new Integer(value);
        } else if (clazz == Long.TYPE || clazz == Long.class) {
            castValue = new Long(value);
        } else if (clazz == Short.TYPE || clazz == Short.class) {
            castValue = new Short(value);
        } else if (clazz == BigDecimal.class) {
            castValue = new BigDecimal(value);
        } else if (clazz == BigInteger.class) {
            castValue = new BigInteger(value);
        } else if(clazz.isArray()) {
            String[] values = value.split(",");
            Object castArray = Array.newInstance(clazz.getComponentType(), values.length);
            
            for (int i = 0; i < values.length; i++) {
                Array.set(castArray, i, cast(clazz.getComponentType(), values[i].trim()));
            } 
            
            castValue = castArray;
        } else if(clazz == List.class) {
            List list = new ArrayList();
            String[] values = value.split(",");
            
            for (int i = 0; i < values.length; i++) {
                list.add(values[i].trim());
            }
            
            castValue = list;
        } else if (clazz == URL.class) {
            try {
                castValue = new URL(value);
            } catch (MalformedURLException e) {
                throw new RuntimeException("Malformed URL: " + value, e);
            }
        } else if (clazz == InetAddress.class) {
            try {
                castValue = InetAddress.getByName(value);
            } catch (UnknownHostException e) {
                throw new RuntimeException("Unknown host: " + value, e);
            }
        } else if((pos = value.lastIndexOf('.')) != -1) {
            try {
                Class c = Class.forName(value.substring(0, pos));
                Field f = c.getDeclaredField(value.substring(pos+1));

                castValue = f.get(null);
            } catch (Exception ex) {
                throw new RuntimeException("Failed to get static field value for " + value, ex);
            }
        } else {
            throw new RuntimeException("Unable to cast \""+value+"\" as a "+clazz.getName());
        }

        return castValue;
    }
}
