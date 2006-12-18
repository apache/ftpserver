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

package org.apache.ftpserver.gui;

import javax.swing.JComponent;

import org.apache.ftpserver.interfaces.FtpServerContext;

/**
 * This is the plugin panel container.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
interface PluginPanelContainer {

    public final static int ROOT_INDEX = 0;
    public final static int USER_INDEX = 1;
    public final static int IP_INDEX   = 2;
    public final static int MSG_INDEX  = 3;
    public final static int CON_INDEX  = 4;
    public final static int SPY_INDEX  = 5;
    public final static int FILE_INDEX = 6;
    public final static int DIR_INDEX  = 7;
    public final static int LOG_INDEX  = 8;
    public final static int STAT_INDEX = 9;
    
    /**
     * Add plugin panel.
     */
    void add(PluginPanel panel);
    
    /**
     * Get selected panel index.
     */
    int getSelectedIndex();
    
    /**
     * Set selected panel index.
     */
    void setSelectedIndex(int index);
    
    /**
     * Get the plugin panel at the specified index.
     */
    PluginPanel getPluginPanel(int index);
    
    /**
     * Get component.
     */
    JComponent getComponent();
    
    /**
     * Refresh with new ftp configuration object.
     */
    void refresh(FtpServerContext serverContext);
    
    /**
     * Get current ftp config
     */
    FtpServerContext getServerContext();
}
