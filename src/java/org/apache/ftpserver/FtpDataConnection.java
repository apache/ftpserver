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

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;


/**
 * We can get the ftp data connection using this class.
 * It uses either PORT or PASV command.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
class FtpDataConnection {

    private FtpConfig mConfig = null;
    private Socket mDataSoc = null;
    private ServerSocket mServSoc = null;

    private InetAddress mAddress = null;
    private int          miPort   = 0;

    private boolean mbPort = false;
    private boolean mbPasv = false;


    /**
     * Constructor.
     * @param cfg ftp config object.
     */
    public FtpDataConnection(FtpConfig cfg) {
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
            catch(Exception ex) {
                mConfig.getLogger().warn("FtpDataConnection.closeDataSocket()", ex);
            }
            mDataSoc = null;
        }

        // close server socket if any
        if(mServSoc != null) {
            try {
                mServSoc.close();
            }
            catch(Exception ex) {
                mConfig.getLogger().warn("FtpDataConnection.closeDataSocket()", ex);
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
                throw new Exception("No available port found for PASV connection.");
            }
            mServSoc = new ServerSocket(port, 1, mConfig.getSelfAddress());
            mAddress = mConfig.getServerAddress();
            miPort = mServSoc.getLocalPort();

            // set different state variables
            mbPort = false;
            mbPasv = true;
            bRet = true;
        }
        catch(Exception ex) {
            mServSoc = null;
            mConfig.getLogger().warn("FtpDataConnection.setPasvCommand()", ex);
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
        catch(Exception ex) {
            mConfig.getLogger().warn("FtpDataConnection.getDataSocket()", ex);
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

