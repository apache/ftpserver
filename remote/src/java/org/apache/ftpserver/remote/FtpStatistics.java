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
package org.apache.ftpserver.remote;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Date;

import org.apache.ftpserver.remote.adapter.FileListenerAdapter;
import org.apache.ftpserver.remote.adapter.StatisticsListenerAdapter;
import org.apache.ftpserver.remote.interfaces.FtpStatisticsInterface;
import org.apache.ftpserver.remote.interfaces.FtpStatisticsListener;
import org.apache.ftpserver.remote.interfaces.FtpFileListener;

/**
 * Ftp statistis remote adapter class.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public
class FtpStatistics implements FtpStatisticsInterface {

    private org.apache.ftpserver.FtpStatistics mStatistics;

    private StatisticsListenerAdapter  mStatisticsListener;
    private FileListenerAdapter mFileListener;

    /**
     * Constructor - sets the actual statistics object
     */
    public FtpStatistics(final org.apache.ftpserver.FtpStatistics statistics) throws RemoteException {
        mStatistics = statistics;

        mStatisticsListener = new StatisticsListenerAdapter();
        mFileListener = new FileListenerAdapter();

        UnicastRemoteObject.exportObject(this);
    }

    /**
     * Get server start time.
     */
    public Date getStartTime() {
        return mStatistics.getStartTime();
    }

    /**
     * Get number of files uploaded.
     */
    public int getFileUploadNbr() {
        return mStatistics.getFileUploadNbr();
    }

    /**
     * Get number of files downloaded.
     */
    public int getFileDownloadNbr() {
        return mStatistics.getFileDownloadNbr();
    }

    /**
     * Get number of files deleted.
     */
    public int getFileDeleteNbr() {
        return mStatistics.getFileDeleteNbr();
    }

    /**
     * Get total number of bytes uploaded.
     */
    public long getFileUploadSize() {
        return mStatistics.getFileUploadSize();
    }

    /**
     * Get total number of bytes downloaded.
     */
    public long getFileDownloadSize() {
        return mStatistics.getFileDownloadSize();
    }

    /**
     * Get current number of connections.
     */
    public int getConnectionNbr() {
        return mStatistics.getConnectionNbr();
    }

    /**
     * Get total number of connections
     */
    public int getTotalConnectionNbr() {
        return mStatistics.getTotalConnectionNbr();
    }

    /**
     * Get current number of logins
     */
    public int getLoginNbr() {
        return mStatistics.getLoginNbr();
    }

    /**
     * Get total number of logins
     */
    public int getTotalLoginNbr() {
        return mStatistics.getTotalLoginNbr();
    }

    /**
     * Get current number of anonymous logins.
     */
    public int getAnonLoginNbr() {
        return mStatistics.getAnonLoginNbr();
    }

    /**
     * Get total number of anonymous logins
     */
    public int getTotalAnonLoginNbr() {
        return mStatistics.getTotalAnonLoginNbr();
    }

    /**
     * Set a listener object.
     */
    public void setListener(FtpStatisticsListener listener) {
        mStatisticsListener.setStatisticsListener(listener);
        if (listener == null) {
            mStatistics.setListener(null);
        }
        else {
            mStatistics.setListener(mStatisticsListener);
        }
    }

    /**
     * Get listener object.
     */
    public FtpStatisticsListener getListener() {
        return mStatisticsListener.getStatisticsListener();
    }

    /**
     * Get file listener
     */
    public void setFileListener(FtpFileListener listener) {
        mFileListener.setFileListener(listener);
        if (listener == null) {
            mStatistics.setFileListener(null);
        }
        else {
            mStatistics.setFileListener(mFileListener);
        }
    }

    /**
     * Set file listener
     */
    public FtpFileListener getFileListener() {
        return mFileListener.getFileListener();
    }


}
