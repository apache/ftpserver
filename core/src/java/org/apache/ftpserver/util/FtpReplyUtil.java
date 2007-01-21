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

package org.apache.ftpserver.util;

import java.net.InetAddress;

import org.apache.ftpserver.DefaultFtpReply;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.ftplet.FtpReply;
import org.apache.ftpserver.ftplet.FtpStatistics;
import org.apache.ftpserver.interfaces.FtpServerSession;
import org.apache.ftpserver.interfaces.MessageResource;

/**
 * FTP reply object.
 */
public class FtpReplyUtil  {
    
    public static final String CLIENT_ACCESS_TIME = "client.access.time";
    public static final String CLIENT_CON_TIME    = "client.con.time";
    public static final String CLIENT_DIR         = "client.dir";
    public static final String CLIENT_HOME        = "client.home";
    public static final String CLIENT_IP          = "client.ip";
    public static final String CLIENT_LOGIN_NAME  = "client.login.name";
    public static final String CLIENT_LOGIN_TIME  = "client.login.time";
    public static final String OUTPUT_CODE = "output.code";
    public static final String OUTPUT_MSG  = "output.msg";
    public static final String REQUEST_ARG  = "request.arg";
    public static final String REQUEST_CMD  = "request.cmd";
    public static final String REQUEST_LINE = "request.line";
    ///////////////////////// All Server Vatiables /////////////////////////
    public static final String SERVER_IP   = "server.ip";
    public static final String SERVER_PORT = "server.port";
    public static final String STAT_CON_CURR  = "stat.con.curr";
    public static final String STAT_CON_TOTAL = "stat.con.total";
    public static final String STAT_DIR_CREATE_COUNT  = "stat.dir.create.count";
    public static final String STAT_DIR_DELETE_COUNT  = "stat.dir.delete.count";
    public static final String STAT_FILE_DELETE_COUNT = "stat.file.delete.count";
    public static final String STAT_FILE_DOWNLOAD_BYTES = "stat.file.download.bytes";
    public static final String STAT_FILE_DOWNLOAD_COUNT = "stat.file.download.count";
    public static final String STAT_FILE_UPLOAD_BYTES = "stat.file.upload.bytes";
    public static final String STAT_FILE_UPLOAD_COUNT = "stat.file.upload.count";
    public static final String STAT_LOGIN_ANON_CURR  = "stat.login.anon.curr";
    public static final String STAT_LOGIN_ANON_TOTAL = "stat.login.anon.total";
    public static final String STAT_LOGIN_CURR  = "stat.login.curr";
    public static final String STAT_LOGIN_TOTAL = "stat.login.total";
    public static final String STAT_START_TIME = "stat.start.time";
    
    public static FtpReply translate(FtpServerSession session, int code, String subId, String basicMsg) {
        String msg = translateMessage(session, code, subId, basicMsg);
        
        return new DefaultFtpReply(code, msg);
    }
    
    private static String translateMessage(FtpServerSession session, int code, String subId, String basicMsg) {
        MessageResource resource = session.getServerContext().getMessageResource();
        String lang = session.getLanguage();
        
        String msg = null;
        if(resource != null ) {
            msg = resource.getMessage(code, subId, lang);
        }
        if(msg == null) {
            msg = "";
        }
        msg = replaceVariables(session, code, basicMsg, msg);
        
        return msg;
    }
    
