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
package org.apache.ftpserver.remote.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Date;


/**
 * Ftp statistis remote interface.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public
interface FtpStatisticsInterface extends Remote {

    /**
     * Get server start time.
     */
    Date getStartTime() throws RemoteException;

    /**
     * Get number of files uploaded.
     */
    int getFileUploadNbr() throws RemoteException;

    /**
     * Get number of files downloaded.
     */
    int getFileDownloadNbr() throws RemoteException;

    /**
     * Get number of files deleted.
     */
    int getFileDeleteNbr() throws RemoteException;

    /**
     * Get total number of bytes uploaded.
     */
    long getFileUploadSize() throws RemoteException;

    /**
     * Get total number of bytes downloaded.
     */
    long getFileDownloadSize() throws RemoteException;

    /**
     * Get current number of connections.
     */
    int getConnectionNbr() throws RemoteException;

    /**
     * Get total number of connections
     */
    int getTotalConnectionNbr() throws RemoteException;

    /**
     * Get current number of logins
     */
    int getLoginNbr() throws RemoteException;

    /**
     * Get total number of logins
     */
    int getTotalLoginNbr() throws RemoteException;

    /**
     * Get current number of anonymous logins.
     */
    int getAnonLoginNbr() throws RemoteException;

    /**
     * Get total number of anonymous logins
     */
    int getTotalAnonLoginNbr() throws RemoteException;

    /**
     * Set a listener object.
     */
    void setListener(FtpStatisticsListener listener) throws RemoteException;

    /**
     * Get listener object.
     */
    FtpStatisticsListener getListener() throws RemoteException;

    /**
     * Get file listener
     */
    void setFileListener(FtpFileListener listener) throws RemoteException;

    /**
     * Set file listener
     */
    FtpFileListener getFileListener() throws RemoteException;

}
