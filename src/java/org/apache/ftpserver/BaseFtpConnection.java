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
 */package org.apache.ftpserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.Socket;

import org.apache.avalon.cornerstone.services.connection.ConnectionHandler;
import org.apache.ftpserver.interfaces.FtpConnectionObserver;
import org.apache.ftpserver.interfaces.SpyConnectionInterface;
import org.apache.ftpserver.util.IoUtils;
import org.apache.ftpserver.util.Message;
import org.apache.ftpserver.util.StreamConnectorObserver;


/**
 * This is a generic ftp connection handler. It delegates
 * the request to appropriate methods in subclasses.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public
class BaseFtpConnection implements ConnectionHandler, StreamConnectorObserver {

    protected static final Class[] METHOD_INPUT_SIG = new Class[] {FtpRequest.class, FtpWriter.class};

    protected FtpConfig mConfig                 = null;
    protected FtpStatus mFtpStatus              = null;
    protected FtpDataConnection mDataConnection = null;
    protected FtpUser mUser                     = null;
    protected SpyConnectionInterface mSpy       = null;
    protected FtpConnectionObserver mObserver   = null;
    protected Socket mControlSocket             = null;
    protected FtpWriter mWriter                 = null;
    protected boolean mbStopRequest             = false;


    /**
     * Set configuration file and the control socket.
     */
    public BaseFtpConnection(FtpConfig ftpConfig) {
      mConfig = ftpConfig;
      mFtpStatus = mConfig.getStatus();
      mUser = new FtpUser();
    }

    /**
     * Server one FTP connection.
     */
    public void handleConnection(final Socket socket) {
        mControlSocket = socket;
        InetAddress clientAddress = mControlSocket.getInetAddress();
        mConfig.getLogger().info("Handling new request from " + clientAddress.getHostAddress());
        mDataConnection = new FtpDataConnection(mConfig);
        mUser.setClientAddress(clientAddress);
        mConfig.getConnectionService().newConnection(this);

        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(mControlSocket.getInputStream(), "ASCII"));
            mWriter = new FtpWriter(mControlSocket, mConfig);

            // permission check
            if( !mConfig.getIpRestrictor().hasPermission(mControlSocket.getInetAddress()) ) {
                mWriter.write(mFtpStatus.getResponse(530, null, mUser, null));
                return;
            }
            mWriter.write(mFtpStatus.getResponse(220, null, mUser, null));

            do {
                notifyObserver();
                String commandLine = in.readLine();

                // test command line
                if(commandLine == null) {
                    break;
                }

                spyRequest(commandLine);
                if(commandLine.equals("")) {
                    continue;
                }

                FtpRequest request = new FtpRequest(commandLine);
                if(!hasPermission(request)) {
                    mWriter.write(mFtpStatus.getResponse(530, request, mUser, null));
                    break;
                }

                // execute command
                service(request, mWriter);
            }
            while(!mbStopRequest);
        }
        catch(Exception ex) {
        }
        finally {
            IoUtils.close(in);
            IoUtils.close(mWriter);
            ConnectionService conService = mConfig.getConnectionService();
            if (conService != null) {
                conService.closeConnection(mUser.getSessionId());
            }
        }
    }


    /**
     * Execute the ftp command.
     */
    public void service(FtpRequest request, FtpWriter writer) throws IOException {
        try {
             String metName = "do" + request.getCommand();
             Method actionMet = getClass().getDeclaredMethod(metName, METHOD_INPUT_SIG);
             actionMet.invoke(this, new Object[] {request, writer});
         }
         catch(NoSuchMethodException ex) {
             writer.write(mFtpStatus.getResponse(502, request, mUser, null));
         }
         catch(InvocationTargetException ex) {
             writer.write(mFtpStatus.getResponse(500, request, mUser, null));
             Throwable th = ex.getTargetException();
             if (th instanceof java.io.IOException) {
                throw (IOException)th;
             }
             else {
                mConfig.getLogger().warn("BaseFtpConnection.service()", th);
             }
         }
         catch(Exception ex) {
             writer.write(mFtpStatus.getResponse(500, request, mUser, null));
             if (ex instanceof java.io.IOException) {
                throw (IOException)ex;
             }
             else {
                mConfig.getLogger().warn("BaseFtpConnection.service()", ex);
             }
         }
    }

    /**
     * Check permission - default implementation - does nothing.
     */
    protected boolean hasPermission(FtpRequest request) {
        return true;
    }

    /**
     * User logout and stop this thread.
     */
    public void stop() {
        mbStopRequest = true;
        if (mDataConnection != null) {
            mDataConnection.dispose();
            mDataConnection = null;
        }
        if (mControlSocket != null) {
            try {
                mControlSocket.close();
            }
            catch(Exception ex) {
            }
            mControlSocket = null;
        }
        if (mUser.hasLoggedIn()) {
            mUser.logout();
        }
        mObserver = null;
    }

    /**
     * Is the connection closed?
     */
    public boolean isClosed() {
        return mbStopRequest;
    }

    /**
     * Monitor the user request.
     */
    protected void spyRequest(final String str) {
        final SpyConnectionInterface spy = mSpy;
        if (spy != null) {
            Message msg = new Message() {
                public void execute() {
                    try {
                        spy.request(str + '\n');
                    }
                    catch(Exception ex) {
                        mSpy = null;
                        mConfig.getLogger().error("BaseFtpConnection.spyPrint()", ex);
                    }
                }
            };
            mConfig.getMessageQueue().add(msg);
        }
    }

    /**
     * Get user object
     */
    public FtpUser getUser() {
        return mUser;
    }

    /**
     * Get connection spy object
     */
    public SpyConnectionInterface getSpyObject() {
        return mSpy;
    }

    /**
     * Set spy object
     */
    public void setSpyObject(SpyConnectionInterface spy) {
        mWriter.setSpyObject(spy);
        mSpy = spy;
    }

    /**
     * Get observer
     */
    public FtpConnectionObserver getObserver() {
        return mObserver;
    }

    /**
     * Set observer
     */
    public void setObserver(FtpConnectionObserver obsr) {
        mObserver = obsr;
    }

    /**
     * Notify observer.
     */
    public void notifyObserver() {
       mUser.hitUser();
       final FtpUser thisUser = mUser;
       final FtpConnectionObserver obsr = mObserver;

       if (obsr != null) {
            Message msg = new Message() {
                public void execute() {
                    obsr.updateConnection(thisUser);
                }
            };
            mConfig.getMessageQueue().add(msg);
       }
    }

    /**
     * This method tracks data transfer.
     */
    public void dataTransferred(int sz) {
         notifyObserver();
    }

    /**
     * Get config object
     */
    public FtpConfig getConfig() {
        return mConfig;
    }

    /**
     * Get status object
     */
    public FtpStatus getStatus() {
        return mFtpStatus;
    }

}




