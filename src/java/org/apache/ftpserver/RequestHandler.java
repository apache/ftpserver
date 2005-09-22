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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;

import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.ftplet.Ftplet;
import org.apache.ftpserver.ftplet.FtpletEnum;
import org.apache.ftpserver.ftplet.Logger;
import org.apache.ftpserver.interfaces.ConnectionObserver;
import org.apache.ftpserver.interfaces.IConnection;
import org.apache.ftpserver.interfaces.IConnectionManager;
import org.apache.ftpserver.interfaces.IFtpConfig;
import org.apache.ftpserver.interfaces.IFtpStatistics;
import org.apache.ftpserver.interfaces.IIpRestrictor;
import org.apache.ftpserver.interfaces.ISsl;
import org.apache.ftpserver.util.IoUtils;


/**
 * This is a generic request handler. It delegates 
 * the request to appropriate method in subclass.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class RequestHandler implements IConnection {

    private static final HashMap COMMAND_MAP = new HashMap(64);
    
    private IFtpConfig m_fconfig;
    
    private Socket m_controlSocket;
    private FtpRequestImpl m_request;
    private FtpWriter m_writer;
    private BufferedReader m_reader;
    private boolean m_isConnectionClosed;
    
    private DirectoryLister m_directoryLister;
    private char m_dataType    = 'A';
    private char m_structure   = 'F';
    
    
    /**
     * Constructor - set the control socket.
     */
    public RequestHandler(IFtpConfig fconfig, Socket controlSocket) throws IOException {
        m_fconfig = fconfig;
        m_controlSocket = controlSocket;
        
        // data connection object
        FtpDataConnection dataCon = new FtpDataConnection();
        dataCon.setFtpConfig(m_fconfig);
        
        // reader object
        m_request = new FtpRequestImpl();
        m_request.setClientAddress(m_controlSocket.getInetAddress());
        m_request.setFtpDataConnection(dataCon);
        
        // writer object
        m_writer = new FtpWriter();
        m_writer.setControlSocket(m_controlSocket);
        m_writer.setFtpConfig(m_fconfig);
        m_writer.setFtpRequest(m_request);
    }
    
    /**
     * Set observer.
     */
    public void setObserver(ConnectionObserver observer) {
        
        // set writer observer
        FtpWriter writer = m_writer;
        if(writer != null) {
            writer.setObserver(observer);
        }
        
        // set request observer
        FtpRequestImpl request = m_request;
        if(request != null) {
            request.setObserver(observer);
        }
    }   
    
    /**
     * Get the configuration object.
     */
    public IFtpConfig getConfig() {
        return m_fconfig;
    }
    
    /**
     * Get directory lister.
     */
    public DirectoryLister getDirectoryLister() {
        return m_directoryLister;
    }
    
    /**
     * Set directory lister.
     */
    public void setDirectoryLister(DirectoryLister lister) {
        m_directoryLister = lister;
    }
                
    /**
     * Get the data type.
     */
    public char getDataType() {
        return m_dataType;
    }
    
    /**
     * Set the data type.
     */
    public void setDataType(char type) {
        m_dataType = type;
    }

    /**
     * Get structure.
     */
    public char getStructure() {
        return m_structure;
    }
    
    /**
     * Set structure
     */
    public void setStructure(char stru) {
        m_structure = stru;
    }
        
    /**
     * Get request.
     */
    public FtpRequest getRequest() {
        return m_request;
    }
    
    /**
     * Server one FTP client connection.
     */
    public void run() {
        
        InetAddress clientAddr = m_request.getRemoteAddress();
        IConnectionManager conManager = m_fconfig.getConnectionManager();
        Logger logger = m_fconfig.getLogger();
        
        try {
            
            // write log message
            logger.info("Open connection - " + clientAddr.getHostAddress());
            
            // notify ftp statistics
            IFtpStatistics ftpStat = (IFtpStatistics)m_fconfig.getFtpStatistics();
            ftpStat.setOpenConnection(this);
            
            // call Ftplet.onConnect() method
            boolean isSkipped = false;
            Ftplet ftpletContainer = m_fconfig.getFtpletContainer();
            FtpletEnum ftpletRet = ftpletContainer.onConnect(m_request, m_writer);
            if(ftpletRet == FtpletEnum.RET_SKIP) {
                isSkipped = true;
            }
            else if(ftpletRet == FtpletEnum.RET_DISCONNECT) {
                conManager.closeConnection(this);
                return;
            }
            
            if(!isSkipped) {

                // IP permission check
                IIpRestrictor ipRestrictor = m_fconfig.getIpRestrictor();
                if( !ipRestrictor.hasPermission(clientAddr) ) {
                    m_writer.send(530, "ip.restricted", null);
                    return;
                }
                
                // connection limit check
                int maxConnections = conManager.getMaxConnections();
                if(ftpStat.getCurrentConnectionNumber() > maxConnections) {
                    m_writer.send(530, "connection.limit", null);
                    return;
                }
                
                // everything is fine - go ahead 
                m_writer.send(220, null, null);
            }
            
            m_reader = new BufferedReader(new InputStreamReader(m_controlSocket.getInputStream(), "UTF-8"));
            do {
                notifyObserver();
                String commandLine = m_reader.readLine();
                
                // test command line
                if(commandLine == null) {
                    break;
                }
                if(commandLine.equals("")) {
                    continue;
                }
                
                // parse and check permission
                m_request.parse(commandLine);
                if(!hasPermission()) {
                    m_writer.send(530, "permission", null);
                    continue;
                }

                // execute command
                service(m_request, m_writer);
            }
            while(!m_isConnectionClosed);
        }
        catch(SocketException ex) {
            // socket closed - no need to do anything
        }
        catch(Exception ex) {
            logger.warn("RequestHandler.run()", ex);
        }
        finally {
            // close all resources if not done already
            if(!m_isConnectionClosed) {
                 conManager.closeConnection(this);
            }
        }
    }
    
    /**
     * Notify connection manager observer.
     */
    protected void notifyObserver() {
        m_request.updateLastAccessTime();
        m_fconfig.getConnectionManager().updateConnection(this);
    }

    /**
     * Execute the ftp command.
     */
    public void service(FtpRequestImpl request, FtpWriter out) throws IOException, FtpException {
        try {
            Command command = (Command)COMMAND_MAP.get( request.getCommand() );
            if(command != null) {
                command.execute(this, request, out);
            }
            else {
                out.send(502, "not.implemented", null);
            }
        }
        catch(Exception ex) {
            
            // send error reply
            try { 
                out.send(550, null, null);
            }
            catch(Exception ex1) {
            }
            
            if (ex instanceof java.io.IOException) {
               throw (IOException)ex;
            }
            else {
               m_fconfig.getLogger().warn("RequestHandler.service()", ex);
            }
        }
    }
    
    /**
     * Close connection. This is called by the connection service.
     */
    public void close() {
        
        // check whether already closed or not
        synchronized(this) {
            if(m_isConnectionClosed) {
                return;
            }
            m_isConnectionClosed = true;
        }
        
        // call Ftplet.onDisconnect() method.
        Logger logger = m_fconfig.getLogger();
        try {
            Ftplet ftpletContainer = m_fconfig.getFtpletContainer();
            ftpletContainer.onDisconnect(m_request, m_writer);
        }
        catch(Exception ex) {
            logger.warn("RequestHandler.close()", ex);
        }

        // notify statistics object and close request
        IFtpStatistics ftpStat = (IFtpStatistics)m_fconfig.getFtpStatistics();
        FtpRequestImpl request = m_request;
        if(request != null) {
            
            // log message
            String userName = request.getUser().getName();
            InetAddress clientAddr = request.getRemoteAddress(); 
            logger.info("Close connection : " + clientAddr.getHostAddress() + " - " + userName);
            
            // logout if necessary and notify statistics
            if(request.isLoggedIn()) {
                request.setLogout();
                ftpStat.setLogout(this);
            }
            ftpStat.setCloseConnection(this);
            
            // clear request
            request.clear();
            request.setObserver(null);
            request.getFtpDataConnection().dispose();
            m_request = null;
        }
                
        // close ftp writer
        FtpWriter writer = m_writer;
        if(writer != null) {
            writer.setObserver(null);
            writer.close();
            m_writer = null;
        }
        
        // close buffered reader
        BufferedReader reader = m_reader;
        if(reader != null) {
            IoUtils.close(reader);
            m_reader = null;
        }
        
        // close control socket
        Socket controlSocket = m_controlSocket;
        if (controlSocket != null) {
            try {
                controlSocket.close();
            }
            catch(Exception ex) {
                logger.warn("RequestHandler.close()", ex);
            }
            m_controlSocket = null;
        }
    }

    /**
     * Check user permission to execute ftp command. 
     */
    protected boolean hasPermission() {
        String cmd = m_request.getCommand();
        if(cmd == null) {
            return false;
        }
        return m_request.isLoggedIn() ||
        cmd.equals("USER")            || 
        cmd.equals("PASS")            ||
        cmd.equals("AUTH")            ||
        cmd.equals("HELP")            ||
        cmd.equals("SYST")            ||
        cmd.equals("FEAT")            ||
        cmd.equals("PBSZ")            ||
        cmd.equals("PROT")            ||
        cmd.equals("LANG")            ||
        cmd.equals("QUIT");
    }    
    
    /**
     * Transfer data.
     */
    public final long transfer(BufferedInputStream in, 
                               BufferedOutputStream out,
                               int maxRate) throws IOException {
        
        boolean isAscii = m_dataType == 'A';
        long startTime = System.currentTimeMillis();
        long transferredSize = 0L;
        byte[] buff = new byte[4096];
        
        while(true) {
            
            // if current rate exceeds the max rate, sleep for 50ms
            if(maxRate > 0) {
                
                // prevent "divide by zero" exception
                long interval = System.currentTimeMillis() - startTime;
                if(interval == 0) {
                    interval = 1;
                }
                
                // check current rate
                long currRate = (transferredSize*1000L)/interval;
                if(currRate > maxRate) {
                    try { Thread.sleep(50); } catch(InterruptedException ex) {break;}
                }
            }
            
            // read data
            int count = in.read(buff);
            if(count == -1) {
                break;
            }
            
            // write data
            // if ascii, replace \n by \r\n
            if(isAscii) {
                for(int i=0; i<count; ++i) {
                    byte b = buff[i];
                    if(b == '\n') {
                        out.write('\r');
                    }
                    out.write(b);
                }
            }
            else {
                out.write(buff, 0, count);
            }
            
            transferredSize += count;
            notifyObserver();
        }
        
        return transferredSize;
    }       
    
    /**
     * Create secure socket.
     */
    public void createSecureSocket() throws Exception {
        
        // change socket to SSL socket
        ISsl ssl = m_fconfig.getDataConnectionConfig().getSSL();
        if(ssl == null) {
            throw new FtpException("Socket factory SSL not configured");
        }
        Socket ssoc = ssl.createSocket(m_controlSocket, false);
        
        // change streams
        m_reader = new BufferedReader(new InputStreamReader(ssoc.getInputStream(), "UTF-8"));
        m_writer.setControlSocket(ssoc);
        
        // set control socket
        m_controlSocket = ssoc;
    }
    
    /////////////////////////////////////////////////////////////////////
    static {
        COMMAND_MAP.put("ABOR", new org.apache.ftpserver.command.ABOR());
        COMMAND_MAP.put("ACCT", new org.apache.ftpserver.command.ACCT());
        COMMAND_MAP.put("APPE", new org.apache.ftpserver.command.APPE());
        COMMAND_MAP.put("AUTH", new org.apache.ftpserver.command.AUTH());
        COMMAND_MAP.put("CDUP", new org.apache.ftpserver.command.CDUP());
        COMMAND_MAP.put("CWD",  new org.apache.ftpserver.command.CWD());
        COMMAND_MAP.put("DELE", new org.apache.ftpserver.command.DELE());
        COMMAND_MAP.put("EPRT", new org.apache.ftpserver.command.EPRT());
        COMMAND_MAP.put("EPSV", new org.apache.ftpserver.command.EPSV());
        COMMAND_MAP.put("FEAT", new org.apache.ftpserver.command.FEAT());
        COMMAND_MAP.put("HELP", new org.apache.ftpserver.command.HELP());
        COMMAND_MAP.put("LANG", new org.apache.ftpserver.command.LANG());
        COMMAND_MAP.put("LIST", new org.apache.ftpserver.command.LIST());
        COMMAND_MAP.put("MDTM", new org.apache.ftpserver.command.MDTM());
        COMMAND_MAP.put("MLST", new org.apache.ftpserver.command.MLST());
        COMMAND_MAP.put("MKD",  new org.apache.ftpserver.command.MKD());
        COMMAND_MAP.put("MLSD", new org.apache.ftpserver.command.MLSD());
        COMMAND_MAP.put("MODE", new org.apache.ftpserver.command.MODE());
        COMMAND_MAP.put("NLST", new org.apache.ftpserver.command.NLST());
        COMMAND_MAP.put("NOOP", new org.apache.ftpserver.command.NOOP());
        COMMAND_MAP.put("OPTS", new org.apache.ftpserver.command.OPTS());
        COMMAND_MAP.put("PASS", new org.apache.ftpserver.command.PASS());
        COMMAND_MAP.put("PASV", new org.apache.ftpserver.command.PASV());
        COMMAND_MAP.put("PBSZ", new org.apache.ftpserver.command.PBSZ());
        COMMAND_MAP.put("PORT", new org.apache.ftpserver.command.PORT());
        COMMAND_MAP.put("PROT", new org.apache.ftpserver.command.PROT());
        COMMAND_MAP.put("PWD",  new org.apache.ftpserver.command.PWD());
        COMMAND_MAP.put("QUIT", new org.apache.ftpserver.command.QUIT());
        COMMAND_MAP.put("REIN", new org.apache.ftpserver.command.REIN());
        COMMAND_MAP.put("REST", new org.apache.ftpserver.command.REST());
        COMMAND_MAP.put("RETR", new org.apache.ftpserver.command.RETR());
        COMMAND_MAP.put("RMD",  new org.apache.ftpserver.command.RMD());
        COMMAND_MAP.put("RNFR", new org.apache.ftpserver.command.RNFR());
        COMMAND_MAP.put("RNTO", new org.apache.ftpserver.command.RNTO());
        COMMAND_MAP.put("SITE", new org.apache.ftpserver.command.SITE());
        COMMAND_MAP.put("SIZE", new org.apache.ftpserver.command.SIZE());
        COMMAND_MAP.put("STAT", new org.apache.ftpserver.command.STAT());
        COMMAND_MAP.put("STOR", new org.apache.ftpserver.command.STOR());
        COMMAND_MAP.put("STOU", new org.apache.ftpserver.command.STOU());
        COMMAND_MAP.put("STRU", new org.apache.ftpserver.command.STRU());
        COMMAND_MAP.put("SYST", new org.apache.ftpserver.command.SYST());
        COMMAND_MAP.put("TYPE", new org.apache.ftpserver.command.TYPE());
        COMMAND_MAP.put("USER", new org.apache.ftpserver.command.USER());
    }
}
