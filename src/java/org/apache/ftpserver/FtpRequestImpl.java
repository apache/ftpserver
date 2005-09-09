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

import org.apache.ftpserver.ftplet.FileObject;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.interfaces.ConnectionObserver;
import org.apache.ftpserver.usermanager.BaseUser;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

/**
 * FTP request object.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public
class FtpRequestImpl implements FtpRequest {
    
    private String m_line;
    private String m_command;
    private String m_argument;
    
    private User m_user;
    private HashMap m_attributeMap;
    private InetAddress m_remoteAddr;
    private ConnectionObserver m_observer;
    private String m_language;
    
    private long m_connectionTime = 0L;
    private long m_loginTime = 0L;
    private long m_lastAccessTime = 0L;
    
    private FtpDataConnection m_dataConnection;
    private FileSystemView m_fileSystemView;
    
    private FileObject m_renameFrom;
    private long m_fileOffset;
    
    /**
     * Default constructor.
     */
    public FtpRequestImpl() {
        m_attributeMap = new HashMap();
        m_user = new BaseUser();
        m_connectionTime = System.currentTimeMillis();
    } 
    
    /**
     * Set client address.
     */
    public void setClientAddress(InetAddress addr) {
        m_remoteAddr = addr;
    }

    /**
     * Set FTP data connection.
     */
    public void setFtpDataConnection(FtpDataConnection dataCon) {
        m_dataConnection = dataCon;
    }
    
    /**
     * Get the observer object to get what the user is sending.
     */
    public void setObserver(ConnectionObserver observer) {
        m_observer = observer;
    } 
    
    /**
     * Reset temporary state variables.
     */
    public void resetState() {
        m_renameFrom = null;
        m_fileOffset = 0L;
    }
    
    /**
     * Reinitialize request.
     */
    public void reinitialize() {
        m_user = new BaseUser();
        m_loginTime = 0L;
        m_fileSystemView = null;
        m_renameFrom = null;
        m_fileOffset = 0L;
    }
    
    /**
     * Parse the ftp command line.
     */
    public void parse(String line) {
        
        // notify connection observer
        spyRequest(line);
        
        // parse request
        m_line = line;
        m_command = null;
        m_argument = null;
        int spInd = m_line.indexOf(' ');
        if(spInd != -1) {
            m_argument = m_line.substring(spInd + 1);
            m_command = m_line.substring(0, spInd).toUpperCase();
        }
        else {
            m_command = m_line.toUpperCase();
        }
        
        if( (m_command.length()>0) && (m_command.charAt(0)=='X') ) {
            m_command = m_command.substring(1);
        }
    }
    
    /**
     * Spy print. Monitor user request.
     */
    private void spyRequest(String str) {
        ConnectionObserver observer = m_observer;
        if(observer != null) {
            observer.request(str + "\r\n");
        }
    }
    
    /**
     * Set login attribute & user file system view.
     */
    public void setLogin(FileSystemView userFsView) {
        m_loginTime = System.currentTimeMillis();
        m_fileSystemView = userFsView;
    }
    
    /**
     * Set logout.
     */
    public void setLogout() {
        m_loginTime = 0L;
    }
    
    /**
     * Update last access time.
     */
    public void updateLastAccessTime() {
        m_lastAccessTime = System.currentTimeMillis();
    }
    
    /**
     * Is logged-in
     */
    public boolean isLoggedIn() {
        return (m_loginTime != 0L);
    }
    
    /**
     * Get FTP data connection.
     */
    public FtpDataConnection getFtpDataConnection() {
        return m_dataConnection;
    }
    
    /**
     * Get file system view.
     */
    public FileSystemView getFileSystemView() {
        return m_fileSystemView;
    }
    
    /**
     * Get connection time.
     */
    public Date getConnectionTime() {
        return new Date(m_connectionTime);
    }
    
    /**
     * Get the login time.
     */
    public Date getLoginTime() {
        return new Date(m_loginTime);
    }
    
    /**
     * Get last access time.
     */
    public Date getLastAccessTime() {
        return new Date(m_lastAccessTime);
    }
    
    /**
     * Get file offset.
     */
    public long getFileOffset() {
        return m_fileOffset;
    }
    
    /**
     * Set the file offset.
     */
    public void setFileOffset(long offset) {
        m_fileOffset = offset;
    }
    
    /**
     * Get rename from file object.
     */
    public FileObject getRenameFrom() {
        return m_renameFrom;
    }
    
    /**
     * Set rename from.
     */
    public void setRenameFrom(FileObject file) {
        m_renameFrom = file;
    }
    
    /**
     * Get the ftp command.
     */
    public String getCommand() {
        return m_command;
    }
    
    /**
     * Get ftp input argument.  
     */ 
    public String getArgument() {
        return m_argument;
    }
    
    /**
     * Get the ftp request line.
     */
    public String getRequestLine() {
        return m_line;
    }
    
    /**
     * Has argument.
     */
    public boolean hasArgument() {
        return getArgument() != null;
    }
    
    /**
     * Get language.
     */
    public String getLanguage() {
        return m_language;
    }
    
    /**
     * Set language.
     */
    public void setLanguage(String language) {
        m_language = language;
    }
    
    /**
     * Get user.
     */
    public User getUser() {
        return m_user;
    }
    
    /**
     * Get remote address
     */
    public InetAddress getRemoteAddress() {
        return m_remoteAddr;
    }
    
    /**
     * Get data input stream.
     */
    public InputStream getDataInputStream() throws IOException {
        try {
            Socket dataSoc = m_dataConnection.getDataSocket();
            InputStream is = dataSoc.getInputStream();
            if(m_dataConnection.isZipMode()) {
                is = new InflaterInputStream(is);
            }
            return is;
        }
        catch(IOException ex) {
            m_dataConnection.closeDataSocket();
            throw ex;
        }
    }
    
    /**
     * Get data output stream.
     */
    public OutputStream getDataOutputStream() throws IOException {
        try {
            Socket dataSoc = m_dataConnection.getDataSocket();
            OutputStream os = dataSoc.getOutputStream();
            if(m_dataConnection.isZipMode()) {
                os = new DeflaterOutputStream(os);
            }
            return os;
        }
        catch(IOException ex) {
            m_dataConnection.closeDataSocket();
            throw ex;
        }
    }
    
    /**
     * Get attribute
     */
    public Object getAttribute(String name) {
        return m_attributeMap.get(name);
    }
    
    /**
     * Set attribute.
     */
    public void setAttribute(String name, Object value) {
        m_attributeMap.put(name, value);
    }
    
    /**
     * Remove attribute.
     */
    public void removeAttribute(String name) {
        m_attributeMap.remove(name);
    }
    
    /**
     * Remove all attributes.
     */
    public void clear() {
        m_attributeMap.clear();
    }
    
    /**
     * It checks the request timeout.
     * Compares the last access time with the specified time.
     */
    public boolean isTimeout(long currTime) {
         boolean bActive = true;
         int maxIdleTime = m_user.getMaxIdleTime();
         if(maxIdleTime > 0) {
             long currIdleTimeMillis = currTime - m_lastAccessTime;
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
}
