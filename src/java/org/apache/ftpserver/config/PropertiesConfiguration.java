// $Id$
/*
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ftpserver.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.ftpserver.ftplet.Configuration;
import org.apache.ftpserver.ftplet.FtpException;

/**
 * Properties based configuration.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class PropertiesConfiguration implements Configuration {
    
    private final static String PREFIX = "config.";
    
    private Properties m_prop;
    
    /**
     * Constructor - set the properties input stream.
     */
    public PropertiesConfiguration(InputStream in) throws IOException {
        
        // load properties
        Properties prop = new Properties();
        prop.load(in);
        
        setProperties(prop);
    }    
    
    /**
     * Constructor - set the properties.
     */
    public PropertiesConfiguration(Properties prop) {
        setProperties(prop);
    }    
    
    /**
     * Set properties.
     */
    private void setProperties(Properties prop) {
    
        // strip prefix
        m_prop = new Properties();
        int prefixLen = PREFIX.length();
        Enumeration keys = prop.propertyNames();
        while(keys.hasMoreElements()) {
            String key = (String)keys.nextElement();
            String val = prop.getProperty(key);
            if(key.startsWith(PREFIX)) {
                key = key.substring(prefixLen);
            }
            m_prop.setProperty(key, val);
        }
        prop.clear();
    }
    
    /**
     * Get string - if not found throws FtpException.
     */
    public String getString(String param) throws FtpException {
        String val = m_prop.getProperty(param);
        if(val == null) {
            throw new FtpException("Not found : " + param);
        }
        return val;
    }
    
    /**
     * Get string - if not found returns the default value.
     */
    public String getString(String param, String defaultVal) {
        return m_prop.getProperty(param, defaultVal);
    }
    
    /**
     * Get integer - if not found throws FtpException.
     */
    public int getInt(String param) throws FtpException {
        String val = m_prop.getProperty(param);
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
        String val = m_prop.getProperty(param);
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
        String val = m_prop.getProperty(param);
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
        String val = m_prop.getProperty(param);
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
     * Get sub configuration - if not found throws FtpException.
     */
    public Configuration getConfiguration(String param) throws FtpException {
        Properties prop = new Properties();
        Enumeration propNames = m_prop.propertyNames();
        String prefix = param + '.';
        int prefixLen = prefix.length();
        while(propNames.hasMoreElements()) {
            String key = (String)propNames.nextElement();
            if(!key.startsWith(prefix)) {
                continue;
            }
            String val = m_prop.getProperty(key);
            key = key.substring(prefixLen);
            prop.setProperty(key, val);
        }
        
        if(prop.isEmpty()) {
            throw new FtpException("Not found : " + param);
        }
        return new PropertiesConfiguration(prop);
    }
    
    /**
     * Get sub configuration - if not found returns the default value.
     */
    public Configuration getConfiguration(String param, Configuration defaultVal) {
        Configuration conf = defaultVal;
        try {
            conf = getConfiguration(param);
        }
        catch(Exception ex) {
        }
        return conf;
    }
    
    /**
     * Get the configuration keys.
     */
    public Enumeration getKeys() {
        return m_prop.propertyNames();
    }
}
