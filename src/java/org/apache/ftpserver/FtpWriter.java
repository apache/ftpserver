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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.net.InetAddress;
import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.ftplet.FtpResponse;
import org.apache.ftpserver.ftplet.FtpStatistics;
import org.apache.ftpserver.interfaces.ConnectionObserver;
import org.apache.ftpserver.interfaces.IFtpConfig;
import org.apache.ftpserver.interfaces.IMessageResource;
import org.apache.ftpserver.util.DateUtils;
import org.apache.ftpserver.util.IoUtils;

/**
 * FTP response object. The server uses this to send server messages
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class FtpWriter implements FtpResponse {

    private final static String CRLF     = "\r\n";
    
    ///////////////////////// All Server Vatiables /////////////////////////
    public static final String SERVER_IP   = "server.ip";
    public static final String SERVER_PORT = "server.port";
    
    public static final String REQUEST_LINE = "request.line";
    public static final String REQUEST_CMD  = "request.cmd";
    public static final String REQUEST_ARG  = "request.arg";
    
    public static final String STAT_START_TIME = "stat.start.time";
    
    public static final String STAT_CON_TOTAL = "stat.con.total";
    public static final String STAT_CON_CURR  = "stat.con.curr";
    
    public static final String STAT_LOGIN_TOTAL = "stat.login.total";
    public static final String STAT_LOGIN_CURR  = "stat.login.curr";
    
    public static final String STAT_LOGIN_ANON_TOTAL = "stat.login.anon.total";
    public static final String STAT_LOGIN_ANON_CURR  = "stat.login.anon.curr";
    
    public static final String STAT_FILE_UPLOAD_COUNT = "stat.file.upload.count";
    public static final String STAT_FILE_UPLOAD_BYTES = "stat.file.upload.bytes";
    
    public static final String STAT_FILE_DOWNLOAD_COUNT = "stat.file.download.count";
    public static final String STAT_FILE_DOWNLOAD_BYTES = "stat.file.download.bytes";
    
    public static final String STAT_FILE_DELETE_COUNT = "stat.file.delete.count";
    
    public static final String STAT_DIR_CREATE_COUNT  = "stat.dir.create.count";
    public static final String STAT_DIR_DELETE_COUNT  = "stat.dir.delete.count";
    
    public static final String OUTPUT_CODE = "output.code";
    public static final String OUTPUT_MSG  = "output.msg";
    
    public static final String CLIENT_IP          = "client.ip";
    public static final String CLIENT_CON_TIME    = "client.con.time";
    public static final String CLIENT_LOGIN_NAME  = "client.login.name";
    public static final String CLIENT_LOGIN_TIME  = "client.login.time";
    public static final String CLIENT_ACCESS_TIME = "client.access.time"; 
    public static final String CLIENT_HOME        = "client.home";
    public static final String CLIENT_DIR         = "client.dir";
    
    /////////////////////////////////////////////////////////////////////////////
    
    private Log log;
    private Writer writer;
    private ConnectionObserver observer;
    private IFtpConfig fconfig;
    private FtpRequest request;

        
    /**
     * Set the control socket.
     */
    public void setControlSocket(Socket soc) throws IOException {
        writer = new OutputStreamWriter(soc.getOutputStream(), "UTF-8");
    }
    
    /**
     * Set ftp config.
     */
    public void setFtpConfig(IFtpConfig fconfig) {
        this.fconfig = fconfig;
        log = this.fconfig.getLogFactory().getInstance(getClass());
    }

    /**
     * Set ftp request.
     */
    public void setFtpRequest(FtpRequest request) {
        this.request = request;
    }
    
    /**
     * Get the observer object to get what the server response.
     */
    public void setObserver(ConnectionObserver observer) {
        this.observer = observer;
    }        
    
    /**
     * Spy print. Monitor server response.
     */
    private void spyResponse(String str) {
        ConnectionObserver observer = this.observer;
        if(observer != null) {
            observer.response(str);
        }
    }
    
    /**
     * Generate and send ftp server response.
     */
    public void send(int code, String subId, String basicMsg) throws IOException {
        IMessageResource resource = fconfig.getMessageResource();
        String lang = request.getLanguage();
        
        String msg = null;
        if(resource != null ) {
            msg = resource.getMessage(code, subId, lang);
        }
        if(msg == null) {
            log.error("Message not found : " + code + ',' + subId + ',' + lang);
            msg = "";
        }
        msg = replaceVariables(code, basicMsg, msg);
        write(code, msg);
    }

    /**
     * Send the ftp server reply code to client.
     */
    public void write(int code) throws IOException {
        write(code, null);
    }
    
    /**
     * Send the ftp code and message to client
     */
    public void write(int code, String msg) throws IOException {
        if(msg == null) {
            msg = "";
        }
        
        msg = processNewLine(code, msg);
        spyResponse(msg);
        writer.write(msg);
        writer.flush();
    }
    
    /**
     * Close writer.
     */
    public void close() {
        IoUtils.close(writer);
    }
    
    /**
     * Process ftp response new line character.
     */
    private String processNewLine(int code, String msg) {
        
        // no newline
        if(msg.indexOf('\n') == -1) {
            return code + " " + msg + CRLF;
        }
        
        StringBuffer buff = new StringBuffer(256);
        try {
            BufferedReader sr = new BufferedReader(new StringReader(msg));
        
            buff.append(code);
            buff.append('-');
        
            String line = sr.readLine();
            for(;;) {
                String nextLine = sr.readLine();    
            
                if(nextLine != null) {
                    buff.append(line);
                    buff.append(CRLF);
                }
                else {
                    buff.append(code);
                    buff.append(' ');
                    buff.append(line);
                    buff.append(CRLF);
                    break;
                }
                line = nextLine;
            }
            sr.close();
        }
        catch(IOException ex) {
        }
        return buff.toString();
    }
    
    /**
     * Replace server variables.
     */
    private String replaceVariables(int code, String basicMsg, String str) {
        
        int startIndex = 0;
        int openIndex = str.indexOf('{', startIndex);
        if (openIndex == -1) {
            return str;
        }
        
        int closeIndex = str.indexOf('}', startIndex);
        if( (closeIndex == -1) || (openIndex > closeIndex) ) {
            return str;
        }
        
        StringBuffer sb = new StringBuffer(128);
        sb.append(str.substring(startIndex, openIndex));
        while(true) {
            String varName = str.substring(openIndex+1, closeIndex);
            sb.append( getVariableValue(code, basicMsg, varName) );
            
            startIndex = closeIndex + 1;
            openIndex = str.indexOf('{', startIndex);
            if (openIndex == -1) {
                sb.append(str.substring(startIndex));
                break;
            }
            
            closeIndex = str.indexOf('}', startIndex);
            if( (closeIndex == -1) || (openIndex > closeIndex) ) {
               sb.append(str.substring(startIndex));
               break;
            }
            sb.append(str.substring(startIndex, openIndex));
        }
        return sb.toString();
    } 
    
    /**
     * Get the variable value.
     */
    public String getVariableValue(int code, String basicMsg, String varName) {
        
        String varVal = null;
        
        // all output variables
        if(varName.startsWith("output.")) {
            varVal = getOutputVariableValue(code, basicMsg, varName);
        }

        // all server variables
        else if(varName.startsWith("server.")) {
            varVal = getServerVariableValue(varName);
        }
        
        // all request variables
        else if(varName.startsWith("request.")) {
            varVal = getRequestVariableValue(varName);
        }
        
        // all statistical variables
        else if(varName.startsWith("stat.")) {
            varVal = getStatisticalVariableValue(varName);
        }
                
        // all client variables
        else if(varName.startsWith("client.")) {
            varVal = getClientVariableValue(varName);
        }
        
        if(varVal == null) {
            varVal = "";
        }
        return varVal;
    } 
    
    /**
     * Get server variable value.
     */
    private String getServerVariableValue(String varName) {
        
        String varVal = null;
        
        // server address
        if(varName.equals(SERVER_IP)) {
            InetAddress addr = fconfig.getDataConnectionConfig().getPassiveAddress();
            varVal = addr.getHostAddress();
        }
        
        // server port
        else if(varName.equals(SERVER_PORT)) {
            varVal = String.valueOf(fconfig.getServerPort());
        }
        
        return varVal;
    }

    /**
     * Get request variable value.
     */
    private String getRequestVariableValue(String varName) {
        
        String varVal = null;
        
        // request line
        if(varName.equals(REQUEST_LINE)) {
            varVal = request.getRequestLine();
        }
        
        // request command
        else if(varName.equals(REQUEST_CMD)) {
            varVal = request.getCommand();
        }
        
        // request argument
        else if(varName.equals(REQUEST_ARG)) {
            varVal = request.getArgument();
        }
        
        return varVal;
    }
    
    /**
     * Get statistical variable value. 
     */
    private String getStatisticalVariableValue(String varName) {
    
        String varVal = null;
        FtpStatistics stat = fconfig.getFtpStatistics();
        
        // server start time
        if(varName.equals(STAT_START_TIME)) {
            varVal = DateUtils.getISO8601Date(stat.getStartTime().getTime());
        }
        
        // connection statistical variables
        else if(varName.startsWith("stat.con")) {
            varVal = getStatisticalConnectionVariableValue(varName);
        }
        
        // login statistical variables
        else if(varName.startsWith("stat.login.")) {
            varVal = getStatisticalLoginVariableValue(varName);
        }
        
        // file statistical variable
        else if(varName.startsWith("stat.file")) {
            varVal = getStatisticalFileVariableValue(varName);
        }
        
        // directory statistical variable
        else if(varName.startsWith("stat.dir.")) {
            varVal = getStatisticalDirectoryVariableValue(varName);
        }
        
        return varVal;
    }
    
    /**
     * Get statistical connection variable value.
     */
    private String getStatisticalConnectionVariableValue(String varName) {
        String varVal = null;
        FtpStatistics stat = fconfig.getFtpStatistics();
        
        // total connection number
        if(varName.equals(STAT_CON_TOTAL)) {
            varVal = String.valueOf(stat.getTotalConnectionNumber());
        }
        
        // current connection number
        else if(varName.equals(STAT_CON_CURR)) {
            varVal = String.valueOf(stat.getCurrentConnectionNumber());
        }
        
        return varVal;
    }
    
    /**
     * Get statistical login variable value.
     */
    private String getStatisticalLoginVariableValue(String varName) {
        String varVal = null;
        FtpStatistics stat = fconfig.getFtpStatistics();
        
        // total login number
        if(varName.equals(STAT_LOGIN_TOTAL)) {
            varVal = String.valueOf(stat.getTotalLoginNumber());
        }
        
        // current login number
        else if(varName.equals(STAT_LOGIN_CURR)) {
            varVal = String.valueOf(stat.getCurrentLoginNumber());
        }
        
        // total anonymous login number
        else if(varName.equals(STAT_LOGIN_ANON_TOTAL)) {
            varVal = String.valueOf(stat.getTotalAnonymousLoginNumber());
        }
        
        // current anonymous login number
        else if(varName.equals(STAT_LOGIN_ANON_CURR)) {
            varVal = String.valueOf(stat.getCurrentAnonymousLoginNumber());
        }
        
        return varVal; 
    }
    
    /**
     * Get statistical file variable value.
     */
    private String getStatisticalFileVariableValue(String varName) {
        String varVal = null;
        FtpStatistics stat = fconfig.getFtpStatistics();
        
        // total number of file upload
        if(varName.equals(STAT_FILE_UPLOAD_COUNT)) {
            varVal = String.valueOf(stat.getTotalUploadNumber());
        }
        
        // total bytes uploaded
        else if(varName.equals(STAT_FILE_UPLOAD_BYTES)) {
            varVal = String.valueOf(stat.getTotalUploadSize());
        }
        
        // total number of file download
        else if(varName.equals(STAT_FILE_DOWNLOAD_COUNT)) {
            varVal = String.valueOf(stat.getTotalDownloadNumber());
        }
        
        // total bytes downloaded
        else if(varName.equals(STAT_FILE_DOWNLOAD_BYTES)) {
            varVal = String.valueOf(stat.getTotalDownloadSize());
        }
        
        // total number of files deleted
        else if(varName.equals(STAT_FILE_DELETE_COUNT)) {
            varVal = String.valueOf(stat.getTotalDeleteNumber());
        }
        
        return varVal;
    }
    
    /**
     * Get statistical directory variable value.
     */
    private String getStatisticalDirectoryVariableValue(String varName) {
        String varVal = null;
        FtpStatistics stat = fconfig.getFtpStatistics();
        
        // total directory created
        if(varName.equals(STAT_DIR_CREATE_COUNT)) {
            varVal = String.valueOf(stat.getTotalDirectoryCreated());
        }
        
        // total directory removed
        else if(varName.equals(STAT_DIR_DELETE_COUNT)) {
            varVal = String.valueOf(stat.getTotalDirectoryRemoved());
        }
        
        return varVal;
    }
    
    /**
     * Get output variable value.
     */
    private String getOutputVariableValue(int code, String basicMsg, String varName) {
        String varVal = null;
        
        // output code
        if(varName.equals(OUTPUT_CODE)) {
            varVal = String.valueOf(code);
        }
        
        // output message
        else if(varName.equals(OUTPUT_MSG)) {
            varVal = basicMsg;
        }
        
        return varVal;
    }

    /**
     * Get client variable value.
     */
    private String getClientVariableValue(String varName) {
        
        String varVal = null;
        
        // client ip
        if(varName.equals(CLIENT_IP)) {
            varVal = request.getRemoteAddress().getHostAddress();
        }
        
        // client connection time
        else if(varName.equals(CLIENT_CON_TIME)) {
            varVal = DateUtils.getISO8601Date(request.getConnectionTime().getTime());
        }
        
        // client login name
        else if(varName.equals(CLIENT_LOGIN_NAME)) {
            varVal = request.getUserArgument();
        }
        
        // client login time
        else if(varName.equals(CLIENT_LOGIN_TIME)) {
            varVal = DateUtils.getISO8601Date(request.getLoginTime().getTime());
        }
        
        // client last access time
        else if(varName.equals(CLIENT_ACCESS_TIME)) {
            varVal = DateUtils.getISO8601Date(request.getLastAccessTime().getTime());
        }
        
        // client home
        else if(varName.equals(CLIENT_HOME)) {
            varVal = request.getUser().getHomeDirectory();
        }
        
        // client directory
        else if(varName.equals(CLIENT_DIR)) {
            FileSystemView fsView = request.getFileSystemView();
            if(fsView != null) {
                try {
                    varVal = fsView.getCurrentDirectory().getFullName();
                }
                catch(Exception ex) {
                }
            }
        }
        return varVal; 
    }
}
