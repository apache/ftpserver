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

import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.interfaces.ConnectionObserver;

/**
 * FTP request object.
 */
public class FtpRequestImpl implements FtpRequest {
    
    private String line;
    private String command;
    private String argument;
    
    private ConnectionObserver observer;

    
    /**
     * Default constructor.
     */
    public FtpRequestImpl(String requestLine) {
        parse(requestLine);
    } 
    
    /**
     * Parse the ftp command line.
     */
    private void parse(String lineToParse) {
        
        // notify connection observer
        spyRequest(lineToParse);
        
        // parse request
        line = lineToParse;
        command = null;
        argument = null;
        int spInd = line.indexOf(' ');
        if(spInd != -1) {
            argument = line.substring(spInd + 1);
            if(argument.equals("")) {
                argument = null;
            }
            command = line.substring(0, spInd).toUpperCase();
        }
        else {
            command = line.toUpperCase();
        }
        
        if( (command.length() > 0) && (command.charAt(0) == 'X') ) {
            command = command.substring(1);
        }
    }
    
    /**
     * Spy print. Monitor user request.
     */
    private void spyRequest(String str) {
        ConnectionObserver observer = this.observer;
        if(observer != null) {
            observer.request(str + "\r\n");
        }
    }
    
    /**
     * Get the ftp command.
     */
    public String getCommand() {
        return command;
    }
    
    /**
     * Get ftp input argument.  
     */ 
    public String getArgument() {
        return argument;
    }
    
    /**
     * Get the ftp request line.
     */
    public String getRequestLine() {
        return line;
    }
    

    /**
     * Has argument.
     * TODO: should be in interface?
     */
    public boolean hasArgument() {
        return getArgument() != null;
    }
}
