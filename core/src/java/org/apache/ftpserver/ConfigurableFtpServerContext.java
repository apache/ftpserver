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

import java.net.InetAddress;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ftpserver.filesystem.NativeFileSystemManager;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.Component;
import org.apache.ftpserver.ftplet.Configuration;
import org.apache.ftpserver.ftplet.DefaultFtpletContainer;
import org.apache.ftpserver.ftplet.FileSystemManager;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpStatistics;
import org.apache.ftpserver.ftplet.Ftplet;
import org.apache.ftpserver.ftplet.FtpletContainer;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.interfaces.CommandFactory;
import org.apache.ftpserver.interfaces.DataConnectionConfig;
import org.apache.ftpserver.interfaces.FtpServerContext;
import org.apache.ftpserver.interfaces.IpRestrictor;
import org.apache.ftpserver.interfaces.MessageResource;
import org.apache.ftpserver.interfaces.ServerFtpStatistics;
import org.apache.ftpserver.interfaces.SocketFactory;
import org.apache.ftpserver.iprestrictor.FileIpRestrictor;
import org.apache.ftpserver.listener.ConnectionManager;
import org.apache.ftpserver.listener.ConnectionManagerImpl;
import org.apache.ftpserver.message.MessageResourceImpl;
import org.apache.ftpserver.socketfactory.FtpSocketFactory;
import org.apache.ftpserver.usermanager.BaseUser;
import org.apache.ftpserver.usermanager.ConcurrentLoginPermission;
import org.apache.ftpserver.usermanager.PropertiesUserManager;
import org.apache.ftpserver.usermanager.TransferRatePermission;
import org.apache.ftpserver.usermanager.WritePermission;

/**
 * FTP server configuration implementation. It holds all 
 * the components used.
 */
public
class ConfigurableFtpServerContext implements FtpServerContext {

    private LogFactory logFactory;
    private SocketFactory socketFactory;
    private DataConnectionConfig dataConConfig;
    private MessageResource messageResource;
    private ConnectionManager connectionManager;
    private IpRestrictor ipRestrictor;
    private UserManager userManager;
    private FileSystemManager fileSystemManager;
    private FtpletContainer ftpletContainer;
    private ServerFtpStatistics statistics;
    private CommandFactory commandFactory;
    
    private Log log;
    
    private static final Authority[] ADMIN_AUTHORITIES = new Authority[]{
        new WritePermission()
    };

    private static final Authority[] ANON_AUTHORITIES = new Authority[]{
        new ConcurrentLoginPermission(20, 2),
        new TransferRatePermission(4800, 4800)
    };
    
    /**
     * Constructor - set the root configuration.
     */
    public ConfigurableFtpServerContext(Configuration conf) throws Exception {
        
        try {
            
            // get the log classes
            logFactory = LogFactory.getFactory();
            logFactory = new FtpLogFactory(logFactory);
            log        = logFactory.getInstance(ConfigurableFtpServerContext.class);
            
            // create all the components
            socketFactory     = (SocketFactory)        createComponent(conf, "socket-factory",      FtpSocketFactory.class.getName());
            dataConConfig     = (DataConnectionConfig) createComponent(conf, "data-connection",     DefaultDataConnectionConfig.class.getName()); 
            messageResource   = (MessageResource)      createComponent(conf, "message",             MessageResourceImpl.class.getName());
            connectionManager = (ConnectionManager)    createComponent(conf, "connection-manager",  ConnectionManagerImpl.class.getName());
            ipRestrictor      = (IpRestrictor)         createComponent(conf, "ip-restrictor",       FileIpRestrictor.class.getName());
            userManager       = (UserManager)           createComponent(conf, "user-manager",        PropertiesUserManager.class.getName());
            fileSystemManager = (FileSystemManager)     createComponent(conf, "file-system-manager", NativeFileSystemManager.class.getName());
            statistics        = (ServerFtpStatistics)        createComponent(conf, "statistics",          FtpStatisticsImpl.class.getName());
            commandFactory    = (CommandFactory)       createComponent(conf, "command-factory",     DefaultCommandFactory.class.getName());
            
            // create user if necessary
            boolean userCreate = conf.getBoolean("create-default-user", true);
            if(userCreate) {
                createDefaultUsers();
            }
            
            ftpletContainer    = (FtpletContainer) createComponent(conf, "ftplet-container",     DefaultFtpletContainer.class.getName());
       
            initFtplets(ftpletContainer, conf);
        }
        catch(Exception ex) {
            dispose();
            throw ex;
        }
    }

