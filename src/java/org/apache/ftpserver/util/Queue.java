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

import java.util.LinkedList;

/**
 * Queue (first in first out) implementation. It supports two types of queues.
 * <ul>
 *   <li> Queue is empty : throws NoSuchElementException.</li>
 *   <li> Queue is empty : waits for the new element.</li>
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
        if(mbWait) {
            while(mList.size() == 0) {
                try {
                   wait();
                }
                catch(InterruptedException ex) {
                    return null;
                }
            }
            return mList.removeFirst();
        }
        else {
           return mList.removeFirst();
        }
    }

    /**
     * Try to get the first element. If the list is empty,
     * the thread will wait. If interrupted returns null.
     */
    public synchronized Object get(long waitTimeMillis) {
        if(mbWait) {
            if(mList.size() == 0) {
                try {
                   wait(waitTimeMillis);
                }
                catch(InterruptedException ex) {
                    return null;
                }
            }

            if(mList.size() == 0) {
                return null;
            }
            else {
                return mList.removeFirst();
            }
        }
        else {
           return mList.removeFirst();
        }
    }

    /**
     * Put an object into the queue and notify the waiting thread.
     */
    public synchronized void put(Object obj) {
        if(obj == null) {
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
