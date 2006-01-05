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


/**
 * This is an abstraction over the user file system view.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public
interface FileSystemView {

    /**
     * Get the user root directory.
     */
    FileObject getRootDirectory() throws FtpException;
    
    /**
     * Get user current directory.
     */
    FileObject getCurrentDirectory() throws FtpException;
    
    /**
     * Change directory.
     */ 
    boolean changeDirectory(String dir) throws FtpException;
    
    /**
     * Get file object.
     */
    FileObject getFileObject(String file) throws FtpException;
    
    /**
     * List file objects
     */
    FileObject[] listFiles(String file) throws FtpException;
    
    /**
     * Dispose file system view.
     */
    void dispose();
}
