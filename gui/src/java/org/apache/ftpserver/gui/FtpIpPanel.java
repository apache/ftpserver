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

import org.apache.ftpserver.remote.interfaces.IpRestrictorInterface;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * Ip restrictor panel.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public
class FtpIpPanel extends PluginPanel {

    private static final String SAVE_IMG = "org/apache/ftpserver/gui/save.gif";
    private static final String RELOAD_IMG = "org/apache/ftpserver/gui/reload.gif";

    private JTextArea mjIpTxt;
    private IpRestrictorInterface mRestrictor;
    private JTextField mHeaderLab;

    /**
     * Instantiate IP restrictor panel
     */
    public FtpIpPanel(CommonHandler commonHandler, JTree tree) {
        super(commonHandler, tree);
        mRestrictor = commonHandler.getIpRestrictor();
        try {
            initComponents();
        } catch (Exception ex) {
            commonHandler.handleException(ex);
        }
        refresh();
    }

    /**
     * Initialize UI components
     */
    private void initComponents() throws RemoteException {
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(3, 0, 0, 3);
        setLayout(new GridBagLayout());
        int yindex = -1;


        // header
        String headerStr = "";
        if (mRestrictor.isAllowIp()) {
            headerStr = "Allow IP listed";
        } else {
            headerStr = "Ban IP listed";
        }
        mHeaderLab = new JTextField();
        mHeaderLab.setHorizontalAlignment(JTextField.CENTER);
        mHeaderLab.setColumns(12);
        mHeaderLab.setEditable(false);
        mHeaderLab.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        mHeaderLab.setFont(new Font(null, Font.BOLD, 12));
        mHeaderLab.setText(headerStr);
        gc.gridx = 0;
        gc.gridy = ++yindex;
        gc.gridwidth = 1;
        add(mHeaderLab, gc);


        // text area
        mjIpTxt = new JTextArea();
        JScrollPane txtPane = new JScrollPane(mjIpTxt,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        txtPane.setPreferredSize(new Dimension(150, 250));
        gc.gridx = 0;
        gc.gridy = ++yindex;
        gc.gridwidth = 1;
        add(txtPane, gc);


        // buttons
        JPanel btnPane = new JPanel(new FlowLayout(FlowLayout.CENTER));

        JButton jSaveBtn = new JButton("Save", GuiUtils.createImageIcon(SAVE_IMG));
        btnPane.add(jSaveBtn);

        JButton jResetBtn = new JButton("Reload", GuiUtils.createImageIcon(RELOAD_IMG));
        btnPane.add(jResetBtn);

        gc.gridx = 0;
        gc.gridy = ++yindex;
        gc.gridwidth = 1;
        add(btnPane, gc);


        // event handlers
        jSaveBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                save();
            }
        });

        jResetBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                refresh();
            }
        });
    }

    /**
     * Save IP data
     */
    public void save() {
        if (mRestrictor != null) {
            try {
                mRestrictor.clear();
                StringTokenizer st = new StringTokenizer(mjIpTxt.getText(), "\r\n");
                while (st.hasMoreTokens()) {
                    mRestrictor.addEntry(st.nextToken());
                }
                mRestrictor.save();
            } catch (Exception ex) {
                getCommonHandler().handleException(ex);
            }
        }
    }

    /**
     * Refresh table data
     */
    private void refresh() {
        try {
            mjIpTxt.setText("");
            Iterator ipRestrictorIt = mRestrictor.getAllEntries().iterator();
            while (ipRestrictorIt.hasNext()) {
                mjIpTxt.append(ipRestrictorIt.next().toString());
                mjIpTxt.append("\n");
            }
        } catch (Exception ex) {
            getCommonHandler().handleException(ex);
        }
    }

}
