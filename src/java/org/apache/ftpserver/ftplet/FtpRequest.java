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
package org.apache.ftpserver.ftplet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.Date;

/**
 * Defines an object to provide client request information to a ftplet.
 * Ftplet methods will always get the same instance of Ftplet request.
 * So the attributes set by <code>setAttribute()</code> will be always 
 * available later unless that attribute is removed.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
interface FtpRequest {

    /**
     * Get the client request string.
     */
    String getRequestLine();
    
    /**
     * Returns the ftp request command.
     */
    String getCommand();

    /**
     * Get the ftp request argument.
     */
    String getArgument();
    
    /**
     * Returns the IP address of the client that sent the request.
     */
    InetAddress getRemoteAddress();
    
    /**
     * Get connection time.
     */
    Date getConnectionTime();
    
    /**
     * Get the login time.
     */
    Date getLoginTime();
    
    /**
     * Get last access time.
     */
    Date getLastAccessTime();
    
    /**
     * Get user object.
     */
    User getUser();
    
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
     * Get data input stream.
     */
    InputStream getDataInputStream() throws IOException;
    
    /**
     * Get data output stream.
     */
    OutputStream getDataOutputStream() throws IOException;
    
    /**
     * Returns the value of the named attribute as an Object, 
     * or null if no attribute of the given name exists.
     */
    Object getAttribute(String name);
    
    /**
     * Stores an attribute in this request. It will be available 
     * until it was removed or when the connection ends. 
     */
    void setAttribute(String name, Object value);
    
    /**
     * Removes an attribute from this request.
     */
    void removeAttribute(String name);
    
    /**
     * Clear all attributes
     */
    void clear();
}
