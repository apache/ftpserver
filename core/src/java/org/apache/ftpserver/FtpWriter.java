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

import java.io.IOException;
import java.io.Writer;
import java.io.OutputStreamWriter;
import java.net.Socket;

import org.apache.ftpserver.util.Message;
import org.apache.ftpserver.SpyConnectionInterface;
import org.apache.ftpserver.WriterMonitor;

/**
 * Writer object used by the server. It has the spying capability.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public
class FtpWriter extends Writer {

    private OutputStreamWriter mOriginalWriter;
    private SpyConnectionInterface mSpy;
    private AbstractFtpConfig mConfig;
    private WriterMonitor writerMonitor;

    /**
     * Constructor - set the actual writer object
     */
    public FtpWriter(Socket soc, AbstractFtpConfig config) throws IOException {
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
                    catch(IOException ex) {
                        mSpy = null;
                        writerMonitor.responseException("FtpWriter.spyResponse()", ex);
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
