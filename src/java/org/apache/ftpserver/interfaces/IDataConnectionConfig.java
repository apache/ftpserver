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

import java.net.InetAddress;

import org.apache.ftpserver.ftplet.Component;

/**
 * Data connection configuration interface.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
interface IDataConnectionConfig extends Component {

    /**
     * Is PORT data connection enabled?
     */
    boolean isPortEnabled();
    
    /**
     * Check the PORT IP?
     */
    boolean isPortIpCheck();
    
    /**
     * Get passive address.
     */
    InetAddress getPassiveAddress();
    
    /**
     * Get passive port.
     */
    int getPassivePort();
    
    /**
     * Release passive port.
     */
    void releasePassivePort(int port);
    
    /**
     * Get SSL component.
     */
    ISsl getSSL();
}
