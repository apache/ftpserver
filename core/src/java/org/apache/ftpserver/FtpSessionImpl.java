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
import java.security.cert.Certificate;
import java.util.Date;
import java.util.HashMap;

import org.apache.ftpserver.ftplet.DataType;
import org.apache.ftpserver.ftplet.FileObject;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.DataConnectionFactory;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.ftplet.Structure;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.interfaces.FtpServerContext;
import org.apache.ftpserver.interfaces.FtpServerSession;
import org.apache.ftpserver.listener.Listener;

/**
 * FTP session
 */
public class FtpSessionImpl implements FtpServerSession {

    
    /**
     * Contains user name between USER and PASS commands
     */
    private String userArgument;
    private User user;
    private HashMap attributeMap;
    private InetAddress remoteAddr;
    private InetAddress serverAddr;
    private int serverPort;
    private String language;
    private Certificate[] clientCertificates;
    
    private int maxIdleTime = 0;
    private long connectionTime = 0L;
    private long loginTime = 0L;
    private long lastAccessTime = 0L;
    
    private IODataConnectionFactory dataConnection;
    private FileSystemView fileSystemView;
    
    private FileObject renameFrom;
    private long fileOffset;
    private FtpRequest request;
    
    private DataType dataType    = DataType.ASCII;
    private Structure structure  = Structure.FILE;
    private FtpServerContext serverContext;
    private Listener listener;
    
    public Listener getListener() {
        return listener;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    /**
     * Default constructor.
     */
    public FtpSessionImpl(FtpServerContext serverContext) {
        this.serverContext = serverContext;
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
    public void setFtpDataConnection(IODataConnectionFactory dataCon) {
        dataConnection = dataCon;
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
    public DataConnectionFactory getDataConnection() {
        return dataConnection;
    }

    /**
     * Get FTP data connection.
     */
    public ServerDataConnectionFactory getServerDataConnection() {
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
    public InetAddress getClientAddress() {
        return remoteAddr;
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
    
    
    /**
     * Get the data type.
     */
    public DataType getDataType() {
        return dataType;
    }
    
    /**
     * Set the data type.
     */
    public void setDataType(DataType type) {
        dataType = type;
    }

    /**
     * Get structure.
     */
    public Structure getStructure() {
        return structure;
    }
    
    /**
     * Set structure
     */
    public void setStructure(Structure stru) {
        structure = stru;
    }
    
    public Certificate[] getClientCertificates() {
        return clientCertificates;
    }
    
    public void setClientCertificates(Certificate[] certificates) {
        this.clientCertificates = certificates;
    }

    public InetAddress getServerAddress() {
        return serverAddr;
    }

    public void setServerAddress(InetAddress adress) {
        this.serverAddr = adress;
    }

    public FtpServerContext getServerContext() {
        return serverContext;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }
}
