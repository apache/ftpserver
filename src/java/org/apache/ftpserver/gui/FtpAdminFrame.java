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
import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.rmi.RemoteException;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import org.apache.ftpserver.remote.interfaces.RemoteHandlerInterface;


/**
 * Ftp server admin user interface.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public
class FtpAdminFrame extends JFrame implements TreeSelectionListener {

    private static final ImageIcon ICON_IMG = GuiUtils.createImageIcon("org/apache/ftpserver/gui/server.gif");

    private JTabbedPane mjTabPane;

    private FtpTree mjFtpTree   = null;
    private JPanel mjFtpPane    = null;

    private CommonHandler mCommonHandler = null;

    /**
     * Creates new form MyServerFrame
     */
    public FtpAdminFrame(CommonHandler commonHandler) throws RemoteException {
        mCommonHandler = commonHandler;
        initComponents();
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     */
    private void initComponents() throws RemoteException {
        mjTabPane = new JTabbedPane();

        // top level
        getContentPane().setLayout(new BorderLayout());
        JSplitPane jSplitPane = new JSplitPane();
        jSplitPane.setDividerSize(2);

        // left pane
        mjFtpTree = new FtpTree(mCommonHandler);
        mjFtpTree.addTreeSelectionListener(this);
        JScrollPane custPane = new JScrollPane(mjFtpTree,
                                               JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                               JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        jSplitPane.setLeftComponent(custPane);

        // right pane
        mjFtpPane = new JPanel();
        mjFtpPane.setLayout(new BorderLayout());
        jSplitPane.setRightComponent(mjFtpPane);

        mjFtpPane.add(mjFtpTree.getRootPanel());

        jSplitPane.setDividerLocation(100);
        mjTabPane.addTab(RemoteHandlerInterface.DISPLAY_NAME, jSplitPane);

        getContentPane().add(mjTabPane, BorderLayout.CENTER);

        pack();
        setTitle("Ftp Server");
        if (ICON_IMG != null) {
            setIconImage(ICON_IMG.getImage());
        }
        setSize(new Dimension(610, 450));
        GuiUtils.setLocation(this);
    }

    /*
     * Handle window closing event.
     */
    protected void processWindowEvent(WindowEvent e) {
        int id = e.getID();
        if (id == WindowEvent.WINDOW_CLOSING) {
            if ( !GuiUtils.getConfirmation(mCommonHandler.getTopFrame(), "Do you really want to exit?") ) {
                return;
            }
            mCommonHandler.terminate();
        }
        else {
            super.processWindowEvent(e);
        }
    }

    /**
     * Handle tree selection
     */
    public void valueChanged(TreeSelectionEvent e) {
        JPanel dispPane = mjFtpTree.getSelectedPanel();
        if(dispPane != null) {
            GuiUtils.showNewPanel(mjFtpPane, dispPane);
        }
    }

    /**
     * Release resources
     */
    public void close() {
        setVisible(false);
        mjFtpTree.close();
        dispose();
    }

}
