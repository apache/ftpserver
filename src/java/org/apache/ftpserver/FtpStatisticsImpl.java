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
package org.apache.ftpserver;

import java.util.Date;

import org.apache.ftpserver.ftplet.Configuration;
import org.apache.ftpserver.ftplet.FileObject;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.Logger;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.interfaces.FileObserver;
import org.apache.ftpserver.interfaces.IConnection;
import org.apache.ftpserver.interfaces.IFtpStatistics;
import org.apache.ftpserver.interfaces.StatisticsObserver;

/**
 * This is ftp statistice implementation.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class FtpStatisticsImpl implements IFtpStatistics {

    private StatisticsObserver m_observer = null;
    private FileObserver m_fileObserver   = null;
    
    private Date m_startTime         = new Date();
    
    private int m_uploadCount        = 0;
    private int m_downloadCount      = 0;
    private int m_deleteCount        = 0;
    
    private int m_mkdirCount         = 0;
    private int m_rmdirCount         = 0;
    
    private int m_currLogins         = 0;
    private int m_totalLogins        = 0;
    
    private int m_currAnonLogins     = 0;
    private int m_totalAnonLogins    = 0;
    
    private int m_currConnections    = 0;
    private int m_totalConnections   = 0;
    
    private long m_bytesUpload       = 0L;
    private long m_bytesDownload     = 0L;
    
    
    /**
     * Set the logger object - does nothing.
     */
    public void setLogger(Logger logger) {
    }
    
    /**
     * Configure component - does nothing.
     */
    public void configure(Configuration conf) throws FtpException {
    }
    
    /**
     * Dispose component - does nothing.
     */
    public void dispose() {
    }
    
    /**
     * Set the observer.
     */
    public void setObserver(StatisticsObserver observer) {
        m_observer = observer;
    }
    
    /**
     * Set the file observer.
     */
    public void setFileObserver(FileObserver observer) {
        m_fileObserver = observer;
    }
    
    
    ////////////////////////////////////////////////////////
    /////////////////  All getter methods  /////////////////
    /**
     * Get server start time.
     */
    public Date getStartTime() {
        return m_startTime;
    }
     
    /**
     * Get number of files uploaded.
     */
    public int getTotalUploadNumber() {
        return m_uploadCount;
    }
    
    /**
     * Get number of files downloaded.
     */
    public int getTotalDownloadNumber() {
        return m_downloadCount;
    }
    
    /**
     * Get number of files deleted.
     */
    public int getTotalDeleteNumber() {
        return m_deleteCount;
    }
     
    /**
     * Get total number of bytes uploaded.
     */
    public long getTotalUploadSize() {
        return m_bytesUpload;
    }
     
    /**
     * Get total number of bytes downloaded.
     */
    public long getTotalDownloadSize() {
        return m_bytesDownload;
    }
    
    /**
     * Get total directory created.
     */
    public int getTotalDirectoryCreated() {
        return m_mkdirCount;
    }
    
    /**
     * Get total directory removed.
     */
    public int getTotalDirectoryRemoved() {
        return m_mkdirCount;
    }
    
    /**
     * Get total number of connections.
     */
    public int getTotalConnectionNumber() {
        return m_totalConnections;
    }
     
    /**
     * Get current number of connections.
     */
    public int getCurrentConnectionNumber() {
        return m_currConnections;
    }
    
    /**
     * Get total number of logins.
     */
    public int getTotalLoginNumber() {
        return m_totalLogins;
    }
     
    /**
     * Get current number of logins.
     */
    public int getCurrentLoginNumber() {
        return m_currLogins;
    }
    
    /**
     * Get total number of anonymous logins.
     */
    public int getTotalAnonymousLoginNumber() {
        return m_totalAnonLogins;
    }
     
    /**
     * Get current number of anonymous logins.
     */
    public int getCurrentAnonymousLoginNumber() {
        return m_currAnonLogins;
    }
    
    
    ////////////////////////////////////////////////////////
    /////////////////  All setter methods  /////////////////
    /**
     * Increment upload count.
     */
    public void setUpload(IConnection connection, FileObject file, long size) {
        ++m_uploadCount;
        m_bytesUpload += size;
        notifyUpload(connection, file, size);
    }
     
    /**
     * Increment download count.
     */
    public void setDownload(IConnection connection, FileObject file, long size) {
        ++m_downloadCount;
        m_bytesDownload += size;
        notifyDownload(connection, file, size);
    }
     
    /**
     * Increment delete count.
     */
    public void setDelete(IConnection connection, FileObject file) {
        ++m_deleteCount;
        notifyDelete(connection, file);
    }
     
    /**
     * Increment make directory count.
     */
    public void setMkdir(IConnection connection, FileObject file) {
        ++m_mkdirCount;
        notifyMkdir(connection, file);
    }
    
    /**
     * Increment remove directory count.
     */
    public void setRmdir(IConnection connection, FileObject file) {
        ++m_rmdirCount;
        notifyRmdir(connection, file);
    }
    
    /**
     * Increment open connection count.
     */
    public void setOpenConnection(IConnection connection) {
        ++m_currConnections;
        ++m_totalConnections;
        notifyOpenConnection(connection);
    }
    
    /**
     * Decrement open connection count.
     */
    public void setCloseConnection(IConnection connection) {
        --m_currConnections;
        notifyCloseConnection(connection);
    }
    
    /**
     * New login.
     */
    public void setLogin(IConnection connection) {
        ++m_currLogins;
        ++m_totalLogins;
        User user = connection.getRequest().getUser();
        if( "anonymous".equals(user.getName()) ) {
            ++m_currAnonLogins;
            ++m_totalAnonLogins;
        }
        notifyLogin(connection);
    }
     
    /**
     * User logout
     */
    public void setLogout(IConnection connection) {
        --m_currLogins;
        User user = connection.getRequest().getUser();
        if( "anonymous".equals(user.getName()) ) {
            --m_currAnonLogins;
        }
        notifyLogout(connection);
    }
    
    
    ////////////////////////////////////////////////////////////
    ///////////////// all observer methods  ////////////////////
    /**               
     * Observer upload notification.
     */
    private void notifyUpload(IConnection connection, FileObject file, long size) {
        StatisticsObserver observer = m_observer;
        if (observer != null) {
            observer.notifyUpload();
        }

        FileObserver fileObserver = m_fileObserver;
        if (fileObserver != null) {
            fileObserver.notifyUpload(connection, file, size);
        }
    }
    
    /**               
     * Observer download notification.
     */
    private void notifyDownload(IConnection connection, FileObject file, long size) {
        StatisticsObserver observer = m_observer;
        if (observer != null) {
            observer.notifyDownload();
        }

        FileObserver fileObserver = m_fileObserver;
        if (fileObserver != null) {
            fileObserver.notifyDownload(connection, file, size);
        }
    }
    
    /**               
     * Observer delete notification.
     */
    private void notifyDelete(IConnection connection, FileObject file) {
        StatisticsObserver observer = m_observer;
        if (observer != null) {
            observer.notifyDelete();
        }

        FileObserver fileObserver = m_fileObserver;
        if (fileObserver != null) {
            fileObserver.notifyDelete(connection, file);
        }
    }
    
    /**               
     * Observer make directory notification.
     */
    private void notifyMkdir(IConnection connection, FileObject file) {
        StatisticsObserver observer = m_observer;
        if (observer != null) {
            observer.notifyMkdir();
        }

        FileObserver fileObserver = m_fileObserver;
        if (fileObserver != null) {
            fileObserver.notifyMkdir(connection, file);
        }
    }
    
    /**               
     * Observer remove directory notification.
     */
    private void notifyRmdir(IConnection connection, FileObject file) {
        StatisticsObserver observer = m_observer;
        if (observer != null) {
            observer.notifyRmdir();
        }

        FileObserver fileObserver = m_fileObserver;
        if (fileObserver != null) {
            fileObserver.notifyRmdir(connection, file);
        }
    }
    
    /**
     * Observer open connection notification.
     */
    private void notifyOpenConnection(IConnection connection) {
        StatisticsObserver observer = m_observer;
        if (observer != null) {
            observer.notifyOpenConnection();
        }
    } 
    
    /**
     * Observer close connection notification.
     */
    private void notifyCloseConnection(IConnection connection) {
        StatisticsObserver observer = m_observer;
        if (observer != null) {
            observer.notifyCloseConnection();
        }
    } 
    
    /**
     * Observer login notification.
     */
    private void notifyLogin(IConnection connection) {
        StatisticsObserver observer = m_observer;
        if (observer != null) {
            
            // is anonymous login
            User user = connection.getRequest().getUser();
            boolean anonymous = false;
            if(user != null) {
                String login = user.getName();
                anonymous = (login != null) && login.equals("anonymous");
            }
            observer.notifyLogin(anonymous);
        }
    }
    
    /**
     * Observer logout notification.
     */
    private void notifyLogout(IConnection connection) {
        StatisticsObserver observer = m_observer;
        if (observer != null) {
            // is anonymous login
            User user = connection.getRequest().getUser();
            boolean anonymous = false;
            if(user != null) {
                String login = user.getName();
                anonymous = (login != null) && login.equals("anonymous");
            }
            observer.notifyLogout(anonymous);
        }
    } 
}