    /**
     * Replace server variables.
     */
    private static String replaceVariables(FtpServerSession session, int code, String basicMsg, String str) {
        
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
            sb.append( getVariableValue(session, code, basicMsg, varName) );
            
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
    private static String getVariableValue(FtpServerSession session, int code, String basicMsg, String varName) {
        
        String varVal = null;
        
        // all output variables
        if(varName.startsWith("output.")) {
            varVal = getOutputVariableValue(session, code, basicMsg, varName);
        }
    
        // all server variables
        else if(varName.startsWith("server.")) {
            varVal = getServerVariableValue(session, varName);
        }
        
        // all request variables
        else if(varName.startsWith("request.")) {
            varVal = getRequestVariableValue(session, varName);
        }
        
        // all statistical variables
        else if(varName.startsWith("stat.")) {
            varVal = getStatisticalVariableValue(session, varName);
        }
                
        // all client variables
        else if(varName.startsWith("client.")) {
            varVal = getClientVariableValue(session, varName);
        }
        
        if(varVal == null) {
            varVal = "";
        }
        return varVal;
    }

    /**
     * Get client variable value.
     */
    private static String getClientVariableValue(FtpServerSession session, String varName) {
        
        String varVal = null;
        
        // client ip
        if(varName.equals(CLIENT_IP)) {
            varVal = session.getClientAddress().getHostAddress();
        }
        
        // client connection time
        else if(varName.equals(CLIENT_CON_TIME)) {
            varVal = DateUtils.getISO8601Date(session.getConnectionTime().getTime());
        }
        
        // client login name
        else if(varName.equals(CLIENT_LOGIN_NAME)) {
            varVal = session.getUserArgument();
        }
        
        // client login time
        else if(varName.equals(CLIENT_LOGIN_TIME)) {
            varVal = DateUtils.getISO8601Date(session.getLoginTime().getTime());
        }
        
        // client last access time
        else if(varName.equals(CLIENT_ACCESS_TIME)) {
            varVal = DateUtils.getISO8601Date(session.getLastAccessTime().getTime());
        }
        
        // client home
        else if(varName.equals(CLIENT_HOME)) {
            varVal = session.getUser().getHomeDirectory();
        }
        
        // client directory
        else if(varName.equals(CLIENT_DIR)) {
            FileSystemView fsView = session.getFileSystemView();
            if(fsView != null) {
                try {
                    varVal = fsView.getCurrentDirectory().getFullName();
                }
                catch(Exception ex) {
                    varVal = "";
                }
            }
        }
        return varVal; 
    }

    /**
     * Get output variable value.
     */
    private static String getOutputVariableValue(FtpServerSession session, int code, String basicMsg, String varName) {
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
     * Get request variable value.
     */
    private static String getRequestVariableValue(FtpServerSession session, String varName) {
        
        String varVal = null;
        
        FtpRequest request = session.getCurrentRequest();
        
        if(request == null) {
            return "";
        }
        
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
     * Get server variable value.
     */
    private static String getServerVariableValue(FtpServerSession session, String varName) {
        
        String varVal = null;
        
        // server address
        if(varName.equals(SERVER_IP)) {
            InetAddress addr = session.getServerAddress();;

            if(addr != null) {
                varVal = addr.getHostAddress();
            }
        }
        
        // server port
        else if(varName.equals(SERVER_PORT)) {
            varVal = String.valueOf(session.getServerContext().getServerPort());
        }
        
        return varVal;
    }

    /**
     * Get statistical connection variable value.
     */
    private static String getStatisticalConnectionVariableValue(FtpServerSession session, String varName) {
        String varVal = null;
        FtpStatistics stat = session.getServerContext().getFtpStatistics();
        
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
     * Get statistical directory variable value.
     */
    private static String getStatisticalDirectoryVariableValue(FtpServerSession session, String varName) {
        String varVal = null;
        FtpStatistics stat = session.getServerContext().getFtpStatistics();
        
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
     * Get statistical file variable value.
     */
    private static String getStatisticalFileVariableValue(FtpServerSession session, String varName) {
        String varVal = null;
        FtpStatistics stat = session.getServerContext().getFtpStatistics();
        
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
     * Get statistical login variable value.
     */
    private static String getStatisticalLoginVariableValue(FtpServerSession session, String varName) {
        String varVal = null;
        FtpStatistics stat = session.getServerContext().getFtpStatistics();
        
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
     * Get statistical variable value. 
     */
    private static String getStatisticalVariableValue(FtpServerSession session, String varName) {
    
        String varVal = null;
        FtpStatistics stat = session.getServerContext().getFtpStatistics();
        
        // server start time
        if(varName.equals(STAT_START_TIME)) {
            varVal = DateUtils.getISO8601Date(stat.getStartTime().getTime());
        }
        
        // connection statistical variables
        else if(varName.startsWith("stat.con")) {
            varVal = getStatisticalConnectionVariableValue(session, varName);
        }
        
        // login statistical variables
        else if(varName.startsWith("stat.login.")) {
            varVal = getStatisticalLoginVariableValue(session, varName);
        }
        
        // file statistical variable
        else if(varName.startsWith("stat.file")) {
            varVal = getStatisticalFileVariableValue(session, varName);
        }
        
        // directory statistical variable
        else if(varName.startsWith("stat.dir.")) {
            varVal = getStatisticalDirectoryVariableValue(session, varName);
        }
        
        return varVal;
    }
    
}
