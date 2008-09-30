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

import org.apache.ftpserver.FtpHandler;
import org.apache.ftpserver.filter.FtpLoggingFilter;
import org.apache.ftpserver.impl.DefaultFtpHandler;
import org.apache.ftpserver.interfaces.DataConnectionConfiguration;
import org.apache.ftpserver.interfaces.FtpIoSession;
import org.apache.ftpserver.interfaces.FtpServerContext;
import org.apache.ftpserver.listener.Listener;
import org.apache.ftpserver.ssl.ClientAuth;
import org.apache.ftpserver.ssl.SslConfiguration;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
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
 * @author The Apache MINA Project (dev@mina.apache.org)
 * @version $Rev$, $Date$
 */
public class NioListener extends AbstractListener {

    private final Logger LOG = LoggerFactory.getLogger(NioListener.class);

    private SocketAcceptor acceptor;

    private InetSocketAddress address;

    boolean suspended = false;

    private ExecutorService filterExecutor = new OrderedThreadPoolExecutor();

    private FtpHandler handler = new DefaultFtpHandler();

    private FtpServerContext context;

    public NioListener(InetAddress serverAddress, int port,
            boolean implicitSsl,
            SslConfiguration sslConfiguration,
            DataConnectionConfiguration dataConnectionConfig, 
            int idleTimeout, List<InetAddress> blockedAddresses, List<Subnet> blockedSubnets) {
        super(serverAddress, port, implicitSsl, sslConfiguration, dataConnectionConfig, 
                idleTimeout, blockedAddresses, blockedSubnets);   
        
        updateBlacklistFilter();
    }

    private void updateBlacklistFilter() {
        if (acceptor != null) {
            BlacklistFilter filter = (BlacklistFilter) acceptor
                    .getFilterChain().get("ipFilter");

            if (filter != null) {
                if (getBlockedAddresses() != null) {
                    filter.setBlacklist(getBlockedAddresses());
                } else if (getBlockedSubnets() != null) {
                    filter.setSubnetBlacklist(getBlockedSubnets());
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

        acceptor = new NioSocketAcceptor(Runtime.getRuntime()
                .availableProcessors());

        if (getServerAddress() != null) {
            address = new InetSocketAddress(getServerAddress(), getPort());
        } else {
            address = new InetSocketAddress(getPort());
        }

        acceptor.setReuseAddress(true);
        acceptor.getSessionConfig().setReadBufferSize(2048);
        acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE,
                getIdleTimeout());
        // Decrease the default receiver buffer size
        ((SocketSessionConfig) acceptor.getSessionConfig())
                .setReceiveBufferSize(512);

        MdcInjectionFilter mdcFilter = new MdcInjectionFilter();

        acceptor.getFilterChain().addLast("mdcFilter", mdcFilter);

        // add and update the blacklist filter
        acceptor.getFilterChain().addLast("ipFilter", new BlacklistFilter());
        updateBlacklistFilter();

        acceptor.getFilterChain().addLast("threadPool",
                new ExecutorFilter(filterExecutor));
        acceptor.getFilterChain().addLast("codec",
                new ProtocolCodecFilter(new FtpServerProtocolCodecFactory()));
        acceptor.getFilterChain().addLast("mdcFilter2", mdcFilter);
        acceptor.getFilterChain().addLast("logger", new FtpLoggingFilter());

        if (isImplicitSsl()) {
            SslConfiguration ssl = getSslConfiguration();
            SslFilter sslFilter = new SslFilter(ssl.getSSLContext());

            if (ssl.getClientAuth() == ClientAuth.NEED) {
                sslFilter.setNeedClientAuth(true);
            } else if (ssl.getClientAuth() == ClientAuth.WANT) {
                sslFilter.setWantClientAuth(true);
            }

            if (ssl.getEnabledCipherSuites() != null) {
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

        if (filterExecutor != null) {
            filterExecutor.shutdown();
            try {
                filterExecutor.awaitTermination(5000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
            } finally {
                // TODO: how to handle?
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
        if (acceptor != null && suspended) {
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
        if (acceptor != null && !suspended) {
            acceptor.unbind(address);
        }
    }
    
    /**
     * @see Listener#getActiveSessions()
     */
    public Set<FtpIoSession> getActiveSessions() {
        Map<Long, IoSession> sessions = acceptor.getManagedSessions();

        Set<FtpIoSession> ftpSessions = new HashSet<FtpIoSession>();
        for (IoSession session : sessions.values()) {
            ftpSessions.add(new FtpIoSession(session, context));
        }
        return ftpSessions;
    }
}
