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

import org.apache.ftpserver.ftplet.Configuration;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.Logger;
import org.apache.ftpserver.interfaces.ConnectionManagerObserver;
import org.apache.ftpserver.interfaces.IConnection;
import org.apache.ftpserver.interfaces.IConnectionManager;
import org.apache.ftpserver.usermanager.BaseUser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

/**
 * Connection service to manage all the connections (request handlers).
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class ConnectionManagerImpl implements IConnectionManager {

    private ConnectionManagerObserver m_observer;              
    private Timer m_timer;
    private Vector m_conList = new Vector();  
    
    private int m_maxConnections;
    private int m_maxLogins;
    private boolean m_anonEnabled;
    private int m_maxAnonLogins;
    
    private int m_defaultIdleSec;
    private int m_pollIntervalSec;
    
    private Logger m_logger;
    
    /**
     * Set logger object.
     */
    public void setLogger(Logger logger) {
        m_logger = logger;
    }
    
    /**
     * Configure connection service
     */
    public void configure(Configuration config) throws FtpException {
        
        // get configuration parameters
        m_maxConnections  = config.getInt     ("max-connection",          20);
        m_maxLogins       = config.getInt     ("max-login",               10);
        m_anonEnabled     = config.getBoolean ("anonymous-login-enabled", true);
        m_maxAnonLogins   = config.getInt     ("max-anonymous-login",     10);
        m_defaultIdleSec  = config.getInt     ("default-idle-time",       60);
        m_pollIntervalSec = config.getInt     ("timeout-poll-inverval",   60);
        
        // set timer to remove inactive users and load data
        m_timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            public void run() {
                timerTask();
            }
        };
        m_timer.schedule(timerTask, 0, m_pollIntervalSec*1000L);
    } 
    
    /**
     * Set connection manager observer.
     */
    public void setObserver(ConnectionManagerObserver observer) {
        m_observer = observer;
    }
    
    /**
     * Get maximum number of connections.
     */
    public int getMaxConnections() {
        return m_maxConnections;
    }
     
    /**
     * Get maximum number of logins.
     */
    public int getMaxLogins() {
        return m_maxLogins;
    }
    
    /**
     * Is anonymous login enabled?
     */
    public boolean isAnonymousLoginEnabled() {
        return m_anonEnabled;
    }
    
    /**
     * Get maximum anonymous logins
     */
    public int getMaxAnonymousLogins() {
        return m_maxAnonLogins;
    }

    /**
     * Get all request handlers
     */
    public List getAllConnections() {
        List cons = m_conList;
        if(cons == null) {
            return new Vector();
        }
        return new Vector(cons);
    }
    
    /**
     * New connection has been established.
     */
    public void newConnection(IConnection connection) {
        
        // null connection - ignore
        if (connection == null) {
            return;
        }
        
        // disposed - ignore
        List cons = m_conList;
        if(cons == null) {
            return;
        }
        cons.add(connection);
        
        // notify observer about a new connection
        ConnectionManagerObserver observer = m_observer;
        if (observer != null) {
            observer.openedConnection(connection);
            observer.updatedConnection(connection);
        }
        
        // set default idle time
        BaseUser user = (BaseUser)connection.getRequest().getUser();
        user.setMaxIdleTime(m_defaultIdleSec);
        
        // now start a new thread to serve this connection
        new Thread(connection).start();
    }
    
    /**
     * Connection has been updated - notify listeners
     */
    public void updateConnection(IConnection connection) {
    
        // null connection - ignore
        if(connection == null) {
            return;
        }
        
        // notify observer
        ConnectionManagerObserver observer = m_observer;
        if(observer != null) {
            observer.updatedConnection(connection); 
        }
    }
    
    /**
     * Close connection.
     */
    public void closeConnection(IConnection connection) {
        
        // null connection - ignore
        if (connection == null) {
            return;
        }
        
        // close socket
        List cons = m_conList;
        if(cons != null) {
            cons.remove(connection);
        }
        connection.close();
        
        // notify observer
        ConnectionManagerObserver observer = m_observer;
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
            IConnection connection = (IConnection)it.next();
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
        Vector conList = m_conList;
        if(conList == null) {
            return;
        }
        
        // get inactive client connection list 
        synchronized(conList) {
            long idleTimeMillis = m_defaultIdleSec*1000L;
            for( int i = conList.size(); --i>=0; ) {
                IConnection con = (IConnection)conList.get(i);
                if(con == null) {
                    continue;
                }
                    
                // idle client connection
                FtpRequestImpl request = (FtpRequestImpl)con.getRequest();
                if(request.isTimeout(currTime)) {
                    inactiveCons.add(con);
                }
                
                // idle data connection
                FtpDataConnection dataCon = request.getFtpDataConnection();
                if(dataCon == null) {
                    continue;
                }
                synchronized(dataCon) {
                    
                    // data transfer is going on - no need to close
                    if(dataCon.isActive()) {
                        continue;
                    }
                    
                    // data connection is already closed 
                    long requestTime = dataCon.getRequestTime();
                    if(requestTime == 0L) {
                        continue;
                    }
                    
                    // idle data connectin timeout - close it 
                    if( (currTime - requestTime) > idleTimeMillis ) {
                        m_logger.info("Removing idle data connection for " + con.getRequest().getUser());
                        dataCon.closeDataSocket();
                    }
                }
            }
        }
        
        // close idle client connections
        for( Iterator conIt=inactiveCons.iterator(); conIt.hasNext(); ) {
            IConnection connection = (IConnection)conIt.next();
            m_logger.info("Removing idle user " + connection.getRequest().getUser());
            closeConnection(connection);
        }
    }
    
    /**
     * Dispose connections
     */
    public void dispose() {
        
        // stop timer
        Timer timer = m_timer;
        if (timer != null) {
            timer.cancel();
            m_timer = null;
        }
        
        // close all connections
        List cons = m_conList;
        if (cons != null) {
            closeAllConnections();
            m_conList = null;
        }
    } 
}
