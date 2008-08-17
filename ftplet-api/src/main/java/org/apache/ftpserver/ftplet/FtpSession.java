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
 * Defines an client session with the FTP server. The session is born when the
 * client connects and dies when the client disconnects. Ftplet methods will
 * always get the same session for one user and one connection. So the
 * attributes set by <code>setAttribute()</code> will be always available later
 * unless that attribute is removed or the client disconnects.
 *
 * @author The Apache MINA Project (dev@mina.apache.org)
 * @version $Rev$, $Date$
 */
public interface FtpSession {

    /**
     * Returns the IP address of the client that sent the request.
     */
    InetAddress getClientAddress();

    InetAddress getServerAddress();

    int getServerPort();

    /**
     * Get FTP data connection.
     */
    DataConnectionFactory getDataConnection();

    Certificate[] getClientCertificates();

    /**
     * Get connection time.
     */
    Date getConnectionTime();

    /**
     * Get the login time.
     */
    Date getLoginTime();

    /**
     * Get the number of failed logins. When login succeeds, this will return 0.
     */
    int getFailedLogins();

    /**
     * Get last access time.
     */
    Date getLastAccessTime();

    /**
     * Returns maximum idle time. This time equals to
     * {@link ConnectionManagerImpl#getDefaultIdleSec()} until user login, and
     * {@link User#getMaxIdleTime()} after user login.
     */
    int getMaxIdleTime();

    /**
     * Set maximum idle time in seconds. This time equals to
     * {@link ConnectionManagerImpl#getDefaultIdleSec()} until user login, and
     * {@link User#getMaxIdleTime()} after user login.
     */
    void setMaxIdleTime(int maxIdleTimeSec);

    /**
     * Get user object.
     */
    User getUser();

    /**
     * Returns user name entered in USER command
     * 
     * @return user name entered in USER command
     */
    String getUserArgument();

    /**
     * Get the requested language.
     */
    String getLanguage();

    /**
     * Is the user logged in?
     */
    boolean isLoggedIn();

    /**
     * Get user file system view.
     */
    FileSystemView getFileSystemView();

    /**
     * Get file upload/download offset.
     */
    long getFileOffset();

    /**
     * Get rename from file object.
     */
    FileObject getRenameFrom();

    /**
     * Get the data type.
     */
    DataType getDataType();

    /**
     * Get structure.
     */
    Structure getStructure();

    /**
     * Returns the value of the named attribute as an Object, or null if no
     * attribute of the given name exists.
     */
    Object getAttribute(String name);

    /**
     * Stores an attribute in this request. It will be available until it was
     * removed or when the connection ends.
     */
    void setAttribute(String name, Object value);

    /**
     * Removes an attribute from this request.
     */
    void removeAttribute(String name);

    /**
     * Write a reply to the client
     * 
     * @param reply
     *            The reply that will be sent to the client
     * @throws FtpException
     */
    void write(FtpReply reply) throws FtpException;

    /**
     * Indicates whether the control socket for this session is secure, that is,
     * running over SSL/TLS
     * 
     * @return true if the control socket is secured
     */
    boolean isSecure();

}
