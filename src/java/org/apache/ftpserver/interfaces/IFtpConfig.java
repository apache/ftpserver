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
package org.apache.ftpserver.interfaces;

import org.apache.ftpserver.ftplet.FtpConfig;
import org.apache.ftpserver.ftplet.Ftplet;

/**
 * This is basically <code>org.apache.ftpserver.ftplet.FtpConfig</code> with added
 * connection manager, message resource functionalities.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
interface IFtpConfig extends FtpConfig {

    /**
     * Get connection manager.
     */
    IConnectionManager getConnectionManager();
    
    /**
     * Get message resource.
     */
    IMessageResource getMessageResource();
    
    /**
     * Get IP restrictor.
     */
    IIpRestrictor getIpRestrictor();
    
    /**
     * Get ftplet container.
     */
    Ftplet getFtpletContainer();
    
    /**
     * Get server socket factory.
     */
    ISocketFactory getSocketFactory();
        
    /**
     * Get data connection config.
     */
    IDataConnectionConfig getDataConnectionConfig();
    
    /**
     * Release all components.
     */
    void dispose();
}
