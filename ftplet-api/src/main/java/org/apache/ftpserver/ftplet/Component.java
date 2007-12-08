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
 * This interface abstracts the basic lifecycle method. 
 * <ol>
 *     <li>Default constructor will be called.</li>
 *     <li>The component will be configured using Component.configure(Configuration)</li>
 *     <li>Other component specific methods will be called.</li>
 *     <li>Finnaly Component.dispose() method will be called.</li>
 * </ol>
 */
public 
interface Component {
    
    /**
     * Configure the component.
     */
    void configure(Configuration config) throws FtpException;
    
    /**
     * Dispose component - release all the resources.
     */
    void dispose();
}
