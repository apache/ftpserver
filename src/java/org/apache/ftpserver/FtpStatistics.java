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
 package org.apache.ftpserver;

import java.io.File;
import java.util.Date;
import org.apache.ftpserver.util.Message;
import org.apache.ftpserver.interfaces.FtpStatisticsListener;
import org.apache.ftpserver.interfaces.FtpFileListener;

/**
 * This class encapsulates all the global statistics.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public
class FtpStatistics {

    private FtpStatisticsListener mListener = null;
    private FtpFileListener mFileListener   = null;
    private FtpConfig mConfig               = null;

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
    public FtpStatistics(FtpConfig cfg) {
        mConfig = cfg;
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
    void setUpload(File fl, FtpUser user, long sz) {
        ++miNbrUpload;
        mlBytesUpload += sz;
        mConfig.getLogger().info("File upload : " + user.getName() + " - " + fl.getAbsolutePath());
        notifyUpload(fl, user);
    }

    /**
     * Increment download count.
     */
    void setDownload(File fl, FtpUser user, long sz) {
        ++miNbrDownload;
        mlBytesDownload += sz;
        mConfig.getLogger().info("File download : " + user.getName() + " - " + fl.getAbsolutePath());
        notifyDownload(fl, user);
    }

    /**
     * Increment delete count.
     */
    void setDelete(File fl, FtpUser user) {
        ++miNbrDelete;
        mConfig.getLogger().info("File delete : " + user.getName() + " - " + fl.getAbsolutePath());
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
    private void notifyUpload(final File fl, final FtpUser user) {
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
    private void notifyDownload(final File fl, final FtpUser user) {
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
    private void notifyDelete(final File fl, final FtpUser user) {
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
