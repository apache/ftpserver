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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;


/**
 * FTP user interface root panel. We can view comfig parameters
 * using this panel.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>.
 */
public
class FtpRootPanel extends PluginPanel {

    private JTextField mjHostTxt = null;
    private JTable mjCfgTbl      = null;

    /**
     * Creates new panel for root.
     */
    public FtpRootPanel(CommonHandler commonHandler, JTree tree) {
        super(commonHandler, tree);
        try {
            initComponents ();
        }
        catch(Exception ex) {
            commonHandler.handleException(ex);
        }
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     */
    private void initComponents() throws Exception {

        setLayout(new BorderLayout());

        // top panel
        GridBagConstraints gc = new GridBagConstraints();
        JPanel topPane = new JPanel();
        topPane.setLayout(new GridBagLayout());
        gc.insets = new Insets(10, 0, 0, 10);
        int yIndex = -1;

        JLabel hostLab = new JLabel("Remote Host");
        hostLab.setForeground(Color.black);
        gc.gridx = 0;
        gc.gridy = ++yIndex;
        gc.gridwidth = 1;
        gc.anchor = GridBagConstraints.EAST;
        topPane.add(hostLab, gc);

        mjHostTxt = new JTextField();
        mjHostTxt.setColumns(15);
        mjHostTxt.setEditable(false);
        mjHostTxt.setText(getCommonHandler().getConfig().getAddressString());
        gc.gridx = 1;
        gc.gridy = yIndex;
        gc.gridwidth = 1;
        gc.anchor = GridBagConstraints.WEST;
        topPane.add(mjHostTxt, gc);

        JLabel headerLab = new JLabel("Configuration Parameters");
        headerLab.setHorizontalAlignment(JLabel.CENTER);
        headerLab.setForeground(Color.black);
        gc.gridx = 0;
        gc.gridy = ++yIndex;
        gc.gridwidth = 2;
        gc.anchor = GridBagConstraints.CENTER;
        topPane.add(headerLab, gc);
        add(topPane, BorderLayout.NORTH);


        // bottom panel - display config parameters
        PropertiesTableModel cfgModel = PropertiesTableModel.getTableModel(getCommonHandler().getConfig());
        mjCfgTbl = new JTable(cfgModel);
        mjCfgTbl.setPreferredScrollableViewportSize(new Dimension(420, 200));
        mjCfgTbl.setColumnSelectionAllowed(false);
        JScrollPane bottomPane = new JScrollPane(mjCfgTbl,
                                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(bottomPane, BorderLayout.CENTER);
    }

}
