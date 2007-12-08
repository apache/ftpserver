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

package org.apache.ftpserver;

import org.apache.ftpserver.ftplet.FtpReplyOutput;
import org.apache.ftpserver.listener.ConnectionObserver;

/**
 * FTP response object. The server uses this to send server messages
 */
public abstract class FtpWriter implements FtpReplyOutput {

    private ConnectionObserver observer;
    
    
    /**
     * Get the observer object to get what the server response.
     */
    public void setObserver(ConnectionObserver observer) {
        this.observer = observer;
    }        
    
    /**
     * Spy print. Monitor server response.
     */
    protected void spyResponse(String str) {
        ConnectionObserver observer = this.observer;
        if(observer != null) {
            observer.response(str);
        }
    }
    
    public abstract void close();
}
