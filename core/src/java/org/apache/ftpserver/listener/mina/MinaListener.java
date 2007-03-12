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
import java.security.GeneralSecurityException;

import org.apache.ftpserver.interfaces.FtpServerContext;
import org.apache.ftpserver.listener.AbstractListener;
import org.apache.ftpserver.listener.FtpProtocolHandler;
import org.apache.ftpserver.listener.Listener;
import org.apache.mina.common.DefaultIoFilterChainBuilder;
import org.apache.mina.common.IoAcceptor;
import org.apache.mina.common.ThreadModel;
import org.apache.mina.filter.LoggingFilter;
import org.apache.mina.filter.SSLFilter;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.transport.socket.nio.SocketAcceptor;
import org.apache.mina.transport.socket.nio.SocketAcceptorConfig;
import org.apache.mina.transport.socket.nio.SocketSessionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.emory.mathcs.backport.java.util.concurrent.ExecutorService;
import edu.emory.mathcs.backport.java.util.concurrent.Executors;
import edu.emory.mathcs.backport.java.util.concurrent.TimeUnit;

/**
 * The default {@link Listener} implementation.
 *
 */
public class MinaListener extends AbstractListener {

    private final Logger LOG = LoggerFactory.getLogger(MinaListener.class);

    private IoAcceptor acceptor;
    
    private InetSocketAddress address;
    
    private MinaFtpProtocolHandler protocolHandler;

    private SocketAcceptorConfig cfg;
    
    boolean suspended = false;

    private int numberOfIoProcessingThread = Runtime.getRuntime().availableProcessors() + 1;
    private ExecutorService ioProcessingExecutor = Executors.newCachedThreadPool();
    private ExecutorService filterExecutor = Executors.newCachedThreadPool();

    /**
     * @see Listener#start(FtpServerContext)
     */
    public void start(FtpServerContext serverContext) throws Exception {
        
        acceptor = new SocketAcceptor(numberOfIoProcessingThread, ioProcessingExecutor);
        
        if(getServerAddress() != null) {
            address = new InetSocketAddress(getServerAddress(), getPort() );
        } else {
            address = new InetSocketAddress( getPort() );
        }
        
        cfg = new SocketAcceptorConfig();
        
        cfg.setReuseAddress( true );
        cfg.getFilterChain().addLast(
                "protocolFilter",
                new ProtocolCodecFilter( new FtpServerProtocolCodecFactory() ) );
        cfg.getFilterChain().addLast( "logger", new LoggingFilter() );

        cfg.setThreadModel(ThreadModel.MANUAL);
        
        DefaultIoFilterChainBuilder filterChainBuilder = cfg.getFilterChain();
        filterChainBuilder.addLast("threadPool", new ExecutorFilter(filterExecutor));
        
        // Decrease the default receiver buffer size
        ((SocketSessionConfig) cfg.getSessionConfig()).setReceiveBufferSize(512); 
        
        if(isImplicitSsl()) {
            try {
                SSLFilter sslFilter = new SSLFilter( getSsl().getSSLContext() );
                cfg.getFilterChain().addFirst("sslFilter", sslFilter);

            } catch (GeneralSecurityException e) {
                throw e;
            }
            
        }
        
        protocolHandler = new MinaFtpProtocolHandler(serverContext, new FtpProtocolHandler(serverContext), this);

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
        
        if(ioProcessingExecutor != null) {
            ioProcessingExecutor.shutdown();
            try {
                ioProcessingExecutor.awaitTermination(5000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                // TODO: how to handle?
            }
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
                acceptor.bind(address, protocolHandler, cfg);
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

    /**
     * Get the {@link ExecutorService} used for reading and writing to sockets. The default
     * value is a cached thread pool.
     * @return The {@link ExecutorService}
     */
    public ExecutorService getIoProcessingExecutor() {
        return ioProcessingExecutor;
    }

    /**
     * Set the {@link ExecutorService} used for reading and writing to sockets
     * @param ioProcessingExecutor The {@link ExecutorService}
     */
    public void setIoProcessingExecutor(ExecutorService ioProcessingExecutor) {
        this.ioProcessingExecutor = ioProcessingExecutor;
    }

    /**
     * Get the number of threads used for IO processing. The default value is
     * set to the number of available CPUs + 1
     * @return The number of threads used for IO processing
     */
    public int getNumberOfIoProcessingThread() {
        return numberOfIoProcessingThread;
    }

    /**
     * Set the number of threads used for IO processing
     * @param numberOfIoProcessingThread The number of threads used for IO processing.
     */
    public void setNumberOfIoProcessingThread(int numberOfIoProcessingThread) {
        this.numberOfIoProcessingThread = numberOfIoProcessingThread;
    }
}