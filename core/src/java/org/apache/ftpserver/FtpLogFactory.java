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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Custom log factory to add other log implementations.
 */
public
class FtpLogFactory extends LogFactory {
    
    private LogFactory original;
    private Vector logs;
    
    
    /**
     * Constructor - pass the original log factory.
     */
    public FtpLogFactory(LogFactory original) {
        this.original = original;
        logs = new Vector();
    }
    
    /**
     * Add log object.
     */
    public void addLog(Log log) {
        logs.add(log);
    }
    
    /**
     * Remove log object.
     */
    public void removeLog(Log log) {
        logs.remove(log);
    }
    
    /**
     * Get log object.
     */
    public Log getInstance(Class clazz) {
        Log olog = original.getInstance(clazz);
        return createProxyLog(olog);
    }
    
    /**
     * Get log object.
     */
    public Log getInstance(String name) {
        Log olog = original.getInstance(name);
        return createProxyLog(olog);
    }
    
    /**
     * Get attribute.
     */
    public Object getAttribute(String arg) {
        return original.getAttribute(arg);
    }
    
    /**
     * Get all the attribute names.
     */
    public String[] getAttributeNames() {
        return original.getAttributeNames();
    }
    
    /**
     * Set attribute.
     */
    public void setAttribute(String arg, Object val) {
        original.setAttribute(arg, val);
    }
    
    /**
     * Remove attribute.
     */
    public void removeAttribute(String arg) {
        original.removeAttribute(arg);
    }
    
    /**
     * Release log factory.
     */
    public void release() {
        original.release();
        logs.clear();
    }
    
    /**
     * Create a proxy log object to redirect log message.
     */
    private Log createProxyLog(final Log original) {
        InvocationHandler handler = new InvocationHandler() {
            public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
                Object retVal = m.invoke(original, args);
                for(int i=logs.size(); --i>=0;) {
                    Log log = (Log)logs.get(i);
                    m.invoke(log, args);
                }
                return retVal;
            }
        };
        
        return (Log)Proxy.newProxyInstance(getClass().getClassLoader(), 
                                           new Class[] {Log.class}, 
                                           handler);
    }
}