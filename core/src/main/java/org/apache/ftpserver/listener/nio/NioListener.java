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

package org.apache.ftpserver.listener.nio;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.ftpserver.DefaultFtpHandler;
import org.apache.ftpserver.FtpHandler;
import org.apache.ftpserver.filter.FtpLoggingFilter;
import org.apache.ftpserver.interfaces.FtpIoSession;
import org.apache.ftpserver.interfaces.FtpServerContext;
import org.apache.ftpserver.listener.AbstractListener;
import org.apache.ftpserver.listener.Listener;
import org.apache.ftpserver.ssl.ClientAuth;
import org.apache.ftpserver.ssl.SslConfiguration;
import org.apache.mina.common.session.IdleStatus;
import org.apache.mina.common.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.executor.OrderedThreadPoolExecutor;
import org.apache.mina.filter.firewall.BlacklistFilter;
import org.apache.mina.filter.firewall.Subnet;
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
public class NioListener extends AbstractListener {

    private final Logger LOG = LoggerFactory.getLogger(NioListener.class);

    private SocketAcceptor acceptor;
    
    private InetSocketAddress address;
    
    boolean suspended = false;
    
    private ExecutorService filterExecutor = new OrderedThreadPoolExecutor();

	private FtpHandler handler = new DefaultFtpHandler();
	
	private int idleTimeout = 300;
	
	private List<InetAddress> blockedAddresses;
	private List<Subnet> blockedSubnets;

    private FtpServerContext context;


	public int getIdleTimeout() {
		return idleTimeout;
	}

	public void setIdleTimeout(int idleTimeout) {
		this.idleTimeout = idleTimeout;
	}

	private void updateBlacklistFilter() {
	    if(acceptor != null) {
    	    BlacklistFilter filter = (BlacklistFilter) acceptor.getFilterChain().get("ipFilter");
    	    
    	    if(filter != null) {
    	        if(blockedAddresses != null) {
    	            filter.setBlacklist(blockedAddresses);
    	        } else if(blockedSubnets != null) {
    	            filter.setSubnetBlacklist(blockedSubnets);
    	        } else {
    	            // an empty list clears the blocked addresses
                    filter.setSubnetBlacklist(new ArrayList<Subnet>());
    	        }
    	        
    	    }
	    }
	}
	
	/**
     * @see Listener#start(FtpServerContext)
     */
    public void start(FtpServerContext context) throws Exception {
        this.context = context;
        
        
        acceptor = new NioSocketAcceptor(Runtime.getRuntime().availableProcessors());
        
        if(getServerAddress() != null) {
            address = new InetSocketAddress(getServerAddress(), getPort() );
        } else {
            address = new InetSocketAddress( getPort() );
        }
        
        acceptor.setReuseAddress(true);
        acceptor.getSessionConfig().setReadBufferSize( 2048 );
        acceptor.getSessionConfig().setIdleTime( IdleStatus.BOTH_IDLE, idleTimeout );
        // Decrease the default receiver buffer size
        ((SocketSessionConfig) acceptor.getSessionConfig()).setReceiveBufferSize(512); 

        MdcInjectionFilter mdcFilter = new MdcInjectionFilter();

        acceptor.getFilterChain().addLast("mdcFilter", mdcFilter);
        
        // add and update the blacklist filter
        acceptor.getFilterChain().addLast("ipFilter", new BlacklistFilter());
        updateBlacklistFilter();
        
        acceptor.getFilterChain().addLast("threadPool", new ExecutorFilter(filterExecutor));
        acceptor.getFilterChain().addLast(
        		"codec",
        		new ProtocolCodecFilter( new FtpServerProtocolCodecFactory() ) );
        acceptor.getFilterChain().addLast("logger", new FtpLoggingFilter() );
        
        acceptor.getFilterChain().addLast("mdcFilter2", mdcFilter);

        
        if(isImplicitSsl()) {
            SslConfiguration ssl = getSslConfiguration();
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

        
		handler.init(context, this);
        acceptor.setHandler(new FtpHandlerAdapter(context, handler));
        
        acceptor.bind(address);
        
        // update the port to the real port bound by the listener
        setPort(acceptor.getLocalAddress().getPort());
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


    public FtpHandler getHandler() {
    	return handler;
	}

	public void setHandler(FtpHandler handler) {
		this.handler = handler;
		
		if(acceptor != null) {
			((FtpHandlerAdapter)acceptor.getHandler()).setFtpHandler(handler);
		}
	}

	/**
	 * Retrives the {@link InetAddress} for which this listener blocks connections
	 * @return The list of {@link InetAddress}es
	 */
    public List<InetAddress> getBlockedAddresses() {
        return blockedAddresses;
    }

    /**
     * Sets the {@link InetAddress} that this listener will block from connecting
     * @param blockedAddresses The list of {@link InetAddress}es
     */
    public synchronized void setBlockedAddresses(List<InetAddress> blockedAddresses) {
        this.blockedAddresses = blockedAddresses;
        updateBlacklistFilter();
    }

    /**
     * Retrives the {@link Subnet}s for which this acceptor blocks connections
     * @return The list of {@link Subnet}s
     */
    public List<Subnet> getBlockedSubnets() {
        return blockedSubnets;
    }

    /**
     * Sets the {@link Subnet}s that this listener will block from connecting
     * @param blockedAddresses The list of {@link Subnet}s
     */
    public synchronized void setBlockedSubnets(List<Subnet> blockedSubnets) {
        this.blockedSubnets = blockedSubnets;
        updateBlacklistFilter();
    }

    /**
     * @see Listener#getActiveSessions()
     */
    public Set<FtpIoSession> getActiveSessions() {
        Map<Long,IoSession> sessions = acceptor.getManagedSessions();
        
        Set<FtpIoSession> ftpSessions = new HashSet<FtpIoSession>();
        for(IoSession session : sessions.values()) {
            ftpSessions.add(new FtpIoSession(session, context));
        }
        return ftpSessions;
    }
}
