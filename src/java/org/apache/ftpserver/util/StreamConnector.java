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

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Connect one <code>java.io.InputStream</code> with a
 * <code>java.io.OutputStream</code>.
 *
 * Features:
 * <ul>
 *   <li> Buffered transfer or not (default unbuffered).</li>
 *   <li> Threaded transfer or not (default false).</li>
 *   <li> Set transfer rate limit (default no limit).</li>
 *   <li> Stop transfer at any time.</li>
 *   <li> Get current byte transferred.</li>
 *   <li> Transfer notification</li>
 * </ul>
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public
class StreamConnector implements Runnable {

    private InputStream mInStream;
    private OutputStream mOutStream;

    private boolean mbThreaded    = false;
    private boolean mbBuffered    = false;
    private boolean mbStopRequest = false;

    private int  miTransferLimit  = 0;
    private long mlTransferSize   = 0;

    private Exception mExp        = null;
    private Thread mConThread     = null; // stream conneector thread

    private StreamConnectorObserver mObserver = null;


    /**
     * Constructors
     * @param in pipe input
     * @param out pipe output
     */
    public StreamConnector(InputStream in, OutputStream out) {
        mInStream = in;
        mOutStream = out;
    }

    /**
     * Set stream connector observer.
     */
    public synchronized void setObserver(StreamConnectorObserver obsr) {
        mObserver = obsr;
    }

    /**
     * Set buffered transferred property.
     */
    public void setIsBuffered(boolean buf) {
        mbBuffered = buf;
    }

    /**
     * Get is buffered.
     */
    public boolean getIsBuffered() {
        return mbBuffered;
    }

    /**
     * Set threaded transfer property.
     */
    public void setIsThreaded(boolean thr) {
        mbThreaded = thr;
    }

    /**
     * Is the data transfer threaded?
     */
    public boolean getIsThreaded() {
        return mbThreaded;
    }

    /**
     * Get exception.
     */
    public Exception getException() {
        return mExp;
    }

    /**
     * Get transferred size in bytes.
     */
    public long getTransferredSize() {
        return mlTransferSize;
    }

    /**
     * Get transfer limit in bytes/second.
     */
    public int getMaxTransferRate() {
        return miTransferLimit;
    }

    /**
     * Set transfer limit - bytes/second.
     */
    public void setMaxTransferRate(int limit) {
        miTransferLimit = limit;
    }

    /**
     * Check exception status.
     */
    public boolean hasException() {
        return mExp != null;
    }

    /**
     * Stop data transfer.
     */
    public synchronized void stopTransfer() {
        mbStopRequest = true;
        if(mConThread != null) {
            mConThread.interrupt();
        }
        IoUtils.close(mInStream);
        IoUtils.close(mOutStream);
        mConThread = null;
        mInStream = null;
        mOutStream = null;
    }

    /**
     * Is stopped?
     */
    public boolean isStopped() {
        return mbStopRequest;
    }

    /**
     * Connect two streams.
     */
    public void connect() {

        // error test
        if(mbStopRequest) {
            throw new IllegalStateException("Data already transferred.");
        }
        if(mConThread != null) {
            throw new IllegalStateException("Streams already connected.");
        }

        // now connect
        if(mbThreaded) {
            new Thread(this).start();
        }
        else {
            run();
        }
    }


    /**
     * Transfer data from one stream to another.
     */
    public void run() {
        long startTime = System.currentTimeMillis();
        mConThread = Thread.currentThread();
        InputStream in = mInStream;
        OutputStream out = mOutStream;
        byte[] buff = new byte[4096];

        if(mbBuffered) {
            in = IoUtils.getBufferedInputStream(in);
            out = IoUtils.getBufferedOutputStream(out);
        }

        try {
           while(! (mbStopRequest || mConThread.isInterrupted()) ) {

               // check transfer rate
               if(miTransferLimit > 0) {
                   long interval = System.currentTimeMillis() - startTime;

                   // prevent "divide by zero" exception
                   if(interval == 0) {
                       interval = 1;
                   }

                   int rate = (int)((mlTransferSize*1000)/interval);
                   if(rate > miTransferLimit) {
                       try { Thread.sleep(100); } catch(InterruptedException ex) {break;}
                       continue;
                   }
               }

               // read data
               int count = in.read(buff);
               if(count == -1) {
                   break;
               }

               // write data
               out.write(buff, 0, count);
               mlTransferSize += count;
               notifyObserver(count);
           }
           out.flush();
        }
        catch(Exception ex) {
            mExp = ex;
        }
        finally {
            synchronized (this) {
                mbStopRequest = true;
                IoUtils.close(in);
                IoUtils.close(out);
                notifyObserver(-1);
                mConThread = null;
            }
        }
    }

    /**
     * Notify the observer.
     * @param sz bytes transferred
     */
    private void notifyObserver(int sz) {
        StreamConnectorObserver observer = mObserver;
        if(observer != null) {
            observer.dataTransferred(sz);
        }
    }

    /**
     * Last defense to stop thread.
     */
    protected void finalize() throws Throwable {
        stopTransfer();
        super.finalize();
    }

}
