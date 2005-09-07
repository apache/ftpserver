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

/**
 * This is an abstraction over the file.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
interface FileObject {
    
    /**
     * Get the fully qualified name.
     */
    String getFullName();
    
    /**
     * Get the file short name.
     */
    String getShortName();
    
    /**
     * Is a hidden file?
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
    boolean hasReadPermission();
    
    /**
     * Has write permission?
     */ 
    boolean hasWritePermission(); 
    
    /**
     * Has delete permission?
     */
    boolean hasDeletePermission();
    
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
     * Move file
     */
    boolean move(FileObject destination);
    
    /**
     * Create output stream for writing.
     */
    OutputStream createOutputStream(boolean append) throws IOException;
    
    /**
     * Create output stream for writing.
     */
    OutputStream createOutputStream(long offset) throws IOException;
    
    /**
     * Create input stream for reading.
     */
    InputStream createInputStream(long offset) throws IOException;
}
