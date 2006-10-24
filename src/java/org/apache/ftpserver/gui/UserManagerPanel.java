/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
    
    private final static Object[] MAX_LOGIN_NUMBER = {
      "No limit",
      new Integer(5),
      new Integer(10),
      new Integer(20),
      new Integer(40)
    };
    
    private final static Object[] MAX_LOGIN_PER_IP = {
      "No limit",
      new Integer(1),
      new Integer(2),
      new Integer(4),
      new Integer(8)
    };
    
    private IFtpConfig fconfig;                  
    
    private JComboBox userLst;
    private JTextField nameTxt;
    
    private JPasswordField passwordTxt;
    private JPasswordField retypePasswordTxt;
    private JCheckBox passwordChkBox;
    
    private JTextField directoryTxt;
    private JCheckBox enabledChkBox;
    private JCheckBox writeChkBox;
    private JComboBox loginNumberLst;
    private JComboBox loginPerIPLst;
    private JComboBox idleLst;
    private JComboBox uploadLst;
    private JComboBox downloadLst;
    
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
        userLst = new JComboBox(new String[]{"         "});
        userLst.addActionListener(this);
        userLst.setMinimumSize(new Dimension(160, 22));
        userLst.setPreferredSize(new Dimension(160, 22));
        gc.gridx = 0;
        gc.gridy = ++yindex;
        gc.gridwidth = 3;
        gc.anchor = GridBagConstraints.CENTER;
        topPanel.add(userLst, gc);
        
        // user name
        JLabel nameLab = new JLabel("Name :: ");
        nameLab.setHorizontalAlignment(JLabel.RIGHT);
        nameLab.setForeground(Color.black);
        gc.gridx = 0;
        gc.gridy = ++yindex;
        gc.gridwidth = 1;
        gc.anchor = GridBagConstraints.EAST;
        topPanel.add(nameLab, gc);
         
        nameTxt = new JTextField();
        nameTxt.setColumns(12);
        nameTxt.setPreferredSize(new Dimension(120, 22));
        gc.gridx = 1;
        gc.gridy = yindex;
        gc.gridwidth = 2;
        gc.anchor = GridBagConstraints.WEST;
        topPanel.add(nameTxt, gc);
        
        // password
        JLabel passwordLab = new JLabel("Password :: ");
        passwordLab.setHorizontalAlignment(JLabel.RIGHT);
        passwordLab.setForeground(Color.black);
        gc.gridx = 0;
        gc.gridy = ++yindex;
        gc.gridwidth = 1;
        gc.anchor = GridBagConstraints.EAST;
        topPanel.add(passwordLab, gc);        
        
        passwordTxt = new JPasswordField();
        passwordTxt.setColumns(12);
        passwordTxt.setPreferredSize(new Dimension(120, 22));
        gc.gridx = 1;
        gc.gridy = yindex;
        gc.gridwidth = 2;
        gc.anchor = GridBagConstraints.WEST;
        topPanel.add(passwordTxt, gc);

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
        
        retypePasswordTxt = new JPasswordField();
        retypePasswordTxt.setColumns(12);
        retypePasswordTxt.setPreferredSize(new Dimension(120, 22));
        gc.gridx = 1;
        gc.gridy = yindex;
        gc.gridwidth = 2;
        gc.anchor = GridBagConstraints.WEST;
        topPanel.add(retypePasswordTxt, gc);
        
        // set password
        JLabel setPasswordLab = new JLabel("Set Password :: ");
        setPasswordLab.setHorizontalAlignment(JLabel.RIGHT);
        setPasswordLab.setForeground(Color.black);
        gc.gridx = 0;
        gc.gridy = ++yindex;
        gc.gridwidth = 1;
        gc.anchor = GridBagConstraints.EAST;
        topPanel.add(setPasswordLab, gc);
        
        passwordChkBox = new JCheckBox();
        passwordChkBox.setHorizontalTextPosition(SwingConstants.LEFT);
        gc.gridx = 1;
        gc.gridy = yindex;
        gc.gridwidth = 2;
        gc.anchor = GridBagConstraints.WEST;
        topPanel.add(passwordChkBox, gc);

        // root directory
        JLabel directoryLab = new JLabel("Root Directory :: ");
        directoryLab.setHorizontalAlignment(JLabel.RIGHT);
        directoryLab.setForeground(Color.black);
        gc.gridx = 0;
        gc.gridy = ++yindex;
        gc.gridwidth = 1;
        gc.anchor = GridBagConstraints.EAST;
        topPanel.add(directoryLab, gc);
        
        directoryTxt = new JTextField("./res/home");
        directoryTxt.setColumns(12);
        directoryTxt.setPreferredSize(new Dimension(120, 22));
        gc.gridx = 1;
        gc.gridy = yindex;
        gc.gridwidth = 1;
        gc.anchor = GridBagConstraints.WEST;
        topPanel.add(directoryTxt, gc); 
        
        JButton directoryBtn = new JButton("Browse");
        directoryBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                String dir = DirChooser.showDialog(UserManagerPanel.this, "Select User Home", null);
                if(dir != null) {
                    directoryTxt.setText(dir);
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

        enabledChkBox = new JCheckBox();
        gc.gridx = 1;
        gc.gridy = yindex;
        gc.gridwidth = 2;
        gc.anchor = GridBagConstraints.WEST;
        topPanel.add(enabledChkBox, gc);
        
        // write permission
        JLabel writePermLab = new JLabel("Write Permission :: ");
        writePermLab.setHorizontalAlignment(JLabel.RIGHT);
        writePermLab.setForeground(Color.black);
        gc.gridx = 0;
        gc.gridy = ++yindex;
        gc.gridwidth = 1;
        gc.anchor = GridBagConstraints.EAST;
        topPanel.add(writePermLab, gc);
        
        writeChkBox = new JCheckBox();
        gc.gridx = 1;
        gc.gridy = yindex;
        gc.gridwidth = 2;
        gc.anchor = GridBagConstraints.WEST;
        topPanel.add(writeChkBox, gc);
            
        //max login number
        JLabel loginNumLab = new JLabel("Max. Login Number :: ");
        loginNumLab.setHorizontalAlignment(JLabel.RIGHT);
        loginNumLab.setForeground(Color.black);
        gc.gridx = 0;
        gc.gridy = ++yindex;
        gc.gridwidth = 1;
        gc.anchor = GridBagConstraints.EAST;
        topPanel.add(loginNumLab, gc);
        
        loginNumberLst = new JComboBox(MAX_LOGIN_NUMBER);
        loginNumberLst.setPreferredSize(new Dimension(130, 22));
        loginNumberLst.setEditable(true);
        gc.gridx = 1;
        gc.gridy = yindex;
        gc.gridwidth = 2;
        gc.anchor = GridBagConstraints.WEST;
        topPanel.add(loginNumberLst, gc);
        
        //max login number per IP
        JLabel loginPerIPLab = new JLabel("Max. Login from Same IP :: ");
        loginPerIPLab.setHorizontalAlignment(JLabel.RIGHT);
        loginPerIPLab.setForeground(Color.black);
        gc.gridx = 0;
        gc.gridy = ++yindex;
        gc.gridwidth = 1;
        gc.anchor = GridBagConstraints.EAST;
        topPanel.add(loginPerIPLab, gc);
        
        loginPerIPLst = new JComboBox(MAX_LOGIN_PER_IP);
        loginPerIPLst.setPreferredSize(new Dimension(130, 22));
        loginPerIPLst.setEditable(true);
        gc.gridx = 1;
        gc.gridy = yindex;
        gc.gridwidth = 2;
        gc.anchor = GridBagConstraints.WEST;
        topPanel.add(loginPerIPLst, gc);
        
        // idle time
        JLabel idleLab = new JLabel("Max. Idle Time (seconds) :: ");
        idleLab.setHorizontalAlignment(JLabel.RIGHT);
        idleLab.setForeground(Color.black);
        gc.gridx = 0;
        gc.gridy = ++yindex;
        gc.gridwidth = 1;
        gc.anchor = GridBagConstraints.EAST;
        topPanel.add(idleLab, gc);
        
        idleLst = new JComboBox(IDLE_SECONDS);
        idleLst.setPreferredSize(new Dimension(130, 22));
        idleLst.setEditable(true);
        gc.gridx = 1;
        gc.gridy = yindex;
        gc.gridwidth = 2;
        gc.anchor = GridBagConstraints.WEST;
        topPanel.add(idleLst, gc);
        
        // user upload limit
        JLabel uploadLab = new JLabel("Max. Upload (bytes/sec) :: ");
        uploadLab.setHorizontalAlignment(JLabel.RIGHT);
        uploadLab.setForeground(Color.black);
        gc.gridx = 0;
        gc.gridy = ++yindex;
        gc.gridwidth = 1;
        gc.anchor = GridBagConstraints.EAST;
        topPanel.add(uploadLab, gc);
        
        uploadLst = new JComboBox(BYTE_RATES);
        uploadLst.setPreferredSize(new Dimension(130, 22));
        uploadLst.setEditable(true);
        gc.gridx = 1;
        gc.gridy = yindex;
        gc.gridwidth = 2;
        gc.anchor = GridBagConstraints.WEST;
        topPanel.add(uploadLst, gc);
        
        // user download limit
        JLabel jDownloadLab = new JLabel("Max. Download (bytes/sec) :: ");
        jDownloadLab.setHorizontalAlignment(JLabel.RIGHT);
        jDownloadLab.setForeground(Color.black);
        gc.gridx = 0;
        gc.gridy = ++yindex;
        gc.gridwidth = 1;
        gc.anchor = GridBagConstraints.EAST;
        topPanel.add(jDownloadLab, gc);
        
        downloadLst = new JComboBox(BYTE_RATES);
        downloadLst.setPreferredSize(new Dimension(130, 22));
        downloadLst.setEditable(true);
        gc.gridx = 1;
        gc.gridy = yindex;
        gc.gridwidth = 2;
        gc.anchor = GridBagConstraints.WEST;
        topPanel.add(downloadLst, gc);
        
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
        JButton addBtn = new JButton("Add");
        addBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                add();
            }
        });
        btnPanel.add(addBtn);
        
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
                refresh(fconfig);
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
            passwordTxt.setText(password);
            retypePasswordTxt.setText(password);
            passwordChkBox.setSelected(true);
        }
    }

    
    /**
     * Refresh the panel - set the ftp config.
     */
    public void refresh(IFtpConfig config) {
        fconfig = config;
        userLst.removeAllItems();
        if(fconfig == null) {
            return;
        }
        
        UserManager userManager = fconfig.getUserManager();
        try {
            String[] userNames = userManager.getAllUserNames();
            
            for (int i = 0; i < userNames.length; i++) {
                userLst.addItem(userNames[i]);
            }

            if(userNames.length > 0) {
                userLst.setSelectedIndex(0);
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
        Object selVal = userLst.getSelectedItem();
        try {      
            if(selVal != null) {
                String userName = selVal.toString();
                User user = fconfig.getUserManager().getUserByName(userName);
                if (user == null) {
                    GuiUtils.showErrorMessage(this, userName + " : does not exist.");
                    refresh(fconfig);
                }
                else {
                    
                    // populate UI components
                    nameTxt.setText(user.getName());
                    passwordTxt.setText("");
                    retypePasswordTxt.setText("");
                    passwordChkBox.setSelected(false);
                    directoryTxt.setText(user.getHomeDirectory());
                    enabledChkBox.setSelected(user.getEnabled());
                    writeChkBox.setSelected(user.getWritePermission());
                    setLoginNumberCombo(loginNumberLst, user.getMaxLoginNumber());
                    setLoginPerIPCombo(loginPerIPLst, user.getMaxLoginPerIP());
                    setIdleTimeCombo(idleLst, user.getMaxIdleTime());
                    setByteRateCombo(uploadLst, user.getMaxUploadRate());
                    setByteRateCombo(downloadLst, user.getMaxDownloadRate());
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
        String userName = nameTxt.getText().trim();
        if(userName.equals("")) {
            GuiUtils.showErrorMessage(this, "Please enter an user name");
            return;
        }
        
        try {
            BaseUser user = new BaseUser();
            user.setName(userName);
            if(setPassword(user)) {
                user.setHomeDirectory(directoryTxt.getText());
                user.setEnabled(enabledChkBox.isSelected());
                user.setWritePermission(writeChkBox.isSelected());
                user.setMaxLoginNumber(getMaxLoginNumber(loginNumberLst));
                user.setMaxLoginPerIP(getMaxLoginPerIP(loginPerIPLst));
                user.setMaxIdleTime(getMaxIdleTime(idleLst));
                user.setMaxUploadRate(getBytesTransferRate(uploadLst));
                user.setMaxDownloadRate(getBytesTransferRate(downloadLst));
                fconfig.getUserManager().save(user);
                refresh(fconfig);
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
        boolean bNewUser = !fconfig.getUserManager().doesExist(userName);
        boolean bPassSet = passwordChkBox.isSelected();
        String password = new String(passwordTxt.getPassword());
        String repassword = new String(retypePasswordTxt.getPassword()); 
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
     * Get Max Login Number
     */
    private int getMaxLoginNumber(JComboBox combo) {
      int maxLoginNumber = 0;
      Object selObj = combo.getSelectedItem();
        if (!selObj.equals(MAX_LOGIN_NUMBER[0])) {
            try {
                maxLoginNumber = Integer.parseInt(selObj.toString());
            }
            catch(NumberFormatException ex) {
                GuiUtils.showErrorMessage(this, ex.getMessage());
            }
        }
        
        return maxLoginNumber;
    }
    
    /**
     * Get Max Login Number per IP
     */
    private int getMaxLoginPerIP(JComboBox combo) {
      int maxLoginPerIP= 0;
      Object selObj = combo.getSelectedItem();
        if (!selObj.equals(MAX_LOGIN_PER_IP[0])) {
            try {
                maxLoginPerIP = Integer.parseInt(selObj.toString());
            }
            catch(NumberFormatException ex) {
                GuiUtils.showErrorMessage(this, ex.getMessage());
            }
        }
        
        return maxLoginPerIP;
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
     * Set login number combo
     */
    private void setLoginNumberCombo(JComboBox combo, int loginNumber) {
        Object selItem = new Integer(loginNumber);
        if (loginNumber == 0){
            selItem = MAX_LOGIN_NUMBER[0];
        }
        combo.setSelectedItem(selItem);
    }
    
    /**
     * Set login number combo
     */
    private void setLoginPerIPCombo(JComboBox combo, int loginPerIP) {
        Object selItem = new Integer(loginPerIP);
        if (loginPerIP == 0){
            selItem = MAX_LOGIN_NUMBER[0];
        }
        combo.setSelectedItem(selItem);
    }

    /*
     *add a user
     */
    private void add(){
      String userName = JOptionPane.showInputDialog("User Name:");
      if(userName != null && !userName.trim().equals("")){
        try{
          if(fconfig.getUserManager().doesExist(userName)){
            GuiUtils.showInformationMessage(this, "User Name already exists!");
          } else{
            BaseUser user = new BaseUser();
            user.setName(userName);
            user.setPassword(userName);
            user.setEnabled(true);
            user.setWritePermission(false);
            user.setMaxUploadRate(0);
            user.setMaxDownloadRate(0);
            user.setHomeDirectory("./res/home");
            user.setMaxIdleTime(0);
            
            fconfig.getUserManager().save(user);
            refresh(fconfig);
            userLst.setSelectedItem(userName);
          }
        } catch(FtpException ex){
          ex.printStackTrace();
          GuiUtils.showErrorMessage(this, "Failed to save the User.");
        }
      }
    }
    
    /**
     * Delete user.
     */
    private void delete() {
        Object selVal = userLst.getSelectedItem();
        if(selVal == null) {
            return;
        }
        
        String userName = selVal.toString();
        if(!GuiUtils.getConfirmation(this, "Do you really want to delete user " + userName + "?")) {
            return;
        }
        
        try {
            fconfig.getUserManager().delete(userName);
            refresh(fconfig);
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
        return (fconfig != null);
    }
    
    
    /**
     * String representation of this panel.
     */
    public String toString() {
        return "Users";
    }
}
