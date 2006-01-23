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
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ftpserver.ftplet.Configuration;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.interfaces.IDataConnectionConfig;
import org.apache.ftpserver.interfaces.ISsl;

/**
 * Data connection configuration.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class DataConnectionConfig implements IDataConnectionConfig {

    private Log log;
    private LogFactory logFactory;
    
    private InetAddress pasvAddress;
    private int pasvPort[][];
    
    private boolean portEnable;
    private boolean portIpCheck;
    
    private ISsl ssl;

    
    /**
     * Set the log factory. 
     */
    public void setLogFactory(LogFactory factory) {
        logFactory = factory;
        log = logFactory.getInstance(getClass());
    }
    
    /**
     * Configure the data connection config object.
     */
    public void configure(Configuration conf) throws FtpException {
        
        try {
            
            // get passive address
            String pasvAddress = conf.getString("pasv-address", null);
            if(pasvAddress == null) {
                this.pasvAddress = InetAddress.getLocalHost();
            }
            else {
                this.pasvAddress = InetAddress.getByName(pasvAddress);
            }
            
            // get PASV ports
            String pasvPorts = conf.getString("pasv-port", "0");
            StringTokenizer st = new StringTokenizer(pasvPorts, " ,;\t\n\r\f");
            pasvPort = new int[st.countTokens()][2];
            for(int i=0; i<pasvPort.length; i++) {
                pasvPort[i][0] = Integer.parseInt(st.nextToken());
                pasvPort[i][1] = 0;
            }
            
            // get PORT parameters
            portEnable = conf.getBoolean("port-enable", true);
            portIpCheck = conf.getBoolean("port-ip-check", false);
            
            // create SSL component
            Configuration sslConf = conf.subset("ssl");
            if(!sslConf.isEmpty()) {
                ssl = (ISsl)Class.forName("org.apache.ftpserver.ssl.Ssl").newInstance();
                ssl.setLogFactory(logFactory);
                ssl.configure(sslConf);
            }
        }
        catch(FtpException ex) {
            throw ex;
        }
        catch(Exception ex) {
            log.error("DataConnectionConfig.configure()", ex);
            throw new FtpException("DataConnectionConfig.configure()", ex);
        }
    }
    
    /**
     * Is PORT enabled?
     */
    public boolean isPortEnabled() {
        return portEnable;
    }
    
    /**
     * Check the PORT IP?
     */
    public boolean isPortIpCheck() {
        return portIpCheck;
    }
    
    /**
     * Get passive host.
     */
    public InetAddress getPassiveAddress() {
        return pasvAddress;
    }
    
    /**
     * Get SSL component.
     */
    public ISsl getSSL() {
        return ssl;
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
            for(int i=0; i<pasvPort.length; i++) {
                if(pasvPort[i][1] == 0) {
                    if(pasvPort[i][0] != 0) {
                        pasvPort[i][1] = 1;
                    }
                    dataPort = pasvPort[i][0];
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
        for(int i=0; i<pasvPort.length; i++) {
            if(pasvPort[i][0] == port) {
                pasvPort[i][1] = 0;
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
