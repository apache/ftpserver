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

import java.util.LinkedList;

/**
 * Queue (first in first out) implementation. It supports two types of queues.
 * <ul>
 * <li> Queue is empty : throws NoSuchElementException.</li>
 * <li> Queue is empty : waits for the new element.</li>
 * </ul>
 * Null values <b>cannot</b> be inserted.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */

public
class Queue {

    private LinkedList mList = new LinkedList();
    private boolean mbWait;
    private int miMaxSize = 0;

    /**
     * Constructor.
     *
     * @param bWait - thread will wait or not
     */
    public Queue(boolean bWait) {
        mbWait = bWait;
    }


    /**
     * Try to get the first element. If the list is empty,
     * the thread will wait. If interrupted returns null.
     */
    public synchronized Object get() {
        if (mbWait) {
            while (mList.size() == 0) {
                try {
                    wait();
                } catch (InterruptedException ex) {
                    return null;
                }
            }
            return mList.removeFirst();
        } else {
            return mList.removeFirst();
        }
    }

    /**
     * Try to get the first element. If the list is empty,
     * the thread will wait. If interrupted returns null.
     */
    public synchronized Object get(long waitTimeMillis) {
        if (mbWait) {
            if (mList.size() == 0) {
                try {
                    wait(waitTimeMillis);
                } catch (InterruptedException ex) {
                    return null;
                }
            }

            if (mList.size() == 0) {
                return null;
            } else {
                return mList.removeFirst();
            }
        } else {
            return mList.removeFirst();
        }
    }

    /**
     * Put an object into the queue and notify the waiting thread.
     */
    public synchronized void put(Object obj) {
        if (obj == null) {
            throw new NullPointerException("Queue element cannot be null");
        }

        if (miMaxSize <= 0 || mList.size() < miMaxSize) {
            mList.addLast(obj);
            notify();
        }
    }

    /**
     * Get the number of elements in the queue.
     */
    public synchronized int size() {
        return mList.size();
    }

    /**
     * Get max size
     */
    public int getMaxSize() {
        return miMaxSize;
    }

    /**
     * Set max size
     */
    public void setMaxSize(int maxSize) {
        miMaxSize = maxSize;
    }

    /**
     * Is the list empty (size == 0)
     */
    public synchronized boolean isEmpty() {
        return mList.size() == 0;
    }

    /**
     * Remove all the elements.
     */
    public synchronized void clear() {
        mList.clear();
    }
}
