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
import org.apache.ftpserver.interfaces.IDataConnectionConfig;
import org.apache.ftpserver.interfaces.ISsl;

import java.net.InetAddress;
import java.util.StringTokenizer;

/**
 * Data connection configuration.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class DataConnectionConfig implements IDataConnectionConfig {

    private Logger m_logger;
    private InetAddress m_pasvAddress;
    private int m_pasvPort[][];
    
    private boolean m_portEnable;
    private boolean m_portIpCheck;
    
    private ISsl m_ssl;
    
    /**
     * Set logger.
     */
    public void setLogger(Logger logger) {
        m_logger = logger;
    }
    
    /**
     * Configure the data connection config object.
     */
    public void configure(Configuration conf) throws FtpException {
        
        try {
            
            // get passive address
            String pasvAddress = conf.getString("pasv-address", null);
            if(pasvAddress == null) {
                m_pasvAddress = InetAddress.getLocalHost();
            }
            else {
                m_pasvAddress = InetAddress.getByName(pasvAddress);
            }
            
            // get PASV ports
            String pasvPorts = conf.getString("pasv-port", "0");
            StringTokenizer st = new StringTokenizer(pasvPorts, " ,;\t\n\r\f");
            m_pasvPort = new int[st.countTokens()][2];
            for(int i=0; i<m_pasvPort.length; i++) {
                m_pasvPort[i][0] = Integer.parseInt(st.nextToken());
                m_pasvPort[i][1] = 0;
            }
            
            // get PORT parameters
            m_portEnable = conf.getBoolean("port-enable", true);
            m_portIpCheck = conf.getBoolean("port-ip-check", false);
            
            // create SSL component
            Configuration sslConf = conf.getConfiguration("ssl", null);
            if(sslConf != null) {
                m_ssl = (ISsl)Class.forName("org.apache.ftpserver.ssl.Ssl").newInstance();
                m_ssl.setLogger(m_logger);
                m_ssl.configure(sslConf);
            }
        }
        catch(FtpException ex) {
            throw ex;
        }
        catch(Exception ex) {
            m_logger.error("DataConnectionConfig.configure()", ex);
            throw new FtpException("DataConnectionConfig.configure()", ex);
        }
    }
    
    /**
     * Is PORT enabled?
     */
    public boolean isPortEnabled() {
        return m_portEnable;
    }
    
    /**
     * Check the PORT IP?
     */
    public boolean isPortIpCheck() {
        return m_portIpCheck;
    }
    
    /**
     * Get passive host.
     */
    public InetAddress getPassiveAddress() {
        return m_pasvAddress;
    }
    
    /**
     * Get SSL component.
     */
    public ISsl getSSL() {
        return m_ssl;
    }
    
    /**
     * Get passive data port. Data port number zero (0) means that 
     * any available port will be used.
     */
    public synchronized int getPassivePort() {        
        int dataPort = -1;
        int loopTimes = 2;
        Thread currThread = Thread.currentThread();
        
        while( (dataPort==-1) && (--loopTimes >= 0)  && (!currThread.isInterrupted()) ) {

            // search for a free port            
            for(int i=0; i<m_pasvPort.length; i++) {
                if(m_pasvPort[i][1] == 0) {
                    if(m_pasvPort[i][0] != 0) {
                        m_pasvPort[i][1] = 1;
                    }
                    dataPort = m_pasvPort[i][0];
                    break;
                }
            }

            // no available free port - wait for the release notification
            if(dataPort == -1) {
                try {
                    wait();
                }
                catch(InterruptedException ex) {
                }
            }

        }
        return dataPort;
    }

    /**
     * Release data port
     */
    public synchronized void releasePassivePort(int port) {
        for(int i=0; i<m_pasvPort.length; i++) {
            if(m_pasvPort[i][0] == port) {
                m_pasvPort[i][1] = 0;
                break;
            }
        }
        notify();
    }
    
    /**
     * Dispose it.
     */
    public void dispose() {
    }
}
