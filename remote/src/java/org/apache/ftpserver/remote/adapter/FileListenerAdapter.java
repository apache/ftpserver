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

import org.apache.ftpserver.remote.interfaces.FtpFileListener;

import java.io.File;
import java.rmi.RemoteException;

/**
 * Ftp file upload/download/delete listener remote interface.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public
class FileListenerAdapter implements org.apache.ftpserver.FileListener {

    private FtpFileListener mFileListener = null;

    /**
     * Default constructor.
     */
    public FileListenerAdapter() {
    }

    /**
     * Get actual listener object
     */
    public FtpFileListener getFileListener() {
        return mFileListener;
    }

    /**
     * Set file listener
     */
    public void setFileListener(FtpFileListener listener) {
        mFileListener = listener;
    }

    /**
     * User file upload notification.
     */
    public void notifyUpload(final File file, final String sessionId) {
        FtpFileListener listener = mFileListener;
        if (listener != null) {
            try {
                listener.notifyUpload(file.getAbsolutePath(), sessionId);
            } catch (RemoteException ex) {
                mFileListener = null;
            }
        }
    }

    /**
     * User file download notification.
     */
    public void notifyDownload(final File file, final String sessionId) {
        FtpFileListener listener = mFileListener;
        if (listener != null) {
            try {
                listener.notifyDownload(file.getAbsolutePath(), sessionId);
            } catch (RemoteException ex) {
                mFileListener = null;
            }
        }
    }

    /**
     * User file delete notification.
     */
    public void notifyDelete(final File file, final String sessionId) {
        FtpFileListener listener = mFileListener;
        if (listener != null) {
            try {
                listener.notifyDelete(file.getAbsolutePath(), sessionId);
            } catch (RemoteException ex) {
                mFileListener = null;
            }
        }
    }

}

