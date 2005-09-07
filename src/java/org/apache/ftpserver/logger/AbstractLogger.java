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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;


/**
 * Abstract logger class.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
abstract class AbstractLogger implements Logger {

    protected final static String LINE_SEP = System.getProperty("line.separator", "\n");
    protected final static String[] LOG_LABELS = {"DEBUG", "INFO", "WARN", "ERROR"};
    
    
    public final static int LEVEL_DEBUG = 0;
    public final static int LEVEL_INFO  = 1;
    public final static int LEVEL_WARN  = 2;
    public final static int LEVEL_ERROR = 3;
    public final static int LEVEL_NONE  = 4;

    private int m_level = LEVEL_INFO;
    private SimpleDateFormat m_fmt;
    
    
    /**
     * Set logger - does nothing.
     */
    public void setLogger(Logger logger) {
    }
    
    /**
     * Configure - set the level
     */
    public void configure(Configuration conf) throws FtpException {
        m_level = conf.getInt("level", LEVEL_INFO);
        m_fmt   = new SimpleDateFormat(conf.getString("date-format", "yyyy-MM-dd'T'HH:mm:ss"), Locale.US);
    }
    
    /**
     * Destroy - does nothing
     */
    public void dispose() {
    }
    
    /**
     * Get date format.
     */
    public DateFormat getDateFormat() {
        return m_fmt;
    }
     
    /**
     * Check the log enable status
     */
    protected boolean canBeWritten(int level) {
        return level >= m_level;
    }
    
    /**
     * Write debug message
     */
    public void debug(String msg) {
        if(canBeWritten(LEVEL_DEBUG)) {
            write(LEVEL_DEBUG, msg);
        }
    }

    /**
     * Write debug throwable object
     */
    public void debug(String msg, Throwable th) {
        if(canBeWritten(LEVEL_DEBUG)) {
            write(LEVEL_DEBUG, msg, th);
        }
    }    
    
    /**
     * Write info message
     */
    public void info(String msg) {
        if(canBeWritten(LEVEL_INFO)) {
            write(LEVEL_INFO, msg);
        }
    }

    /**
     * Write info throwable object
     */
    public void info(String msg, Throwable th) {
        if(canBeWritten(LEVEL_INFO)) {
            write(LEVEL_INFO, msg, th);
        }
    }

    /**
     * Write warning message
     */
    public void warn(String msg) {
        if(canBeWritten(LEVEL_WARN)) {
            write(LEVEL_WARN, msg);
        }
    }

    /**
     * Write warning throwable object
     */
    public void warn(String msg, Throwable th) {
        if(canBeWritten(LEVEL_WARN)) {
            write(LEVEL_WARN, msg, th);
        }
    }

    /**
     * Write error message
     */
    public void error(String msg) {
        if(canBeWritten(LEVEL_ERROR)) {
            write(LEVEL_ERROR, msg);
        }
    }

    /**
     * Write warning throwable object
     */
    public void error(String msg, Throwable th) {
        if(canBeWritten(LEVEL_ERROR)) {
            write(LEVEL_ERROR, msg, th);
        }
    }    
     
    /**
     * Write log message.
     */
    protected abstract void write(int level, String msg);
    
    /**
     * Write log message and exception.
     */
    protected abstract void write(int level, String msg, Throwable th);
}
