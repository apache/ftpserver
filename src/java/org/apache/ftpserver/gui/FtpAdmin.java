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

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.rmi.Naming;
import java.rmi.registry.Registry;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.apache.ftpserver.FtpUser;
import org.apache.ftpserver.remote.interfaces.ConnectionServiceInterface;
import org.apache.ftpserver.remote.interfaces.FtpConfigInterface;
import org.apache.ftpserver.remote.interfaces.FtpStatisticsInterface;
import org.apache.ftpserver.remote.interfaces.IpRestrictorInterface;
import org.apache.ftpserver.remote.interfaces.RemoteHandlerInterface;
import org.apache.ftpserver.remote.interfaces.UserManagerInterface;


/**
 * Ftp admin gui starting point. It look up for
 * the server object and logs in the admin user.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public
class FtpAdmin extends JFrame implements CommonHandler {

    private static final String LOGIN_IMG = "org/apache/ftpserver/gui/login.gif";
    private static final String PROCEED_IMG = "org/apache/ftpserver/gui/start.gif";
    private static final String CANCEL_IMG = "org/apache/ftpserver/gui/stop.gif";


    private String mstSessionId            = null;
    private JTextField mjHostTxt           = null;
    private JTextField mjPortTxt           = null;
    private JTextField mjAdminTxt          = null;
    private JPasswordField mjPasswordTxt   = null;

    private FtpAdminFrame mAdminFrame      = null;

    private RemoteHandlerInterface mRemote         = null;
    private FtpConfigInterface mConfig             = null;
    private FtpStatisticsInterface mStat           = null;
    private ConnectionServiceInterface mConService = null;
    private IpRestrictorInterface mIpRestrictor    = null;
    private UserManagerInterface mUserManager      = null;


    /**
     * Consructor - initialize components and display
     * the login dialog box.
     */
    public FtpAdmin() {
        initComponents();
        pack();
        setResizable(false);
        setTitle("Login");
        GuiUtils.setLocation(this);
        show();
    }

    /**
     * Iniialize all the swing components.
     */
    public void initComponents() {
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(0, 0, 5, 5);
        gc.gridwidth = 1;
        int yIndex = -1;
        getContentPane().setLayout(new GridBagLayout());


        // Host name
        JLabel jHostLab = new JLabel("Host");
        jHostLab.setHorizontalAlignment(JLabel.RIGHT);
        jHostLab.setForeground(Color.black);
        gc.gridx = 0;
        gc.gridy = ++yIndex;
        gc.anchor = GridBagConstraints.EAST;
        getContentPane().add(jHostLab, gc);

        mjHostTxt = new JTextField("localhost");
        mjHostTxt.setColumns(15);
        gc.gridx = 1;
        gc.gridy = yIndex;
        gc.anchor = GridBagConstraints.WEST;
        getContentPane().add(mjHostTxt, gc);


        // Port number
        JLabel jPortLab = new JLabel("Port");
        jPortLab.setHorizontalAlignment(JLabel.RIGHT);
        jPortLab.setForeground(Color.black);
        gc.gridx = 0;
        gc.gridy = ++yIndex;
        gc.anchor = GridBagConstraints.EAST;
        getContentPane().add(jPortLab, gc);

        mjPortTxt = new JTextField(String.valueOf(Registry.REGISTRY_PORT));
        mjPortTxt.setColumns(5);
        gc.gridx = 1;
        gc.gridy = yIndex;
        gc.anchor = GridBagConstraints.WEST;
        getContentPane().add(mjPortTxt, gc);


        // Admin login id
        JLabel jAdminLab = new JLabel("Admin Id");
        jAdminLab.setHorizontalAlignment(JLabel.RIGHT);
        jAdminLab.setForeground(Color.black);
        gc.gridx = 0;
        gc.gridy = ++yIndex;
        gc.anchor = GridBagConstraints.EAST;
        getContentPane().add(jAdminLab, gc);

        mjAdminTxt = new JTextField();
        mjAdminTxt.setColumns(15);
        gc.gridx = 1;
        gc.gridy = yIndex;
        gc.anchor = GridBagConstraints.WEST;
        getContentPane().add(mjAdminTxt, gc);


        // Admin password
        JLabel jPasswordLab = new JLabel("Admin Password");
        jPasswordLab.setHorizontalAlignment(JLabel.RIGHT);
        jPasswordLab.setForeground(Color.black);
        gc.gridx = 0;
        gc.gridy = ++yIndex;
        gc.anchor = GridBagConstraints.EAST;
        getContentPane().add(jPasswordLab, gc);

        mjPasswordTxt = new JPasswordField();
        mjPasswordTxt.setColumns(15);
        gc = new GridBagConstraints();
        gc.gridx = 1;
        gc.gridy = yIndex;
        gc.anchor = GridBagConstraints.WEST;
        getContentPane().add(mjPasswordTxt, gc);


        // button panel
        JPanel btnPane = new JPanel(new FlowLayout(FlowLayout.CENTER));

        JButton jLoginBtn = new JButton("Login", GuiUtils.createImageIcon(PROCEED_IMG));
        jLoginBtn.setSelected(true);
        jLoginBtn.setDefaultCapable(true);
        btnPane.add(jLoginBtn);

        JButton jCancelBtn = new JButton("Cancel", GuiUtils.createImageIcon(CANCEL_IMG));
        btnPane.add(jCancelBtn);

        gc.gridx = 0;
        gc.gridy = ++yIndex;
        gc.gridwidth = 2;
        gc.anchor = GridBagConstraints.CENTER;
        getContentPane().add(btnPane, gc);


        // event listeners
        jLoginBtn.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                login();
             }
        });

        jCancelBtn.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                terminate();
             }
        });


        // set login icon
        ImageIcon loginIcon = GuiUtils.createImageIcon(LOGIN_IMG);
        if (loginIcon != null) {
            setIconImage(loginIcon.getImage());
        }
    }

    /*
     * Handle window closing event.
     */
    protected void processWindowEvent(WindowEvent e) {
        int id = e.getID();
        if (id == WindowEvent.WINDOW_CLOSING) {
            terminate();
        }
        else {
            super.processWindowEvent(e);
        }
    }


    /**
     * Login and get remote object
     */
    private void login() {
        try {
            String host = mjHostTxt.getText();
            String port = mjPortTxt.getText();
            String login = mjAdminTxt.getText();
            String password = new String(mjPasswordTxt.getPassword());

            mjHostTxt.setText("localhost");
            mjPortTxt.setText(String.valueOf(Registry.REGISTRY_PORT));
            mjAdminTxt.setText("");
            mjPasswordTxt.setText("");

            String url = "rmi://" + host + ":" + port + "/" + RemoteHandlerInterface.BIND_NAME;
            mRemote = (RemoteHandlerInterface)Naming.lookup(url);
            mstSessionId = mRemote.login(login, password);

            mConfig       = mRemote.getConfigInterface(mstSessionId);
            mStat         = mConfig.getStatistics();
            mConService   = mConfig.getConnectionService();
            mIpRestrictor = mConfig.getIpRestrictor();
            mUserManager  = mConfig.getUserManager();

            if (mAdminFrame != null) {
                mAdminFrame.close();
                mAdminFrame = null;
            }
            mAdminFrame = new FtpAdminFrame(this);

            setVisible(false);
            mAdminFrame.show();
        }
        catch(Exception ex) {
            handleException(ex);
        }
    }

    /**
     * Handle exception
     */
    public void handleException(Exception ex) {
        //ex.printStackTrace();
        GuiUtils.showErrorMessage(getTopFrame(), ex.getMessage());
        if (ex instanceof java.rmi.RemoteException) {
            logout();
            setVisible(true);
        }
    }

    /**
     * Terminate application
     */
    public void terminate() {
        logout();
        dispose();
        System.exit(0);
    }


    /**
     * Get admin session id
     */
    public String getSessionId() {
        return mstSessionId;
    }


    /**
     * Get server interface
     */
    public RemoteHandlerInterface getRemoteHandler() {
        return mRemote;
    }


    /**
     * Get ftp server configuration
     */
    public FtpConfigInterface getConfig() {
        return mConfig;
    }

    /**
     * Get statistics interface
     */
    public FtpStatisticsInterface getStatistics() {
        return mStat;
    }

    /**
     * Get connection service
     */
    public ConnectionServiceInterface getConnectionService() {
        return mConService;
    }

    /**
     * Get IP restrictor
     */
    public IpRestrictorInterface getIpRestrictor() {
        return mIpRestrictor;
    }

    /**
     * Get user manager
     */
    public UserManagerInterface getUserManager() {
        return mUserManager;
    }

    /**
     * Get user object from the session id
     */
    public FtpUser getUser(String sessionId) {
        try {
            return mConService.getUser(sessionId);
        }
        catch(Exception ex) {
            handleException(ex);
        }
        return null;
    }

    /**
     * Logout admin user
     */
    private void logout() {
        if (mAdminFrame != null) {
            mAdminFrame.close();
            mAdminFrame = null;
        }
        if (mRemote != null) {
            try {
                mRemote.logout(mstSessionId);
            }
            catch(Exception ex) {
            }
            mRemote = null;
        }
        mstSessionId = null;
    }


    /**
     * Get top component of this application
     */
    public Component getTopFrame() {
        Component topComp = this;
        if (mAdminFrame != null) {
            topComp = mAdminFrame;
        }

        return topComp;
    }

    ////////////////////////////////////////////////////////////////////////
    ///////////////////////  Program Starting Point  ///////////////////////
    ////////////////////////////////////////////////////////////////////////
    /**
     * Remote admin starting point.
     */
    public static void main(String args[]) {
        new FtpAdmin();
    }

}
