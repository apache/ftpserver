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

/**
 * Simple message queue implementation.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public
class AsyncMessageQueue implements Runnable {

    private Queue mMsgQueue;
    private boolean mbStopRequest;
    private Thread mThread;

    /**
     * Constructor - instantiate the queue and start the thread.
     */
    public AsyncMessageQueue() {
        mMsgQueue = new Queue(true);
        mbStopRequest = false;
        mThread = new Thread(this);
        mThread.start();
    }

    /**
     * Add a new message object.
     */
    public void add(Message msg) {
        if(mbStopRequest) {
            throw new IllegalArgumentException("Message queue has been stopped.");
        }
        mMsgQueue.put(msg);
    }

    /**
     * Get the number of elements in the queue.
     */
    public int size() {
        return mMsgQueue.size();
    }

    /**
     * Get max size
     */
    public int getMaxSize() {
        return mMsgQueue.getMaxSize();
    }

    /**
     * Set max size
     */
    public void setMaxSize(int maxSize) {
        mMsgQueue.setMaxSize(maxSize);
    }

    /**
     * Thread starting point - get message ant execute.
     */
    public void run() {
        while(!mbStopRequest) {
            Message msg = (Message)mMsgQueue.get();
            if(msg == null) {
                return;
            }
            try {
                msg.execute();
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Stop the message queue.
     */
    public void stop() {
        mbStopRequest = true;
        mMsgQueue.clear();
        if(mThread != null) {
            mThread.interrupt();
            mThread = null;
        }
    }

    /**
     * Check whether the message queue is active or not.
     */
    public boolean isDisposed() {
        return mbStopRequest;
    }

}
