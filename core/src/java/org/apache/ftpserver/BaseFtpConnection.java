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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.Socket;

import org.apache.avalon.cornerstone.services.connection.ConnectionHandler;
import org.apache.ftpserver.ConnectionObserver;
import org.apache.ftpserver.SpyConnectionInterface;
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

    protected AbstractFtpConfig mConfig                 = null;
    protected FtpStatus mFtpStatus              = null;
    protected FtpDataConnection mDataConnection = null;
    protected UserImpl mUser                     = null;
    protected SpyConnectionInterface mSpy       = null;
    protected ConnectionObserver mObserver   = null;
    protected Socket mControlSocket             = null;
    protected FtpWriter mWriter                 = null;
    protected boolean mbStopRequest             = false;


    /**
     * Set configuration file and the control socket.
     */
    public BaseFtpConnection(AbstractFtpConfig ftpConfig) {
      mConfig = ftpConfig;
      mFtpStatus = mConfig.getStatus();
      mUser = new UserImpl();
    }

    /**
     * Server one FTP connection.
     */
    public void handleConnection(final Socket socket) {
        mControlSocket = socket;
        InetAddress clientAddress = mControlSocket.getInetAddress();
        mObserver.newRequest("Handling new request from " + clientAddress.getHostAddress());
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
                mObserver.unknownServiceException("BaseFtpConnection.service()", th);
             }
         }
         catch(Exception ex) {
             writer.write(mFtpStatus.getResponse(500, request, mUser, null));
             if (ex instanceof java.io.IOException) {
                throw (IOException)ex;
             }
             else {
                mObserver.unknownServiceException("BaseFtpConnection.service()", ex);
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
                    catch(IOException ex) {
                        mSpy = null;
                        mObserver.requestError("BaseFtpConnection.spyPrint()", ex);
                    }
                }
            };
            mConfig.getMessageQueue().add(msg);
        }
    }

    /**
     * Get user object
     */
    public UserImpl getUser() {
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
    public ConnectionObserver getObserver() {
        return mObserver;
    }

    /**
     * Set observer
     */
    public void setObserver(ConnectionObserver obsr) {
        mObserver = obsr;
    }

    /**
     * Notify observer.
     */
    public void notifyObserver() {
       mUser.hitUser();
       final User thisUser = mUser;
       final ConnectionObserver obsr = mObserver;

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
    public AbstractFtpConfig getConfig() {
        return mConfig;
    }

    /**
     * Get status object
     */
    public FtpStatus getStatus() {
        return mFtpStatus;
    }

}




