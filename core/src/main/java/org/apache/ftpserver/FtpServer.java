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

import java.util.Map;

import org.apache.ftpserver.command.CommandFactory;
import org.apache.ftpserver.ftplet.FileSystemFactory;
import org.apache.ftpserver.ftplet.Ftplet;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.interfaces.FtpServerContext;
import org.apache.ftpserver.listener.Listener;
import org.apache.ftpserver.message.MessageResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the starting point of all the servers. It invokes a new listener
 * thread. <code>Server</code> implementation is used to create the server
 * socket and handle client connection.
 *
 * @author The Apache MINA Project (dev@mina.apache.org)
 * @version $Rev$, $Date$
 */
public class FtpServer {

    private final Logger LOG = LoggerFactory.getLogger(FtpServer.class);

    private FtpServerContext serverContext;

    private boolean suspended = false;

    private boolean started = false;

    /**
     * Creates a server with the default configuration
     * 
     * @throws Exception
     */
    public FtpServer() throws Exception {
        serverContext = new DefaultFtpServerContext();
    }

    /**
     * Constructor. Set a custom the server context.
     * 
     * @throws Exception
     */
    public FtpServer(final FtpServerContext serverContext) throws Exception {
        this.serverContext = serverContext;
    }

    /**
     * Start the server. Open a new listener thread.
     */
    public void start() throws Exception {

        Map<String, Listener> listeners = serverContext.getListeners();
        for (Listener listener : listeners.values()) {
            listener.start(serverContext);
        }

        // init the Ftplet container
        serverContext.getFtpletContainer().init(serverContext);
        
        started = true;

        LOG.info("FTP server started");

    }

    /**
     * Stop the server. Stop the listener thread.
     */
    public void stop() {
        if (!started || serverContext == null) {
            // we have already been stopped, ignore
            return;
        }

        // stop all listeners
        Map<String, Listener> listeners = serverContext.getListeners();
        for (Listener listener : listeners.values()) {
            listener.stop();
        }

        // release server resources
        if (serverContext != null) {
            serverContext.dispose();
            serverContext = null;
        }

        started = false;
    }

    /**
     * Get the server status.
     */
    public boolean isStopped() {
        return !started;
    }

    /**
     * Suspend further requests
     */
    public void suspend() {
        if (!started) {
            return;
        }

        // stop all listeners
        Map<String, Listener> listeners = serverContext.getListeners();
        for (Listener listener : listeners.values()) {
            listener.suspend();
        }

        suspended = true;
    }

    /**
     * Resume the server handler
     */
    public void resume() {
        if (!suspended) {
            return;
        }

        Map<String, Listener> listeners = serverContext.getListeners();
        for (Listener listener : listeners.values()) {
            listener.resume();
        }

        suspended = false;
    }

    /**
     * Is the server suspended
     */
    public boolean isSuspended() {
        return suspended;
    }

    /**
     * Get the root server context.
     */
    public FtpServerContext getServerContext() {
        return serverContext;
    }

    private DefaultFtpServerContext checkAndGetContext() {
        if (getServerContext() instanceof DefaultFtpServerContext) {
            return (DefaultFtpServerContext) getServerContext();
        } else {
            throw new IllegalStateException(
                    "Custom FtpServerContext provided, setters can not be used on FtpServer");
        }
    }

    /**
     * Get all listeners available one this server
     * 
     * @return The current listeners
     */
    public Map<String, Listener> getListeners() {
        return getServerContext().getListeners();
    }

    /**
     * Get a specific listener identified by its name
     * 
     * @param name
     *            The name of the listener
     * @return The {@link Listener} matching the provided name
     */
    public Listener getListener(final String name) {
        return getServerContext().getListener(name);
    }

    public void addListener(final String name, final Listener listener) {
        checkAndGetContext().addListener(name, listener);
    }

    /**
     * Set the listeners for this server, replaces existing listeners
     * 
     * @param listeners
     *            The listeners to use for this server with the name as the key
     *            and the listener as the value
     * @throws IllegalStateException
     *             If a custom server context has been set
     */
    public void setListeners(final Map<String, Listener> listeners) {
        checkAndGetContext().setListeners(listeners);
    }

    /**
     * Get all {@link Ftplet}s registered at this server
     * 
     * @return All {@link Ftplet}s
     */
    public Map<String, Ftplet> getFtplets() {
        return getServerContext().getFtpletContainer().getFtplets();
    }

    /**
     * Set the {@link Ftplet}s to be active for this server. Replaces existing
     * {@link Ftplet}s
     * 
     * @param ftplets
     *            Ftplets as a map with the name as the key and the Ftplet as
     *            the value
     * @throws IllegalStateException
     *             If a custom server context has been set
     */
    public void setFtplets(final Map<String, Ftplet> ftplets) {
        getServerContext().getFtpletContainer().setFtplets(ftplets);
    }

    /**
     * Retrieve the user manager used with this server
     * 
     * @return The user manager
     */
    public UserManager getUserManager() {
        return getServerContext().getUserManager();
    }

    /**
     * Set the user manager to be used for this server
     * 
     * @param userManager
     *            The {@link UserManager}
     * @throws IllegalStateException
     *             If a custom server context has been set
     */
    public void setUserManager(final UserManager userManager) {
        checkAndGetContext().setUserManager(userManager);
    }

    /**
     * Retrieve the file system used with this server
     * 
     * @return The {@link FileSystemFactory}
     */
    public FileSystemFactory getFileSystem() {
        return getServerContext().getFileSystemManager();
    }

    /**
     * Set the file system to be used for this server
     * 
     * @param fileSystem
     *            The {@link FileSystemFactory}
     * @throws IllegalStateException
     *             If a custom server context has been set
     */
    public void setFileSystem(final FileSystemFactory fileSystem) {
        checkAndGetContext().setFileSystemManager(fileSystem);
    }

    /**
     * Retrieve the command factory used with this server
     * 
     * @return The {@link CommandFactory}
     */
    public CommandFactory getCommandFactory() {
        return getServerContext().getCommandFactory();
    }

    /**
     * Set the command factory to be used for this server
     * 
     * @param commandFactory
     *            The {@link CommandFactory}
     * @throws IllegalStateException
     *             If a custom server context has been set
     */
    public void setCommandFactory(final CommandFactory commandFactory) {
        checkAndGetContext().setCommandFactory(commandFactory);
    }

    /**
     * Retrieve the message resource used with this server
     * 
     * @return The {@link MessageResource}
     */
    public MessageResource getMessageResource() {
        return getServerContext().getMessageResource();
    }

    /**
     * Set the message resource to be used with this server
     * 
     * @param messageResource
     *            The {@link MessageResource}
     * @throws IllegalStateException
     *             If a custom server context has been set
     */
    public void setMessageResource(final MessageResource messageResource) {
        checkAndGetContext().setMessageResource(messageResource);
    }

    /**
     * Retrieve the connection configuration this server
     * 
     * @return The {@link MessageResource}
     */
    public ConnectionConfig getConnectionConfig() {
        return getServerContext().getConnectionConfig();
    }

    /**
     * Set the message resource to be used with this server
     * 
     * @param messageResource
     *            The {@link MessageResource}
     * @throws IllegalStateException
     *             If a custom server context has been set
     */
    public void setConnectionConfig(final ConnectionConfig connectionConfig) {
        checkAndGetContext().setConnectionConfig(connectionConfig);
    }
}
