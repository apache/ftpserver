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

import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Properties;

/**
 * Ftp status line parser class. This class loads <code>FtpStatus.properties</code>
 * file from the classpath. It generates the descriptive ftp status for
 * astatus code. The actual response depends on the status code, the ftp
 * command and the passed argument list.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public
class FtpStatus extends Properties {

    private static final String RESOURCE = "org/apache/ftpserver/FtpStatus.properties";
    private static final String PREFIX   = "FtpServer.status.";
    private static final String EMPTY    = "";
    private static final String CRLF     = "\r\n";

    private static final String CMD      = "CMD";
    private static final String ARG      = "ARG";

    /**
     * Load status propeties file from the classpath.
     */
    public FtpStatus() throws IOException {
        InputStream pis = getClass().getClassLoader().getResourceAsStream(RESOURCE);
        load(pis);
        pis.close();
    }


    /**
     * Process ftp response new line character.
     */
    public String processNewLine(String msg, int status) {

        // no newline
        if(msg.indexOf('\n') == -1) {
            return status + " " + msg + CRLF;
        }

        StringBuffer sw = new StringBuffer(256);

        try {
        BufferedReader sr = new BufferedReader(new StringReader(msg));

            sw.append(String.valueOf(status));
            sw.append('-');

            String line = sr.readLine();
            for(;;) {
                String nextLine = sr.readLine();

                if(nextLine != null) {
                    sw.append(line);
                    sw.append(CRLF);
                }
                else {
                    sw.append(String.valueOf(status));
                    sw.append(' ');
                    sw.append(line);
                    sw.append(CRLF);
                    break;
                }
                line = nextLine;
            }
            sr.close();
        }
        catch(IOException ex) {
        }

        return sw.toString();
    }


    /**
     * Get ftp message from the properties file and replace the variables.
     */
    private String getMessage(int status, FtpRequest cmdLine, String[] args) {

        // make the key from the passed parameters
        String key = PREFIX + status;
        String keyc = key;
        if(cmdLine != null) {
            keyc = keyc + '.' + cmdLine.getCommand();
        }

        // get status property
        String str = getProperty(keyc);
        if(str == null) {
            str = getProperty(key);
        }
        if(str == null) {
            str = EMPTY;
        }

        // replace variables
        int startIndex = 0;
        int openIndex = str.indexOf('{', startIndex);
        if (openIndex == -1) {
            return str;
        }

        int closeIndex = str.indexOf('}', startIndex);
        if( (closeIndex == -1) || (openIndex > closeIndex) ) {
            return str;
        }

        StringBuffer sb = new StringBuffer();
        sb.append(str.substring(startIndex, openIndex));
        while(true) {
            String intStr = str.substring(openIndex+1, closeIndex);
            sb.append(getParam(cmdLine, args, intStr));

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
     * Get variable value.
     */
    private String getParam(FtpRequest cmdLine, String[] elms, String intStr) {

        // command line param
        if(cmdLine != null) {
            if(intStr.equals(CMD)) {
                return cmdLine.getCommand();
            }
            if(intStr.equals(ARG)) {
                return cmdLine.getArgument();
            }
        }

        // list param
        if(elms == null) {
            return EMPTY;
        }

        int index = 0;
        try {
            index = Integer.parseInt(intStr);
        }
        catch(NumberFormatException ex) {
            return EMPTY;
        }
        if( (index < 0) || (index >= elms.length) ) {
            return EMPTY;
        }
        return elms[index];
    }


    /**
     * Get ftp response.
     * @param status ftp status code.
     * @param cmd ftp request object (may be null).
     * @param ars variable arguent list (may be null).
     */
    public String getResponse(int status, FtpRequest cmd, FtpUser user, String[] args) {
        String strRes = getMessage(status, cmd, args);
        return processNewLine(strRes, status);
    }

}
