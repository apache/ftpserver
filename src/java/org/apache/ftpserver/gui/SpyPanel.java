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
package org.apache.ftpserver.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.rmi.RemoteException;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.apache.ftpserver.FtpUser;
import org.apache.ftpserver.gui.remote.SpyConnectionAdapter;
import org.apache.ftpserver.remote.interfaces.SpyConnectionInterface;


/**
 * This panel is used to monitor user activities.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public
class SpyPanel extends JPanel implements SpyConnectionInterface {

    private JTextPane mLogTxt    = null;

    private SpyConnectionAdapter mSpyAdapter = null;
    private CommonHandler mCommonHandler     = null;
    private FtpUser mUser                    = null;

    private SimpleAttributeSet mReqAttrs = null;
    private SimpleAttributeSet mResAttrs = null;

    /**
     * Instantiate this dialog box
     */
    public SpyPanel(CommonHandler commonHandler, FtpUser user) throws RemoteException {
        mUser = user;
        mCommonHandler = commonHandler;

        initComponents();
        mSpyAdapter = new SpyConnectionAdapter(commonHandler.getConnectionService(), mUser.getSessionId(), this);
    
        mReqAttrs = new SimpleAttributeSet();
        StyleConstants.setForeground(mReqAttrs, new Color(0xFF, 0x00, 0xFF));
        
        mResAttrs = new SimpleAttributeSet();
        StyleConstants.setForeground(mResAttrs, new Color(0x00, 0x00, 0x8B));
    }

    /**
     * Initialize the UI components
     */
    private void initComponents() throws RemoteException {
        setLayout(new BorderLayout());
        mLogTxt = new JTextPane();
        mLogTxt.setEditable(false);
        JScrollPane txtPane = new JScrollPane(mLogTxt,
                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(txtPane, BorderLayout.CENTER);
    }

    /**
     * Get user
     */
    public FtpUser getUser() {
        return mUser;
    }

     /**
     * Get connection session id.
     */
    public String getSessionId() {
        return mUser.getSessionId();
    }

    /**
     * Write server response.
     */
    public void response(String msg) {
        Document doc = mLogTxt.getDocument();
        try {
            doc.insertString(doc.getLength(), msg, mResAttrs);
        }
        catch(BadLocationException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Write user request.
     */
    public void request(String msg) {
        Document doc = mLogTxt.getDocument();
        try {
            doc.insertString(doc.getLength(), msg, mReqAttrs);
        }
        catch(BadLocationException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Clear log messages
     */
    public void clearLog() {
        mLogTxt.setText("");
    }

    /**
     * Close pane
     */
    public void close() {
        mSpyAdapter.close();
        clearLog();
    }

    /**
     * Disconnect user
     */
    public void disconnect() {
        try {
            mSpyAdapter.close();
            mCommonHandler.getConnectionService().closeConnection(mUser.getSessionId());
        }
        catch(Exception ex) {
            mCommonHandler.handleException(ex);
        }
    }

}

