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
package org.apache.ftpserver.message;

import org.apache.ftpserver.ftplet.Configuration;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.Logger;
import org.apache.ftpserver.interfaces.IMessageResource;
import org.apache.ftpserver.util.IoUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Class to get ftp server reply messages.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class MessageResourceImpl implements IMessageResource {

    private final static String RESOURCE = "org/apache/ftpserver/message/FtpStatus.properties";
    
    private String m_customMessageFile;
    private Properties m_messages;
    private Properties m_customMessages;
    private Logger m_logger;
    
    
    /**
     * Set logger
     */
    public void setLogger(Logger logger) {
        m_logger = logger;
    }
    
    /**
     * Configure - load properties file.
     */
    public void configure(Configuration config) throws FtpException {
        
        // load default messages 
        InputStream in = null;
        try {
            in = getClass().getClassLoader().getResourceAsStream(RESOURCE);
            m_messages = new Properties();
            m_messages.load(in);
        }
        catch(IOException ex) {
            m_logger.error("MessageResourceImpl.configure()", ex);
            throw new FtpException("MessageResourceImpl.configure()", ex);
        }
        finally {
            IoUtils.close(in);
        }
        
        // load custom messages
        m_customMessageFile = config.getString("custom-message-file", "./res/messages.gen");
        in = null;
        try {
            m_customMessages = new Properties();
            File file = new File(m_customMessageFile);
            if(file.isFile()) {
                in = new FileInputStream(m_customMessageFile);
                m_customMessages.load(in);
            }
        }
        catch(IOException ex) {
            m_logger.error("MessageResourceImpl.configure()", ex);
            throw new FtpException("MessageResourceImpl.configure()", ex);
        }
        finally {
            IoUtils.close(in);
        }
    }
    
    /**
     * Get all the available languages.
     */
    public String[] getAvailableLanguages() {
        return null;
    }
    
    /**
     * Get the message.
     */
    public String getMessage(int code, String subId, String language) {
        String msg = null;
        String codeStr = String.valueOf(code);
        
        // first try to get property for code.subId
        if(subId != null) {
            String key = codeStr + '.' + subId;
            msg = m_customMessages.getProperty(key);
            if(msg == null) {
                msg = m_messages.getProperty(key);
            }
        }
        
        // if not found get it for code
        if(msg == null) {
            msg = m_customMessages.getProperty(codeStr);
            if(msg == null) {
                msg = m_messages.getProperty(codeStr);
            }
        }
        
        // not found return empty string
        if(msg == null) {
            msg = "";
        }
        return msg;
    }
    
    /**
     * Get all messages.
     */
    public Properties getMessages(String language) {
        Properties messages = new Properties(m_messages);
        messages.putAll(m_customMessages);
        return messages;
    }
    
    /**
     * Save properties in file.
     */
    public void save(Properties prop, String language) throws FtpException {
        if(prop == null) {
            return;
        }
        
        // get only the new or modified properties
        Properties customMessages = new Properties();
        Enumeration props = prop.propertyNames();
        while( props.hasMoreElements() ) {
            String key = (String)props.nextElement();
            String newVal = prop.getProperty(key);
            String val = m_messages.getProperty(key);
            if( (val == null) || (!val.equals(newVal)) ) {
                customMessages.setProperty(key, newVal);
            }
        }
        
        // save the newly created properties
        OutputStream out = null;
        try {
            out = new FileOutputStream(m_customMessageFile);
            customMessages.store(out, "Custom Messages");
        }
        catch(IOException ex) {
            m_logger.error("MessageResourceImpl.save()", ex);
            throw new FtpException("MessageResourceImpl.save()", ex);
        }
        finally {
            IoUtils.close(out);
        }
        
        // assign the new custom properties
        m_customMessages = customMessages;
    }
    
    /**
     * Dispose component - clear properties.
     */
    public void dispose() {
        if(m_messages != null) {
            m_messages.clear();
            m_messages = null;
        }
        if(m_customMessages != null) {
            m_customMessages.clear();
            m_customMessages = null;
        }
    }
}
