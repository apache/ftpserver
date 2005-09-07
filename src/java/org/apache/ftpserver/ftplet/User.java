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
 * Basic user interface.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
interface User {
    
    /**
     * Get the user name.
     */
    String getName();
    
    /**
     * Get password.
     */
    String getPassword();
    
    /**
     * Get the maximum idle time in seconds. Zero or less idle time means no limit.
     */
    int getMaxIdleTime();
    
    /**
     * Get the user enable status.
     */
    boolean getEnabled();
    
    /**
     * Get maximum user upload rate in bytes/sec. Zero or less means no limit.
     */
    int getMaxUploadRate();
    
    /**
     * Get maximum user download rate in bytes/sec. Zero or less means no limit.
     */
    int getMaxDownloadRate();
    
    /**
     * get user home directory
     */
    String getHomeDirectory();
    
    /**
     * Get write permission
     */
    boolean getWritePermission();
}
