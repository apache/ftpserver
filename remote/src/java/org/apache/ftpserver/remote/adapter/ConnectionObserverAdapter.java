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
package org.apache.ftpserver.remote.adapter;

import java.io.IOException;
import java.rmi.RemoteException;

import org.apache.ftpserver.User;
import org.apache.ftpserver.remote.interfaces.FtpConnectionObserver;


/**
 * This connection observer remote adapter class.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public
class ConnectionObserverAdapter implements org.apache.ftpserver.ConnectionObserver {

    private FtpConnectionObserver mObserver;

    /**
     * Default constructor.
     */
    public ConnectionObserverAdapter() {
    }

    /**
     * Get observer
     */
    public FtpConnectionObserver getConnectionObserver() {
        return mObserver;
    }

    /**
     * Set observer
     */
    public void setConnectionObserver(FtpConnectionObserver observer) {
        mObserver = observer;
    }

    /**
     * New connection notification.
     * @param user newly connected user
     */
    public void newConnection(final User user) {
        FtpConnectionObserver observer = mObserver;
        if (observer != null) {
            try {
                observer.newConnection(user);
            }
            catch(RemoteException ex) {
                mObserver = null;
            }
        }
    }

    /**
     * Close connection notification
     * @param user closed user object.
     */
    public void removeConnection(final User user) {
        FtpConnectionObserver observer = mObserver;
        if (observer != null) {
            try {
                observer.removeConnection(user);
            }
            catch(RemoteException ex) {
                mObserver = null;
            }
        }
    }

    /**
     * Update connection notification
     * @param user updated user object
     */
    public void updateConnection(final User user) {
        FtpConnectionObserver observer = mObserver;
        if (observer != null) {
            try {
                observer.updateConnection(user);
            }
            catch(RemoteException ex) {
                mObserver = null;
            }
        }
    }

    public void requestError(String message, IOException ex) {
        //TODO
    }

    public void unknownServiceException(String message, Throwable th) {
        //TODO
    }

    public void newRequest(String message) {
        //TODO
    }
}
