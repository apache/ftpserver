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
import java.util.Map;

import javax.net.ssl.SSLException;

import org.apache.commons.logging.Log;
import org.apache.ftpserver.ftplet.DataType;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.ftplet.FtpSession;
import org.apache.ftpserver.ftplet.Ftplet;
import org.apache.ftpserver.ftplet.FtpletEnum;
import org.apache.ftpserver.ftplet.Structure;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.interfaces.Command;
import org.apache.ftpserver.interfaces.CommandFactory;
import org.apache.ftpserver.interfaces.Connection;
import org.apache.ftpserver.interfaces.ConnectionManager;
import org.apache.ftpserver.interfaces.ConnectionObserver;
import org.apache.ftpserver.interfaces.FtpServerContext;
import org.apache.ftpserver.interfaces.IpRestrictor;
import org.apache.ftpserver.interfaces.ServerFtpStatistics;
import org.apache.ftpserver.interfaces.Ssl;
import org.apache.ftpserver.util.IoUtils;


/**
 * This is a generic request handler. It delegates 
 * the request to appropriate method in subclass.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class RequestHandler implements Connection {
    
    private FtpServerContext serverContext;
    private Log log;
    
    private Socket controlSocket;
    private FtpSessionImpl session;
    private FtpWriter writer;
    private BufferedReader reader;
    private boolean isConnectionClosed;
    
    private DataType dataType    = DataType.ASCII;
    private Structure structure  = Structure.FILE;
    private Map attributes = new HashMap();
    
    
    /**
     * Constructor - set the control socket.
     */
    public RequestHandler(FtpServerContext serverContext, Socket controlSocket) throws IOException {
        this.serverContext = serverContext;
        this.controlSocket = controlSocket;
        log = this.serverContext.getLogFactory().getInstance(getClass());
        
        // data connection object
        FtpDataConnection dataCon = new FtpDataConnection();
        dataCon.setServerContext(this.serverContext);
        dataCon.setServerControlAddress(controlSocket.getLocalAddress());
        
        // reader object
        session = new FtpSessionImpl();
        session.setClientAddress(this.controlSocket.getInetAddress());
        session.setFtpDataConnection(dataCon);
        
        // writer object
        writer = new FtpWriter();
        writer.setControlSocket(this.controlSocket);
        writer.setServerContext(this.serverContext);
        writer.setFtpSession(session);
    }
    
    /**
     * Set observer.
     */
    public void setObserver(ConnectionObserver observer) {
        
        // set writer observer
        FtpWriter writer = this.writer;
        if(writer != null) {
            writer.setObserver(observer);
        }
        
        // set request observer
        FtpSessionImpl session = this.session;
        if(session != null) {
            session.setObserver(observer);
        }
    }   
    
    /**
     * Get the configuration object.
     */
    public FtpServerContext getServerContext() {
        return serverContext;
    }
                
    /**
     * Get the data type.
     */
    public DataType getDataType() {
        return dataType;
    }
    
    /**
     * Set the data type.
     */
    public void setDataType(DataType type) {
        dataType = type;
    }

    /**
     * Get structure.
     */
    public Structure getStructure() {
        return structure;
    }
    
    /**
     * Get a session attribute.
     * @param name The name of the attribute
     * 
     * @return The attribute value, or null if the attribute does not exist
     */
    public Object getAttribute(String name) {
        return attributes.get(name);
    }
    
    /**
     * Set a session attribute.
     * @param name The name of the attribute
     * @param value The value of the attribute
     */
    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }
    
    /**
     * Set structure
     */
    public void setStructure(Structure stru) {
        structure = stru;
    }
        
    /**
     * Get request.
     */
    public FtpSession getSession() {
        return session;
    }
    
    /**
     * Server one FTP client connection.
     */
    public void run() {
        if(session == null ) {
            return;
        }
        if(serverContext == null) {
        	return;
        }
        
        InetAddress clientAddr = session.getRemoteAddress();
        ConnectionManager conManager = serverContext.getConnectionManager();
        Ftplet ftpletContainer = serverContext.getFtpletContainer();
        
        if(conManager == null) {
        	return;
        }
        if(ftpletContainer == null) {
        	return;
        }
        try {
            
            // write log message
            String hostAddress = clientAddr.getHostAddress();
            log.info("Open connection - " + hostAddress);
            
            // notify ftp statistics
            ServerFtpStatistics ftpStat = (ServerFtpStatistics)serverContext.getFtpStatistics();
            ftpStat.setOpenConnection(this);
            
            // call Ftplet.onConnect() method
            boolean isSkipped = false;

            FtpletEnum ftpletRet = ftpletContainer.onConnect(session, writer);
            if(ftpletRet == FtpletEnum.RET_SKIP) {
                isSkipped = true;
            }
            else if(ftpletRet == FtpletEnum.RET_DISCONNECT) {
                conManager.closeConnection(this);
                return;
            }
            
            if(!isSkipped) {

                // IP permission check
                IpRestrictor ipRestrictor = serverContext.getIpRestrictor();
                if( !ipRestrictor.hasPermission(clientAddr) ) {
                    log.warn("No permission to access from " + hostAddress);
                    writer.send(530, "ip.restricted", null);
                    return;
                }
                
                // connection limit check
                int maxConnections = conManager.getMaxConnections();
                
                if(maxConnections != 0 && ftpStat.getCurrentConnectionNumber() > maxConnections) {
                    log.warn("Maximum connection limit reached.");
                    writer.send(530, "connection.limit", null);
                    return;
                }
                
                // everything is fine - go ahead 
                writer.send(220, null, null);
            }
            
            reader = new BufferedReader(new InputStreamReader(controlSocket.getInputStream(), "UTF-8"));
            do {
                notifyObserver();
                String commandLine = reader.readLine();
                
                // test command line
                if(commandLine == null) {
                    break;
                }
                commandLine = commandLine.trim();
                if(commandLine.equals("")) {
                    continue;
                }
                
                // parse and check permission
                FtpRequestImpl request = new FtpRequestImpl(commandLine);
                session.setCurrentRequest(request);
                
                if(!hasPermission(request)) {
                    writer.send(530, "permission", null);
                    continue;
                }

                // execute command
                service(request, session, writer);
                
                if(session != null) {
                    session.setCurrentRequest(null);
                }
            }
            while(!isConnectionClosed);
        } catch(SocketException ex) {
            // socket closed - no need to do anything
        } catch(SSLException ex) {
            log.warn("The client did not initiate the SSL connection correctly", ex);
        } catch(Exception ex) {
            log.warn("RequestHandler.run()", ex);
        }
        finally {
            // close all resources if not done already
            if(!isConnectionClosed) {
                 conManager.closeConnection(this);
            }
        }
    }
    
    /**
     * Notify connection manager observer.
     */
    protected void notifyObserver() {
        session.updateLastAccessTime();
        serverContext.getConnectionManager().updateConnection(this);
    }

    /**
     * Execute the ftp command.
     */
    public void service(FtpRequestImpl request, FtpSessionImpl session, FtpWriter out) throws IOException, FtpException {
        try {
            String commandName = request.getCommand();
            CommandFactory commandFactory = serverContext.getCommandFactory();
            Command command = commandFactory.getCommand(commandName);
            if(command != null) {
                command.execute(this, request, session, out);
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
                log.warn("RequestHandler.service()", ex);
            }
        }
    }
    
    /**
     * Close connection. This is called by the connection service.
     */
    public void close() {
        
        // check whether already closed or not
        synchronized(this) {
            if(isConnectionClosed) {
                return;
            }
            isConnectionClosed = true;
        }
        
        // call Ftplet.onDisconnect() method.
        try {
            Ftplet ftpletContainer = serverContext.getFtpletContainer();
            ftpletContainer.onDisconnect(session, writer);
        }
        catch(Exception ex) {
            log.warn("RequestHandler.close()", ex);
        }

        // notify statistics object and close request
        ServerFtpStatistics ftpStat = (ServerFtpStatistics)serverContext.getFtpStatistics();

        if(session != null) {
            
            // log message
            User user = session.getUser();
            String userName = user != null ? user.getName() : "<Not logged in>";
            InetAddress clientAddr = session.getRemoteAddress(); 
            log.info("Close connection : " + clientAddr.getHostAddress() + " - " + userName);
            
            // logout if necessary and notify statistics
            if(session.isLoggedIn()) {
                session.setLogout();
                ftpStat.setLogout(this);
            }
            ftpStat.setCloseConnection(this);
            
            // clear request
            session.clear();
            session.setObserver(null);
            session.getFtpDataConnection().dispose();
            FileSystemView fview = session.getFileSystemView();
            if(fview != null) {
                fview.dispose();
            }
            session = null;
        }
                
        // close ftp writer
        FtpWriter writer = this.writer;
        if(writer != null) {
            writer.setObserver(null);
            writer.close();
            writer = null;
        }
        
        // close buffered reader
        BufferedReader reader = this.reader;
        if(reader != null) {
            IoUtils.close(reader);
            reader = null;
        }
        
        // close control socket
        Socket controlSocket = this.controlSocket;
        if (controlSocket != null) {
            try {
                controlSocket.close();
            }
            catch(Exception ex) {
                log.warn("RequestHandler.close()", ex);
            }
            controlSocket = null;
        }
    }

    /**
     * Check user permission to execute ftp command. 
     */
    protected boolean hasPermission(FtpRequest request) {
        String cmd = request.getCommand();
        if(cmd == null) {
            return false;
        }
        return session.isLoggedIn() ||
               cmd.equals("USER")   || 
		       cmd.equals("PASS")   ||
		       cmd.equals("QUIT")   ||
		       cmd.equals("AUTH")   ||
		       cmd.equals("HELP")   ||
		       cmd.equals("SYST")   ||
		       cmd.equals("FEAT")   ||
		       cmd.equals("PBSZ")   ||
		       cmd.equals("PROT")   ||
	           cmd.equals("LANG")   ||
	           cmd.equals("ACCT");
    }    
    
    /**
     * Transfer data.
     */
    public final long transfer(BufferedInputStream in, 
                               BufferedOutputStream out,
                               int maxRate) throws IOException {
        
        boolean isAscii = dataType == DataType.ASCII;
        long startTime = System.currentTimeMillis();
        long transferredSize = 0L;
        byte[] buff = new byte[4096];
        
        while(true) {
            
            // if current rate exceeds the max rate, sleep for 50ms 
            // and again check the current transfer rate
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
                    continue;
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
    public void createSecureSocket(String protocol) throws Exception {

        // change socket to SSL socket
        Ssl ssl = serverContext.getSocketFactory().getSSL();
        if(ssl == null) {
            throw new FtpException("Socket factory SSL not configured");
        }
        Socket ssoc = ssl.createSocket(protocol, controlSocket, false);
        
        // change streams
        reader = new BufferedReader(new InputStreamReader(ssoc.getInputStream(), "UTF-8"));
        writer.setControlSocket(ssoc);
        
        // set control socket
        controlSocket = ssoc;
    }
    
    /**
     * Retrive the socket used for the control channel
     * @return The control socket
     */
    public Socket getControlSocket() {
        return controlSocket;
    }
}
