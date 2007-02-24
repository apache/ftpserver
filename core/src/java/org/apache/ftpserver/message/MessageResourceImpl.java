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

package org.apache.ftpserver.message;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.ftpserver.ftplet.Component;
import org.apache.ftpserver.ftplet.Configuration;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.interfaces.MessageResource;
import org.apache.ftpserver.util.IoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Class to get ftp server reply messages. This supports i18n.
 * Basic message search path is: 
 * 
 * Custom Language Specific Messages -> Default Language Specific Messages ->
 * Custom Common Messages -> Default Common Messages -> null (not found)
 */
public 
class MessageResourceImpl implements MessageResource, Component {

    private static final Logger LOG = LoggerFactory.getLogger(MessageResourceImpl.class);
    
    private final static String RESOURCE_PATH = "org/apache/ftpserver/message/";
    
    private String[] languages;
    private HashMap messages;
    private String customMessageDir;
    
    private static class PropertiesPair {
        public Properties defaultProperties = new Properties();
        public Properties customProperties = new Properties();
    } 
    
    /**
     * Configure - load properties file.
     */
    public void configure(Configuration config) throws FtpException {
        
        // get the custom message directory
        customMessageDir = config.getString("custom-message-dir", "./res");
        
        // get all the languages
        String languages = config.getString("languages", null);
        if(languages != null) {
            StringTokenizer st = new StringTokenizer(languages, ",; \t");
            int tokenCount = st.countTokens();
            this.languages = new String[tokenCount];
            for(int i=0; i<tokenCount; ++i) {
                this.languages[i] = st.nextToken().toLowerCase();
            }
        }
        
        // populate different properties
        messages = new HashMap();
        if(this.languages != null) {
            for(int i=0; i<this.languages.length; ++i) {
                String lang = this.languages[i];
                PropertiesPair pair = createPropertiesPair(lang);
                messages.put(lang, pair);
            }
        }
        PropertiesPair pair = createPropertiesPair(null);
        messages.put(null, pair);
    }
    
    /**
     * Create Properties pair object. It stores the default 
     * and the custom messages.
     */
    private PropertiesPair createPropertiesPair(String lang) throws FtpException {
        PropertiesPair pair = new PropertiesPair();
        
        // load default resource
        String defaultResourceName;
        if(lang == null) {
            defaultResourceName = RESOURCE_PATH + "FtpStatus.properties";
        }
        else {
            defaultResourceName = RESOURCE_PATH + "FtpStatus_" + lang + ".properties";
        }
        InputStream in = null;
        try {
            in = getClass().getClassLoader().getResourceAsStream(defaultResourceName);
            if(in != null) {
                pair.defaultProperties.load(in);
            }
        }
        catch(Exception ex) {
            LOG.warn("MessageResourceImpl.createPropertiesPair()", ex);
            throw new FtpException("MessageResourceImpl.createPropertiesPair()", ex);
        }
        finally {
            IoUtils.close(in);
        }
        
        // load custom resource
        File resourceFile = null;
        if(lang == null) {
            resourceFile = new File(customMessageDir, "FtpStatus.gen");
        }
        else {
            resourceFile = new File(customMessageDir, "FtpStatus_" + lang + ".gen");
        }
        in = null;
        try {
            if(resourceFile.exists()) {
                in = new FileInputStream(resourceFile);
                pair.customProperties.load(in);
            }
        }
        catch(Exception ex) {
            LOG.warn("MessageResourceImpl.createPropertiesPair()", ex);
            throw new FtpException("MessageResourceImpl.createPropertiesPair()", ex);
        }
        finally {
            IoUtils.close(in);
        }
        
        return pair;
    }
    
    /**
     * Get all the available languages.
     */
    public String[] getAvailableLanguages() {
        if(languages == null) {
            return null;
        } else {         
            return (String[]) languages.clone();
        }
    }
    
    /**
     * Get the message. If the message not found, it will return null.
     */
    public String getMessage(int code, String subId, String language) {
        
        // find the message key
        String key = String.valueOf(code);
        if(subId != null) {
            key = key + '.' + subId;
        }
        
        // get language specific value
        String value = null;
        PropertiesPair pair = null;
        if(language != null) {
            language = language.toLowerCase();
            pair = (PropertiesPair)messages.get(language);
            if(pair != null) {
                value = pair.customProperties.getProperty(key);
                if(value == null) {
                    value = pair.defaultProperties.getProperty(key);
                }
            }
        }
        
        // if not available get the default value
        if(value == null) {
            pair = (PropertiesPair)messages.get(null);
            if(pair != null) {
                value = pair.customProperties.getProperty(key);
                if(value == null) {
                    value = pair.defaultProperties.getProperty(key);
                }
            }
        }
        
        return value;
    }
    
    /**
     * Get all messages.
     */
    public Properties getMessages(String language) {
        Properties messages = new Properties();
        
        // load properties sequentially 
        // (default,custom,default language,custom language)
        PropertiesPair pair = (PropertiesPair)this.messages.get(null);
        if(pair != null) {
            messages.putAll(pair.defaultProperties);
            messages.putAll(pair.customProperties);
        }
        if(language != null) {
            language = language.toLowerCase();
            pair = (PropertiesPair)this.messages.get(language);
            if(pair != null) {
                messages.putAll(pair.defaultProperties);
                messages.putAll(pair.customProperties);
            }
        }
        return messages;
    }
    
    /**
     * Save properties in file.
     */
    public void save(Properties prop, String language) throws FtpException {
        
        // null properties - nothing to save
        if(prop == null) {
            return;
        }
        
        // empty properties - nothing to save
        if(prop.isEmpty()) {
            return;
        }
        
        // get custom resource file name
        File resourceFile = null;
        if(language == null) {
            resourceFile = new File(customMessageDir, "FtpStatus.gen");
        }
        else {
            language = language.toLowerCase();
            resourceFile = new File(customMessageDir, "FtpStatus_" + language + ".gen");
        }
        
        // save resource file
        OutputStream out = null;
        try {
            out = new FileOutputStream(resourceFile);
            prop.store(out, "Custom Messages");
        }
        catch(IOException ex) {
            LOG.error("MessageResourceImpl.save()", ex);
            throw new FtpException("MessageResourceImpl.save()", ex);
        }
        finally {
            IoUtils.close(out);
        }
        
        // assign new messages
        PropertiesPair pair = (PropertiesPair)messages.get(language);
        if(pair == null) {
            pair = new PropertiesPair();
            messages.put(language, pair);
        }
        pair.customProperties = prop;
    }
    
    /**
     * Dispose component - clear all maps.
     */
    public void dispose() {
        Iterator it = messages.keySet().iterator();
        while(it.hasNext()) {
            String language = (String)it.next();
            PropertiesPair pair = (PropertiesPair)messages.get(language);
            pair.customProperties.clear();
            pair.defaultProperties.clear();
        }
        messages.clear();
    }
}
