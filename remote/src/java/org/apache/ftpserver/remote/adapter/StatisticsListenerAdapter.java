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


import org.apache.ftpserver.remote.interfaces.FtpStatisticsListener;

import java.rmi.RemoteException;

/**
 * Ftp statistics listener remote interface.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public
class StatisticsListenerAdapter implements org.apache.ftpserver.StatisticsListener {

    private FtpStatisticsListener mListener = null;

    /**
     * Constructor - set the actual listener object
     */
    public StatisticsListenerAdapter() {
    }

    /**
     * Get the actual listener object
     */
    public FtpStatisticsListener getStatisticsListener() {
        return mListener;
    }

    /**
     * Set the actual listener object.
     */
    public void setStatisticsListener(FtpStatisticsListener listener) {
        mListener = listener;
    }

    /**
     * User file upload notification.
     */
    public void notifyUpload() {
        FtpStatisticsListener listener = mListener;
        if (listener != null) {
            try {
                listener.notifyUpload();
            } catch (RemoteException ex) {
                mListener = null;
            }
        }
    }

    /**
     * User file download notification.
     */
    public void notifyDownload() {
        FtpStatisticsListener listener = mListener;
        if (listener != null) {
            try {
                listener.notifyDownload();
            } catch (RemoteException ex) {
                mListener = null;
            }
        }
    }

    /**
     * User file delete notification.
     */
    public void notifyDelete() {
        FtpStatisticsListener listener = mListener;
        if (listener != null) {
            try {
                listener.notifyDelete();
            } catch (RemoteException ex) {
                mListener = null;
            }
        }
    }

    /**
     * New user login notification.
     */
    public void notifyLogin() {
        FtpStatisticsListener listener = mListener;
        if (listener != null) {
            try {
                listener.notifyLogin();
            } catch (RemoteException ex) {
                mListener = null;
            }
        }
    }

    /**
     * User logout notification.
     */
    public void notifyLogout() {
        FtpStatisticsListener listener = mListener;
        if (listener != null) {
            try {
                listener.notifyLogout();
            } catch (RemoteException ex) {
                mListener = null;
            }
        }
    }

    /**
     * Connection open/close notification
     */
    public void notifyConnection() {
        FtpStatisticsListener listener = mListener;
        if (listener != null) {
            try {
                listener.notifyConnection();
            } catch (RemoteException ex) {
                mListener = null;
            }
        }
    }

}
