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

import java.util.Iterator;


/**
 * Configuration interface. 
 */
public 
interface Configuration {

    /**
     * Is it an empty configuration?
     */
    boolean isEmpty();
    
    /**
     * Get string - if not found throws FtpException.
     */
    String getString(String param) throws FtpException;

    /**
     * Get string - if not found returns the default value.
     */
    String getString(String param, String defaultVal);

    /**
     * Get integer - if not found throws FtpException.
     */
    int getInt(String param) throws FtpException;
        
    /**
     * Get integer - if not found returns the default value.
     */
    int getInt(String param, int defaultVal);
    
    /**
     * Get long - if not found throws FtpException.
     */
    long getLong(String param) throws FtpException;
    
    /**
     * Get long - if not found returns the default value.
     */
    long getLong(String param, long defaultVal);
    
    /**
     * Get boolean - if not found throws FtpException.
     */
    boolean getBoolean(String param) throws FtpException;
    
    /**
     * Get boolean - if not found returns the default value.
     */
    boolean getBoolean(String param, boolean defaultVal);
    
    /**
     * Get double - if not found throws FtpException.
     */
    double getDouble(String param) throws FtpException;
    
    /**
     * Get double - if not found returns the default value.
     */
    double getDouble(String param, double defaultVal);

    /**
     * Get configuration subset. The return value will never be null.
     */
    Configuration subset(String param);
    
    /**
     * Get the configuration keys. The order of the keys is not guaranteed
     * to be the same as that of the input.
     */
    Iterator getKeys();
}
