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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.ftpserver.FtpSessionImpl;
import org.apache.ftpserver.ftplet.DataType;
import org.apache.ftpserver.ftplet.FtpSession;
import org.apache.ftpserver.interfaces.FtpServerContext;
import org.apache.ftpserver.util.IoUtils;


/**
 * This is a generic request handler. It delegates 
 * the request to appropriate method in subclass.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public abstract class AbstractConnection implements Connection {
    
    protected FtpServerContext serverContext;
    protected Log log;
    
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
        log = this.serverContext.getLogFactory().getInstance(getClass());
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
        ftpSession.updateLastAccessTime();
        serverContext.getConnectionManager().updateConnection(this);
    }


    /**
     * Transfer data.
     */
    public final long transfer(InputStream in, OutputStream out, int maxRate) throws IOException {
        
        BufferedInputStream bis = IoUtils.getBufferedInputStream(in);
        BufferedOutputStream bos = IoUtils.getBufferedOutputStream( out );
        
        boolean isAscii = ftpSession.getDataType() == DataType.ASCII;
        long startTime = System.currentTimeMillis();
        long transferredSize = 0L;
        byte[] buff = new byte[4096];
        
        while(true) {
            
            // if current rate exceeds the max rate, sleep for 50ms 
            // and again check the current transfer rate
            if(maxRate > 0) {
                
                // prevent "divide by zero" exception
                long interval = System.currentTimeMillis() - startTime;
                if(interval == 0) {
                    interval = 1;
                }
                
                // check current rate
                long currRate = (transferredSize*1000L)/interval;
                if(currRate > maxRate) {
                    try { Thread.sleep(50); } catch(InterruptedException ex) {break;}
                    continue;
                }
            }
            
            // read data
            int count = bis.read(buff);
            if(count == -1) {
                break;
            }
            
            // write data
            // if ascii, replace \n by \r\n
            if(isAscii) {
                for(int i=0; i<count; ++i) {
                    byte b = buff[i];
                    if(b == '\n') {
                        bos.write('\r');
                    }
                    bos.write(b);
                }
            }
            else {
                bos.write(buff, 0, count);
            }
            
            transferredSize += count;
            notifyObserver();
        }
        
        return transferredSize;
    }
}
