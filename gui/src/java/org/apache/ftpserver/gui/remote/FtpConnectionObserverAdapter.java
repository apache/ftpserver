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

package org.apache.ftpserver.gui.remote;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import org.apache.ftpserver.FtpUser;
import org.apache.ftpserver.remote.interfaces.ConnectionServiceInterface;
import org.apache.ftpserver.remote.interfaces.FtpConnectionObserver;

/**
 * Monitor all ftp connections.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public
class FtpConnectionObserverAdapter implements FtpConnectionObserver {

    private FtpConnectionObserver mObserver;
    private ConnectionServiceInterface mConService;

    /**
     * Constructor - set the actual listener object
     */
    public FtpConnectionObserverAdapter(ConnectionServiceInterface conService,
                                        FtpConnectionObserver observer) throws RemoteException {
        mObserver = observer;
        mConService = conService;

        UnicastRemoteObject.exportObject(this);
        mConService.setObserver(this);
    }

    /**
     * New connection notification.
     */
    public void newConnection(final FtpUser user) throws RemoteException {
        mObserver.newConnection(user);
    }

    /**
     * Close connection notification.
     */
    public void removeConnection(final FtpUser user) throws RemoteException {
        mObserver.removeConnection(user);
    }

    /**
     * User update notification.
     */
    public void updateConnection(final FtpUser user) throws RemoteException {
        mObserver.updateConnection(user);
    }

    /**
     * Close it
     */
    public void close() {
        System.out.println("Closing connection listener...");
        try {
            mConService.setObserver(null);
        }
        catch(Exception ex) {
            //ex.printStackTrace();
        }

        try {
            UnicastRemoteObject.unexportObject(this, true);
        }
        catch(Exception ex) {
            //ex.printStackTrace();
        }
    }
}

