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
import java.io.File;
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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.ftpserver.FtpServerConfigurationException;
import org.apache.ftpserver.ftplet.Configuration;



public class ClassUtils {
 
    public static void setProperty(Object target, String propertyName, String propertyValue) {
        PropertyDescriptor setter = getDescriptor(target.getClass(), propertyName);

        setProperty(target, setter, propertyValue);
    }

    public static void setProperty(Object target, String propertyName, Object propertyValue) {
        PropertyDescriptor setter = getDescriptor(target.getClass(), propertyName);
        
        if(setter == null) {
            return;
        }
        
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
    
    public static String normalizePropertyName(String propertyName){
        StringTokenizer st = new StringTokenizer(propertyName, "-");
        
        if(st.countTokens() > 1) {
            StringBuffer sb = new StringBuffer();
            
            // add first unchanged
            sb.append(st.nextToken());
            
            while(st.hasMoreTokens()) {
                String token = st.nextToken().trim();
                
                if(token.length() > 0) {
                    sb.append(Character.toUpperCase(token.charAt(0)));
                    sb.append(token.substring(1));
                }
            }
            
            return sb.toString();
        } else {
            return propertyName;
        }
        
    }
    
    private static PropertyDescriptor getDescriptor(Class clazz, String propertyName) {
        propertyName = normalizePropertyName(propertyName);
        
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
        
    private static Object createObject(Class clazz, Configuration config, String propValue) {
        Object value;
        
        if(config.isEmpty()) {
            // regular property
            value = cast(clazz, propValue);
        } else {
            if(clazz == null) {
                String className = config.getString("class", null);
                if(className != null) {
                    try {
                        clazz = Class.forName(className);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException("Class not found: " + className, e);
                    }
                } else {
                    // TODO improve error message
                    throw new RuntimeException("Can not resolve class");
                }
            }
            
            if(Map.class.isAssignableFrom(clazz)) {
                Map map = new HashMap();
                
                Iterator mapKeys = getKeysInOrder(config.getKeys());
                
                while (mapKeys.hasNext()) {
                    String mapKey = (String) mapKeys.next();
                    String mapValue = config.getString(mapKey, null);
                    Configuration mapConfig = config.subset(mapKey);
                    
                    map.put(mapKey, createObject(String.class, mapConfig, mapValue));
                }
                
                value = map;
            } else if(Collection.class.isAssignableFrom(clazz)) {
                List list = new ArrayList();
                
                Iterator mapKeys = getKeysInOrder(config.getKeys());
                
                while (mapKeys.hasNext()) {
                    String mapKey = (String) mapKeys.next();
                    
                    String listValue = config.getString(mapKey, null);

                    list.add(createObject(null, config.subset(mapKey), listValue));
                }
                
                value = list;
            } else if(clazz.isArray()) {
                List list = new ArrayList();
                
                Iterator mapKeys = getKeysInOrder(config.getKeys());
                
                while (mapKeys.hasNext()) {
                    String mapKey = (String) mapKeys.next();
                    
                    String listValue = config.getString(mapKey, null);

                    list.add(createObject(clazz.getComponentType(), config.subset(mapKey), listValue));
                }
                
                Object castArray = Array.newInstance(clazz.getComponentType(), list.size());
                
                for (int i = 0; i < list.size(); i++) {
                    Array.set(castArray, i, list.get(i));
                } 
                
                
                value = castArray;
            } else {
                // create new bean
                
                value = createBean(config, clazz.getName());
            }
            
        }

        return value;
    }
    
    public static class KeyComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            String key1 = (String) o1;
            String key2 = (String) o2;

            // assume they are integers
            try {
                int intKey1 = Integer.parseInt(key1);
                int intKey2 = Integer.parseInt(key2);
            
                return intKey1 - intKey2;
            } catch(NumberFormatException e) {
                return key1.compareToIgnoreCase(key2);
            }
        }
    }
    
    private static Iterator getKeysInOrder(Iterator keys) {
        List keyList = new ArrayList();
        
        while (keys.hasNext()) {
            String key = (String) keys.next();
            keyList.add(key);
        }
        
        Collections.sort(keyList, new KeyComparator());

        return keyList.iterator();
    }
    
    public static Map createMap(Configuration config) {
        return (Map) createObject(Map.class, config, null);
        
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
            
            String propValue = config.getString(key, null);
            
            PropertyDescriptor descriptor = getDescriptor(clazz, key);
            
            if(descriptor == null) {
                throw new FtpServerConfigurationException("Unknown property \"" + key + "\" on class " + className);
            }

            Object value = createObject(descriptor.getPropertyType(), subConfig, propValue);

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
        } else if (clazz == File.class) {
            castValue = new File(value);
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
