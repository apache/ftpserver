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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Writer;
import java.util.Date;

import org.apache.ftpserver.ftplet.Configuration;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.util.DateUtils;
import org.apache.ftpserver.util.IoUtils;

/**
 * Log class to write log data. It uses <code>RandomAccessFile</code>.
 * If the log file size exceeds the limit, a new log file will be created.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class FileLogger extends AbstractLogger {

    private final static long NO_LIMIT = 0;
    
    private Writer m_writer;
    private long m_currentSize;
    
    private File m_logDir;
    private File m_file;
    private long m_maxSize = NO_LIMIT;
    private boolean m_autoFlush;
    

    /**
     * Configure the logger
     */
    public void configure(Configuration conf) throws FtpException {
        super.configure(conf);
        
        m_logDir = new File(conf.getString("dir", "./res/log"));
        m_file = new File(m_logDir, "log.gen");
        m_maxSize = conf.getLong("max-size", NO_LIMIT);
        m_autoFlush = conf.getBoolean("auto-flush", true);
        
        open();
    }

    /**
     * Open log file.
     */
    private synchronized void open() throws FtpException {
        FileWriter fw = null;
        try {
            
            // create directory if does not exist
            File logDir = m_file.getParentFile();
            if( (!logDir.exists()) && (!logDir.mkdirs()) ) {
                String dirName = logDir.getAbsolutePath();
                throw new FtpException("Cannot create directory : " + dirName);
            }
            
            RandomAccessFile raf = new RandomAccessFile(m_file, "rw");
            raf.seek(raf.length());
            fw = new FileWriter(raf.getFD()); 
            m_writer = new BufferedWriter(fw);
        } 
        catch (IOException ex) {
            IoUtils.close(fw);
            m_writer = null;
            throw new FtpException("FileLogger.open()", ex);
        }
    }

    /**
     * Write log message
     */
    protected void write(int level, String msg) {
        String logStr = "[" + getDateFormat().format(new Date()) + "] " +
                        "(" + LOG_LABELS[level] + ") " + 
                        msg + LINE_SEP;
        write(logStr);
    }

    /**
     * Write log message and exception
     */
    protected void write(int level, String msg, Throwable th) {
        String logStr = "[" + getDateFormat().format(new Date()) + "] " +
                        "(" + LOG_LABELS[level] + ") " + 
                        msg + LINE_SEP + IoUtils.getStackTrace(th);
        write(logStr);
    }
    
    /**
     * Write string.
     */
    protected void write(String msg) {
        if (m_writer != null) {

            // save file if needed
            long strLen = msg.length();
            if ( (m_maxSize > NO_LIMIT) && ((strLen + m_currentSize) > m_maxSize) ) {
                save();
            }
            
            // now write it
            try {
                m_writer.write(msg);
                if (m_autoFlush) {
                    m_writer.flush();
                }
                m_currentSize += strLen;
            } 
            catch (Exception ex) {
                IoUtils.close(m_writer);
                m_writer = null;
            }
        }
    }
    
    /**
     * Set the log file and open a new log file.
     * Returns the name of the saved log file.
     */
    private synchronized void save() {
        try {
            dispose();
            
            String toFileName = DateUtils.getISO8601Date(System.currentTimeMillis()) + "-log.gen";
            File toFile = new File(m_logDir, toFileName);
            toFile = IoUtils.getUniqueFile(toFile);
            m_file.renameTo(toFile);
            open();
        } 
        catch (Exception ex) {
            m_writer = null;
        }
    } 


    /**
     * Dispose - close stream
     */
    public void dispose() {
        if (m_writer != null){
            IoUtils.close(m_writer);
            m_writer = null;
        }
        super.dispose();
    }
}
