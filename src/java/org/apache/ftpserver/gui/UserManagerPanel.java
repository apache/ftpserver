// $Id$
/*
 * Copyright 2004 The Apache Software Foundation
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
 */
package org.apache.ftpserver.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Iterator;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.interfaces.IFtpConfig;
import org.apache.ftpserver.usermanager.BaseUser;

/**
 * User management panel.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class UserManagerPanel extends PluginPanel implements ActionListener {

    private static final long serialVersionUID = 2496923918460548623L;

    private final static Random PASS_GEN = new Random(System.currentTimeMillis()); 
    
    private final static Object[] BYTE_RATES = {
        "No limit",
        new Integer(1200),
        new Integer(2400),
        new Integer(4800),
        new Integer(9600),
        new Integer(14400),
        new Integer(28800),    
        new Integer(57600),
        new Integer(115200)    
    };                   
    
    private final static Object[] IDLE_SECONDS = {
        "No limit",
        new Integer(60),
        new Integer(300),
        new Integer(900),
        new Integer(1800),
        new Integer(3600)    
    };
    
    private IFtpConfig m_fconfig;                  
    
    private JComboBox m_userLst;
    private JTextField m_nameTxt;
    
    private JPasswordField m_passwordTxt;
    private JPasswordField m_retypePasswordTxt;
    private JCheckBox m_passwordChkBox;
    
    private JTextField m_directoryTxt;
    private JCheckBox m_enabledChkBox;
    private JCheckBox m_writeChkBox;
    private JComboBox m_idleLst;
    private JComboBox m_uploadLst;
    private JComboBox m_downloadLst;
    
    /**
     * Constructor - create all UI components.
     */
    public UserManagerPanel(PluginPanelContainer container) {
        super(container);
        initComponents();
    }
    
    /**
     * Initial all UI components.
     */
    private void initComponents() {
        
        setLayout(new BorderLayout());
        
        JPanel topPanel = new JPanel(new GridBagLayout()); 
        topPanel.setBorder(BorderFactory.createEtchedBorder());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 0, 0, 5);
        add(topPanel, BorderLayout.CENTER);
        int yindex = -1;        

        // user list
        m_userLst = new JComboBox();
        m_userLst.addActionListener(this);
        m_userLst.setPreferredSize(new Dimension(120, 22));
        gc.gridx = 0;
        gc.gridy = ++yindex;
        gc.gridwidth = 3;
        gc.anchor = GridBagConstraints.CENTER;
        topPanel.add(m_userLst, gc);
        
        // user name
        JLabel nameLab = new JLabel("Name :: ");
        nameLab.setHorizontalAlignment(JLabel.RIGHT);
        nameLab.setForeground(Color.black);
        gc.gridx = 0;
        gc.gridy = ++yindex;
        gc.gridwidth = 1;
        gc.anchor = GridBagConstraints.EAST;
        topPanel.add(nameLab, gc);
         
        m_nameTxt = new JTextField();
        m_nameTxt.setColumns(12);
        gc.gridx = 1;
        gc.gridy = yindex;
        gc.gridwidth = 2;
        gc.anchor = GridBagConstraints.WEST;
        topPanel.add(m_nameTxt, gc);
        
        // password
        JLabel passwordLab = new JLabel("Password :: ");
        passwordLab.setHorizontalAlignment(JLabel.RIGHT);
        passwordLab.setForeground(Color.black);
        gc.gridx = 0;
        gc.gridy = ++yindex;
        gc.gridwidth = 1;
        gc.anchor = GridBagConstraints.EAST;
        topPanel.add(passwordLab, gc);        
        
        m_passwordTxt = new JPasswordField();
        m_passwordTxt.setColumns(12);
        gc.gridx = 1;
        gc.gridy = yindex;
        gc.gridwidth = 2;
        gc.anchor = GridBagConstraints.WEST;
        topPanel.add(m_passwordTxt, gc);

        JButton generatePassBtn = new JButton("Generate");
        generatePassBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                generatePassword();
            }
         });
        generatePassBtn.setPreferredSize(new Dimension(90, 22));
        gc.gridx = 2;
        gc.gridy = yindex;
        gc.gridwidth = 1;
        gc.anchor = GridBagConstraints.WEST;
        topPanel.add(generatePassBtn, gc);
        
        // retype password
        JLabel retypePasswordLab = new JLabel("Retype Password :: ");
        retypePasswordLab.setHorizontalAlignment(JLabel.RIGHT);
        retypePasswordLab.setForeground(Color.black);
        gc.gridx = 0;
        gc.gridy = ++yindex;
        gc.gridwidth = 1;
        gc.anchor = GridBagConstraints.EAST;
        topPanel.add(retypePasswordLab, gc);
        
        m_retypePasswordTxt = new JPasswordField();
        m_retypePasswordTxt.setColumns(12);
        gc.gridx = 1;
        gc.gridy = yindex;
        gc.gridwidth = 2;
        gc.anchor = GridBagConstraints.WEST;
        topPanel.add(m_retypePasswordTxt, gc);
        
        // set password
        JLabel setPasswordLab = new JLabel("Set Password :: ");
        setPasswordLab.setHorizontalAlignment(JLabel.RIGHT);
        setPasswordLab.setForeground(Color.black);
        gc.gridx = 0;
        gc.gridy = ++yindex;
        gc.gridwidth = 1;
        gc.anchor = GridBagConstraints.EAST;
        topPanel.add(setPasswordLab, gc);
        
        m_passwordChkBox = new JCheckBox();
        m_passwordChkBox.setHorizontalTextPosition(SwingConstants.LEFT);
        gc.gridx = 1;
        gc.gridy = yindex;
        gc.gridwidth = 2;
        gc.anchor = GridBagConstraints.WEST;
        topPanel.add(m_passwordChkBox, gc);

        // root directory
        JLabel directoryLab = new JLabel("Root Directory :: ");
        directoryLab.setHorizontalAlignment(JLabel.RIGHT);
        directoryLab.setForeground(Color.black);
        gc.gridx = 0;
        gc.gridy = ++yindex;
        gc.gridwidth = 1;
        gc.anchor = GridBagConstraints.EAST;
        topPanel.add(directoryLab, gc);
        
        m_directoryTxt = new JTextField("./res/home");
        m_directoryTxt.setColumns(12);
        gc.gridx = 1;
        gc.gridy = yindex;
        gc.gridwidth = 1;
        gc.anchor = GridBagConstraints.WEST;
        topPanel.add(m_directoryTxt, gc); 
        
        JButton directoryBtn = new JButton("Browse");
        directoryBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                String dir = DirChooser.showDialog(UserManagerPanel.this, "Select User Home", null);
                if(dir != null) {
                    m_directoryTxt.setText(dir);
                }
            }
         });
        directoryBtn.setPreferredSize(new Dimension(90, 22));
        gc.gridx = 2;
        gc.gridy = yindex;
        gc.gridwidth = 1;
        gc.anchor = GridBagConstraints.WEST;
        topPanel.add(directoryBtn, gc);
        
        // enable/disable
        JLabel enabledLab = new JLabel("Enabled :: ");
        enabledLab.setHorizontalAlignment(JLabel.RIGHT);
        enabledLab.setForeground(Color.black);
        gc.gridx = 0;
        gc.gridy = ++yindex;
        gc.gridwidth = 1;
        gc.anchor = GridBagConstraints.EAST;
        topPanel.add(enabledLab, gc);

        m_enabledChkBox = new JCheckBox();
        gc.gridx = 1;
        gc.gridy = yindex;
        gc.gridwidth = 2;
        gc.anchor = GridBagConstraints.WEST;
        topPanel.add(m_enabledChkBox, gc);
        
        // write permission
        JLabel writePermLab = new JLabel("Write Permission :: ");
        writePermLab.setHorizontalAlignment(JLabel.RIGHT);
        writePermLab.setForeground(Color.black);
        gc.gridx = 0;
        gc.gridy = ++yindex;
        gc.gridwidth = 1;
        gc.anchor = GridBagConstraints.EAST;
        topPanel.add(writePermLab, gc);
        
        m_writeChkBox = new JCheckBox();
        gc.gridx = 1;
        gc.gridy = yindex;
        gc.gridwidth = 2;
        gc.anchor = GridBagConstraints.WEST;
        topPanel.add(m_writeChkBox, gc);
                
        // idle time
        JLabel idleLab = new JLabel("Max. Idle Time (seconds) :: ");
        idleLab.setHorizontalAlignment(JLabel.RIGHT);
        idleLab.setForeground(Color.black);
        gc.gridx = 0;
        gc.gridy = ++yindex;
        gc.gridwidth = 1;
        gc.anchor = GridBagConstraints.EAST;
        topPanel.add(idleLab, gc);
        
        m_idleLst = new JComboBox(IDLE_SECONDS);
        m_idleLst.setPreferredSize(new Dimension(130, 22));
        m_idleLst.setEditable(true);
        gc.gridx = 1;
        gc.gridy = yindex;
        gc.gridwidth = 2;
        gc.anchor = GridBagConstraints.WEST;
        topPanel.add(m_idleLst, gc);
        
        // user upload limit
        JLabel uploadLab = new JLabel("Max. Upload (bytes/sec) :: ");
        uploadLab.setHorizontalAlignment(JLabel.RIGHT);
        uploadLab.setForeground(Color.black);
        gc.gridx = 0;
        gc.gridy = ++yindex;
        gc.gridwidth = 1;
        gc.anchor = GridBagConstraints.EAST;
        topPanel.add(uploadLab, gc);
        
        m_uploadLst = new JComboBox(BYTE_RATES);
        m_uploadLst.setPreferredSize(new Dimension(130, 22));
        m_uploadLst.setEditable(true);
        gc.gridx = 1;
        gc.gridy = yindex;
        gc.gridwidth = 2;
        gc.anchor = GridBagConstraints.WEST;
        topPanel.add(m_uploadLst, gc);
        
        // user download limit
        JLabel jDownloadLab = new JLabel("Max. Download (bytes/sec) :: ");
        jDownloadLab.setHorizontalAlignment(JLabel.RIGHT);
        jDownloadLab.setForeground(Color.black);
        gc.gridx = 0;
        gc.gridy = ++yindex;
        gc.gridwidth = 1;
        gc.anchor = GridBagConstraints.EAST;
        topPanel.add(jDownloadLab, gc);
        
        m_downloadLst = new JComboBox(BYTE_RATES);
        m_downloadLst.setPreferredSize(new Dimension(130, 22));
        m_downloadLst.setEditable(true);
        gc.gridx = 1;
        gc.gridy = yindex;
        gc.gridwidth = 2;
        gc.anchor = GridBagConstraints.WEST;
        topPanel.add(m_downloadLst, gc);
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        add(btnPanel, BorderLayout.SOUTH);
        
        // save user
        JButton saveBtn = new JButton("Save");
        saveBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                save();
            }
        });
        btnPanel.add(saveBtn);
        
        // delete user
        JButton deleteBtn = new JButton("Delete");
        deleteBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                delete();
            }
        });
        btnPanel.add(deleteBtn);
        
        // reload user data
        JButton reloadBtn = new JButton("Reload");
        reloadBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                refresh(m_fconfig);
            }
        });
        btnPanel.add(reloadBtn);
    }
    
    /**
     * Generate random password.
     */
    private void generatePassword() {
        StringBuffer sb = new StringBuffer(8);
        for(int i=0; i<8; i++) {
            int charType = PASS_GEN.nextInt(3);
            switch (charType) {
            
                // number
                case 0:
                    sb.append( (char)('0' + PASS_GEN.nextInt(10)) );
                    break;
                
                // uppercase character    
                case 1:    
                    sb.append( (char)('A' + PASS_GEN.nextInt(26)) );
                    break;
                    
                // lowercase character
                case 2:
                    sb.append( (char)('a' + PASS_GEN.nextInt(26)) );
                    break;
            }
        }
        String password = sb.toString();
        int option = JOptionPane.showConfirmDialog(this, 
                                                   "Generated password: " + password, 
                                                   "Password Generation", 
                                                   JOptionPane.OK_CANCEL_OPTION,
                                                   JOptionPane.INFORMATION_MESSAGE);
        if(option == JOptionPane.OK_OPTION) {
            m_passwordTxt.setText(password);
            m_retypePasswordTxt.setText(password);
            m_passwordChkBox.setSelected(true);
        }
    }

    
    /**
     * Refresh the panel - set the ftp config.
     */
    public void refresh(IFtpConfig config) {
        m_fconfig = config;
        m_userLst.removeAllItems();
        if(m_fconfig == null) {
            return;
        }
        
        UserManager userManager = m_fconfig.getUserManager();
        try {
            Iterator userIt = userManager.getAllUserNames().iterator();
            boolean hasUser = false;
            while(userIt.hasNext()) {
                hasUser = true;
                m_userLst.addItem(userIt.next());
            }
            if(hasUser) {
                m_userLst.setSelectedIndex(0);
            }
        }
        catch(Exception ex) {
            ex.printStackTrace();
            GuiUtils.showErrorMessage(this, "Cannot load user list.");
        }
    }
    
    /**
     * List selection changed. 
     */
    public void actionPerformed(ActionEvent e) {
        Object selVal = m_userLst.getSelectedItem();
        try {      
            if(selVal != null) {
                String userName = selVal.toString();
                User user = m_fconfig.getUserManager().getUserByName(userName);
                if (user == null) {
                    GuiUtils.showErrorMessage(this, userName + " : does not exist.");
                    refresh(m_fconfig);
                }
                else {
                    
                    // populate UI components
                    m_nameTxt.setText(user.getName());
                    m_passwordTxt.setText("");
                    m_retypePasswordTxt.setText("");
                    m_passwordChkBox.setSelected(false);
                    m_directoryTxt.setText(user.getHomeDirectory());
                    m_enabledChkBox.setSelected(user.getEnabled());
                    m_writeChkBox.setSelected(user.getWritePermission());
                    setIdleTimeCombo(m_idleLst, user.getMaxIdleTime());
                    setByteRateCombo(m_uploadLst, user.getMaxUploadRate());
                    setByteRateCombo(m_downloadLst, user.getMaxDownloadRate());
                }
            }
        }
        catch(FtpException ex) {
            GuiUtils.showErrorMessage(this, "Cannot fetch user information : " + selVal);
        }
    } 

        
    /**
     * Save user.
     */
    private void save() {
        // check user name field
        String userName = m_nameTxt.getText().trim();
        if(userName.equals("")) {
            GuiUtils.showErrorMessage(this, "Please enter an user name");
            return;
        }
        
        try {
            BaseUser user = new BaseUser();
            user.setName(userName);
            if(setPassword(user)) {
                user.setHomeDirectory(new File(m_directoryTxt.getText()).getCanonicalPath());
                user.setEnabled(m_enabledChkBox.isSelected());
                user.setWritePermission(m_writeChkBox.isSelected());
                user.setMaxIdleTime(getMaxIdleTime(m_uploadLst));
                user.setMaxUploadRate(getBytesTransferRate(m_uploadLst));
                user.setMaxDownloadRate(getBytesTransferRate(m_downloadLst));
                m_fconfig.getUserManager().save(user);
                refresh(m_fconfig);
                GuiUtils.showInformationMessage(this, "Saved user - " + user.getName());
            }
        }
        catch(Exception ex) {
            GuiUtils.showErrorMessage(this, ex.getMessage());
        }
    }
    
    
    /**
     * Set password if necessary.
     */
    private boolean setPassword(BaseUser usr) throws FtpException {
        
        String userName = usr.getName();
        boolean bNewUser = !m_fconfig.getUserManager().doesExist(userName);
        boolean bPassSet = m_passwordChkBox.isSelected();
        String password = new String(m_passwordTxt.getPassword());
        String repassword = new String(m_retypePasswordTxt.getPassword()); 
        boolean bAnonymous = userName.equals("anonymous");
        
        // new user
        if( bNewUser && (!bPassSet) && (!bAnonymous) ) {
            GuiUtils.showErrorMessage(this, "New user - password required");
            return false;
        }
        
        // password set 
        if( bPassSet && (!password.equals(repassword)) && (!bAnonymous) ) {
            GuiUtils.showErrorMessage(this, "Password entries are not equal");
            return false;
        }
        
        // set password if necessary
        if(bPassSet && (!bAnonymous)) {
            usr.setPassword(password);
        }
        else {
            usr.setPassword(null);
        }
        return true;
    } 

    /**
     * Get max bytes/sec.
     */
    private int getBytesTransferRate(JComboBox combo) {
        int rate = 0;
        Object selObj = combo.getSelectedItem();
        if (!selObj.equals(BYTE_RATES[0])) {
            try {
                rate = Integer.parseInt(selObj.toString());
            }
            catch(NumberFormatException ex) {
                GuiUtils.showErrorMessage(this, ex.getMessage());
            }
        }
        
        return rate;
    }
     
    /**
     * Get max idle time in sec.
     */
    private int getMaxIdleTime(JComboBox combo) {
        int sec = 0;
        Object selObj = combo.getSelectedItem();
        if (!selObj.equals(IDLE_SECONDS[0])) {
            try {
                sec = Integer.parseInt(selObj.toString());
            }
            catch(NumberFormatException ex) {
                GuiUtils.showErrorMessage(this, ex.getMessage());
            }
        }
        
        return sec;
    }    
    
    /**
     * Set byte transfer rate combo.
     */
    private void setByteRateCombo(JComboBox combo, int rate) {
        Object selItem = new Integer(rate);
        if (rate == 0) {
            selItem = BYTE_RATES[0];
        }
        combo.setSelectedItem(selItem);
    }
    
    /**
     * Set idle time combo.
     */
    private void setIdleTimeCombo(JComboBox combo, int idle) {
        Object selItem = new Integer(idle);
        if (idle == 0){
            selItem = IDLE_SECONDS[0];
        }
        combo.setSelectedItem(selItem);
    }

    
    /**
     * Delete user.
     */
    private void delete() {
        Object selVal = m_userLst.getSelectedItem();
        if(selVal == null) {
            return;
        }
        
        String userName = selVal.toString();
        if(!GuiUtils.getConfirmation(this, "Do you really want to delete user " + userName + "?")) {
            return;
        }
        
        try {
            m_fconfig.getUserManager().delete(userName);
            refresh(m_fconfig);
            GuiUtils.showInformationMessage(this, "Deleted user - " + userName);
        }
        catch(Exception ex) {
            GuiUtils.showErrorMessage(this, "User delete error : " + userName);
        }
    }
    
    
    /**
     * Can this panel be displayed.
     */
    public boolean canBeDisplayed() {
        return (m_fconfig != null);
    }
    
    
    /**
     * String representation of this panel.
     */
    public String toString() {
        return "Users";
    }
}
