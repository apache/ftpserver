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

import org.apache.ftpserver.interfaces.FtpDataConnectionMonitor;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.activity.Disposable;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;


/**
 * We can get the ftp data connection using this class.
 * It uses either PORT or PASV command.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
class FtpDataConnection

        {

    private AbstractFtpConfig mConfig = null;
    private Socket mDataSoc = null;
    private ServerSocket mServSoc = null;

    private InetAddress mAddress = null;
    private int          miPort   = 0;

    private boolean mbPort = false;
    private boolean mbPasv = false;
    private FtpDataConnectionMonitor ftpDataConnectionMonitor;


    /**
     * Constructor.
     * @param cfg ftp config object.
     */
    public FtpDataConnection(AbstractFtpConfig cfg) {
        mConfig = cfg;
    }


    /**
     * Close data socket.
     */
    public void closeDataSocket() {

        // close client socket if any
        if(mDataSoc != null) {
            try {
                mDataSoc.close();
            }
            catch(IOException ex) {
                ftpDataConnectionMonitor.socketCloseException("FtpDataConnection.closeDataSocket()", ex);
            }
            mDataSoc = null;
        }

        // close server socket if any
        if(mServSoc != null) {
            try {
                mServSoc.close();
            }
            catch(IOException ex) {
                ftpDataConnectionMonitor.socketCloseException("FtpDataConnection.closeDataSocket()", ex);
            }
            mConfig.releaseDataPort(miPort);
            mServSoc = null;
        }
    }


    /**
     * Port command.
     */
    public void setPortCommand(InetAddress addr, int port) {

        // close old sockets if any
        closeDataSocket();

        // set variables
        mbPort = true;
        mbPasv = false;
        mAddress = addr;
        miPort = port;
    }


    /**
     * Passive command. It returns the success flag.
     */
    public boolean setPasvCommand() {
        boolean bRet = false;

        // close old sockets if any
        closeDataSocket();

        try {

            // open passive server socket and get parameters
            int port = getPassivePort();
            if(port == -1) {
                throw new IOException("No available port found for PASV connection.");
            }
            mServSoc = new ServerSocket(port, 1, mConfig.getSelfAddress());
            mAddress = mConfig.getServerAddress();
            miPort = mServSoc.getLocalPort();

            // set different state variables
            mbPort = false;
            mbPasv = true;
            bRet = true;
        }
        catch(IOException ex) {
            mServSoc = null;
            ftpDataConnectionMonitor.serverSocketOpenException("FtpDataConnection.setPasvCommand()", ex);
        }
        return bRet;
    }


    /**
     * Get client address.
     */
    public InetAddress getInetAddress() {
        return mAddress;
    }


    /**
     * Get port number.
     */
    public int getPort() {
        return miPort;
    }


    /**
     * Get the data socket. In case of error returns null.
     */
    public Socket getDataSocket() {

        try {

            // get socket depending on the selection
            if(mbPort) {
                mDataSoc = new Socket(mAddress, miPort);
            }
            else if(mbPasv) {
                mDataSoc = mServSoc.accept();
            }
        }
        catch(IOException ex) {
            ftpDataConnectionMonitor.socketException("FtpDataConnection.getDataSocket()", ex);
            mDataSoc = null;
        }

        return mDataSoc;
    }


    /**
     * Get the passive port. Get it from the port pool.
     */
    private int getPassivePort() {
        return mConfig.getDataPort();
    }

    /**
     * Dispose data connection
     */
    public void dispose() {
        closeDataSocket();
    }

}

