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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JTree;

import org.apache.ftpserver.UserImpl;
import org.apache.ftpserver.gui.remote.FtpFileListenerAdapter;
import org.apache.ftpserver.gui.remote.FtpStatisticsListenerAdapter;
import org.apache.ftpserver.remote.interfaces.FtpFileListener;
import org.apache.ftpserver.remote.interfaces.FtpStatisticsInterface;
import org.apache.ftpserver.remote.interfaces.FtpStatisticsListener;

/**
 * Ftp server global statistics panel. It listenes to the global
 * statistics changes.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public
class FtpStatisticsPanel extends PluginPanel
                         implements FtpStatisticsListener, FtpFileListener {

    private static final String RELOAD_IMG = "org/apache/ftpserver/gui/reload.gif";
    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("MM/dd HH:mm:ss");

    private JTextField mjStartTimeTxt;

    private JTextField mjUploadNbrTxt;
    private JTextField mjDownloadNbrTxt;
    private JTextField mjDeleteNbrTxt;

    private JTextField mjUploadBytesTxt;
    private JTextField mjDownloadBytesTxt;

    private JTextField mjLoginNbrTxt;
    private JTextField mjAnonLoginNbrTxt;
    private JTextField mjConNbrTxt;

    private JTextField mjTotalLoginNbrTxt;
    private JTextField mjTotalAnonLoginNbrTxt;
    private JTextField mjTotalConNbrTxt;

    private FtpFileTableModel mUploadModel;
    private FtpFileTableModel mDownloadModel;
    private FtpFileTableModel mDeleteModel;

    private FtpStatisticsInterface mStat;

    private FtpFileListenerAdapter mFileListener;
    private FtpStatisticsListenerAdapter mListener;

    /**
     * Creates new panel to display ftp global statistics.
     */
    public FtpStatisticsPanel(CommonHandler commonHandler, JTree tree) {
        super(commonHandler, tree);
        mStat = commonHandler.getStatistics();

        mUploadModel = new FtpFileTableModel();
        mDownloadModel = new FtpFileTableModel();
        mDeleteModel = new FtpFileTableModel();
        initComponents();

        try {
            mFileListener = new FtpFileListenerAdapter(mStat, this);
            mListener = new FtpStatisticsListenerAdapter(mStat, this);
            reload();
            mjStartTimeTxt.setText(DATE_FMT.format(mStat.getStartTime()));
        }
        catch(Exception ex) {
            commonHandler.handleException(ex);
        }
    }

    /**
     * This method is called from within the constructor to
     * initialize the panel.
     */
    private void initComponents() {

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(3, 0, 0, 3);
        gc.gridwidth = 2;
        setLayout(new GridBagLayout());
        int yindex = -1;

        // start time
        JLabel jStartTimeLab = new JLabel("Start Time");
        jStartTimeLab.setHorizontalAlignment(JLabel.RIGHT);
        jStartTimeLab.setForeground(Color.black);
        gc.gridx = 0;
        gc.gridy = ++yindex;
        gc.anchor = GridBagConstraints.EAST;
        add(jStartTimeLab, gc);

        mjStartTimeTxt = new JTextField();
        mjStartTimeTxt.setColumns(12);
        mjStartTimeTxt.setEditable(false);
        mjStartTimeTxt.setBackground(Color.white);
        gc.gridx = 2;
        gc.gridy = yindex;
        gc.anchor = GridBagConstraints.WEST;
        add(mjStartTimeTxt, gc);

        // number of uploads
        JLabel jUploadNbrLab = new JLabel("Number of uploads");
        jUploadNbrLab.setHorizontalAlignment(JLabel.RIGHT);
        jUploadNbrLab.setForeground(Color.black);
        gc.gridx = 0;
        gc.gridy = ++yindex;
        gc.anchor = GridBagConstraints.EAST;
        add(jUploadNbrLab, gc);

        mjUploadNbrTxt = new JTextField();
        mjUploadNbrTxt.setColumns(6);
        mjUploadNbrTxt.setEditable(false);
        mjUploadNbrTxt.setBackground(Color.white);
        gc.gridx = 2;
        gc.gridy = yindex;
        gc.anchor = GridBagConstraints.WEST;
        add(mjUploadNbrTxt, gc);

        // number of downloads
        JLabel jDownloadNbrLab = new JLabel("Number of downloads");
        jDownloadNbrLab.setHorizontalAlignment(JLabel.RIGHT);
        jDownloadNbrLab.setForeground(Color.black);
        gc.gridx = 0;
        gc.gridy = ++yindex;
        gc.anchor = GridBagConstraints.EAST;
        add(jDownloadNbrLab, gc);

        mjDownloadNbrTxt = new JTextField();
        mjDownloadNbrTxt.setColumns(6);
        mjDownloadNbrTxt.setEditable(false);
        mjDownloadNbrTxt.setBackground(Color.white);
        gc.gridx = 2;
        gc.gridy = yindex;
        gc.anchor = GridBagConstraints.WEST;
        add(mjDownloadNbrTxt, gc);

        // number of downloads
        JLabel jDeleteNbrLab = new JLabel("Number of deletes");
        jDeleteNbrLab.setHorizontalAlignment(JLabel.RIGHT);
        jDeleteNbrLab.setForeground(Color.black);
        gc.gridx = 0;
        gc.gridy = ++yindex;
        gc.anchor = GridBagConstraints.EAST;
        add(jDeleteNbrLab, gc);

        mjDeleteNbrTxt = new JTextField();
        mjDeleteNbrTxt.setColumns(6);
        mjDeleteNbrTxt.setEditable(false);
        mjDeleteNbrTxt.setBackground(Color.white);
        gc.gridx = 2;
        gc.gridy = yindex;
        gc.anchor = GridBagConstraints.WEST;
        add(mjDeleteNbrTxt, gc);

        // number of uploaded bytes
        JLabel jUploadBytesLab = new JLabel("Uploaded bytes");
        jUploadBytesLab.setHorizontalAlignment(JLabel.RIGHT);
        jUploadBytesLab.setForeground(Color.black);
        gc.gridx = 0;
        gc.gridy = ++yindex;
        gc.anchor = GridBagConstraints.EAST;
        add(jUploadBytesLab, gc);

        mjUploadBytesTxt = new JTextField();
        mjUploadBytesTxt.setColumns(12);
        mjUploadBytesTxt.setEditable(false);
        mjUploadBytesTxt.setBackground(Color.white);
        gc.gridx = 2;
        gc.gridy = yindex;
        gc.anchor = GridBagConstraints.WEST;
        add(mjUploadBytesTxt, gc);

        // number of uploaded bytes
        JLabel jDownloadBytesLab = new JLabel("Downloaded bytes");
        jDownloadBytesLab.setHorizontalAlignment(JLabel.RIGHT);
        jDownloadBytesLab.setForeground(Color.black);
        gc.gridx = 0;
        gc.gridy = ++yindex;
        gc.anchor = GridBagConstraints.EAST;
        add(jDownloadBytesLab, gc);

        mjDownloadBytesTxt = new JTextField();
        mjDownloadBytesTxt.setColumns(12);
        mjDownloadBytesTxt.setEditable(false);
        mjDownloadBytesTxt.setBackground(Color.white);
        gc.gridx = 2;
        gc.gridy = yindex;
        gc.anchor = GridBagConstraints.WEST;
        add(mjDownloadBytesTxt, gc);

        // number of current logins
        JLabel jLoginNbrLab = new JLabel("Current logins");
        jLoginNbrLab.setHorizontalAlignment(JLabel.RIGHT);
        jLoginNbrLab.setForeground(Color.black);
        gc.gridx = 0;
        gc.gridy = ++yindex;
        gc.anchor = GridBagConstraints.EAST;
        add(jLoginNbrLab, gc);

        mjLoginNbrTxt = new JTextField();
        mjLoginNbrTxt.setColumns(6);
        mjLoginNbrTxt.setEditable(false);
        mjLoginNbrTxt.setBackground(Color.white);
        gc.gridx = 2;
        gc.gridy = yindex;
        gc.anchor = GridBagConstraints.WEST;
        add(mjLoginNbrTxt, gc);

        // number of total logins
        JLabel jTotalLoginNbrLab = new JLabel("Total logins");
        jTotalLoginNbrLab.setHorizontalAlignment(JLabel.RIGHT);
        jTotalLoginNbrLab.setForeground(Color.black);
        gc.gridx = 0;
        gc.gridy = ++yindex;
        gc.anchor = GridBagConstraints.EAST;
        add(jTotalLoginNbrLab, gc);

        mjTotalLoginNbrTxt = new JTextField();
        mjTotalLoginNbrTxt.setColumns(6);
        mjTotalLoginNbrTxt.setEditable(false);
        mjTotalLoginNbrTxt.setBackground(Color.white);
        gc.gridx = 2;
        gc.gridy = yindex;
        gc.anchor = GridBagConstraints.WEST;
        add(mjTotalLoginNbrTxt, gc);

        // number of current anonymous logins
        JLabel jAnonLoginNbrLab = new JLabel("Current anonymous logins");
        jAnonLoginNbrLab.setHorizontalAlignment(JLabel.RIGHT);
        jAnonLoginNbrLab.setForeground(Color.black);
        gc.gridx = 0;
        gc.gridy = ++yindex;
        gc.anchor = GridBagConstraints.EAST;
        add(jAnonLoginNbrLab, gc);

        mjAnonLoginNbrTxt = new JTextField();
        mjAnonLoginNbrTxt.setColumns(6);
        mjAnonLoginNbrTxt.setEditable(false);
        mjAnonLoginNbrTxt.setBackground(Color.white);
        gc.gridx = 2;
        gc.gridy = yindex;
        gc.anchor = GridBagConstraints.WEST;
        add(mjAnonLoginNbrTxt, gc);

        // number of total anonymous logins
        JLabel jTotalAnonLoginNbrLab = new JLabel("Total anonymous logins");
        jTotalAnonLoginNbrLab.setHorizontalAlignment(JLabel.RIGHT);
        jTotalAnonLoginNbrLab.setForeground(Color.black);
        gc.gridx = 0;
        gc.gridy = ++yindex;
        gc.anchor = GridBagConstraints.EAST;
        add(jTotalAnonLoginNbrLab, gc);

        mjTotalAnonLoginNbrTxt = new JTextField();
        mjTotalAnonLoginNbrTxt.setColumns(6);
        mjTotalAnonLoginNbrTxt.setEditable(false);
        mjTotalAnonLoginNbrTxt.setBackground(Color.white);
        gc.gridx = 2;
        gc.gridy = yindex;
        gc.anchor = GridBagConstraints.WEST;
        add(mjTotalAnonLoginNbrTxt, gc);

        // number of current connections
        JLabel jConNbrLab = new JLabel("Current connections");
        jConNbrLab.setHorizontalAlignment(JLabel.RIGHT);
        jConNbrLab.setForeground(Color.black);
        gc.gridx = 0;
        gc.gridy = ++yindex;
        gc.anchor = GridBagConstraints.EAST;
        add(jConNbrLab, gc);

        mjConNbrTxt = new JTextField();
        mjConNbrTxt.setColumns(6);
        mjConNbrTxt.setEditable(false);
        mjConNbrTxt.setBackground(Color.white);
        gc.gridx = 2;
        gc.gridy = yindex;
        gc.anchor = GridBagConstraints.WEST;
        add(mjConNbrTxt, gc);

        // number of current connections
        JLabel jTotalConNbrLab = new JLabel("Total connections");
        jTotalConNbrLab.setHorizontalAlignment(JLabel.RIGHT);
        jTotalConNbrLab.setForeground(Color.black);
        gc.gridx = 0;
        gc.gridy = ++yindex;
        gc.anchor = GridBagConstraints.EAST;
        add(jTotalConNbrLab, gc);

        mjTotalConNbrTxt = new JTextField();
        mjTotalConNbrTxt.setColumns(6);
        mjTotalConNbrTxt.setEditable(false);
        mjTotalConNbrTxt.setBackground(Color.white);
        gc.gridx = 2;
        gc.gridy = yindex;
        gc.anchor = GridBagConstraints.WEST;
        add(mjTotalConNbrTxt, gc);

        // reload statistics
        JButton reloadButton = new JButton("Reload", GuiUtils.createImageIcon(RELOAD_IMG));
        gc.gridx = 0;
        gc.gridy = ++yindex;
        gc.gridwidth = 4;
        gc.anchor = GridBagConstraints.CENTER;
        add(reloadButton, gc);

        reloadButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                reload();
             }
        });
    }

    /**
     * Get upload file table model.
     */
    public FtpFileTableModel getUploadModel() {
        return mUploadModel;
    }

    /**
     * Get download file table model.
     */
    public FtpFileTableModel getDownloadModel() {
        return mDownloadModel;
    }

    /**
     * Get delete file table model.
     */
    public FtpFileTableModel getDeleteModel() {
        return mDeleteModel;
    }

    /**
     * Upload notification.
     */
    public void notifyUpload() {
       try {
           mjUploadNbrTxt.setText(String.valueOf(mStat.getFileUploadNbr()));
           mjUploadBytesTxt.setText(String.valueOf(mStat.getFileUploadSize()));
       }
       catch(Exception ex) {
           getCommonHandler().handleException(ex);
       }
    }

    /**
     * Download notification.
     */
    public void notifyDownload() {
       try {
           mjDownloadNbrTxt.setText(String.valueOf(mStat.getFileDownloadNbr()));
           mjDownloadBytesTxt.setText(String.valueOf(mStat.getFileDownloadSize()));
       }
       catch(Exception ex) {
           getCommonHandler().handleException(ex);
       }
    }


    /**
     * Delete notification.
     */
    public void notifyDelete() {
       try {
           mjDeleteNbrTxt.setText(String.valueOf(mStat.getFileDeleteNbr()));
       }
       catch(Exception ex) {
           getCommonHandler().handleException(ex);
       }
    }

    /**
     * User login notification.
     */
    public void notifyLogin() {
       try {
           mjLoginNbrTxt.setText(String.valueOf(mStat.getLoginNbr()));
           mjAnonLoginNbrTxt.setText(String.valueOf(mStat.getAnonLoginNbr()));
           mjTotalLoginNbrTxt.setText(String.valueOf(mStat.getTotalLoginNbr()));
           mjTotalAnonLoginNbrTxt.setText(String.valueOf(mStat.getTotalAnonLoginNbr()));
       }
       catch(Exception ex) {
           getCommonHandler().handleException(ex);
       }
    }

    /**
     * User logout notification.
     */
    public void notifyLogout() {
        notifyLogin();
    }

    /**
     * Notify open/close connection
     */
    public void notifyConnection() {
       try {
           mjConNbrTxt.setText(String.valueOf(mStat.getConnectionNbr()));
           mjTotalConNbrTxt.setText(String.valueOf(mStat.getTotalConnectionNbr()));
       }
       catch(Exception ex) {
           getCommonHandler().handleException(ex);
       }
    }

    /**
     * Notify file upload
     */
    public void notifyUpload(final String fl, final String sessId) {
        UserImpl user = getCommonHandler().getUser(sessId);
        if (user != null) {
            mUploadModel.newEntry(fl, user);
        }
    }

    /**
     * Notify file download
     */
    public void notifyDownload(final String fl, final String sessId) {
        UserImpl user = getCommonHandler().getUser(sessId);
        if (user != null) {
            mDownloadModel.newEntry(fl, user);
        }
    }

    /**
     * Notify file delete
     */
    public void notifyDelete(final String fl, final String sessId) {
        UserImpl user = getCommonHandler().getUser(sessId);
        if (user != null) {
            mDeleteModel.newEntry(fl, user);
        }
    }


    /**
     * Load all the global statistics parameters
     */
    public void reload() {
        notifyUpload();
        notifyDownload();
        notifyDelete();
        notifyLogin();
        notifyConnection();
    }


    /**
     * Close it.
     */
    public void close() {
        mListener.close();
        mFileListener.close();
    }

}
