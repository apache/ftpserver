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

import org.apache.ftpserver.remote.interfaces.FtpFileListener;
import org.apache.ftpserver.remote.interfaces.FtpStatisticsInterface;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Ftp file upload/download/delete listener remote interface.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public
class FtpFileListenerAdapter implements FtpFileListener {

    private FtpFileListener mListener;
    private FtpStatisticsInterface mStatistics;

    /**
     * Constructor - set the actual listener object
     */
    public FtpFileListenerAdapter(FtpStatisticsInterface statistics,
                                  FtpFileListener listener) throws RemoteException {
        mListener = listener;
        mStatistics = statistics;

        UnicastRemoteObject.exportObject(this);
        mStatistics.setFileListener(this);
    }

    /**
     * User file upload notification.
     */
    public void notifyUpload(final String file, final String sessionId) throws RemoteException {
        mListener.notifyUpload(file, sessionId);
    }

    /**
     * User file download notification.
     */
    public void notifyDownload(final String file, final String sessionId) throws RemoteException {
        mListener.notifyDownload(file, sessionId);
    }

    /**
     * User file delete notification.
     */
    public void notifyDelete(final String file, final String sessionId) throws RemoteException {
        mListener.notifyDelete(file, sessionId);
    }

    /**
     * Close it
     */
    public void close() {
        System.out.println("Closing file listener...");
        try {
            mStatistics.setFileListener(null);
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

