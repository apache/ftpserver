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
