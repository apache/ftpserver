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

import org.apache.ftpserver.remote.interfaces.FtpStatisticsInterface;
import org.apache.ftpserver.remote.interfaces.FtpStatisticsListener;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Ftp statistics listener remote interface.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public
class FtpStatisticsListenerAdapter implements FtpStatisticsListener {

    private FtpStatisticsInterface mStatistics;
    private FtpStatisticsListener mListener;

    /**
     * Constructor - set the actual listener object
     */
    public FtpStatisticsListenerAdapter(FtpStatisticsInterface statistics,
                                        FtpStatisticsListener listener) throws RemoteException {
        mListener = listener;
        mStatistics = statistics;

        UnicastRemoteObject.exportObject(this);
        mStatistics.setListener(this);
    }


    /**
     * User file upload notification.
     */
    public void notifyUpload() throws RemoteException {
        mListener.notifyUpload();
    }

    /**
     * User file download notification.
     */
    public void notifyDownload() throws RemoteException {
        mListener.notifyDownload();
    }

    /**
     * User file delete notification.
     */
    public void notifyDelete() throws RemoteException {
        mListener.notifyDelete();
    }

    /**
     * New user login notification.
     */
    public void notifyLogin() throws RemoteException {
        mListener.notifyLogin();
    }

    /**
     * User logout notification.
     */
    public void notifyLogout() throws RemoteException {
        mListener.notifyLogout();
    }

    /**
     * Connection open/close notification
     */
    public void notifyConnection() throws RemoteException {
        mListener.notifyConnection();
    }

    /**
     * Close it
     */
    public void close() {
        System.out.println("Closing statistics listener...");
        try {
            mStatistics.setListener(null);
        } catch (Exception ex) {
            //ex.printStackTrace();
        }

        try {
            UnicastRemoteObject.unexportObject(this, true);
        } catch (Exception ex) {
            //ex.printStackTrace();
        }
    }

}
