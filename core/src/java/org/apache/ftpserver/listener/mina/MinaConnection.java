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
import java.net.InetSocketAddress;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;

import org.apache.ftpserver.IODataConnectionFactory;
import org.apache.ftpserver.FtpSessionImpl;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.interfaces.FtpServerContext;
import org.apache.ftpserver.interfaces.FtpServerSession;
import org.apache.ftpserver.interfaces.Ssl;
import org.apache.ftpserver.listener.AbstractConnection;
import org.apache.ftpserver.listener.ConnectionObserver;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.SSLFilter;

/**
 * Handles MINA type connections.
 */
public class MinaConnection extends AbstractConnection {

    private IoSession session;
    
    public MinaConnection(FtpServerContext serverContext, IoSession session, MinaListener listener) throws IOException {
        super(serverContext);
        
        this.session = session;
        
        // data connection object
        
        // reader object
        ftpSession = new FtpSessionImpl(serverContext);
        ftpSession.setClientAddress(((InetSocketAddress)session.getRemoteAddress()).getAddress());
        ftpSession.setServerAddress(((InetSocketAddress)session.getLocalAddress()).getAddress());
        ftpSession.setServerPort(((InetSocketAddress)session.getLocalAddress()).getPort());
        ftpSession.setListener(listener);

        IODataConnectionFactory dataCon = new IODataConnectionFactory(this.serverContext, ftpSession);
        dataCon.setServerControlAddress(((InetSocketAddress)session.getLocalAddress()).getAddress());
        
        ftpSession.setFtpDataConnection(dataCon);
        
        if(session.getFilterChain().contains("sslFilter")) {
            SSLFilter sslFilter = (SSLFilter) session.getFilterChain().get("sslFilter");
            
            SSLSession sslSession = sslFilter.getSSLSession(session);
            
            if(sslSession != null) {
                try {
                    ftpSession.setClientCertificates(sslFilter.getSSLSession(session).getPeerCertificates());
                } catch(SSLPeerUnverifiedException e) {
                    // ignore, certificate will not be available to the session
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.apache.ftpserver.listener.AbstractConnection#setObserver(org.apache.ftpserver.listener.ConnectionObserver)
     */
    public void setObserver(ConnectionObserver observer) {
        session.setAttribute("observer", observer);
    }

    public void close() {
        session.close().join();
        
    }

    public void beforeSecureControlChannel(FtpServerSession ftpSession, String type) throws Exception {
        Ssl ssl = ftpSession.getListener().getSsl();
        
        if(ssl != null) {
            session.setAttribute(SSLFilter.DISABLE_ENCRYPTION_ONCE);
            
            SSLFilter sslFilter = new SSLFilter( ssl.getSSLContext() );
            sslFilter.setNeedClientAuth(ssl.getClientAuthenticationRequired());
            session.getFilterChain().addFirst("sslSessionFilter", sslFilter);

        } else {
            throw new FtpException("Socket factory SSL not configured");
        }
        
    }

    public void afterSecureControlChannel(FtpServerSession ftpSession, String type) throws Exception {
        // do nothing
    }
}
