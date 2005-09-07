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
package org.apache.ftpserver.logger;

import org.apache.ftpserver.ftplet.Configuration;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.Logger;

import java.util.ArrayList;

/**
 * This composite logger writes into multiple logger.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class CompositeLogger implements Logger {

    private ArrayList m_loggers = new ArrayList();
    
    /**
     * Add logger.
     */
    public void addLogger(Logger logger) {
        m_loggers.add(logger);
    }
    
    /**
     * Remove logger.
     */
    public void removeLogger(Logger logger) {
        m_loggers.remove(logger);
    }
    
    /**
     * Write debug message.
     */
    public void debug(String msg) {
        for(int i=m_loggers.size(); --i>=0; ) {
            Logger logger = (Logger)m_loggers.get(i);
            logger.debug(msg);
        }
    }

    /**
     * Write debug message. 
     */
    public void debug(String msg, Throwable th) {
        for(int i=m_loggers.size(); --i>=0; ) {
            Logger logger = (Logger)m_loggers.get(i);
            logger.debug(msg, th);
        }
    }

    /**
     * Write info message 
     */
    public void info(String msg) {
        for(int i=m_loggers.size(); --i>=0; ) {
            Logger logger = (Logger)m_loggers.get(i);
            logger.info(msg);
        }
    }

    /**
     * Write infor message. 
     */
    public void info(String msg, Throwable th) {
        for(int i=m_loggers.size(); --i>=0; ) {
            Logger logger = (Logger)m_loggers.get(i);
            logger.info(msg, th);
        }
    }

    /**
     * Write warnning message 
     */
    public void warn(String msg) {
        for(int i=m_loggers.size(); --i>=0; ) {
            Logger logger = (Logger)m_loggers.get(i);
            logger.warn(msg);
        }
    }

    /**
     * Write warnning message. 
     */
    public void warn(String msg, Throwable th) {
        for(int i=m_loggers.size(); --i>=0; ) {
            Logger logger = (Logger)m_loggers.get(i);
            logger.warn(msg, th);
        }
    }

    /**
     * Write warning message. 
     */
    public void error(String msg) {
        for(int i=m_loggers.size(); --i>=0; ) {
            Logger logger = (Logger)m_loggers.get(i);
            logger.error(msg);
        }
    }

    /**
     * Write warnning message 
     */
    public void error(String msg, Throwable th) {
        for(int i=m_loggers.size(); --i>=0; ) {
            Logger logger = (Logger)m_loggers.get(i);
            logger.error(msg, th);
        }
    }

    /**
     * It does nothing 
     */
    public void setLogger(Logger logger) {
    }

    /**
     * It does nothing. 
     */
    public void configure(Configuration config) throws FtpException {
    }

    /**
     * Dispose all. 
     */
    public void dispose() {
        for(int i=m_loggers.size(); --i>=0; ) {
            Logger logger = (Logger)m_loggers.get(i);
            logger.dispose();
        }
        m_loggers.clear();
    }
}
