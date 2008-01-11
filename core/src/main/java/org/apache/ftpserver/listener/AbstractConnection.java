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

import java.io.IOException;

import org.apache.ftpserver.FtpSessionImpl;
import org.apache.ftpserver.ftplet.FtpSession;
import org.apache.ftpserver.interfaces.FtpServerContext;


/**
 * This is a generic request handler. It delegates 
 * the request to appropriate method in subclass.
 */
public abstract class AbstractConnection implements Connection {
    
    protected FtpServerContext serverContext;
    
    protected FtpSessionImpl ftpSession;
    private ConnectionObserver observer;
    
    
    /**
     * Spy print. Monitor user request.
     */
    protected void spyRequest(String str) {
        ConnectionObserver observer = this.observer;
        if(observer != null) {
            observer.request(str + "\r\n");
        }
    }
    
    /**
     * Constructor - set the control socket.
     */
    public AbstractConnection(FtpServerContext serverContext) throws IOException {
        this.serverContext = serverContext;
    }
    
    /**
     * Get the configuration object.
     */
    public FtpServerContext getServerContext() {
        return serverContext;
    }

        
    /**
     * Get request.
     */
    public FtpSession getSession() {
        return ftpSession;
    }
    
    /**
     * Set observer.
     */
    public void setObserver(ConnectionObserver observer) {
        this.observer = observer;
    }  
    
    /**
     * Notify connection manager observer.
     */
    protected void notifyObserver() {
        // TODO replace with MINA idle handling
    	//ftpSession.updateLastAccessTime();
        serverContext.getConnectionManager().updateConnection(this);
    }
}
