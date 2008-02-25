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
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.ftpserver.FtpHandler;
import org.apache.ftpserver.interfaces.FtpServerContext;
import org.apache.ftpserver.listener.AbstractListener;
import org.apache.ftpserver.listener.Listener;
import org.apache.ftpserver.ssl.ClientAuth;
import org.apache.ftpserver.ssl.SslConfiguration;
import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoSessionLogger;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.filter.logging.MdcInjectionFilter;
import org.apache.mina.filter.ssl.SslFilter;
import org.apache.mina.transport.socket.SocketAcceptor;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default {@link Listener} implementation.
 *
 */
public class MinaListener extends AbstractListener {

    private final Logger LOG = LoggerFactory.getLogger(MinaListener.class);

    private SocketAcceptor acceptor;
    
    private InetSocketAddress address;
    
    boolean suspended = false;

    private ExecutorService filterExecutor = Executors.newCachedThreadPool();

    /**
     * @see Listener#start(FtpServerContext)
     */
    public void start(FtpServerContext context) throws Exception {
        
        acceptor = new NioSocketAcceptor(Runtime.getRuntime().availableProcessors());
        
        if(getServerAddress() != null) {
            address = new InetSocketAddress(getServerAddress(), getPort() );
        } else {
            address = new InetSocketAddress( getPort() );
        }
        
        acceptor.setReuseAddress(true);
        acceptor.getSessionConfig().setReadBufferSize( 2048 );
        acceptor.getSessionConfig().setIdleTime( IdleStatus.BOTH_IDLE, 10 );
        // Decrease the default receiver buffer size
        ((SocketSessionConfig) acceptor.getSessionConfig()).setReceiveBufferSize(512); 

        acceptor.getFilterChain().addLast("mdcFilter", new MdcInjectionFilter());
        acceptor.getFilterChain().addLast(
                "codec",
                new ProtocolCodecFilter( new FtpServerProtocolCodecFactory() ) );
        
        // dusable the session prefix as we now use MDC logging
        IoSessionLogger.setUsePrefix(false);
        acceptor.getFilterChain().addLast( "logger", new LoggingFilter() );
        acceptor.getFilterChain().addLast("threadPool", new ExecutorFilter(filterExecutor));
        acceptor.getFilterChain().addLast("mdcFilter2", new MdcInjectionFilter());

        
        if(isImplicitSsl()) {
            SslConfiguration ssl = getSsl();
            SslFilter sslFilter = new SslFilter( ssl.getSSLContext() );
            
            if(ssl.getClientAuth() == ClientAuth.NEED) {
                sslFilter.setNeedClientAuth(true);
            } else if(ssl.getClientAuth() == ClientAuth.WANT) {
                sslFilter.setWantClientAuth(true);
            }

            if(ssl.getEnabledCipherSuites() != null) {
                sslFilter.setEnabledCipherSuites(ssl.getEnabledCipherSuites());
            }
            
            acceptor.getFilterChain().addFirst("sslFilter", sslFilter);
        }

        acceptor.setHandler(  new FtpHandler(context, this) );
        
        acceptor.bind(address);
    }

    /**
     * @see Listener#stop()
     */
    public synchronized void stop() {
        // close server socket
        if (acceptor != null) {
            acceptor.unbind();
            acceptor.dispose();
            acceptor = null;
        }
        
        if(filterExecutor != null) {
            filterExecutor.shutdown();
            try {
                filterExecutor.awaitTermination(5000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
            } finally {
//              TODO: how to handle?
            }
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
                acceptor.bind(address);
            } catch (IOException e) {
                LOG.error("Failed to resume listener", e);
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

    /**
     * Get the {@link ExecutorService} used for processing requests. The default
     * value is a cached thread pool.
     * @return The {@link ExecutorService}
     */
    public ExecutorService getFilterExecutor() {
        return filterExecutor;
    }

    /**
     * Set the {@link ExecutorService} used for processing requests
     * @param filterExecutor The {@link ExecutorService}
     */
    public void setFilterExecutor(ExecutorService filterExecutor) {
        this.filterExecutor = filterExecutor;
    }

}