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

import org.apache.ftpserver.ftplet.FtpReply;

/**
 * FTP reply object.
 */
public class DefaultFtpReply implements FtpReply {
    
    private int code;
    private String message;
    private final static String CRLF     = "\r\n";
    /**
     * @param code
     * @param message
     */
    public DefaultFtpReply(int code, String message) {
        this.code = code;
        this.message = message;
    }
    /**
     * @return the code
     */
    public int getCode() {
        return code;
    }
    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        int code = getCode();
        String notNullMessage = getMessage();
        if(notNullMessage == null) {
            notNullMessage = "";
        }
        
        StringBuffer sb = new StringBuffer();

        // no newline
        if (notNullMessage.indexOf('\n') == -1) {
            sb.append(code);
            sb.append(" ");
            sb.append(notNullMessage);
            sb.append(CRLF);
        } else {
            String[] lines = notNullMessage.split("\n");
            
            sb.append(code);
            sb.append("-");

            for (int i = 0; i < lines.length; i++) {
                String line = lines[i].trim();
                
                if(i + 1 == lines.length) {
                    sb.append(code);
                    sb.append(" ");
                }
                
                sb.append(line);
                sb.append(CRLF);
            }
            
        }
        
        return sb.toString();
    }
    
}
