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

package org.apache.ftpserver.impl;

import org.apache.ftpserver.ftplet.FtpRequest;

/**
 * <strong>Internal class, do not use directly.</strong>
 * 
 * FTP request object.
 *
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 */
public class DefaultFtpRequest implements FtpRequest {

    private String line;

    private String command;

    private String argument;

    /**
     * Default constructor.
     */
    public DefaultFtpRequest(final String requestLine) {
        parse(requestLine);
    }

    /**
     * Parse the ftp command line.
     */
    private void parse(final String lineToParse) {

        // parse request
        line = lineToParse.trim();
        command = null;
        argument = null;
        int spInd = line.indexOf(' ');
        if (spInd != -1) {
            argument = line.substring(spInd + 1);
            if (argument.equals("")) {
                argument = null;
            }
            command = line.substring(0, spInd).toUpperCase();
        } else {
            command = line.toUpperCase();
        }

        if ((command.length() > 0) && (command.charAt(0) == 'X')) {
            command = command.substring(1);
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
     */
    public boolean hasArgument() {
        return getArgument() != null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return getRequestLine();
    }
}
