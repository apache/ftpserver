/* ====================================================================
 * Copyright 2002 - 2004
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
 *
 *
 * $Id$
 */
package org.apache.ftpserver;

/**
 * Ftp command request class. We can access command, line and argument using
 * <code>{CMD}, {ARG}</code> within ftp status file. This represents
 * single Ftp request.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public
class FtpRequest {

    private String mstLine     = null;
    private String mstCommand  = null;
    private String mstArgument = null;


    /**
     * Constructor.
     *
     * @param commandLine ftp input command line.
     */
    public FtpRequest(String commandLine) {
        mstLine = commandLine.trim();
        parse();
    }

    /**
     * Parse the ftp command line.
     */
    private void parse() {
       int spInd = mstLine.indexOf(' ');

       if(spInd != -1) {
           mstArgument = mstLine.substring(spInd + 1);
           mstCommand = mstLine.substring(0, spInd).toUpperCase();
       }
       else {
           mstCommand = mstLine.toUpperCase();
       }

       if( (mstCommand.length()>0) && (mstCommand.charAt(0)=='X') ) {
           mstCommand = mstCommand.substring(1);
       }
    }


    /**
     * Get the ftp command.
     */
    public String getCommand() {
        return mstCommand;
    }

    /**
     * Get ftp input argument.
     */
    public String getArgument() {
        return mstArgument;
    }

    /**
     * Get the ftp request line.
     */
    public String getCommandLine() {
        return mstLine;
    }

    /**
     * Has argument.
     */
    public boolean hasArgument() {
        return getArgument() != null;
    }

}
