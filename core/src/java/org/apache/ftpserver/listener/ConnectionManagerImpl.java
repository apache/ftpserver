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

package org.apache.ftpserver.listener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import org.apache.ftpserver.ServerDataConnectionFactory;
import org.apache.ftpserver.FtpSessionImpl;
import org.apache.ftpserver.ftplet.Component;
import org.apache.ftpserver.ftplet.Configuration;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Connection service to manage all the connections (request handlers).
 */
public 
class ConnectionManagerImpl implements ConnectionManager, Component {

    private final Logger LOG = LoggerFactory.getLogger(ConnectionManagerImpl.class);
    
    private ConnectionManagerObserver observer;              
    private Timer timer;
    private Vector conList = new Vector();  
    
    private int maxConnections;
    private int maxLogins;
    private boolean anonEnabled;
    private int maxAnonLogins;
    
    private int defaultIdleSec;
    private int pollIntervalSec;
    
    
    /**
     * Configure connection service
     */
    public void configure(Configuration config) throws FtpException {
        
        // get configuration parameters
        maxConnections  = config.getInt     ("max-connection",          20);
        maxLogins       = config.getInt     ("max-login",               10);
        anonEnabled     = config.getBoolean ("anonymous-login-enabled", true);
        maxAnonLogins   = config.getInt     ("max-anonymous-login",     10);
        defaultIdleSec  = config.getInt     ("default-idle-time",       60);
        pollIntervalSec = config.getInt     ("timeout-poll-interval",   60);
        
        // set timer to remove inactive users and load data
        timer = new Timer(true);
        TimerTask timerTask = new TimerTask() {
            public void run() {
                timerTask();
            }
        };
        timer.schedule(timerTask, 0, pollIntervalSec*1000L);
    } 
    
    /**
     * Set connection manager observer.
     */
    public void setObserver(ConnectionManagerObserver observer) {
        this.observer = observer;
    }
    
    /**
     * Get maximum number of connections.
     */
    public int getMaxConnections() {
        return maxConnections;
    }
     
    /**
     * Get maximum number of logins.
     */
    public int getMaxLogins() {
        return maxLogins;
    }
    
    /**
     * Is anonymous login enabled?
     */
    public boolean isAnonymousLoginEnabled() {
        return anonEnabled;
    }
    
    public int getDefaultIdleSec() {
        return defaultIdleSec;
    }
    
    /**
     * Get maximum anonymous logins
     */
    public int getMaxAnonymousLogins() {
        return maxAnonLogins;
    }

    /**
     * Get all request handlers
     */
    public List getAllConnections() {
        List cons = conList;
        if(cons == null) {
            return new Vector();
        }
        return new Vector(cons);
    }
    
    /**
     * New connection has been established.
     */
    public void newConnection(Connection connection) {
        
        // null connection - ignore
        if (connection == null) {
            return;
        }
        
        // disposed - ignore
        List cons = conList;
        if(cons == null) {
            return;
        }
        cons.add(connection);
        
        // notify observer about a new connection
        ConnectionManagerObserver observer = this.observer;
        if (observer != null) {
            observer.openedConnection(connection);
            observer.updatedConnection(connection);
        }
        
        /*
         * set default idle time for request. This value should be overrided
         * after user login
         */
        connection.getSession().setMaxIdleTime(defaultIdleSec);
        
        // now start a new thread to serve this connection if needed
        if(connection instanceof Runnable) {
            new Thread((Runnable)connection).start();
        }
        
    }
    
    /**
     * Connection has been updated - notify listeners
     */
    public void updateConnection(Connection connection) {
    
        // null connection - ignore
        if(connection == null) {
            return;
        }
        
        // notify observer
        ConnectionManagerObserver observer = this.observer;
        if(observer != null) {
            observer.updatedConnection(connection); 
        }
    }
    
    /**
     * Close connection.
     */
    public void closeConnection(Connection connection) {
        
        // null connection - ignore
        if (connection == null) {
            return;
        }
        
        // close socket
        List cons = conList;
        if(cons != null) {
            cons.remove(connection);
        }
        connection.close();
        
        // notify observer
        ConnectionManagerObserver observer = this.observer;
        if(observer != null) {
            observer.closedConnection(connection);
        }
    }
    
    /**
     * Close all connections.
     */
    public void closeAllConnections() {
        List allCons = getAllConnections();
        for( Iterator it = allCons.iterator(); it.hasNext(); ) {
            Connection connection = (Connection)it.next();
            closeConnection(connection);
        }
        allCons.clear();
    }
    
    /**
     * Timer thread will call this method periodically to
     * close inactice connections.
     */
    public void timerTask() {
    
        // get all connections
        ArrayList inactiveCons = new ArrayList();
        long currTime = System.currentTimeMillis();
        Vector conList = this.conList;
        if(conList == null) {
            return;
        }
        
        // get inactive client connection list 
        synchronized(conList) {
            for( int i = conList.size(); --i>=0; ) {
                Connection con = (Connection)conList.get(i);
                if(con == null) {
                    continue;
                }
                    
                // idle client connection
                FtpSessionImpl session = (FtpSessionImpl)con.getSession();
                if(session == null) {
                    continue;
                }
                if(session.isTimeout(currTime)) {
                    inactiveCons.add(con);
                    continue;
                }
                
                // idle data connection
                ServerDataConnectionFactory dataCon = session.getServerDataConnection();
                if(dataCon == null) {
                    continue;
                }
                synchronized(dataCon) {

                    // if the data connection is not active - close it
                    if(dataCon.isTimeout(currTime)) {
                        LOG.info("Removing idle data connection for " + session.getUser());
                        dataCon.closeDataConnection();
                    }
                }
            }
        }

        // close idle client connections
        for( Iterator conIt=inactiveCons.iterator(); conIt.hasNext(); ) {
            Connection connection = (Connection)conIt.next();
            if(connection == null) {
                continue;
            }
            
            FtpSession session = connection.getSession();
            if(session == null) {
                continue;
            }
            
            LOG.info("Removing idle user " + session.getUser());
            closeConnection(connection);
        }
    }
    
    /**
     * Dispose connections
     */
    public void dispose() {
        
        // stop timer
        Timer timer = this.timer;
        if (timer != null) {
            timer.cancel();
            this.timer = null;
        }
        
        // close all connections
        List cons = conList;
        if (cons != null) {
            closeAllConnections();
            conList = null;
        }
    } 
}
