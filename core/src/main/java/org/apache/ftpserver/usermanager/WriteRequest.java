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

package org.apache.ftpserver.usermanager;

import org.apache.ftpserver.ftplet.AuthorizationRequest;

/**
 * Class representing a write request
 */
public class WriteRequest implements AuthorizationRequest {
   
    private String file;
    
    /**
     * Request write access to the user home directory (/)
     *
     */
    public WriteRequest() {
        this("/");
    }
    
    /**
     * Request write access to a file or directory relative to the user home directory
     * @param file
     */
    public WriteRequest(String file) {
        this.file = file;
    }

    /**
     * Get the file or directory to which write access is requested
     * @return the file The file or directory
     */
    public String getFile() {
        return file;
    }
    
    
    
}
