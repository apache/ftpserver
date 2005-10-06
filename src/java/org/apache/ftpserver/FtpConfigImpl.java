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
import org.apache.ftpserver.ftplet.EmptyConfiguration;
import org.apache.ftpserver.ftplet.FileSystemManager;
import org.apache.ftpserver.ftplet.FtpStatistics;
import org.apache.ftpserver.ftplet.Ftplet;
import org.apache.ftpserver.ftplet.UserManager;
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

    private LogFactory m_logFactory;
    private ISocketFactory m_socketFactory;
    private IDataConnectionConfig m_dataConConfig;
    private IMessageResource m_messageResource;
    private IConnectionManager m_connectionManager;
    private IIpRestrictor m_ipRestrictor;
    private UserManager m_userManager;
    private FileSystemManager m_fileSystemManager;
    private FtpletContainer m_ftpletContainer;
    private IFtpStatistics m_statistics;
    
    private Log m_log;
    
    
    /**
     * Constructor - set the root configuration.
     */
    public FtpConfigImpl(Configuration conf) throws Exception {
        
        try {
            
            // get the log classes
            m_logFactory = LogFactory.getFactory();
            m_logFactory = new FtpLogFactory(m_logFactory);
            m_log        = m_logFactory.getInstance(FtpConfigImpl.class);
            
            // create all the components
            m_socketFactory     = (ISocketFactory)        createComponent(conf, "socket-factory",      "org.apache.ftpserver.socketfactory.FtpSocketFactory");
            m_dataConConfig     = (IDataConnectionConfig) createComponent(conf, "data-connection",     "org.apache.ftpserver.DataConnectionConfig"); 
            m_messageResource   = (IMessageResource)      createComponent(conf, "message",             "org.apache.ftpserver.message.MessageResourceImpl");
            m_connectionManager = (IConnectionManager)    createComponent(conf, "connection-manager",  "org.apache.ftpserver.ConnectionManagerImpl");
            m_ipRestrictor      = (IIpRestrictor)         createComponent(conf, "ip-restrictor",       "org.apache.ftpserver.iprestrictor.FileIpRestrictor");
            m_userManager       = (UserManager)           createComponent(conf, "user-manager",        "org.apache.ftpserver.usermanager.PropertiesUserManager");
            m_fileSystemManager = (FileSystemManager)     createComponent(conf, "file-system-manager", "org.apache.ftpserver.filesystem.NativeFileSystemManager");
            m_statistics        = (IFtpStatistics)        createComponent(conf, "statistics",          "org.apache.ftpserver.FtpStatisticsImpl");
            
            // create user if necessary
            boolean userCreate = conf.getBoolean("create-default-user", true);
            if(userCreate) {
                createDefaultUsers();
            }
            
            // create and initialize ftlets
            m_ftpletContainer = new FtpletContainer();
            String ftpletNames = conf.getString("ftplets", null);
            Configuration ftpletConf = conf.getConfiguration("ftplet", EmptyConfiguration.INSTANCE);
            m_ftpletContainer.init(this, ftpletNames, ftpletConf);        
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
        Configuration conf = parentConfig.getConfiguration(configName, EmptyConfiguration.INSTANCE);
        String className = conf.getString("class", defaultClass);
        Component comp = (Component)Class.forName(className).newInstance();
        comp.setLogFactory(m_logFactory);
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
            m_log.info("Creating user : " + adminName);
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
            m_log.info("Creating user : anonymous");
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
        return m_logFactory;
    }
    
    /**
     * Get socket factory.
     */
    public ISocketFactory getSocketFactory() {
        return m_socketFactory;
    }
    
    /**
     * Get user manager.
     */
    public UserManager getUserManager() {
        return m_userManager;
    }
    
    /**
     * Get IP restrictor.
     */
    public IIpRestrictor getIpRestrictor() {
        return m_ipRestrictor;
    }
     
    /**
     * Get connection manager.
     */
    public IConnectionManager getConnectionManager() {
        return m_connectionManager;
    } 
    
    /**
     * Get file system manager.
     */
    public FileSystemManager getFileSystemManager() {
        return m_fileSystemManager;
    }
     
    /**
     * Get message resource.
     */
    public IMessageResource getMessageResource() {
        return m_messageResource;
    }
    
    /**
     * Get ftp statistics.
     */
    public FtpStatistics getFtpStatistics() {
        return m_statistics;
    }
    
    /**
     * Get ftplet handler.
     */
    public Ftplet getFtpletContainer() {
        return m_ftpletContainer;
    }

    /**
     * Get data connection config.
     */
    public IDataConnectionConfig getDataConnectionConfig() {
        return m_dataConConfig;
    }
    
    /**
     * Get server address.
     */
    public InetAddress getServerAddress() {
        return m_socketFactory.getServerAddress();
    } 
        
    /**
     * Get server port.
     */
    public int getServerPort() {
        return m_socketFactory.getPort();
    } 
    
    /**
     * Get Ftplet.
     */
    public Ftplet getFtplet(String name) {
        return m_ftpletContainer.getFtplet(name);
    }
    
    /**
     * Close all the components.
     */
    public void dispose() {
        
        if(m_connectionManager != null) {
            m_connectionManager.dispose();
            m_connectionManager = null;
        }
        
        if(m_dataConConfig != null) {
            m_dataConConfig.dispose();
            m_dataConConfig = null;
        }
        
        if(m_ftpletContainer != null) {
            m_ftpletContainer.destroy();
            m_ftpletContainer = null;
        }
        
        if(m_userManager != null) {
            m_userManager.dispose();
            m_userManager = null;
        }
        
        if(m_ipRestrictor != null) {
            m_ipRestrictor.dispose();
            m_ipRestrictor = null;
        }
        
        if(m_fileSystemManager != null) {
            m_fileSystemManager.dispose();
            m_fileSystemManager = null;
        }
        
        if(m_statistics != null) {
            m_statistics.dispose();
            m_statistics = null;
        }
        
        if(m_messageResource != null) {
            m_messageResource.dispose();
            m_messageResource = null;
        }
        
        if(m_logFactory != null) {
            m_logFactory.release();
            m_logFactory = null;
        }
    }
} 
