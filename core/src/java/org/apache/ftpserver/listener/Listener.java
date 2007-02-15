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

package org.apache.ftpserver.listener;

import org.apache.ftpserver.interfaces.FtpServerContext;
import org.apache.ftpserver.interfaces.Ssl;


/**
 * Interface for the component responsible for waiting for incoming
 * socket requests and kicking off {@link Connection}s 
 *
 */
public interface Listener {
    
    Ssl getSsl();
    
    /**
     * Start the listener, will initiate the listener waiting
     * on the socket.
     * The method should not return until the listener has
     * started accepting socket requests.
     * 
     * @throws Exception On error during start up
     */
    void start(FtpServerContext serverContext) throws Exception;

    /**
     * Stop the listener, it should no longer except socket requests.
     * The method should not return until the listener has stopped
     * accepting socket requests.
     */
    void stop();

    /**
     * Checks if the listener is currently started.
     * 
     * @return True if the listener is started
     */
    boolean isStopped();

    /**
     * Temporarily stops the listener from accepting socket requests.
     * Resume the listener by using the {@link #resume()} method.
     * The method should not return until the listener has stopped 
     * accepting socket requests.
     */
    void suspend();

    /**
     * Resumes a suspended listener. 
     * The method should not return until the listener has
     * started accepting socket requests.
     */
    void resume();

    /**
     * Checks if the listener is currently suspended
     * @return True if the listener is suspended
     */
    boolean isSuspended();
}