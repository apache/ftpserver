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

 package org.apache.ftpserver;

import java.io.File;
import java.util.Date;
import org.apache.ftpserver.util.Message;
import org.apache.ftpserver.interfaces.FtpStatisticsListener;
import org.apache.ftpserver.interfaces.FtpFileListener;
import org.apache.ftpserver.interfaces.FtpFileMonitor;

/**
 * This class encapsulates all the global statistics.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public
class FtpStatistics {

    private FtpStatisticsListener mListener = null;
    private FtpFileListener mFileListener   = null;
    private AbstractFtpConfig mConfig               = null;
    private FtpFileMonitor ftpFileMonitor;

    private Date mStartTime        = new Date();

    private int miNbrUpload        = 0;
    private int miNbrDownload      = 0;
    private int miNbrDelete        = 0;

    private int miLogins           = 0;
    private int miTotalLogins      = 0;

    private int miAnonLogins       = 0;
    private int miTotalAnonLogins  = 0;

    private int miConnections      = 0;
    private int miTotalConnections = 0;

    private long mlBytesUpload     = 0L;
    private long mlBytesDownload   = 0L;

    /**
     * Default constructor.
     */
    public FtpStatistics(AbstractFtpConfig cfg, FtpFileMonitor ftpFileMonitor) {
        mConfig = cfg;
        this.ftpFileMonitor = ftpFileMonitor;
    }


    /////////////////  All get methods  /////////////////
    /**
     * Get server start time.
     */
    public Date getStartTime() {
        return mStartTime;
    }

    /**
     * Get number of files uploaded.
     */
    public int getFileUploadNbr() {
        return miNbrUpload;
    }

    /**
     * Get number of files downloaded.
     */
    public int getFileDownloadNbr() {
        return miNbrDownload;
    }

    /**
     * Get number of files deleted.
     */
    public int getFileDeleteNbr() {
        return miNbrDelete;
    }

    /**
     * Get total number of bytes uploaded.
     */
    public long getFileUploadSize() {
        return mlBytesUpload;
    }

    /**
     * Get total number of bytes downloaded.
     */
    public long getFileDownloadSize() {
        return mlBytesDownload;
    }

    /**
     * Get current number of connections.
     */
    public int getConnectionNbr() {
        return miConnections;
    }

    /**
     * Get total connection number
     */
    public int getTotalConnectionNbr() {
        return miTotalConnections;
    }

    /**
     * Get current number of logins
     */
    public int getLoginNbr() {
        return miLogins;
    }

    /**
     * Get total number of logins
     */
    public int getTotalLoginNbr() {
        return miTotalLogins;
    }

    /**
     * Get current anonymous logins.
     */
    public int getAnonLoginNbr() {
        return miAnonLogins;
    }

    /**
     * Get total anonymous logins
     */
    public int getTotalAnonLoginNbr() {
        return miTotalAnonLogins;
    }


    /////////////////  All set methods  ///////////////////
    /**
     * Increment upload count.
     */
    void setUpload(File fl, FtpUserImpl user, long sz) {
        ++miNbrUpload;
        mlBytesUpload += sz;
        ftpFileMonitor.fileUploaded(user, fl);
        notifyUpload(fl, user);
    }


    /**
     * Increment download count.
     */
    void setDownload(File fl, FtpUserImpl user, long sz) {
        ++miNbrDownload;
        mlBytesDownload += sz;
        ftpFileMonitor.fileDownloaded(user, fl);
        notifyDownload(fl, user);
    }


    /**
     * Increment delete count.
     */
    void setDelete(File fl, FtpUserImpl user) {
        ++miNbrDelete;
        ftpFileMonitor.fileDeleted(user, fl);
        notifyDelete(fl, user);
    }


    /**
     * New login.
     */
    void setLogin(boolean anonymous) {
        ++miLogins;
        ++miTotalLogins;
        if(anonymous) {
            ++miAnonLogins;
            ++miTotalAnonLogins;
        }
        notifyLogin();
    }

    /**
     * User logout
     */
    void setLogout(boolean anonymous) {
        --miLogins;
        if(anonymous) {
            --miAnonLogins;
        }
        notifyLogout();
    }

    /**
     * New connection
     */
    void setOpenConnection() {
        ++miConnections;
        ++miTotalConnections;
        notifyConnection();
    }

    /**
     * Close connection
     */
    void setCloseConnection() {
        --miConnections;
        notifyConnection();
    }

    ////////////////////////////////////////////////////////////
    //                Event listener methods                  //
    ////////////////////////////////////////////////////////////
    /**
     * Add a listener object.
     */
    public void setListener(FtpStatisticsListener listener) {
        mListener = listener;
    }

    /**
     * Get listener object.
     */
    public FtpStatisticsListener getListener() {
        return mListener;
    }

    /**
     * Get file listener
     */
    public void setFileListener(FtpFileListener listener) {
        mFileListener = listener;
    }

    /**
     * Set file listener
     */
    public FtpFileListener getFileListener() {
        return mFileListener;
    }

    /**
     * Listener upload notification.
     */
    private void notifyUpload(final File fl, final FtpUserImpl user) {
        final FtpStatisticsListener listener = mListener;
        if (listener != null) {
            Message msg = new Message() {
                public void execute() {
                    listener.notifyUpload();
                }
            };
            mConfig.getMessageQueue().add(msg);
        }

        final FtpFileListener fileListener = mFileListener;
        if (fileListener != null) {
            Message msg = new Message() {
                public void execute() {
                    fileListener.notifyUpload(fl, user.getSessionId());
                }
            };
            mConfig.getMessageQueue().add(msg);
        }
    }

    /**
     * Listener download notification.
     */
    private void notifyDownload(final File fl, final FtpUserImpl user) {
        final FtpStatisticsListener listener = mListener;
        if (listener != null) {
            Message msg = new Message() {
                public void execute() {
                    listener.notifyDownload();
                }
            };
            mConfig.getMessageQueue().add(msg);
        }

        final FtpFileListener fileListener = mFileListener;
        if (fileListener != null) {
            Message msg = new Message() {
                public void execute() {
                    fileListener.notifyDownload(fl, user.getSessionId());
                }
            };
            mConfig.getMessageQueue().add(msg);
        }
    }

    /**
     * Listener delete notification.
     */
    private void notifyDelete(final File fl, final FtpUserImpl user) {
        final FtpStatisticsListener listener = mListener;
        if (listener != null) {
            Message msg = new Message() {
                public void execute() {
                    listener.notifyDelete();
                }
            };
            mConfig.getMessageQueue().add(msg);
        }

        final FtpFileListener fileListener = mFileListener;
        if (fileListener != null) {
            Message msg = new Message() {
                public void execute() {
                    fileListener.notifyDelete(fl, user.getSessionId());
                }
            };
            mConfig.getMessageQueue().add(msg);
        }
    }

    /**
     * Listener user login notification.
     */
    private void notifyLogin() {
        final FtpStatisticsListener listener = mListener;
        if (listener != null) {
            Message msg = new Message() {
                public void execute() {
                    listener.notifyLogin();
                }
            };
            mConfig.getMessageQueue().add(msg);
        }
    }

    /**
     * Listener user logout notification.
     */
    private void notifyLogout() {
        final FtpStatisticsListener listener = mListener;
        if (listener != null) {
            Message msg = new Message() {
                public void execute() {
                    listener.notifyLogout();
                }
            };
            mConfig.getMessageQueue().add(msg);
        }
    }

    /**
     * Listener user connection open/close notification.
     */
    private void notifyConnection() {
        final FtpStatisticsListener listener = mListener;
        if (listener != null) {
            Message msg = new Message() {
                public void execute() {
                    listener.notifyConnection();
                }
            };
            mConfig.getMessageQueue().add(msg);
        }
    }
}
