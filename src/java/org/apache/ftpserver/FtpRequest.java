/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1997-2003 The Apache Software Foundation. All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *    "This product includes software developed by the
 *    Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software
 *    itself, if and wherever such third-party acknowledgments
 *    normally appear.
 *
 * 4. The names "Incubator", "FtpServer", and "Apache Software Foundation"
 *    must not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation. For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
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
