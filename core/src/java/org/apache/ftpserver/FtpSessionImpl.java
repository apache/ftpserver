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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import org.apache.ftpserver.ftplet.FileObject;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.ftplet.FtpSession;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.interfaces.ConnectionObserver;

/**
 * FTP session
 */
public class FtpSessionImpl implements FtpSession {

    
    /**
     * Contains user name between USER and PASS commands
     */
    private String userArgument;
    private User user;
    private HashMap attributeMap;
    private InetAddress remoteAddr;
    private ConnectionObserver observer;
    private String language;
    
    private int maxIdleTime = 0;
    private long connectionTime = 0L;
    private long loginTime = 0L;
    private long lastAccessTime = 0L;
    
    private FtpDataConnection dataConnection;
    private FileSystemView fileSystemView;
    
    private FileObject renameFrom;
    private long fileOffset;
    private FtpRequest request;
    
    /**
     * Default constructor.
     */
    public FtpSessionImpl() {
        attributeMap = new HashMap();
        userArgument = null;
        user = null;
        connectionTime = System.currentTimeMillis();
    } 
    
    /**
     * Set client address.
     */
    public void setClientAddress(InetAddress addr) {
        remoteAddr = addr;
    }

    /**
     * Set FTP data connection.
     */
    public void setFtpDataConnection(FtpDataConnection dataCon) {
        dataConnection = dataCon;
    }
    
    /**
     * Get the observer object to get what the user is sending.
     */
    public void setObserver(ConnectionObserver observer) {
        this.observer = observer;
    } 
    
    /**
     * Reset temporary state variables.
     */
    public void resetState() {
        renameFrom = null;
        fileOffset = 0L;
    }
    
    /**
     * Reinitialize request.
     */
    public void reinitialize() {
        userArgument = null;
        user = null;
        loginTime = 0L;
        fileSystemView = null;
        renameFrom = null;
        fileOffset = 0L;
    }
    
    /**
     * Spy print. Monitor user request.
     */
    private void spyRequest(String str) {
        ConnectionObserver observer = this.observer;
        if(observer != null) {
            observer.request(str + "\r\n");
        }
    }
    
    /**
     * Set login attribute & user file system view.
     */
    public void setLogin(FileSystemView userFsView) {
        loginTime = System.currentTimeMillis();
        fileSystemView = userFsView;
    }
    
    /**
     * Set logout.
     */
    public void setLogout() {
        loginTime = 0L;
    }
    
    /**
     * Update last access time.
     */
    public void updateLastAccessTime() {
        lastAccessTime = System.currentTimeMillis();
    }
    
    /**
     * Is logged-in
     */
    public boolean isLoggedIn() {
        return (loginTime != 0L);
    }
    
    /**
     * Get FTP data connection.
     */
    public FtpDataConnection getFtpDataConnection() {
        return dataConnection;
    }
    
    /**
     * Get file system view.
     */
    public FileSystemView getFileSystemView() {
        return fileSystemView;
    }
    
    /**
     * Get connection time.
     */
    public Date getConnectionTime() {
        return new Date(connectionTime);
    }
    
    /**
     * Get the login time.
     */
    public Date getLoginTime() {
        return new Date(loginTime);
    }
    
    /**
     * Get last access time.
     */
    public Date getLastAccessTime() {
        return new Date(lastAccessTime);
    }
    
    /**
     * Get file offset.
     */
    public long getFileOffset() {
        return fileOffset;
    }
    
    /**
     * Set the file offset.
     */
    public void setFileOffset(long offset) {
        fileOffset = offset;
    }
    
    /**
     * Get rename from file object.
     */
    public FileObject getRenameFrom() {
        return renameFrom;
    }
    
    /**
     * Set rename from.
     */
    public void setRenameFrom(FileObject file) {
        renameFrom = file;
    }

    /**
     * Returns user name entered in USER command
     * 
     * @return user name entered in USER command
     */
    public String getUserArgument() {
        return userArgument;
    }

    /**
     * Set user name entered from USER command
     */
    public void setUserArgument(String tmpUserName) {
        this.userArgument = tmpUserName;
    }

    
    /**
     * Get language.
     */
    public String getLanguage() {
        return language;
    }
    
    /**
     * Set language.
     */
    public void setLanguage(String language) {
        this.language = language;
    }
    
    /**
     * Get user.
     */
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
	this.user = user;
    }
    
    /**
     * Get remote address
     */
    public InetAddress getRemoteAddress() {
        return remoteAddr;
    }
    
    /**
     * Get data input stream. The return value will never be null.
     */
    public InputStream getDataInputStream() throws IOException {
        try {
            
            // get data socket
            Socket dataSoc = dataConnection.getDataSocket();
            if(dataSoc == null) {
                throw new IOException("Cannot open data connection.");
            }
            
            // create input stream
            InputStream is = dataSoc.getInputStream();
            if(dataConnection.isZipMode()) {
                is = new InflaterInputStream(is);
            }
            return is;
        }
        catch(IOException ex) {
            dataConnection.closeDataSocket();
            throw ex;
        }
    }
    
    /**
     * Get data output stream. The return value will never be null.
     */
    public OutputStream getDataOutputStream() throws IOException {
        try {
            
            // get data socket
            Socket dataSoc = dataConnection.getDataSocket();
            if(dataSoc == null) {
                throw new IOException("Cannot open data connection.");
            }
            
            // create output stream
            OutputStream os = dataSoc.getOutputStream();
            if(dataConnection.isZipMode()) {
                os = new DeflaterOutputStream(os);
            }
            return os;
        }
        catch(IOException ex) {
            dataConnection.closeDataSocket();
            throw ex;
        }
    }
    
    /**
     * Get attribute
     */
    public Object getAttribute(String name) {
        return attributeMap.get(name);
    }
    
    /**
     * Set attribute.
     */
    public void setAttribute(String name, Object value) {
        attributeMap.put(name, value);
    }
    
    /**
     * Remove attribute.
     */
    public void removeAttribute(String name) {
        attributeMap.remove(name);
    }
    
    /**
     * Remove all attributes.
     */
    public void clear() {
        attributeMap.clear();
    }
    
    /**
     * It checks the request timeout.
     * Compares the last access time with the specified time.
     */
    public boolean isTimeout(long currTime) {
         boolean bActive = true;
         int maxIdleTime = getMaxIdleTime();
         if(maxIdleTime > 0) {
             long currIdleTimeMillis = currTime - lastAccessTime;
             long maxIdleTimeMillis = maxIdleTime * 1000L;
             bActive = currIdleTimeMillis <= maxIdleTimeMillis;
         }
         return !bActive;
    }

    /**
     * Check request timeout.
     */
    public boolean isTimeout() {
        return isTimeout(System.currentTimeMillis());
    }

    public int getMaxIdleTime() {
        return this.maxIdleTime;
    }

    public void setMaxIdleTime(int maxIdleTimeSec) {
        this.maxIdleTime = maxIdleTimeSec;
    }

    public FtpRequest getCurrentRequest() {
        return request;
    }
    
    public void setCurrentRequest(FtpRequest request) {
        this.request = request;
    }
}
