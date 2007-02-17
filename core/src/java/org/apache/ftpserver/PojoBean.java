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

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.logging.LogFactory;
import org.apache.ftpserver.ftplet.Configuration;
import org.apache.ftpserver.util.ClassUtils;

public class PojoBean extends Bean {

    private Configuration config;
    private Object pojo;
    private LogFactory logFactory;
    private Class clazz;
    
    public PojoBean(Configuration config, Class clazz, LogFactory logFactory) {
        this.clazz = clazz;
        this.logFactory = logFactory;
        this.config = config;
    }
    
    public Object initBean() throws Exception {
        pojo = ClassUtils.createBean(config, clazz.getName());
        
        setLogFactory();
        
        configure();
        
        return pojo;
        
    }

    /**
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private void setLogFactory() throws Exception {
        if(logFactory == null) {
            return;
        }
        
        ClassUtils.setProperty(pojo, "logFactory", logFactory);
    }
    
    private void configure() throws Exception {
        String configureMethodName = config.getString("configure-method", "configure");
        
        try {
            ClassUtils.invokeMethod(pojo, configureMethodName);
        } catch(RuntimeException e) {
            // ignore
        }
    }

    public void destroyBean() {
        String disposeMethodName = config.getString("dispose-method", "dispose");

        try {
            ClassUtils.invokeMethod(pojo, disposeMethodName);
            
            pojo = null;
        } catch(Exception e) {
            // TODO log!
        }
    }

    public Object getBean() {
        return pojo;
    }
    
}
