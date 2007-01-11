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

package org.apache.ftpserver.listener.mina;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.GeneralSecurityException;

import org.apache.commons.logging.Log;
import org.apache.ftpserver.interfaces.FtpServerContext;
import org.apache.ftpserver.interfaces.Ssl;
import org.apache.ftpserver.listener.FtpProtocolHandler;
import org.apache.ftpserver.listener.Listener;
import org.apache.ftpserver.socketfactory.SSLFtpSocketFactory;
import org.apache.mina.common.IoAcceptor;
import org.apache.mina.filter.LoggingFilter;
import org.apache.mina.filter.SSLFilter;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.SocketAcceptor;
import org.apache.mina.transport.socket.nio.SocketAcceptorConfig;

/**
 * The default {@link Listener} implementation.
 *
 */
public class MinaListener implements Listener {

    private Log log;
    
    private FtpServerContext serverContext;

    private IoAcceptor acceptor = new SocketAcceptor();
    
    private InetSocketAddress address;
    
    private MinaFtpProtocolHandler protocolHandler;

    private SocketAcceptorConfig cfg;
    
    boolean suspended = false;
 

    /**
     * Constructs a listener based on the configuration object
     * 
     * @param serverContext Configuration for the listener
     * @throws Exception 
     */
    public MinaListener(FtpServerContext serverContext) throws Exception {
        this.serverContext = serverContext;
        
        log = serverContext.getLogFactory().getInstance(getClass());
        
        
        int port = serverContext.getSocketFactory().getPort();
        InetAddress serverAddress = serverContext.getSocketFactory().getServerAddress();
        
        if(serverAddress != null) {
            address = new InetSocketAddress(serverAddress, port );
        } else {
            address = new InetSocketAddress( port );
        }
        
        cfg = new SocketAcceptorConfig();
        cfg.setReuseAddress( true );
        cfg.getFilterChain().addLast(
                "protocolFilter",
                new ProtocolCodecFilter( new FtpServerProtocolCodecFactory() ) );
        cfg.getFilterChain().addLast( "logger", new LoggingFilter() );

        if(serverContext.getSocketFactory() instanceof SSLFtpSocketFactory) {
            Ssl ssl = serverContext.getSocketFactory().getSSL();
            try {
                SSLFilter sslFilter = new SSLFilter( ssl.getSSLContext() );
                cfg.getFilterChain().addFirst("sslFilter", sslFilter);

            } catch (GeneralSecurityException e) {
                throw e;
            }
            
        }
    }

    /**
     * @see Listener#start()
     */
    public void start() throws Exception {
        
        protocolHandler = new MinaFtpProtocolHandler(serverContext, new FtpProtocolHandler(serverContext));

        acceptor.bind(address, protocolHandler, cfg );
    }

    /**
     * @see Listener#stop()
     */
    public synchronized void stop() {
        // close server socket
        if (acceptor != null) {
            acceptor.unbindAll();
            acceptor = null;
        }
    }

    /**
     * @see Listener#isStopped()
     */
    public boolean isStopped() {
        return acceptor == null;
    }

    /**
     * @see Listener#isSuspended()
     */
    public boolean isSuspended() {
        return suspended;
        
    }

    /**
     * @see Listener#resume()
     */
    public void resume() {
        if(acceptor != null && suspended) {
            try {
                acceptor.bind(address, protocolHandler, cfg);
            } catch (IOException e) {
                log.error("Failed to resume listener", e);
            }
        }
    }

    /**
     * @see Listener#suspend()
     */
    public void suspend() {
        if(acceptor != null && !suspended) {
            acceptor.unbind(address);
        }
    }
}