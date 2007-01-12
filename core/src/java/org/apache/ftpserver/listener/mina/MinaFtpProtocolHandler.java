/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *  
 *    http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License. 
 *  
 */
package org.apache.ftpserver.listener.mina;

import java.io.IOException;

import org.apache.ftpserver.FtpSessionImpl;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.interfaces.FtpServerContext;
import org.apache.ftpserver.listener.ConnectionObserver;
import org.apache.ftpserver.listener.FtpProtocolHandler;
import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;
import org.apache.mina.util.SessionLog;

public class MinaFtpProtocolHandler extends IoHandlerAdapter
{
    
    private static final String OUTPUT_KEY = "output";
    private static final String CONNECTION_KEY = "connection";
    
    private FtpServerContext serverContext;
    private FtpProtocolHandler protocolHandler;
    
    public MinaFtpProtocolHandler(FtpServerContext serverContext, FtpProtocolHandler protocolHandler) throws IOException {
        this.serverContext = serverContext;
        this.protocolHandler = protocolHandler;
    }

    /* (non-Javadoc)
     * @see org.apache.mina.common.IoHandlerAdapter#sessionCreated(org.apache.mina.common.IoSession)
     */
    public void sessionCreated(IoSession session) throws Exception {
        MinaConnection connection = new MinaConnection(serverContext, session);
        session.setAttribute(CONNECTION_KEY, connection);
        
        MinaFtpResponseOutput output = new MinaFtpResponseOutput(session);
        output.setServerContext(serverContext);
        output.setFtpSession(connection.getSession());
        
        session.setAttribute(OUTPUT_KEY, output);
    }

    public void sessionOpened( IoSession session ) throws Exception
    {
        // set idle time to 60 seconds
        session.setIdleTime( IdleStatus.BOTH_IDLE, 60 );

        MinaConnection connection = (MinaConnection) session.getAttribute(CONNECTION_KEY);
        MinaFtpResponseOutput output = (MinaFtpResponseOutput) session.getAttribute(OUTPUT_KEY);
        
        protocolHandler.onConnectionOpened(connection, (FtpSessionImpl)connection.getSession(), output);
    }
    
    public void messageReceived( IoSession session, Object message ) throws IOException, FtpException
    {
        FtpRequest request = (FtpRequest) message;


        ConnectionObserver observer = (ConnectionObserver) session.getAttribute("observer");
        if(observer != null) {
            observer.request(request.toString());
        }        
        
        MinaConnection connection = (MinaConnection) session.getAttribute(CONNECTION_KEY);
        MinaFtpResponseOutput output = (MinaFtpResponseOutput) session.getAttribute(OUTPUT_KEY);

        
        protocolHandler.onRequestReceived(connection, (FtpSessionImpl)connection.getSession(), output, request);
    }

    public void sessionIdle( IoSession session, IdleStatus status )
    {
        SessionLog.info( session, "Disconnecting the idle." );
        session.close();
    }

    public void exceptionCaught( IoSession session, Throwable cause )
    {
        session.close();
    }
}
