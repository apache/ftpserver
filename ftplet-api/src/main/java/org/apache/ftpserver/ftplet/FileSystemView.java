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

/**
 * This is an abstraction over the user file system view.
 *
 * @author The Apache MINA Project (dev@mina.apache.org)
 * @version $Rev$, $Date$
 */
public interface FileSystemView {

    /**
     * Get the user home directory.
     */
    FtpFile getHomeDirectory() throws FtpException;

    /**
     * Get user current directory.
     */
    FtpFile getCurrentDirectory() throws FtpException;

    /**
     * Change directory.
     */
    boolean changeDirectory(String dir) throws FtpException;

    /**
     * Get file object.
     */
    FtpFile getFileObject(String file) throws FtpException;

    /**
     * Does the file system support random file access?
     */
    boolean isRandomAccessible() throws FtpException;

    /**
     * Dispose file system view.
     */
    void dispose();
}
