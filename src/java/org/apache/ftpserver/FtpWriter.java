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
import java.io.Writer;
import java.io.OutputStreamWriter;
import java.net.Socket;

import org.apache.ftpserver.util.Message;
import org.apache.ftpserver.interfaces.SpyConnectionInterface;

/**
 * Writer object used by the server. It has the spying capability.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public
class FtpWriter extends Writer {

    private OutputStreamWriter mOriginalWriter;
    private SpyConnectionInterface mSpy;
    private FtpConfig mConfig;

    /**
     * Constructor - set the actual writer object
     */
    public FtpWriter(Socket soc, FtpConfig config) throws IOException {
        mOriginalWriter = new OutputStreamWriter(soc.getOutputStream());
        mConfig = config;
    }

    /**
     * Get the spy object to get what the user is writing.
     */
    public SpyConnectionInterface getSpyObject() {
        return mSpy;
    }

    /**
     * Set the connection spy object.
     */
    public void setSpyObject(SpyConnectionInterface spy) {
        mSpy = spy;
    }

    /**
     * Spy print. Monitor server response.
     */
    private void spyResponse(final String str) throws IOException {
        final SpyConnectionInterface spy = mSpy;
        if (spy != null) {
            Message msg = new Message() {
                public void execute() {
                    try {
                        spy.response(str);
                    }
                    catch(Exception ex) {
                        mSpy = null;
                        mConfig.getLogger().error("FtpWriter.spyResponse()", ex);
                    }
                }
            };
            mConfig.getMessageQueue().add(msg);
        }
    }

    /**
     * Write a character array.
     */
    public void write(char[] cbuf) throws IOException {
        String str = new String(cbuf);
        spyResponse(str);
        mOriginalWriter.write(cbuf);
        mOriginalWriter.flush();
    }

    /**
     * Write a portion of character array
     */
    public void write(char[] cbuf, int off, int len) throws IOException {
        String str = new String(cbuf, off, len);
        spyResponse(str);
        mOriginalWriter.write(cbuf, off, len);
        mOriginalWriter.flush();
    }

    /**
     * Write a single character
     */
    public void write(int c) throws IOException {
        String str = "" + (char)c;
        spyResponse(str);
        mOriginalWriter.write(c);
        mOriginalWriter.flush();
    }

    /**
     * Write a string
     */
    public void write(String str) throws IOException {
        spyResponse(str);
        mOriginalWriter.write(str);
        mOriginalWriter.flush();
    }

    /**
     * Write a portion of the string.
     */
    public void write(String str, int off, int len) throws IOException {
        String strpart = str.substring(off, len);
        spyResponse(strpart);
        mOriginalWriter.write(str, off, len);
        mOriginalWriter.flush();
    }

    /**
     * Close writer.
     */
    public void close() throws IOException {
        mOriginalWriter.close();
    }

    /**
     * Flush the stream
     */
    public void flush() throws IOException {
        mOriginalWriter.flush();
    }

}
