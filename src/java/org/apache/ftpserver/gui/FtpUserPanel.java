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

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingConstants;

import org.apache.ftpserver.FtpUser;
import org.apache.ftpserver.remote.interfaces.UserManagerInterface;
import org.apache.ftpserver.usermanager.User;


/**
 * Ftp server user admin panel. You can create, update,
 * delete user using this panel.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public
class FtpUserPanel extends PluginPanel
                   implements ActionListener {

    private static final String SAVE_IMG = "org/apache/ftpserver/gui/save.gif";
    private static final String DELETE_IMG = "org/apache/ftpserver/gui/delete.gif";
    private static final String RELOAD_IMG = "org/apache/ftpserver/gui/reload.gif";
    private static final String BROWSE_IMG = "org/apache/ftpserver/gui/browse.gif";
    private static final String PASSWORD_IMG = "org/apache/ftpserver/gui/password.gif";

    private static final Random PASS_GEN = new Random(System.currentTimeMillis());

    private static final Object[] BYTE_RATES = {
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

    private static final Object[] IDLE_SECONDS = {
       "No limit",
       new Integer(60),
       new Integer(300),
       new Integer(900),
       new Integer(1800),
       new Integer(3600)
    };

    private UserManagerInterface mUserManager;

    private JComboBox mjUserLst;
    private JTextField mjNameTxt;

    private JPasswordField mjPasswordTxt;
    private JPasswordField mjRetypePasswordTxt;
    private JCheckBox mjPasswordChkBox;

    private JTextField mjDirectoryTxt;
    private JCheckBox mjEnabledChkBox;
    private JCheckBox mjWriteChkBox;
    private JComboBox mjIdleLst;
    private JComboBox mjUploadLst;
    private JComboBox mjDownloadLst;

    /**
     * Creates new panel.
     */
    public FtpUserPanel(CommonHandler commonHandler, JTree tree) {
        super(commonHandler, tree);
        mUserManager = commonHandler.getUserManager();
        initComponents();
        refresh();
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     */
    private void initComponents() {
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(5, 0, 0, 10);
        setLayout(new GridBagLayout());
        int yindex = -1;

        // user list
        mjUserLst = new JComboBox();
        mjUserLst.addActionListener(this);
        gc.gridx = 0;
        gc.gridy = ++yindex;
        gc.gridwidth = 3;
        gc.anchor = GridBagConstraints.CENTER;
        add(mjUserLst, gc);

        // user name
        JLabel jNameLab = new JLabel("Name");
        jNameLab.setHorizontalAlignment(JLabel.RIGHT);
        jNameLab.setForeground(Color.black);
        gc.gridx = 0;
        gc.gridy = ++yindex;
        gc.gridwidth = 1;
        gc.anchor = GridBagConstraints.EAST;
        add(jNameLab, gc);

        mjNameTxt = new JTextField();
        mjNameTxt.setColumns(12);
        gc.gridx = 1;
        gc.gridy = yindex;
        gc.gridwidth = 2;
        gc.anchor = GridBagConstraints.WEST;
        add(mjNameTxt, gc);

        // password
        JLabel jPasswordLab = new JLabel("Password");
        jPasswordLab.setHorizontalAlignment(JLabel.RIGHT);
        jPasswordLab.setForeground(Color.black);
        gc.gridx = 0;
        gc.gridy = ++yindex;
        gc.gridwidth = 1;
        gc.anchor = GridBagConstraints.EAST;
        add(jPasswordLab, gc);

        mjPasswordTxt = new JPasswordField();
        mjPasswordTxt.setColumns(12);
        gc.gridx = 1;
        gc.gridy = yindex;
        gc.gridwidth = 2;
        gc.anchor = GridBagConstraints.WEST;
        add(mjPasswordTxt, gc);

        //JButton jGeneratePassBtn = new JButton("Generate", GuiUtils.createImageIcon(PASSWORD_IMG));
        JButton jGeneratePassBtn = new JButton("Generate");
        jGeneratePassBtn.addActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent evt) {
               generatePassword();
           }
        });
        gc.gridx = 2;
        gc.gridy = yindex;
        gc.gridwidth = 1;
        gc.anchor = GridBagConstraints.WEST;
        add(jGeneratePassBtn, gc);

        // retype password
        JLabel jRetypePasswordLab = new JLabel("Retype Password");
        jRetypePasswordLab.setHorizontalAlignment(JLabel.RIGHT);
        jRetypePasswordLab.setForeground(Color.black);
        gc.gridx = 0;
        gc.gridy = ++yindex;
        gc.gridwidth = 1;
        gc.anchor = GridBagConstraints.EAST;
        add(jRetypePasswordLab, gc);

        mjRetypePasswordTxt = new JPasswordField();
        mjRetypePasswordTxt.setColumns(12);
        gc.gridx = 1;
        gc.gridy = yindex;
        gc.gridwidth = 2;
        gc.anchor = GridBagConstraints.WEST;
        add(mjRetypePasswordTxt, gc);

        // set password
        JLabel jSetPasswordLab = new JLabel("Set Password");
        jSetPasswordLab.setHorizontalAlignment(JLabel.RIGHT);
        jSetPasswordLab.setForeground(Color.black);
        gc.gridx = 0;
        gc.gridy = ++yindex;
        gc.gridwidth = 1;
        gc.anchor = GridBagConstraints.EAST;
        add(jSetPasswordLab, gc);

        mjPasswordChkBox = new JCheckBox();
        mjPasswordChkBox.setHorizontalTextPosition(SwingConstants.LEFT);
        gc.gridx = 1;
        gc.gridy = yindex;
        gc.gridwidth = 2;
        gc.anchor = GridBagConstraints.WEST;
        add(mjPasswordChkBox, gc);

        // root directory
        JLabel jDirectoryLab = new JLabel("Root Directory");
        jDirectoryLab.setHorizontalAlignment(JLabel.RIGHT);
        jDirectoryLab.setForeground(Color.black);
        gc.gridx = 0;
        gc.gridy = ++yindex;
        gc.gridwidth = 1;
        gc.anchor = GridBagConstraints.EAST;
        add(jDirectoryLab, gc);

        mjDirectoryTxt = new JTextField();
        mjDirectoryTxt.setColumns(12);
        gc.gridx = 1;
        gc.gridy = yindex;
        gc.gridwidth = 1;
        gc.anchor = GridBagConstraints.WEST;
        add(mjDirectoryTxt, gc);

        // enable/disable
        JLabel jEnabledLab = new JLabel("Enabled");
        jEnabledLab.setHorizontalAlignment(JLabel.RIGHT);
        jEnabledLab.setForeground(Color.black);
        gc.gridx = 0;
        gc.gridy = ++yindex;
        gc.gridwidth = 1;
        gc.anchor = GridBagConstraints.EAST;
        add(jEnabledLab, gc);

        mjEnabledChkBox = new JCheckBox();
        gc.gridx = 1;
        gc.gridy = yindex;
        gc.gridwidth = 2;
        gc.anchor = GridBagConstraints.WEST;
        add(mjEnabledChkBox, gc);

        // write permission
        JLabel jWritePermLab = new JLabel("Write Permission");
        jWritePermLab.setHorizontalAlignment(JLabel.RIGHT);
        jWritePermLab.setForeground(Color.black);
        gc.gridx = 0;
        gc.gridy = ++yindex;
        gc.gridwidth = 1;
        gc.anchor = GridBagConstraints.EAST;
        add(jWritePermLab, gc);

        mjWriteChkBox = new JCheckBox();
        gc.gridx = 1;
        gc.gridy = yindex;
        gc.gridwidth = 2;
        gc.anchor = GridBagConstraints.WEST;
        add(mjWriteChkBox, gc);

        // idle time
        JLabel jIdleLab = new JLabel("Max. Idle Time (seconds)");
        jIdleLab.setHorizontalAlignment(JLabel.RIGHT);
        jIdleLab.setForeground(Color.black);
        gc.gridx = 0;
        gc.gridy = ++yindex;
        gc.gridwidth = 1;
        gc.anchor = GridBagConstraints.EAST;
        add(jIdleLab, gc);

        mjIdleLst = new JComboBox(IDLE_SECONDS);
        mjIdleLst.setEditable(true);
        gc.gridx = 1;
        gc.gridy = yindex;
        gc.gridwidth = 2;
        gc.anchor = GridBagConstraints.WEST;
        add(mjIdleLst, gc);

        // user upload limit
        JLabel jUploadLab = new JLabel("Max. Upload (bytes/sec)");
        jUploadLab.setHorizontalAlignment(JLabel.RIGHT);
        jUploadLab.setForeground(Color.black);
        gc.gridx = 0;
        gc.gridy = ++yindex;
        gc.gridwidth = 1;
        gc.anchor = GridBagConstraints.EAST;
        add(jUploadLab, gc);

        mjUploadLst = new JComboBox(BYTE_RATES);
        mjUploadLst.setEditable(true);
        gc.gridx = 1;
        gc.gridy = yindex;
        gc.gridwidth = 2;
        gc.anchor = GridBagConstraints.WEST;
        add(mjUploadLst, gc);

        // user download limit
        JLabel jDownloadLab = new JLabel("Max. Download (bytes/sec)");
        jDownloadLab.setHorizontalAlignment(JLabel.RIGHT);
        jDownloadLab.setForeground(Color.black);
        gc.gridx = 0;
        gc.gridy = ++yindex;
        gc.gridwidth = 1;
        gc.anchor = GridBagConstraints.EAST;
        add(jDownloadLab, gc);

        mjDownloadLst = new JComboBox(BYTE_RATES);
        mjDownloadLst.setEditable(true);
        gc.gridx = 1;
        gc.gridy = yindex;
        gc.gridwidth = 2;
        gc.anchor = GridBagConstraints.WEST;
        add(mjDownloadLst, gc);

        JPanel btnPane = new JPanel(new FlowLayout(FlowLayout.CENTER));

        // save user
        JButton jSaveBtn = new JButton("Save", GuiUtils.createImageIcon(SAVE_IMG));
        jSaveBtn.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                save();
             }
        });
        btnPane.add(jSaveBtn);

        // delete user
        JButton jDeleteBtn = new JButton("Delete", GuiUtils.createImageIcon(DELETE_IMG));
        jDeleteBtn.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                delete();
             }
        });
        btnPane.add(jDeleteBtn);

        // reload user data
        JButton jReloadBtn = new JButton("Reload", GuiUtils.createImageIcon(RELOAD_IMG));
        jReloadBtn.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                refresh();
             }
        });
        btnPane.add(jReloadBtn);

        gc.gridx = 0;
        gc.gridy = ++yindex;
        gc.gridwidth = 3;
        gc.anchor = GridBagConstraints.CENTER;
        add(btnPane, gc);
    }


    /**
     * Save the user object
     */
    private void save() {

        // check user name field
        String userName = mjNameTxt.getText().trim();
        if(userName.equals("")) {
            GuiUtils.showErrorMessage(getCommonHandler().getTopFrame(), "Please enter an user name");
            return;
        }

        try {
            FtpUser user = new FtpUser();
            user.setName(userName);
            if(setPassword(user)) {
              user.getVirtualDirectory().setRootDirectory(mjDirectoryTxt.getText());
              user.setEnabled(mjEnabledChkBox.isSelected());
              user.getVirtualDirectory().setWritePermission(mjWriteChkBox.isSelected());
              user.setMaxIdleTime(getMaxIdleTime());
              user.setMaxUploadRate(getBytesTransferRate(mjUploadLst));
              user.setMaxDownloadRate(getBytesTransferRate(mjDownloadLst));
              mUserManager.save(user);
              refresh();
              GuiUtils.showInformationMessage(getCommonHandler().getTopFrame(), "Saved user: " + userName);
            }
        }
        catch(Exception ex) {
            getCommonHandler().handleException(ex);
        }
    }


    /**
     * Save the user object
     */
    private void delete() {
        Object selVal = mjUserLst.getSelectedItem();
        if(selVal == null) {
            return;
        }

        String userName = selVal.toString();
        boolean bConf = GuiUtils.getConfirmation(getCommonHandler().getTopFrame(), "Do you really want to delete user " + userName + "?");
        if(!bConf) {
            return;
        }
        try {
            mUserManager.delete(userName);
            refresh();
        }
        catch(Exception ex) {
            getCommonHandler().handleException(ex);
        }
    }

    /**
     * Initialize user list.
     */
    private void refresh() {
        try {
            mjUserLst.removeAllItems();
            List allUsers = mUserManager.getAllUserNames();
            for(Iterator userIt = allUsers.iterator(); userIt.hasNext(); ) {
                mjUserLst.addItem(userIt.next());
            }
        }
        catch(Exception ex) {
            getCommonHandler().handleException(ex);
        }
    }



    /**
     * List selection changed.
     */
    public void actionPerformed(ActionEvent e) {
        Object selVal = mjUserLst.getSelectedItem();
        try {
            if(selVal != null) {
                String userName = selVal.toString();
                User thisUser = mUserManager.getUserByName(userName);
                populateFields(thisUser);
            }
        }
        catch(Exception ex) {
            getCommonHandler().handleException(ex);
        }
    }

    /**
     * Populate user data fields.
     */
    public void populateFields(User user) {
        mjNameTxt.setText(user.getName());
        mjPasswordTxt.setText("");
        mjRetypePasswordTxt.setText("");
        mjPasswordChkBox.setSelected(false);
        mjDirectoryTxt.setText(user.getVirtualDirectory().getRootDirectory());
        mjEnabledChkBox.setSelected(user.getEnabled());
        mjWriteChkBox.setSelected(user.getVirtualDirectory().getWritePermission());
        setIdleTimeCombo(user.getMaxIdleTime());
        setByteRateCombo(mjUploadLst, user.getMaxUploadRate());
        setByteRateCombo(mjDownloadLst, user.getMaxDownloadRate());
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
        GuiUtils.showInformationMessage(getCommonHandler().getTopFrame(), "Generated password: " + password);
        mjPasswordTxt.setText(password);
        mjRetypePasswordTxt.setText(password);
        mjPasswordChkBox.setSelected(true);
    }


    /**
     * Set password if necessary.
     */
    private boolean setPassword(FtpUser usr) {

        try {
            String userName = usr.getName();
            boolean bNewUser = !mUserManager.doesExist(userName);
            boolean bPassSet = mjPasswordChkBox.isSelected();
            String password = new String(mjPasswordTxt.getPassword());
            String repassword = new String(mjRetypePasswordTxt.getPassword());

            // new user
            if( bNewUser && (!bPassSet) && (!usr.getIsAnonymous()) ) {
                GuiUtils.showErrorMessage(getCommonHandler().getTopFrame(), "New user - password required");
                return false;
            }

            // password set
            if( bPassSet && (!password.equals(repassword)) && (!usr.getIsAnonymous()) ) {
                GuiUtils.showErrorMessage(getCommonHandler().getTopFrame(), "Password entries are not equal");
                return false;
            }

            // set password if necessary
            if(bPassSet && (!usr.getIsAnonymous())) {
                usr.setPassword(password);
            }
            else {
                usr.setPassword(null);
            }
            return true;
        }
        catch(Exception ex) {
            getCommonHandler().handleException(ex);
        }
        return false;
    }

    /**
     * Get max bytes/sec.
     */
    private int getBytesTransferRate(JComboBox byteLst) {
        int rate = 0;
        Object selObj = byteLst.getSelectedItem();
        if (!selObj.equals(BYTE_RATES[0])) {
            try {
                rate = Integer.parseInt(selObj.toString());
            }
            catch(NumberFormatException ex) {
                GuiUtils.showErrorMessage(getCommonHandler().getTopFrame(), ex.getMessage());
            }
        }

        return rate;
    }

    /**
     * Get max idle time in sec.
     */
    private int getMaxIdleTime() {
        int sec = 0;
        Object selObj = mjIdleLst.getSelectedItem();
        if (!selObj.equals(IDLE_SECONDS[0])) {
            try {
                sec = Integer.parseInt(selObj.toString());
            }
            catch(NumberFormatException ex) {
                GuiUtils.showErrorMessage(getCommonHandler().getTopFrame(), ex.getMessage());
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
    private void setIdleTimeCombo(int idle) {
        Object selItem = new Integer(idle);
        if (idle == 0){
            selItem = IDLE_SECONDS[0];
        }
        mjIdleLst.setSelectedItem(selItem);
    }

}
