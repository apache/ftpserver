/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *  
 *    http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License. 
 *  
 */
package org.apache.ftpserver.listener;

import java.io.IOException;
import java.net.InetAddress;

import org.apache.commons.logging.Log;
import org.apache.ftpserver.FtpSessionImpl;
import org.apache.ftpserver.FtpWriter;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.ftplet.FtpResponse;
import org.apache.ftpserver.ftplet.FtpSession;
import org.apache.ftpserver.ftplet.Ftplet;
import org.apache.ftpserver.ftplet.FtpletEnum;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.interfaces.Command;
import org.apache.ftpserver.interfaces.CommandFactory;
import org.apache.ftpserver.interfaces.FtpServerContext;
import org.apache.ftpserver.interfaces.IpRestrictor;
import org.apache.ftpserver.interfaces.ServerFtpStatistics;

public class FtpProtocolHandler {
    
    protected Log log;
    protected FtpServerContext serverContext;
    
    public FtpProtocolHandler(FtpServerContext serverContext) throws IOException {
        this.serverContext = serverContext;
        
        log = serverContext.getLogFactory().getInstance(FtpProtocolHandler.class);
    }

    public void onConnectionOpened(Connection connection, FtpSessionImpl session, FtpWriter writer) throws Exception {
        InetAddress clientAddr = session.getClientAddress();
        ConnectionManager conManager = serverContext.getConnectionManager();
        Ftplet ftpletContainer = serverContext.getFtpletContainer();
        
        if(conManager == null) {
            return;
        }
        if(ftpletContainer == null) {
            return;
        }
        
        // write log message
        String hostAddress = clientAddr.getHostAddress();
        log.info("Open connection - " + hostAddress);
        
        // notify ftp statistics
        ServerFtpStatistics ftpStat = (ServerFtpStatistics)serverContext.getFtpStatistics();
        ftpStat.setOpenConnection(connection);
        
        // call Ftplet.onConnect() method
        boolean isSkipped = false;

        FtpletEnum ftpletRet = ftpletContainer.onConnect(session, writer);
        if(ftpletRet == FtpletEnum.RET_SKIP) {
            isSkipped = true;
        }
        else if(ftpletRet == FtpletEnum.RET_DISCONNECT) {
            conManager.closeConnection(connection);
            return;
        }
        
        if(!isSkipped) {

            // IP permission check
            IpRestrictor ipRestrictor = serverContext.getIpRestrictor();
            if( !ipRestrictor.hasPermission(clientAddr) ) {
                log.warn("No permission to access from " + hostAddress);
                writer.send(FtpResponse.REPLY_530_NOT_LOGGED_IN, "ip.restricted", null);
                return;
            }
            
            // connection limit check
            int maxConnections = conManager.getMaxConnections();
            
            if(maxConnections != 0 && ftpStat.getCurrentConnectionNumber() > maxConnections) {
                log.warn("Maximum connection limit reached.");
                writer.send(FtpResponse.REPLY_530_NOT_LOGGED_IN, "connection.limit", null);
                return;
            }
            
            // everything is fine - go ahead 
            writer.send(FtpResponse.REPLY_220_SERVICE_READY, null, null);
        }
    }
    
    public void onRequestReceived(Connection connection, FtpSessionImpl session, FtpWriter writer, FtpRequest request ) throws IOException, FtpException {
        session.setCurrentRequest(request);
        
        if(!hasPermission(session, request)) {
            writer.send(FtpResponse.REPLY_530_NOT_LOGGED_IN, "permission", null);
            return;
        }

        // execute command
        service(connection, request, session, writer);
        
        if(session != null) {
            session.setCurrentRequest(null);
        }

    }
    
    public void onConnectionClosed(Connection connection, FtpSessionImpl session, FtpWriter writer) {
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
            InetAddress clientAddr = session.getClientAddress(); 
            log.info("Close connection : " + clientAddr.getHostAddress() + " - " + userName);
            
            // logout if necessary and notify statistics
            if(session.isLoggedIn()) {
                session.setLogout();
                ftpStat.setLogout(connection);
            }
            ftpStat.setCloseConnection(connection);
            
            // clear request
            session.clear();
            session.getFtpDataConnection().dispose();
            FileSystemView fview = session.getFileSystemView();
            if(fview != null) {
                fview.dispose();
            }
            session = null;
        }
                
        // close ftp writer
        if(writer != null) {
            writer.setObserver(null);
            writer.close();
            writer = null;
        }

    }
    
    /**
     * Execute the ftp command.
     */
    private void service(Connection connection, FtpRequest request, FtpSessionImpl session, FtpWriter out) throws IOException, FtpException {
        try {
            String commandName = request.getCommand();
            CommandFactory commandFactory = serverContext.getCommandFactory();
            Command command = commandFactory.getCommand(commandName);
            if(command != null) {
                command.execute(connection, request, session, out);
            }
            else {
                out.send(FtpResponse.REPLY_502_COMMAND_NOT_IMPLEMENTED, "not.implemented", null);
            }
        }
        catch(Exception ex) {
            
            // send error reply
            try { 
                out.send(FtpResponse.REPLY_550_REQUESTED_ACTION_NOT_TAKEN, null, null);
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
     * Check user permission to execute ftp command. 
     */
    private boolean hasPermission(FtpSession session, FtpRequest request) {
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
}
