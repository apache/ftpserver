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

package org.apache.ftpserver.ftplet;

import java.net.InetAddress;
import java.security.cert.Certificate;
import java.util.Date;

/**
 * Wrapper used to inhibit an Ftplet from upcasting an FtpSession
 * to FtpServerSession or to the implementation class
 */
public class IsolatingFtpSession implements FtpSession {

    private FtpSession session;
    
    public IsolatingFtpSession(FtpSession session) {
        this.session = session;
    }
    
    public void clear() {
        session.clear();
    }

    public Object getAttribute(String name) {
        return session.getAttribute(name);
    }

    public InetAddress getClientAddress() {
        return session.getClientAddress();
    }

    public Certificate[] getClientCertificates() {
        return session.getClientCertificates();
    }

    public Date getConnectionTime() {
        return session.getConnectionTime();
    }

    public FtpRequest getCurrentRequest() {
        return session.getCurrentRequest();
    }

    public DataType getDataType() {
        return session.getDataType();
    }

    public long getFileOffset() {
        return session.getFileOffset();
    }

    public FileSystemView getFileSystemView() {
        return session.getFileSystemView();
    }

    public String getLanguage() {
        return session.getLanguage();
    }

    public Date getLastAccessTime() {
        return session.getLastAccessTime();
    }

    public Date getLoginTime() {
        return session.getLoginTime();
    }

    public int getMaxIdleTime() {
        return session.getMaxIdleTime();
    }

    public FileObject getRenameFrom() {
        return session.getRenameFrom();
    }

    public InetAddress getServerAddress() {
        return session.getServerAddress();
    }

    public int getServerPort() {
        return session.getServerPort();
    }

    public Structure getStructure() {
        return session.getStructure();
    }

    public User getUser() {
        return session.getUser();
    }

    public String getUserArgument() {
        return session.getUserArgument();
    }

    public boolean isLoggedIn() {
        return session.isLoggedIn();
    }

    public void removeAttribute(String name) {
        session.removeAttribute(name);
    }

    public void setAttribute(String name, Object value) {
        session.setAttribute(name, value);
    }

    public void setMaxIdleTime(int maxIdleTimeSec) {
        session.setMaxIdleTime(maxIdleTimeSec);
    }
    
}