    /**
     * create and initialize ftlets
     * @param container
     * @param conf
     * @throws FtpException
     */
    private void initFtplets(FtpletContainer container, Configuration conf) throws FtpException {
        String ftpletNames = conf.getString("ftplets", null);
        Configuration ftpletConf = conf.subset("ftplet");
                
        if(ftpletNames == null) {
            return;
        }
        
        //log = ftpConfig.getLogFactory().getInstance(getClass());
        StringTokenizer st = new StringTokenizer(ftpletNames, " ,;\r\n\t");
        try {
            while(st.hasMoreTokens()) {
                String ftpletName = st.nextToken();
                log.info("Configuring ftplet : " + ftpletName);
                
                // get ftplet specific configuration
                Configuration subConfig = ftpletConf.subset(ftpletName);
                String className = subConfig.getString("class", null);
                if(className == null) {
                    continue;
                }
                Ftplet ftplet = (Ftplet)Class.forName(className).newInstance();
                ftplet.init(this, subConfig);
                container.addFtplet(ftpletName, ftplet);
            }
        }
        catch(FtpException ex) {
            container.destroy();
            throw ex;
        }
        catch(Exception ex) {
            container.destroy();
            log.fatal("FtpletContainer.init()", ex);
            throw new FtpException("FtpletContainer.init()", ex);
        }
    }
    
    /**
     * Create component. 
     */
    private Component createComponent(Configuration parentConfig, String configName, String defaultClass) throws Exception {
        
        // get configuration subset
        Configuration conf = parentConfig.subset(configName);
        
        // create and configure component
        String className = conf.getString("class", defaultClass);
        Component comp = (Component)Class.forName(className).newInstance();
        comp.setLogFactory(logFactory);
        comp.configure(conf);
        return comp; 
    }
    
    /**
     * Create default users.
     */
    private void createDefaultUsers() throws Exception {
        UserManager userManager = getUserManager();
        
        // create admin user
        String adminName = userManager.getAdminName();
        if(!userManager.doesExist(adminName)) {
            log.info("Creating user : " + adminName);
            BaseUser adminUser = new BaseUser();
            adminUser.setName(adminName);
            adminUser.setPassword(adminName);
            adminUser.setEnabled(true);
            
            adminUser.setAuthorities(ADMIN_AUTHORITIES);

            adminUser.setHomeDirectory("./res/home");
            adminUser.setMaxIdleTime(0);
            userManager.save(adminUser);
        }
        
        // create anonymous user
        if(!userManager.doesExist("anonymous")) {
            log.info("Creating user : anonymous");
            BaseUser anonUser = new BaseUser();
            anonUser.setName("anonymous");
            anonUser.setPassword("");
            
            anonUser.setAuthorities(ANON_AUTHORITIES);
            
            anonUser.setEnabled(true);

            anonUser.setHomeDirectory("./res/home");
            anonUser.setMaxIdleTime(300);
            userManager.save(anonUser);
        }
    }
    
    /**
     * Get the log factory.
     */
    public LogFactory getLogFactory() {
        return logFactory;
    }
    
    /**
     * Get socket factory.
     */
    public SocketFactory getSocketFactory() {
        return socketFactory;
    }
    
    /**
     * Get user manager.
     */
    public UserManager getUserManager() {
        return userManager;
    }
    
    /**
     * Get IP restrictor.
     */
    public IpRestrictor getIpRestrictor() {
        return ipRestrictor;
    }
     
    /**
     * Get connection manager.
     */
    public ConnectionManager getConnectionManager() {
        return connectionManager;
    } 
    
    /**
     * Get file system manager.
     */
    public FileSystemManager getFileSystemManager() {
        return fileSystemManager;
    }
     
    /**
     * Get message resource.
     */
    public MessageResource getMessageResource() {
        return messageResource;
    }
    
    /**
     * Get ftp statistics.
     */
    public FtpStatistics getFtpStatistics() {
        return statistics;
    }
    
    /**
     * Get ftplet handler.
     */
    public Ftplet getFtpletContainer() {
        return ftpletContainer;
    }

    /**
     * Get data connection config.
     */
    public DataConnectionConfig getDataConnectionConfig() {
        return dataConConfig;
    }
    
    /**
     * Get the command factory.
     */
    public CommandFactory getCommandFactory() {
        return commandFactory;
    }
    
    /**
     * Get server address.
     */
    public InetAddress getServerAddress() {
        return socketFactory.getServerAddress();
    } 
        
    /**
     * Get server port.
     */
    public int getServerPort() {
        return socketFactory.getPort();
    } 
    
    /**
     * Get Ftplet.
     */
    public Ftplet getFtplet(String name) {
        return ftpletContainer.getFtplet(name);
    }
    
    /**
     * Close all the components.
     */
    public void dispose() {
        
        if(connectionManager != null) {
            connectionManager.dispose();
            connectionManager = null;
        }
        
        if(dataConConfig != null) {
            dataConConfig.dispose();
            dataConConfig = null;
        }
        
        if(ftpletContainer != null) {
            if(ftpletContainer instanceof Component) {
                ((Component)ftpletContainer).dispose();
            }
            ftpletContainer = null;
        }
        
        if(userManager != null) {
            userManager.dispose();
            userManager = null;
        }
        
        if(ipRestrictor != null) {
            ipRestrictor.dispose();
            ipRestrictor = null;
        }
        
        if(fileSystemManager != null) {
            fileSystemManager.dispose();
            fileSystemManager = null;
        }
        
        if(statistics != null) {
            statistics.dispose();
            statistics = null;
        }
        
        if(messageResource != null) {
            messageResource.dispose();
            messageResource = null;
        }
        
        if(logFactory != null) {
            logFactory.release();
            logFactory = null;
        }
    }
} 
