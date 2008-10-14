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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * This is the file abstraction used by the server.
 *
 * @author The Apache MINA Project (dev@mina.apache.org)
 * @version $Rev$, $Date$
 */
public interface FtpFile {

    /**
     * Get the full path from the base directory of the FileSystemView.
     * @return a path where the path separator is '/' (even if the operating system
     *     uses another character as path separator).
     */
    String getAbsolutePath();

    /**
     * Get the file name of the file
     * @return the last part of the file path (the part after the last '/').
     */
    String getName();

    /**
     * Is the file hidden?
     */
    boolean isHidden();

    /**
     * Is it a directory?
     */
    boolean isDirectory();

    /**
     * Is it a file?
     */
    boolean isFile();

    /**
     * Does this file exists?
     */
    boolean doesExist();

    /**
     * Has read permission?
     */
    boolean isReadable();

    /**
     * Has write permission?
     */
    boolean isWritable();

    /**
     * Has delete permission?
     */
    boolean isRemovable();

    /**
     * Get the owner name.
     */
    String getOwnerName();

    /**
     * Get owner group name.
     */
    String getGroupName();

    /**
     * Get link count.
     */
    int getLinkCount();

    /**
     * Get last modified time.
     */
    long getLastModified();

    /**
     * Get file size.
     */
    long getSize();

    /**
     * Create directory.
     */
    boolean mkdir();

    /**
     * Delete file.
     */
    boolean delete();

    /**
     * Move file.
     */
    boolean move(FtpFile destination);

    /**
     * List file objects. If not a directory or does not exist, null will be
     * returned. Files must be returned in alphabetical order.
     * List must be immutable.
     */
    List<FtpFile> listFiles();

    /**
     * Create output stream for writing. If the file is not random accessible,
     * any offset other than zero will throw an exception.
     */
    OutputStream createOutputStream(long offset) throws IOException;

    /**
     * Create input stream for reading. If the file is not random accessible,
     * any offset other than zero will throw an exception.
     */
    InputStream createInputStream(long offset) throws IOException;
}
