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
package org.apache.ftpserver.remote;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Date;

import org.apache.ftpserver.remote.adapter.FtpFileListenerAdapter;
import org.apache.ftpserver.remote.adapter.FtpStatisticsListenerAdapter;
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

    private FtpStatisticsListenerAdapter  mStatisticsListener;
    private FtpFileListenerAdapter mFileListener;

    /**
     * Constructor - sets the actual statistics object
     */
    public FtpStatistics(final org.apache.ftpserver.FtpStatistics statistics) throws RemoteException {
        mStatistics = statistics;

        mStatisticsListener = new FtpStatisticsListenerAdapter();
        mFileListener = new FtpFileListenerAdapter();

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
