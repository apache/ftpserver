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

package org.apache.ftpserver.gui;

import org.apache.ftpserver.User;
import org.apache.ftpserver.core.UserImpl;
import org.apache.ftpserver.gui.remote.SpyConnectionAdapter;
import org.apache.ftpserver.remote.interfaces.SpyConnectionInterface;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.rmi.RemoteException;


/**
 * This panel is used to monitor user activities.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public
class SpyPanel extends JPanel implements SpyConnectionInterface {

    private JTextPane mLogTxt = null;

    private SpyConnectionAdapter mSpyAdapter = null;
    private CommonHandler mCommonHandler = null;
    private UserImpl mUser = null;

    private SimpleAttributeSet mReqAttrs = null;
    private SimpleAttributeSet mResAttrs = null;

    /**
     * Instantiate this dialog box
     */
    public SpyPanel(CommonHandler commonHandler, UserImpl user) throws RemoteException {
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
    public User getUser() {
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
        } catch (BadLocationException ex) {
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
        } catch (BadLocationException ex) {
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
        } catch (Exception ex) {
            mCommonHandler.handleException(ex);
        }
    }

}

