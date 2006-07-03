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

import org.apache.commons.logging.Log;
import org.apache.ftpserver.ftplet.*;
import org.apache.ftpserver.interfaces.ConnectionObserver;
import org.apache.ftpserver.interfaces.ICommand;
import org.apache.ftpserver.interfaces.ICommandFactory;
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
    
    private IFtpConfig fconfig;
    private Log log;
    
    private Socket controlSocket;
    private FtpRequestImpl request;
    private FtpWriter writer;
    private BufferedReader reader;
    private boolean isConnectionClosed;
    
    private DirectoryLister directoryLister;
    private DataType dataType    = DataType.ASCII;
    private Structure structure  = Structure.FILE;
    
    
    /**
     * Constructor - set the control socket.
     */
    public RequestHandler(IFtpConfig fconfig, Socket controlSocket) throws IOException {
        this.fconfig = fconfig;
        this.controlSocket = controlSocket;
        log = this.fconfig.getLogFactory().getInstance(getClass());
        
        // data connection object
        FtpDataConnection dataCon = new FtpDataConnection();
        dataCon.setFtpConfig(this.fconfig);
        
        // reader object
        request = new FtpRequestImpl();
        request.setClientAddress(this.controlSocket.getInetAddress());
        request.setFtpDataConnection(dataCon);
        
        // writer object
        writer = new FtpWriter();
        writer.setControlSocket(this.controlSocket);
        writer.setFtpConfig(this.fconfig);
        writer.setFtpRequest(request);
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
        FtpRequestImpl request = this.request;
        if(request != null) {
            request.setObserver(observer);
        }
    }   
    
    /**
     * Get the configuration object.
     */
    public IFtpConfig getConfig() {
        return fconfig;
    }
    
    /**
     * Get directory lister.
     */
    public DirectoryLister getDirectoryLister() {
        return directoryLister;
    }
    
    /**
     * Set directory lister.
     */
    public void setDirectoryLister(DirectoryLister lister) {
        directoryLister = lister;
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
     * Set structure
     */
    public void setStructure(Structure stru) {
        structure = stru;
    }
        
    /**
     * Get request.
     */
    public FtpRequest getRequest() {
        return request;
    }
    
    /**
     * Server one FTP client connection.
     */
    public void run() {
        
        InetAddress clientAddr = request.getRemoteAddress();
        IConnectionManager conManager = fconfig.getConnectionManager();
        try {
            
            // write log message
            String hostAddress = clientAddr.getHostAddress();
            log.info("Open connection - " + hostAddress);
            
            // notify ftp statistics
            IFtpStatistics ftpStat = (IFtpStatistics)fconfig.getFtpStatistics();
            ftpStat.setOpenConnection(this);
            
            // call Ftplet.onConnect() method
            boolean isSkipped = false;
            Ftplet ftpletContainer = fconfig.getFtpletContainer();
            FtpletEnum ftpletRet = ftpletContainer.onConnect(request, writer);
            if(ftpletRet == FtpletEnum.RET_SKIP) {
                isSkipped = true;
            }
            else if(ftpletRet == FtpletEnum.RET_DISCONNECT) {
                conManager.closeConnection(this);
                return;
            }
            
            if(!isSkipped) {

                // IP permission check
                IIpRestrictor ipRestrictor = fconfig.getIpRestrictor();
                if( !ipRestrictor.hasPermission(clientAddr) ) {
                    log.warn("No permission to access from " + hostAddress);
                    writer.send(530, "ip.restricted", null);
                    return;
                }
                
                // connection limit check
                int maxConnections = conManager.getMaxConnections();
                if(ftpStat.getCurrentConnectionNumber() > maxConnections) {
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
                request.parse(commandLine);
                if(!hasPermission()) {
                    writer.send(530, "permission", null);
                    continue;
                }

                // execute command
                service(request, writer);
            }
            while(!isConnectionClosed);
        }
        catch(SocketException ex) {
            // socket closed - no need to do anything
        }
        catch(Exception ex) {
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
        request.updateLastAccessTime();
        fconfig.getConnectionManager().updateConnection(this);
    }

    /**
     * Execute the ftp command.
     */
    public void service(FtpRequestImpl request, FtpWriter out) throws IOException, FtpException {
        try {
            String commandName = request.getCommand();
            ICommandFactory commandFactory = fconfig.getCommandFactory();
            ICommand command = commandFactory.getCommand(commandName);
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
            Ftplet ftpletContainer = fconfig.getFtpletContainer();
            ftpletContainer.onDisconnect(request, writer);
        }
        catch(Exception ex) {
            log.warn("RequestHandler.close()", ex);
        }

        // notify statistics object and close request
        IFtpStatistics ftpStat = (IFtpStatistics)fconfig.getFtpStatistics();

        if(request != null) {
            
            // log message
            User user = request.getUser();
            String userName = user != null ? user.getName() : "<Not loginned>";
            InetAddress clientAddr = request.getRemoteAddress(); 
            log.info("Close connection : " + clientAddr.getHostAddress() + " - " + userName);
            
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
            FileSystemView fview = request.getFileSystemView();
            if(fview != null) {
                fview.dispose();
            }
            request = null;
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
    protected boolean hasPermission() {
        String cmd = request.getCommand();
        if(cmd == null) {
            return false;
        }
        return request.isLoggedIn() ||
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
        ISsl ssl = fconfig.getSocketFactory().getSSL();
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
}
