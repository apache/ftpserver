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

import java.net.InetAddress;
import java.util.Date;
import java.util.Hashtable;

import org.apache.commons.logging.LogFactory;
import org.apache.ftpserver.ftplet.Configuration;
import org.apache.ftpserver.ftplet.FileObject;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.interfaces.FileObserver;
import org.apache.ftpserver.interfaces.Connection;
import org.apache.ftpserver.interfaces.ServerFtpStatistics;
import org.apache.ftpserver.interfaces.StatisticsObserver;

/**
 * This is ftp statistice implementation.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public class FtpStatisticsImpl implements ServerFtpStatistics {

    private StatisticsObserver observer = null;
    private FileObserver fileObserver   = null;
    
    private Date startTime         = new Date();
    
    private int uploadCount        = 0;
    private int downloadCount      = 0;
    private int deleteCount        = 0;
    
    private int mkdirCount         = 0;
    private int rmdirCount         = 0;
    
    private int currLogins         = 0;
    private int totalLogins        = 0;
    
    private int currAnonLogins     = 0;
    private int totalAnonLogins    = 0;
    
    private int currConnections    = 0;
    private int totalConnections   = 0;
    
    private long bytesUpload       = 0L;
    private long bytesDownload     = 0L;
    
    /**
     *The user login information. The structure of the hashtable: 
     * userLoginTable {user.getName ==> Hashtable {IP_address String ==> Login_Number}}
     */
    Hashtable userLoginTable = new Hashtable();
    
    public static final String LOGIN_NUMBER = "login_number";
    
    
    /**
     * Set the log factory - does nothing.
     */
    public void setLogFactory(LogFactory factory) {
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
        this.observer = observer;
    }
    
    /**
     * Set the file observer.
     */
    public void setFileObserver(FileObserver observer) {
        fileObserver = observer;
    }
    
    
    ////////////////////////////////////////////////////////
    /////////////////  All getter methods  /////////////////
    /**
     * Get server start time.
     */
    public Date getStartTime() {
        return startTime;
    }
     
    /**
     * Get number of files uploaded.
     */
    public int getTotalUploadNumber() {
        return uploadCount;
    }
    
    /**
     * Get number of files downloaded.
     */
    public int getTotalDownloadNumber() {
        return downloadCount;
    }
    
    /**
     * Get number of files deleted.
     */
    public int getTotalDeleteNumber() {
        return deleteCount;
    }
     
    /**
     * Get total number of bytes uploaded.
     */
    public long getTotalUploadSize() {
        return bytesUpload;
    }
     
    /**
     * Get total number of bytes downloaded.
     */
    public long getTotalDownloadSize() {
        return bytesDownload;
    }
    
    /**
     * Get total directory created.
     */
    public int getTotalDirectoryCreated() {
        return mkdirCount;
    }
    
    /**
     * Get total directory removed.
     */
    public int getTotalDirectoryRemoved() {
        return mkdirCount;
    }
    
    /**
     * Get total number of connections.
     */
    public int getTotalConnectionNumber() {
        return totalConnections;
    }
     
    /**
     * Get current number of connections.
     */
    public int getCurrentConnectionNumber() {
        return currConnections;
    }
    
    /**
     * Get total number of logins.
     */
    public int getTotalLoginNumber() {
        return totalLogins;
    }
     
    /**
     * Get current number of logins.
     */
    public int getCurrentLoginNumber() {
        return currLogins;
    }
    
    /**
     * Get total number of anonymous logins.
     */
    public int getTotalAnonymousLoginNumber() {
        return totalAnonLogins;
    }
     
    /**
     * Get current number of anonymous logins.
     */
    public int getCurrentAnonymousLoginNumber() {
        return currAnonLogins;
    }
    
    /**
     * Get the login number for the specific user
     */
    public int getCurrentUserLoginNumber(User user) {
      Hashtable statisticsTable = (Hashtable) userLoginTable.get(user.getName());
      if(statisticsTable == null){//not found the login user's statistics info
        return 0;
      } else{
       Integer loginNumber = (Integer) statisticsTable.get(LOGIN_NUMBER);
       if(loginNumber == null){
         return 0;
       } else{
         return loginNumber.intValue();
       }
      }
    }

    /**
     * Get the login number for the specific user from the ipAddress
     * @param user login user account
     * @param ipAddress the ip address of the remote user
     */
    public int getCurrentUserLoginNumber(User user, InetAddress ipAddress) {
      Hashtable statisticsTable = (Hashtable) userLoginTable.get(user.getName());
      if(statisticsTable == null){//not found the login user's statistics info
        return 0;
      } else{
        Integer loginNumber = (Integer) statisticsTable.get(ipAddress.getHostAddress());
        if(loginNumber == null){
          return 0;
        } else{
          return loginNumber.intValue();
        }
      }
    }
    
    
    ////////////////////////////////////////////////////////
    /////////////////  All setter methods  /////////////////
    /**
     * Increment upload count.
     */
    public void setUpload(Connection connection, FileObject file, long size) {
        ++uploadCount;
        bytesUpload += size;
        notifyUpload(connection, file, size);
    }
     
    /**
     * Increment download count.
     */
    public void setDownload(Connection connection, FileObject file, long size) {
        ++downloadCount;
        bytesDownload += size;
        notifyDownload(connection, file, size);
    }
     
    /**
     * Increment delete count.
     */
    public void setDelete(Connection connection, FileObject file) {
        ++deleteCount;
        notifyDelete(connection, file);
    }
     
    /**
     * Increment make directory count.
     */
    public void setMkdir(Connection connection, FileObject file) {
        ++mkdirCount;
        notifyMkdir(connection, file);
    }
    
    /**
     * Increment remove directory count.
     */
    public void setRmdir(Connection connection, FileObject file) {
        ++rmdirCount;
        notifyRmdir(connection, file);
    }
    
    /**
     * Increment open connection count.
     */
    public void setOpenConnection(Connection connection) {
        ++currConnections;
        ++totalConnections;
        notifyOpenConnection(connection);
    }
    
    /**
     * Decrement open connection count.
     */
    public void setCloseConnection(Connection connection) {
        --currConnections;
        notifyCloseConnection(connection);
    }
    
    /**
     * New login.
     */
    public void setLogin(Connection connection) {
        ++currLogins;
        ++totalLogins;
        User user = connection.getRequest().getUser();
        if( "anonymous".equals(user.getName()) ) {
            ++currAnonLogins;
            ++totalAnonLogins;
        }
        
        synchronized(user){//thread safety is needed. Since the login occurrs at low frequency, this overhead is endurable
          Hashtable statisticsTable = (Hashtable) userLoginTable.get(user.getName());
          if(statisticsTable == null){
            //the hash table that records the login information of the user and its ip address.
            //structure: IP_Address String ==> login_number
            statisticsTable = new Hashtable();
            userLoginTable.put(user.getName(), statisticsTable);
            //new login, put 1 in the login number
            statisticsTable.put(LOGIN_NUMBER, new Integer(1));
            statisticsTable.put(connection.getRequest().getRemoteAddress().getHostAddress(), new Integer(1));
          } else{
            Integer loginNumber = (Integer) statisticsTable.get(LOGIN_NUMBER);
            statisticsTable.put(LOGIN_NUMBER, new Integer(loginNumber.intValue() + 1));
            Integer loginNumberPerIP = (Integer) statisticsTable.get(connection.getRequest().getRemoteAddress().getHostAddress());
            if(loginNumberPerIP == null){//new connection from this ip
              statisticsTable.put(connection.getRequest().getRemoteAddress().getHostAddress(), new Integer(1));
            } else{//this ip has connections already
              statisticsTable.put(connection.getRequest().getRemoteAddress().getHostAddress(), new Integer(loginNumberPerIP.intValue() + 1));
            }
          }
        }
        
        notifyLogin(connection);
    }
     
    /**
     * User logout
     */
    public void setLogout(Connection connection) {
        --currLogins;
        User user = connection.getRequest().getUser();
        if( "anonymous".equals(user.getName()) ) {
            --currAnonLogins;
        }
        
        synchronized(user){
          Hashtable statisticsTable = (Hashtable) userLoginTable.get(user.getName());
          Integer loginNumber = (Integer) statisticsTable.get(LOGIN_NUMBER);
          statisticsTable.put(LOGIN_NUMBER, new Integer(loginNumber.intValue() - 1));
          Integer loginNumberPerIP = (Integer) statisticsTable.get(connection.getRequest().getRemoteAddress().getHostAddress());
          if(loginNumberPerIP != null){//this should always be true
            if(loginNumberPerIP.intValue() <= 1){//the last login from this ip, remove this ip address
              statisticsTable.remove(connection.getRequest().getRemoteAddress().getHostAddress());
            }
          } else{//this ip has other logins, reduce the number
            statisticsTable.put(connection.getRequest().getRemoteAddress().getHostAddress(), new Integer(loginNumberPerIP.intValue() - 1));
          }
        }
        
        notifyLogout(connection);
    }
    
    
    ////////////////////////////////////////////////////////////
    ///////////////// all observer methods  ////////////////////
    /**               
     * Observer upload notification.
     */
    private void notifyUpload(Connection connection, FileObject file, long size) {
        StatisticsObserver observer = this.observer;
        if (observer != null) {
            observer.notifyUpload();
        }

        FileObserver fileObserver = this.fileObserver;
        if (fileObserver != null) {
            fileObserver.notifyUpload(connection, file, size);
        }
    }
    
    /**               
     * Observer download notification.
     */
    private void notifyDownload(Connection connection, FileObject file, long size) {
        StatisticsObserver observer = this.observer;
        if (observer != null) {
            observer.notifyDownload();
        }

        FileObserver fileObserver = this.fileObserver;
        if (fileObserver != null) {
            fileObserver.notifyDownload(connection, file, size);
        }
    }
    
    /**               
     * Observer delete notification.
     */
    private void notifyDelete(Connection connection, FileObject file) {
        StatisticsObserver observer = this.observer;
        if (observer != null) {
            observer.notifyDelete();
        }

        FileObserver fileObserver = this.fileObserver;
        if (fileObserver != null) {
            fileObserver.notifyDelete(connection, file);
        }
    }
    
    /**               
     * Observer make directory notification.
     */
    private void notifyMkdir(Connection connection, FileObject file) {
        StatisticsObserver observer = this.observer;
        if (observer != null) {
            observer.notifyMkdir();
        }

        FileObserver fileObserver = this.fileObserver;
        if (fileObserver != null) {
            fileObserver.notifyMkdir(connection, file);
        }
    }
    
    /**               
     * Observer remove directory notification.
     */
    private void notifyRmdir(Connection connection, FileObject file) {
        StatisticsObserver observer = this.observer;
        if (observer != null) {
            observer.notifyRmdir();
        }

        FileObserver fileObserver = this.fileObserver;
        if (fileObserver != null) {
            fileObserver.notifyRmdir(connection, file);
        }
    }
    
    /**
     * Observer open connection notification.
     */
    private void notifyOpenConnection(Connection connection) {
        StatisticsObserver observer = this.observer;
        if (observer != null) {
            observer.notifyOpenConnection();
        }
    } 
    
    /**
     * Observer close connection notification.
     */
    private void notifyCloseConnection(Connection connection) {
        StatisticsObserver observer = this.observer;
        if (observer != null) {
            observer.notifyCloseConnection();
        }
    } 
    
    /**
     * Observer login notification.
     */
    private void notifyLogin(Connection connection) {
        StatisticsObserver observer = this.observer;
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
    private void notifyLogout(Connection connection) {
        StatisticsObserver observer = this.observer;
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
