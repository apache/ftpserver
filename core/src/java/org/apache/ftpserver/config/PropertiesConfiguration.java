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

package org.apache.ftpserver.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;

import org.apache.ftpserver.ftplet.Configuration;
import org.apache.ftpserver.ftplet.FtpException;

/**
 * Properties based configuration.
 */
public 
class PropertiesConfiguration implements Configuration {
    
    protected Properties prop;
    protected String prefix = "config.";
    
    /**
     * Private constructor - used internally to get configuration subset.
     */
    protected PropertiesConfiguration() {
    }
    
    /**
     * Constructor - set the properties input stream.
     */
    public PropertiesConfiguration(InputStream in) throws IOException {
        
        // load properties
        prop = new Properties();
        prop.load(in);
    }    
    
    /**
     * Constructor - set the properties.
     */
    public PropertiesConfiguration(Properties prop) {
        this.prop = (Properties) prop.clone();
    }    
    

    /**
     * Is empty?
     */
    public boolean isEmpty() {
        boolean empty = true;
        Enumeration keys = prop.propertyNames();
        while(keys.hasMoreElements()) {
            String key = (String)keys.nextElement();
            if(key.startsWith(prefix)) {
                empty = false;
                break;
            }
        }
        return empty;
    }
    
    /**
     * Get string - if not found throws FtpException.
     */
    public String getString(String param) throws FtpException {
        String val = prop.getProperty(prefix + param);
        if(val == null) {
            throw new FtpException("Not found : " + param);
        }
        return val;
    }
    
    /**
     * Get string - if not found returns the default value.
     */
    public String getString(String param, String defaultVal) {
        return prop.getProperty(prefix + param, defaultVal);
    }
    
    /**
     * Get integer - if not found throws FtpException.
     */
    public int getInt(String param) throws FtpException {
        String val = prop.getProperty(prefix + param);
        if(val == null) {
            throw new FtpException("Not found : " + param);
        }
        
        try {
            return Integer.parseInt(val);
        }
        catch(Exception ex) {
            throw new FtpException("PropertiesConfiguration.getInt()", ex);
        }
    }
    
    /**
     * Get int - if not found returns the default value.
     */
    public int getInt(String param, int defaultVal) {
        int retVal = defaultVal;
        try {
            retVal = getInt(param);
        }
        catch(Exception ex) {
        }
        return retVal;
    }
    
    /**
     * Get long - if not found throws FtpException.
     */
    public long getLong(String param) throws FtpException {
        String val = prop.getProperty(prefix + param);
        if(val == null) {
            throw new FtpException("Not found : " + param);
        }
        
        try {
            return Long.parseLong(val);
        }
        catch(Exception ex) {
            throw new FtpException("PropertiesConfiguration.getLong()", ex);
        }
    }
    
    /**
     * Get long - if not found returns the default value.
     */
    public long getLong(String param, long defaultVal) {
        long retVal = defaultVal;
        try {
            retVal = getLong(param);
        }
        catch(Exception ex) {
        }
        return retVal;
    }
    
    /**
     * Get boolean - if not found throws FtpException.
     */
    public boolean getBoolean(String param) throws FtpException {
        String val = prop.getProperty(prefix + param);
        if(val == null) {
            throw new FtpException("Not found : " + param);
        }
        return val.equalsIgnoreCase("true");
    }
    
    /**
     * Get boolean - if not found returns the default value.
     */
    public boolean getBoolean(String param, boolean defaultVal) {
        boolean retVal = defaultVal;
        try {
            retVal = getBoolean(param);
        }
        catch(Exception ex) {
        }
        return retVal;
    }
    
    /**
     * Get double - if not found throws FtpException.
     */
    public double getDouble(String param) throws FtpException {
        String val = prop.getProperty(prefix + param);
        if(val == null) {
            throw new FtpException("Not found : " + param);
        }
        
        try {
            return Double.parseDouble(val);
        }
        catch(Exception ex) {
            throw new FtpException("PropertiesConfiguration.getDouble()", ex);
        }
    }
    
    /**
     * Get double - if not found returns the default value.
     */
    public double getDouble(String param, double defaultVal) {
        double retVal = defaultVal;
        try {
            retVal = getDouble(param);
        }
        catch(Exception ex) {
        }
        return retVal;
    }
    
    /**
     * Get sub configuration.
     */
    public Configuration subset(String param) {
        PropertiesConfiguration subConfig = new PropertiesConfiguration();
        subConfig.prop = prop;
        subConfig.prefix = prefix + param + '.';
        return subConfig; 
    }
    
    /**
     * Get configuration keys.
     */
    public Iterator getKeys() {
        ArrayList arr = new ArrayList();
        for(Enumeration en = prop.keys(); en.hasMoreElements(); ) {
            String key = (String)en.nextElement();
            if(!key.startsWith(prefix)) {
                continue;
            }
            key = key.substring(prefix.length());
            int indexOfNextDot = key.indexOf('.');
            if(indexOfNextDot > -1) {
                key = key.substring(0, indexOfNextDot);
            }
            
            if(!arr.contains(key)) {
                arr.add(key);
            }
        }
        return arr.iterator();
    }
}
