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
import java.io.RandomAccessFile;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.Writer;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.net.Socket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

import org.apache.ftpserver.util.IoUtils;
import org.apache.ftpserver.util.StreamConnector;

/**
 * This class handles each ftp connection. Here all the ftp command
 * methods take two arguments - a ftp request and a writer object.
 * This is the main backbone of the ftp server.
 * <br>
 * The ftp command method signature is:
 * <code>public void doXYZ(FtpRequest request, FtpWriter out) throws IOException</code>.
 * <br>
 * Here <code>XYZ</code> is the capitalized ftp command.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public
class FtpConnection extends BaseFtpConnection {

    // as SimpleDateFormat is not thread-safe we have to use ThreadLocal
    private final static ThreadLocal DATE_FMT = new ThreadLocal()  {
        protected Object initialValue() {
            return new SimpleDateFormat("yyyyMMddHHmmss.SSS");
        }
    };

    // command state specific temporary variables
    private boolean mbReset = false;
    private long mlSkipLen = 0;

    private boolean mbRenFr = false;
    private String mstRenFr = null;

    private boolean mbUser = false;
    private boolean mbPass = false;

    /**
     * Set configuration file and the control socket.
     */
    public FtpConnection(FtpConfig cfg) {
        super(cfg);
    }

    /**
     * Check the user permission to execute this command.
     */
    protected boolean hasPermission(FtpRequest request) {
        String cmd = request.getCommand();
        return mUser.hasLoggedIn() ||
                cmd.equals("USER") ||
                cmd.equals("PASS") ||
                cmd.equals("AUTH") ||
                cmd.equals("HELP") ||
                cmd.equals("SYST");
    }

    /**
     * Reset temporary state variables.
     */
    private void resetState() {
        mbRenFr = false;
        mstRenFr = null;

        mbReset = false;
        mlSkipLen = 0;

        mbUser = false;
        mbPass = false;
    }

    ////////////////////////////////////////////////////////////
    /////////////////   all the FTP handlers   /////////////////
    ////////////////////////////////////////////////////////////
    /**
     * <code>ABOR &lt;CRLF&gt;</code><br>
     *
     * This command tells the server to abort the previous FTP
     * service command and any associated transfer of data.
     * No action is to be taken if the previous command
     * has been completed (including data transfer).  The control
     * connection is not to be closed by the server, but the data
     * connection must be closed.
     * Current implementation does not do anything. As here data
     * transfers are not multi-threaded.
     */
    public void doABOR(FtpRequest request, FtpWriter out) throws IOException {

        // reset state variables
        resetState();

        // and abort any data connection
        mDataConnection.closeDataSocket();
        out.write(mFtpStatus.getResponse(225, request, mUser, null));
    }


    /**
     * <code>APPE &lt;SP&gt; &lt;pathname&gt; &lt;CRLF&gt;</code><br>
     *
     * This command causes the server-DTP to accept the data
     * transferred via the data connection and to store the data in
     * a file at the server site.  If the file specified in the
     * pathname exists at the server site, then the data shall be
     * appended to that file; otherwise the file specified in the
     * pathname shall be created at the server site.
     */
    public void doAPPE(FtpRequest request, FtpWriter out) throws IOException {

        InputStream is = null;
        OutputStream os = null;
        String[] args = null;

        try {

            // reset state variables
            resetState();

            // argument check
            if(!request.hasArgument()) {
                out.write(mFtpStatus.getResponse(501, request, mUser, null));
                return ;
            }

            // get filenames
            String fileName = request.getArgument();
            fileName = mUser.getVirtualDirectory().getAbsoluteName(fileName);
            String physicalName = mUser.getVirtualDirectory().getPhysicalName(fileName);
            File requestedFile = new File(physicalName);
             args = new String[] {fileName};

            // check file existance
            if(!(requestedFile.exists() && requestedFile.isFile())) {
                out.write(mFtpStatus.getResponse(550, request, mUser, args));
                return ;
            }

            // check permission
            if(!mUser.getVirtualDirectory().hasWritePermission(physicalName, true)) {
                out.write(mFtpStatus.getResponse(450, request, mUser, args));
                return ;
            }

            // now transfer file data
            out.write(mFtpStatus.getResponse(150, request, mUser, args));
            Socket dataSoc = mDataConnection.getDataSocket();
            if(dataSoc == null) {
                out.write(mFtpStatus.getResponse(550, request, mUser, args));
                return ;
            }

            // go to the end of the file
            is = dataSoc.getInputStream();
            RandomAccessFile raf = new RandomAccessFile(requestedFile, "rw");
            raf.seek(raf.length());
            os = mUser.getOutputStream(new FileOutputStream(raf.getFD()));

            // receive data from client
            StreamConnector msc = new StreamConnector(is, os);
            msc.setMaxTransferRate(mUser.getMaxUploadRate());
            msc.setObserver(this);
            msc.connect();

            if(msc.hasException()) {
                out.write(mFtpStatus.getResponse(451, request, mUser, args));
            }
            else {
                mConfig.getStatistics().setUpload(requestedFile, mUser, msc.getTransferredSize());
            }

            out.write(mFtpStatus.getResponse(226, request, mUser, args));
        }
        catch(IOException ex) {
            out.write(mFtpStatus.getResponse(425, request, mUser, args));
        }
        finally {
            IoUtils.close(is);
            IoUtils.close(os);
            mDataConnection.closeDataSocket();
        }
    }


    /**
     * <code>CDUP &lt;CRLF&gt;</code><br>
     *
     * This command is a special case of CWD, and is included to
     * simplify the implementation of programs for transferring
     * directory trees between operating systems having different
     * syntaxes for naming the parent directory.  The reply codes
     * shall be identical to the reply codes of CWD.
     */
    public void doCDUP(FtpRequest request, FtpWriter out) throws IOException {

        // reset state variables
        resetState();

        // change directory
        if(mUser.getVirtualDirectory().changeDirectory("..")) {
            String args[] = {mUser.getVirtualDirectory().getCurrentDirectory()};
            out.write(mFtpStatus.getResponse(250, request, mUser, args));
        }
        else {
            out.write(mFtpStatus.getResponse(550, request, mUser, null));
        }
    }


    /**
     * <code>CWD  &lt;SP&gt; &lt;pathname&gt; &lt;CRLF&gt;</code><br>
     *
     * This command allows the user to work with a different
     * directory for file storage or retrieval without
     * altering his login or accounting information.  Transfer
     * parameters are similarly unchanged.  The argument is a
     * pathname specifying a directory.
     */
    public void doCWD(FtpRequest request, FtpWriter out) throws IOException {

        // reset state variables
        resetState();

        // get new directory name
        String dirName = "/";
        if(request.hasArgument()) {
            dirName = request.getArgument();
        }

        // change directory
        if(mUser.getVirtualDirectory().changeDirectory(dirName)) {
            String args[] = {mUser.getVirtualDirectory().getCurrentDirectory()};
            out.write(mFtpStatus.getResponse(250, request, mUser, args));
        }
        else {
            out.write(mFtpStatus.getResponse(550, request, mUser, null));
        }
    }


    /**
     * <code>DELE &lt;SP&gt; &lt;pathname&gt; &lt;CRLF&gt;</code><br>
     *
     * This command causes the file specified in the pathname to be
     * deleted at the server site.
     */
    public void doDELE(FtpRequest request, FtpWriter out) throws IOException {

        // reset state variables
        resetState();

        // argument check
        if(!request.hasArgument()) {
            out.write(mFtpStatus.getResponse(501, request, mUser, null));
            return ;
        }

        // get filenames
        String fileName = request.getArgument();
        fileName = mUser.getVirtualDirectory().getAbsoluteName(fileName);
        String physicalName = mUser.getVirtualDirectory().getPhysicalName(fileName);
        File requestedFile = new File(physicalName);
        String[] args = {fileName};

        // check file existance
        if( !(requestedFile.exists() && requestedFile.isFile()) ) {
            out.write(mFtpStatus.getResponse(550, request, mUser, args));
            return;
        }
        
        // check permission
        if(!mUser.getVirtualDirectory().hasWritePermission(physicalName, true)) {
            out.write(mFtpStatus.getResponse(450, request, mUser, args));
            return ;
        }

        // now delete
        if(requestedFile.delete()) {
            out.write(mFtpStatus.getResponse(250, request, mUser, args));
            mConfig.getStatistics().setDelete(requestedFile, mUser);
        }
        else {
            out.write(mFtpStatus.getResponse(450, request, mUser, args));
        }
    }


    /**
     * <code>HELP [&lt;SP&gt; <string>] &lt;CRLF&gt;</code><br>
     *
     * This command shall cause the server to send helpful
     * information regarding its implementation status over the
     * control connection to the user.  The command may take an
     * argument (e.g., any command name) and return more specific
     * information as a response.
     */
    public void doHELP(FtpRequest request, FtpWriter out) throws IOException {
        resetState();

        // print global help
        if(!request.hasArgument()) {
            out.write(mFtpStatus.getResponse(214, null, mUser, null));
            return ;
        }

        // print command specific help
        String ftpCmd = request.getArgument().toUpperCase();
        String args[] = null;
        FtpRequest tempRequest = new FtpRequest(ftpCmd);
        out.write(mFtpStatus.getResponse(214, tempRequest, mUser, args));
        return ;
    }


    /**
     * <code>LIST [&lt;SP&gt; &lt;pathname&gt;] &lt;CRLF&gt;</code><br>
     *
     * This command causes a list to be sent from the server to the
     * passive DTP.  If the pathname specifies a directory or other
     * group of files, the server should transfer a list of files
     * in the specified directory.  If the pathname specifies a
     * file then the server should send current information on the
     * file.  A null argument implies the user's current working or
     * default directory.  The data transfer is over the data
     * connection
     */
    public void doLIST(FtpRequest request, FtpWriter out) throws IOException {

        Writer os = null;
        try {

            // reset state variables
            resetState();

            // get data connection
            out.write(mFtpStatus.getResponse(150, request, mUser, null));
            Socket dataSoc = mDataConnection.getDataSocket();
            if(dataSoc == null) {
                out.write(mFtpStatus.getResponse(550, request, mUser, null));
                return ;
            }

            // transfer listing data
            os = new OutputStreamWriter(dataSoc.getOutputStream());
            if(!mUser.getVirtualDirectory().printList(request.getArgument(), os)) {
                out.write(mFtpStatus.getResponse(501, request, mUser, null));
            }
            else {
                out.write(mFtpStatus.getResponse(226, request, mUser, null));
            }
        }
        catch(IOException ex) {
            out.write(mFtpStatus.getResponse(425, request, mUser, null));
        }
        finally {
            IoUtils.close(os);
            mDataConnection.closeDataSocket();
        }
    }


    /**
     * <code>MDTM &lt;SP&gt; &lt;pathname&gt; &lt;CRLF&gt;</code><br>
     *
     * Returns the date and time of when a file was modified.
     */
    public void doMDTM(FtpRequest request, FtpWriter out) throws IOException {

        // reset state
        resetState();

        // argument check
        if(!request.hasArgument()) {
            out.write(mFtpStatus.getResponse(501, request, mUser, null));
            return ;
        }

        // get filenames
        String fileName = request.getArgument();
        fileName = mUser.getVirtualDirectory().getAbsoluteName(fileName);
        String physicalName = mUser.getVirtualDirectory().getPhysicalName(fileName);
        File reqFile = new File(physicalName);

        // now print date
        if(reqFile.exists()) {
            SimpleDateFormat fmt = (SimpleDateFormat)DATE_FMT.get();
             String args[] = {fmt.format(new Date(reqFile.lastModified()))};
            out.write(mFtpStatus.getResponse(213, request, mUser, args));
        }
        else {
            out.write(mFtpStatus.getResponse(550, request, mUser, null));
        }
    }


    /**
     * <code>MKD  &lt;SP&gt; &lt;pathname&gt; &lt;CRLF&gt;</code><br>
     *
     * This command causes the directory specified in the pathname
     * to be created as a directory (if the pathname is absolute)
     * or as a subdirectory of the current working directory (if
     * the pathname is relative).
     */
    public void doMKD(FtpRequest request, FtpWriter out) throws IOException {

        // reset state
        resetState();

        // argument check
        if(!request.hasArgument()) {
            out.write(mFtpStatus.getResponse(501, request, mUser, null));
            return ;
        }

        // get filename
        String fileName = request.getArgument();
        fileName = mUser.getVirtualDirectory().getAbsoluteName(fileName);
        String physicalName = mUser.getVirtualDirectory().getPhysicalName(fileName);
        String args[] = {fileName};

        // check permission
        if(!mUser.getVirtualDirectory().hasCreatePermission(physicalName, true)) {
            out.write(mFtpStatus.getResponse(450, request, mUser, args));
            return ;
        }

        // now create directory
        if(new File(physicalName).mkdirs()) {
            out.write(mFtpStatus.getResponse(250, request, mUser, args));
        }
        else {
            out.write(mFtpStatus.getResponse(450, request, mUser, args));
        }
    }


    /**
     * <code>MODE &lt;SP&gt; <mode-code> &lt;CRLF&gt;</code><br>
     *
     * The argument is a single Telnet character code specifying
     * the data transfer modes described in the Section on
     * Transmission Modes.
     */
    public void doMODE(FtpRequest request, FtpWriter out) throws IOException {

        // reset state
        resetState();

        // argument check
        if(!request.hasArgument()) {
            out.write(mFtpStatus.getResponse(501, request, mUser, null));
            return ;
        }

        // set mode
        if(mUser.setMode(request.getArgument().charAt(0))) {
            out.write(mFtpStatus.getResponse(200, request, mUser, null));
        }
        else {
            out.write(mFtpStatus.getResponse(504, request, mUser, null));
        }
    }


    /**
     * <code>NLST [&lt;SP&gt; &lt;pathname&gt;] &lt;CRLF&gt;</code><br>
     *
     * This command causes a directory listing to be sent from
     * server to user site.  The pathname should specify a
     * directory or other system-specific file group descriptor; a
     * null argument implies the current directory.  The server
     * will return a stream of names of files and no other
     * information.
     */
    public void doNLST(FtpRequest request, FtpWriter out) throws IOException {

        Writer os = null;
        try {

            // reset state
            resetState();

            // get data connection
            out.write(mFtpStatus.getResponse(150, request, mUser, null));
            Socket dataSoc = mDataConnection.getDataSocket();
            if(dataSoc == null) {
                out.write(mFtpStatus.getResponse(550, request, mUser, null));
                return ;
            }

            // print listing data
            os = new OutputStreamWriter(dataSoc.getOutputStream());
            if(!mUser.getVirtualDirectory().printNList(request.getArgument(), os)) {
                out.write(mFtpStatus.getResponse(501, request, mUser, null));
            }
            else {
                out.write(mFtpStatus.getResponse(226, request, mUser, null));
            }
        }
        catch(IOException ex) {
            out.write(mFtpStatus.getResponse(425, request, mUser, null));
        }
        finally {
            IoUtils.close(os);
            mDataConnection.closeDataSocket();
        }
    }


    /**
     * <code>NOOP &lt;CRLF&gt;</code><br>
     *
     * This command does not affect any parameters or previously
     * entered commands. It specifies no action other than that the
     * server send an OK reply.
     */
    public void doNOOP(FtpRequest request, FtpWriter out) throws IOException {
        resetState();
        out.write(mFtpStatus.getResponse(200, request, mUser, null));
    }


    /**
     * <code>PASS &lt;SP&gt; <password> &lt;CRLF&gt;</code><br>
     *
     * The argument field is a Telnet string specifying the user's
     * password.  This command must be immediately preceded by the
     * user name command.
     */
    public void doPASS(FtpRequest request, FtpWriter out) throws IOException {

        // set state variables
        if(!mbUser) {
            out.write(mFtpStatus.getResponse(500, request, mUser, null));
            resetState();
            return ;
        }
        resetState();
        mbPass = true;

        // set user password and login
        String pass = request.hasArgument() ? request.getArgument() : "";
        mUser.setPassword(pass);

        // login failure - close connection
         String args[] = {mUser.getName()};
        if(mConfig.getConnectionService().login(mUser)) {
            out.write(mFtpStatus.getResponse(230, request, mUser, args));
        }
        else {
            out.write(mFtpStatus.getResponse(530, request, mUser, args));
            ConnectionService conService = mConfig.getConnectionService();
            if(conService != null) {
                conService.closeConnection(mUser.getSessionId());
            }
        }
    }


    /**
     * <code>PASV &lt;CRLF&gt;</code><br>
     *
     * This command requests the server-DTP to "listen" on a data
     * port (which is not its default data port) and to wait for a
     * connection rather than initiate one upon receipt of a
     * transfer command.  The response to this command includes the
     * host and port address this server is listening on.
     */
    public void doPASV(FtpRequest request, FtpWriter out) throws IOException {

        // reset state
        resetState();

        // set data connection
        if(!mDataConnection.setPasvCommand()) {
            out.write(mFtpStatus.getResponse(550, request, mUser, null));
            return ;
        }

        // get connection info
        InetAddress servAddr = mDataConnection.getInetAddress();
        if(servAddr == null) {
            servAddr = mConfig.getSelfAddress();
        }

        int servPort = mDataConnection.getPort();

        // send connection info to client
        String addrStr = servAddr.getHostAddress().replace('.', ',') + ',' + (servPort >> 8) + ',' + (servPort & 0xFF);
         String[] args = {addrStr};
        out.write(mFtpStatus.getResponse(227, request, mUser, args));
    }


    /**
     * <code>PORT &lt;SP&gt; <host-port> &lt;CRLF&gt;</code><br>
     *
     * The argument is a HOST-PORT specification for the data port
     * to be used in data connection.  There are defaults for both
     * the user and server data ports, and under normal
     * circumstances this command and its reply are not needed.  If
     * this command is used, the argument is the concatenation of a
     * 32-bit internet host address and a 16-bit TCP port address.
     * This address information is broken into 8-bit fields and the
     * value of each field is transmitted as a decimal number (in
     * character string representation).  The fields are separated
     * by commas.  A port command would be:
     *
     *   PORT h1,h2,h3,h4,p1,p2
     *
     * where h1 is the high order 8 bits of the internet host address.
     */
    public void doPORT(FtpRequest request, FtpWriter out) throws IOException {

        // reset state variables
        resetState();

        // argument check
        if(!request.hasArgument()) {
            out.write(mFtpStatus.getResponse(501, request, mUser, null));
            return ;
        }

        StringTokenizer st = new StringTokenizer(request.getArgument(), ",");
        if(st.countTokens() != 6) {
            out.write(mFtpStatus.getResponse(510, request, mUser, null));
            return ;
        }

        // get data server
         String dataSrvName = st.nextToken() + '.' + st.nextToken() + '.' +
                              st.nextToken() + '.' + st.nextToken();
        InetAddress clientAddr = null;
        try {
            clientAddr = InetAddress.getByName(dataSrvName);
        }
        catch(UnknownHostException ex) {
            out.write(mFtpStatus.getResponse(553, request, mUser, null));
            return ;
        }

        // get data server port
        int clientPort = 0;
        try {
            int hi = Integer.parseInt(st.nextToken());
            int lo = Integer.parseInt(st.nextToken());
            clientPort = (hi << 8) | lo;
        }
        catch(NumberFormatException ex) {
            out.write(mFtpStatus.getResponse(552, request, mUser, null));
            return ;
        }

        mDataConnection.setPortCommand(clientAddr, clientPort);
        out.write(mFtpStatus.getResponse(200, request, mUser, null));
    }


    /**
     * <code>PWD  &lt;CRLF&gt;</code><br>
     *
     * This command causes the name of the current working
     * directory to be returned in the reply.
     */
    public void doPWD(FtpRequest request, FtpWriter out) throws IOException {
        resetState();
         String args[] = {mUser.getVirtualDirectory().getCurrentDirectory()};
        out.write(mFtpStatus.getResponse(257, request, mUser, args));
    }


    /**
     * <code>QUIT &lt;CRLF&gt;</code><br>
     *
     * This command terminates a USER and if file transfer is not
     * in progress, the server closes the control connection.
     */
    public void doQUIT(FtpRequest request, FtpWriter out) throws IOException {
        resetState();
        out.write(mFtpStatus.getResponse(221, request, mUser, null));
        ConnectionService conService = mConfig.getConnectionService();
        if(conService != null) {
            conService.closeConnection(mUser.getSessionId());
        }
    }


    /**
     * <code>REST &lt;SP&gt; <marker> &lt;CRLF&gt;</code><br>
     *
     * The argument field represents the server marker at which
     * file transfer is to be restarted.  This command does not
     * cause file transfer but skips over the file to the specified
     * data checkpoint.  This command shall be immediately followed
     * by the appropriate FTP service command which shall cause
     * file transfer to resume.
     */
    public void doREST(FtpRequest request, FtpWriter out) throws IOException {

        // argument check
        if(!request.hasArgument()) {
            out.write(mFtpStatus.getResponse(501, request, mUser, null));
            return ;
        }

        resetState();
        mlSkipLen = 0;
        String skipNum = request.getArgument();
        try {
            mlSkipLen = Long.parseLong(skipNum);
        }
        catch(NumberFormatException ex) {
            out.write(mFtpStatus.getResponse(501, request, mUser, null));
            return ;
        }
        if(mlSkipLen < 0) {
            mlSkipLen = 0;
            out.write(mFtpStatus.getResponse(501, request, mUser, null));
            return ;
        }
        mbReset = true;
        out.write(mFtpStatus.getResponse(350, request, mUser, null));
    }


    /**
     * <code>RETR &lt;SP&gt; &lt;pathname&gt; &lt;CRLF&gt;</code><br>
     *
     * This command causes the server-DTP to transfer a copy of the
     * file, specified in the pathname, to the server- or user-DTP
     * at the other end of the data connection.  The status and
     * contents of the file at the server site shall be unaffected.
     */
    public void doRETR(FtpRequest request, FtpWriter out) throws IOException {

        InputStream is = null;
        OutputStream os = null;
        try {

            // set state variables
            long skipLen = (mbReset) ? mlSkipLen : 0;
            resetState();

            // argument check
            if(!request.hasArgument()) {
                out.write(mFtpStatus.getResponse(501, request, mUser, null));
                return ;
            }

            // get filename
            String fileName = request.getArgument();
            fileName = mUser.getVirtualDirectory().getAbsoluteName(fileName);
            String physicalName = mUser.getVirtualDirectory().getPhysicalName(fileName);
            File requestedFile = new File(physicalName);
             String args[] = {fileName};

            // check file existance
            if(!(requestedFile.exists() && requestedFile.isFile())) {
                out.write(mFtpStatus.getResponse(550, request, mUser, args));
                return ;
            }

            // check permission
            if(!mUser.getVirtualDirectory().hasReadPermission(physicalName, true)) {
                out.write(mFtpStatus.getResponse(550, request, mUser, args));
                return ;
            }

            // now transfer file data
            out.write(mFtpStatus.getResponse(150, request, mUser, null));
            Socket dataSoc = mDataConnection.getDataSocket();
            if(dataSoc == null) {
                out.write(mFtpStatus.getResponse(550, request, mUser, args));
                return ;
            }

            os = mUser.getOutputStream(dataSoc.getOutputStream());

            // move to the appropriate offset
            RandomAccessFile raf = new RandomAccessFile(requestedFile, "r");
            raf.seek(skipLen);
            is = new FileInputStream(raf.getFD());


            // send file data to client
            StreamConnector msc = new StreamConnector(is, os);
            msc.setMaxTransferRate(mUser.getMaxDownloadRate());
            msc.setObserver(this);
            msc.connect();

            if(msc.hasException()) {
                out.write(mFtpStatus.getResponse(451, request, mUser, args));
                return ;
            }
            else {
                mConfig.getStatistics().setDownload(requestedFile, mUser, msc.getTransferredSize());
            }

            out.write(mFtpStatus.getResponse(226, request, mUser, null));
        }
        catch(IOException ex) {
            out.write(mFtpStatus.getResponse(425, request, mUser, null));
        }
        finally {
            IoUtils.close(is);
            IoUtils.close(os);
            mDataConnection.closeDataSocket();
        }
    }


    /**
     * <code>RMD  &lt;SP&gt; &lt;pathname&gt; &lt;CRLF&gt;</code><br>
     *
     * This command causes the directory specified in the pathname
     * to be removed as a directory (if the pathname is absolute)
     * or as a subdirectory of the current working directory (if
     * the pathname is relative).
     */
    public void doRMD(FtpRequest request, FtpWriter out) throws IOException {

        // reset state variables
        resetState();

        // argument check
        if(!request.hasArgument()) {
            out.write(mFtpStatus.getResponse(501, request, mUser, null));
            return ;
        }

        // get file names
        String fileName = request.getArgument();
        fileName = mUser.getVirtualDirectory().getAbsoluteName(fileName);
        String physicalName = mUser.getVirtualDirectory().getPhysicalName(fileName);
        File requestedFile = new File(physicalName);
        String args[] = {fileName};

        // check permission
        if(!mUser.getVirtualDirectory().hasWritePermission(physicalName, true)) {
            out.write(mFtpStatus.getResponse(450, request, mUser, args));
            return ;
        }

        // now delete
        if(requestedFile.delete()) {
            out.write(mFtpStatus.getResponse(250, request, mUser, args));
        }
        else {
            out.write(mFtpStatus.getResponse(450, request, mUser, args));
        }
    }


    /**
     * <code>RNFR &lt;SP&gt; &lt;pathname&gt; &lt;CRLF&gt;</code><br>
     *
     * This command specifies the old pathname of the file which is
     * to be renamed.  This command must be immediately followed by
     * a "rename to" command specifying the new file pathname.
     */
    public void doRNFR(FtpRequest request, FtpWriter out) throws IOException {

        // reset state variable
        resetState();

        // argument check
        if(!request.hasArgument()) {
            out.write(mFtpStatus.getResponse(501, request, mUser, null));
            return ;
        }

        // set state variable
        mbRenFr = true;

        // get filename
        String fileName = request.getArgument();
        fileName = mUser.getVirtualDirectory().getAbsoluteName(fileName);
        mstRenFr = mUser.getVirtualDirectory().getPhysicalName(fileName);
         String args[] = {fileName};

        out.write(mFtpStatus.getResponse(350, request, mUser, args));
    }


    /**
     * <code>RNTO &lt;SP&gt; &lt;pathname&gt; &lt;CRLF&gt;</code><br>
     *
     * This command specifies the new pathname of the file
     * specified in the immediately preceding "rename from"
     * command.  Together the two commands cause a file to be
     * renamed.
     */
    public void doRNTO(FtpRequest request, FtpWriter out) throws IOException {

        // argument check
        if(!request.hasArgument()) {
            resetState();
            out.write(mFtpStatus.getResponse(501, request, mUser, null));
            return ;
        }

        // set state variables
        if((!mbRenFr) || (mstRenFr == null)) {
            resetState();
            out.write(mFtpStatus.getResponse(100, request, mUser, null));
            return ;
        }

        // get filenames
        String fromFileStr = mUser.getVirtualDirectory().getVirtualName(mstRenFr);
        String toFileStr = request.getArgument();
        toFileStr = mUser.getVirtualDirectory().getAbsoluteName(toFileStr);
        String physicalToFileStr = mUser.getVirtualDirectory().getPhysicalName(toFileStr);
        File fromFile = new File(mstRenFr);
        File toFile = new File(physicalToFileStr);
         String args[] = {fromFileStr, toFileStr};

        resetState();

        // check permission
        if(!mUser.getVirtualDirectory().hasCreatePermission(physicalToFileStr, true)) {
            out.write(mFtpStatus.getResponse(553, request, mUser, null));
            return ;
        }

        // now rename
        if(fromFile.renameTo(toFile)) {
            out.write(mFtpStatus.getResponse(250, request, mUser, args));
        }
        else {
            out.write(mFtpStatus.getResponse(553, request, mUser, args));
        }
    }


    /**
     * <code>SITE &lt;SP&gt; <string> &lt;CRLF&gt;</code><br>
     *
     * This command is used by the server to provide services
     * specific to his system that are essential to file transfer
     * but not sufficiently universal to be included as commands in
     * the protocol.
     */
    public void doSITE(FtpRequest request, FtpWriter out) throws IOException {
        resetState();
        SiteCommandHandler siteCmd = new SiteCommandHandler(mConfig, mUser);
        out.write(siteCmd.getResponse(request));
    }


    /**
     * <code>SIZE &lt;SP&gt; &lt;pathname&gt; &lt;CRLF&gt;</code><br>
     *
     * Returns the size of the file in bytes.
     */
    public void doSIZE(FtpRequest request, FtpWriter out) throws IOException {

        // reset state variables
        resetState();

        // argument check
        if(!request.hasArgument()) {
            out.write(mFtpStatus.getResponse(501, request, mUser, null));
            return ;
        }

        // get filenames
        String fileName = request.getArgument();
        fileName = mUser.getVirtualDirectory().getAbsoluteName(fileName);
        String physicalName = mUser.getVirtualDirectory().getPhysicalName(fileName);
        File reqFile = new File(physicalName);

        // print file size
        if(reqFile.exists()) {
             String args[] = {String.valueOf(reqFile.length())};
            out.write(mFtpStatus.getResponse(213, request, mUser, args));
        }
        else {
            out.write(mFtpStatus.getResponse(550, request, mUser, null));
        }
    }


    /**
     * <code>STAT [&lt;SP&gt; &lt;pathname&gt;] &lt;CRLF&gt;</code><br>
     *
     * This command shall cause a status response to be sent over
     * the control connection in the form of a reply.
     */
    public void doSTAT(FtpRequest request, FtpWriter out) throws IOException {

        // reset state variables
        resetState();

        // write the status info
        String args[] =  {
            mConfig.getSelfAddress().getHostAddress(),
            mControlSocket.getInetAddress().getHostAddress(),
            mUser.getName()
        };
        out.write(mFtpStatus.getResponse(211, request, mUser, args));
    }


    /**
     * <code>STOR &lt;SP&gt; &lt;pathname&gt; &lt;CRLF&gt;</code><br>
     *
     * This command causes the server-DTP to accept the data
     * transferred via the data connection and to store the data as
     * a file at the server site.  If the file specified in the
     * pathname exists at the server site, then its contents shall
     * be replaced by the data being transferred.  A new file is
     * created at the server site if the file specified in the
     * pathname does not already exist.
     */
    public void doSTOR(FtpRequest request, FtpWriter out) throws IOException {

        InputStream is = null;
        OutputStream os = null;
        try {

            // set state variables
            long skipLen = (mbReset) ? mlSkipLen : 0;
            resetState();

            // argument check
            if(!request.hasArgument()) {
                out.write(mFtpStatus.getResponse(501, request, mUser, null));
                return ;
            }

            // get filenames
            String fileName = request.getArgument();
            fileName = mUser.getVirtualDirectory().getAbsoluteName(fileName);
            String physicalName = mUser.getVirtualDirectory().getPhysicalName(fileName);
            File requestedFile = new File(physicalName);

            // get permission
            if(!mUser.getVirtualDirectory().hasCreatePermission(physicalName, true)) {
                out.write(mFtpStatus.getResponse(550, request, mUser, null));
                return ;
            }

            // now transfer file data
            out.write(mFtpStatus.getResponse(150, request, mUser, null));

            // get data connection
            Socket dataSoc = mDataConnection.getDataSocket();
            if(dataSoc == null) {
                out.write(mFtpStatus.getResponse(550, request, mUser, null));
                return ;
            }
            is = dataSoc.getInputStream();

            // go to the appropriate offset
            RandomAccessFile raf = new RandomAccessFile(requestedFile, "rw");
            raf.setLength(skipLen);
            raf.seek(skipLen);
            os = mUser.getOutputStream(new FileOutputStream(raf.getFD()));

            // get data from client
            StreamConnector msc = new StreamConnector(is, os);
            msc.setMaxTransferRate(mUser.getMaxUploadRate());
            msc.setObserver(this);
            msc.connect();
            if(msc.hasException()) {
                out.write(mFtpStatus.getResponse(451, request, mUser, null));
                return ;
            }
            else {
                mConfig.getStatistics().setUpload(requestedFile, mUser, msc.getTransferredSize());
            }

            out.write(mFtpStatus.getResponse(226, request, mUser, null));
        }
        catch(IOException ex) {
            out.write(mFtpStatus.getResponse(425, request, mUser, null));
        }
        finally {
            IoUtils.close(is);
            IoUtils.close(os);
            mDataConnection.closeDataSocket();
        }
    }


    /**
     * <code>STOU &lt;CRLF&gt;</code><br>
     *
     * This command behaves like STOR except that the resultant
     * file is to be created in the current directory under a name
     * unique to that directory.  The 250 Transfer Started response
     * must include the name generated.
     */
    public void doSTOU(FtpRequest request, FtpWriter out) throws IOException {

        InputStream is = null;
        OutputStream os = null;
        try {

            // reset state variables
            resetState();

            // get filenames
            String fileName = mUser.getVirtualDirectory().getAbsoluteName("ftp.dat");
            String physicalName = mUser.getVirtualDirectory().getPhysicalName(fileName);
            File requestedFile = new File(physicalName);
            requestedFile = IoUtils.getUniqueFile(requestedFile);
            fileName = mUser.getVirtualDirectory().getVirtualName(requestedFile.getAbsolutePath());
             String args[] = {fileName};

            // check permission
            if(!mUser.getVirtualDirectory().hasCreatePermission(fileName, false)) {
                out.write(mFtpStatus.getResponse(550, request, mUser, null));
                return ;
            }

            // now transfer file data
            out.write(mFtpStatus.getResponse(150, request, mUser, null));
            Socket dataSoc = mDataConnection.getDataSocket();
            if(dataSoc == null) {
                out.write(mFtpStatus.getResponse(550, request, mUser, args));
                return ;
            }

            // receive data from client
            is = dataSoc.getInputStream();
            os = mUser.getOutputStream(new FileOutputStream(requestedFile));

            StreamConnector msc = new StreamConnector(is, os);
            msc.setMaxTransferRate(mUser.getMaxUploadRate());
            msc.setObserver(this);
            msc.connect();
            if(msc.hasException()) {
                out.write(mFtpStatus.getResponse(451, request, mUser, null));
                return ;
            }
            else {
                mConfig.getStatistics().setUpload(requestedFile, mUser, msc.getTransferredSize());
            }

            out.write(mFtpStatus.getResponse(226, request, mUser, null));
            mDataConnection.closeDataSocket();
            out.write(mFtpStatus.getResponse(250, request, mUser, args));
        }
        catch(IOException ex) {
            out.write(mFtpStatus.getResponse(425, request, mUser, null));
        }
        finally {
            IoUtils.close(is);
            IoUtils.close(os);
            mDataConnection.closeDataSocket();
        }
    }


    /**
     * <code>STRU &lt;SP&gt; &lt;structure-code&gt; &lt;CRLF&gt;</code><br>
     *
     * The argument is a single Telnet character code specifying
     * file structure.
     */
    public void doSTRU(FtpRequest request, FtpWriter out) throws IOException {

        // reset state variables
        resetState();

        // argument check
        if(!request.hasArgument()) {
            out.write(mFtpStatus.getResponse(501, request, mUser, null));
            return ;
        }

        // set structure
        if(mUser.setStructure(request.getArgument().charAt(0))) {
            out.write(mFtpStatus.getResponse(200, request, mUser, null));
        }
        else {
            out.write(mFtpStatus.getResponse(504, request, mUser, null));
        }
    }


    /**
     * <code>SYST &lt;CRLF&gt;</code><br>
     *
     * This command is used to find out the type of operating
     * system at the server.
     */
    public void doSYST(FtpRequest request, FtpWriter out) throws IOException {

        // reset state variables
        resetState();

        // and print server system info
         String args[] = {mConfig.getSystemName()};
        out.write(mFtpStatus.getResponse(215, request, mUser, args));
    }


    /**
     * <code>TYPE &lt;SP&gt; &lt;type-code&gt; &lt;CRLF&gt;</code><br>
     *
     * The argument specifies the representation type.
     */
    public void doTYPE(FtpRequest request, FtpWriter out) throws IOException {

        // reset state variables
        resetState();

        // get type from argument
        char type = 'A';
        if(request.hasArgument()) {
            type = request.getArgument().charAt(0);
        }

        // set it
        if(mUser.setType(type)) {
            out.write(mFtpStatus.getResponse(200, request, mUser, null));
        }
        else {
            out.write(mFtpStatus.getResponse(504, request, mUser, null));
        }
    }


    /**
     * <code>USER &lt;SP&gt; &lt;username&gt; &lt;CRLF&gt;</code><br>
     *
     * The argument field is a Telnet string identifying the user.
     * The user identification is that which is required by the
     * server for access to its file system.  This command will
     * normally be the first command transmitted by the user after
     * the control connections are made.
     */
    public void doUSER(FtpRequest request, FtpWriter out) throws IOException {

        // reset state variables
        resetState();

        // argument check
        if(!request.hasArgument()) {
            out.write(mFtpStatus.getResponse(501, request, mUser, null));
            return ;
        }

        // check user login status
        mbUser = true;
        if(mUser.hasLoggedIn()) {
            if(mUser.getName().equals(request.getArgument())) {
                out.write(mFtpStatus.getResponse(230, request, mUser, null));
                return ;
            }
            else {
                mConfig.getConnectionService().closeConnection(mUser.getSessionId());
            }
        }

        // set user name and send appropriate message
        mUser.setName(request.getArgument());
        if(mUser.getIsAnonymous()) {
            if(mConfig.isAnonymousLoginAllowed()) {
                FtpRequest anoRequest = new FtpRequest(mUser.getName());
                out.write(mFtpStatus.getResponse(331, anoRequest, mUser, null));
            }
            else {
                out.write(mFtpStatus.getResponse(530, request, mUser, null));
                ConnectionService conService = mConfig.getConnectionService();
                if(conService != null) {
                    conService.closeConnection(mUser.getSessionId());
                }
            }
        }
        else {
            out.write(mFtpStatus.getResponse(331, request, mUser, null));
        }
    }

}

