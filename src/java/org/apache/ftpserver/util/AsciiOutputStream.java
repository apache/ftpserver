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
package org.apache.ftpserver.util;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Write ASCII data. Before writing it filters the data.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public
class AsciiOutputStream extends OutputStream {

    private long    mlActualByteWritten = 0;
    private boolean mbIgnoreNonAscii    = true;
    private OutputStream mOutputStream;

    /**
     * Constructor.
     * @param os <code>java.io.OutputStream</code> to be filtered.
     */
    public AsciiOutputStream(OutputStream os) {
        mOutputStream = os;
    }

    /**
     * Write a single byte.
     * ASCII characters are defined to be
     * the lower half of an eight-bit code set (i.e., the most
     * significant bit is zero). Change "\n" to "\r\n".
     */
    public void write(int i) throws IOException {

        if (mbIgnoreNonAscii && (i > 0x7F) ) {
            return;
        }

        if (i == '\r') {
            return;
        }
        if (i == '\n') {
            actualWrite('\r');
        }
        actualWrite(i);

    }

    /**
     * Close stream
     */
    public void close() throws IOException {
        mOutputStream.close();
    }

    /**
     * Flush stream data
     */
    public void flush() throws IOException {
        mOutputStream.flush();
    }

    /**
     * write actual data.
     */
    private void actualWrite(int b) throws IOException {
        mOutputStream.write(b);
        ++mlActualByteWritten;
    }


    /**
     * Get actual byte written.
     */
    public long getByteWritten() {
        return mlActualByteWritten;
    }

    /**
     * Is non ascii character ignored.
     * If true don't write non-ascii character.
     * Else first convert it to ascii by ANDing with 0x7F.
     */
    public boolean getIsIgnoreNonAscii() {
        return mbIgnoreNonAscii;
    }

    /**
     * Set non-ascii ignore boolean value.
     */
    public void setIsIgnoreNonAscii(boolean ig) {
      mbIgnoreNonAscii = ig;
    }

}

