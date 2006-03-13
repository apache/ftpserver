// $Id$
/*
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ftpserver;

import java.net.InetAddress;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ftpserver.ftplet.Component;
import org.apache.ftpserver.ftplet.Configuration;
import org.apache.ftpserver.ftplet.FileSystemManager;
import org.apache.ftpserver.ftplet.FtpStatistics;
import org.apache.ftpserver.ftplet.Ftplet;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.interfaces.ICommandFactory;
import org.apache.ftpserver.interfaces.IConnectionManager;
import org.apache.ftpserver.interfaces.IDataConnectionConfig;
import org.apache.ftpserver.interfaces.IFtpConfig;
import org.apache.ftpserver.interfaces.IFtpStatistics;
import org.apache.ftpserver.interfaces.IIpRestrictor;
import org.apache.ftpserver.interfaces.IMessageResource;
import org.apache.ftpserver.interfaces.ISocketFactory;
import org.apache.ftpserver.usermanager.BaseUser;

/**
 * FTP server configuration implementation. It holds all 
 * the components used.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public
class FtpConfigImpl implements IFtpConfig {

    private LogFactory logFactory;
    private ISocketFactory socketFactory;
    private IDataConnectionConfig dataConConfig;
    private IMessageResource messageResource;
    private IConnectionManager connectionManager;
    private IIpRestrictor ipRestrictor;
    private UserManager userManager;
    private FileSystemManager fileSystemManager;
    private FtpletContainer ftpletContainer;
    private IFtpStatistics statistics;
    private ICommandFactory commandFactory;
    
    private Log log;
    
    
    /**
     * Constructor - set the root configuration.
     */
    public FtpConfigImpl(Configuration conf) throws Exception {
        
        try {
            
            // get the log classes
            logFactory = LogFactory.getFactory();
            logFactory = new FtpLogFactory(logFactory);
            log        = logFactory.getInstance(FtpConfigImpl.class);
            
            // create all the components
            socketFactory     = (ISocketFactory)        createComponent(conf, "socket-factory",      "org.apache.ftpserver.socketfactory.FtpSocketFactory");
            dataConConfig     = (IDataConnectionConfig) createComponent(conf, "data-connection",     "org.apache.ftpserver.DataConnectionConfig"); 
            messageResource   = (IMessageResource)      createComponent(conf, "message",             "org.apache.ftpserver.message.MessageResourceImpl");
            connectionManager = (IConnectionManager)    createComponent(conf, "connection-manager",  "org.apache.ftpserver.ConnectionManagerImpl");
            ipRestrictor      = (IIpRestrictor)         createComponent(conf, "ip-restrictor",       "org.apache.ftpserver.iprestrictor.FileIpRestrictor");
            userManager       = (UserManager)           createComponent(conf, "user-manager",        "org.apache.ftpserver.usermanager.PropertiesUserManager");
            fileSystemManager = (FileSystemManager)     createComponent(conf, "file-system-manager", "org.apache.ftpserver.filesystem.NativeFileSystemManager");
            statistics        = (IFtpStatistics)        createComponent(conf, "statistics",          "org.apache.ftpserver.FtpStatisticsImpl");
            commandFactory    = (ICommandFactory)       createComponent(conf, "command-factory",     "org.apache.ftpserver.CommandFactory");
            
            // create user if necessary
            boolean userCreate = conf.getBoolean("create-default-user", true);
            if(userCreate) {
                createDefaultUsers();
            }
            
            // create and initialize ftlets
            ftpletContainer = new FtpletContainer();
            String ftpletNames = conf.getString("ftplets", null);
            Configuration ftpletConf = conf.subset("ftplet");
            ftpletContainer.init(this, ftpletNames, ftpletConf);        
        }
        catch(Exception ex) {
            dispose();
            throw ex;
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
            adminUser.setWritePermission(true);
            adminUser.setMaxUploadRate(0);
            adminUser.setMaxDownloadRate(0);
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
            anonUser.setEnabled(true);
            anonUser.setWritePermission(false);
            anonUser.setMaxUploadRate(4800);
            anonUser.setMaxDownloadRate(4800);
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
    public ISocketFactory getSocketFactory() {
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
    public IIpRestrictor getIpRestrictor() {
        return ipRestrictor;
    }
     
    /**
     * Get connection manager.
     */
    public IConnectionManager getConnectionManager() {
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
    public IMessageResource getMessageResource() {
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
    public IDataConnectionConfig getDataConnectionConfig() {
        return dataConConfig;
    }
    
    /**
     * Get the command factory.
     */
    public ICommandFactory getCommandFactory() {
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
            ftpletContainer.destroy();
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
