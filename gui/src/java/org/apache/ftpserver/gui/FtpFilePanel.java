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

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * This panel displays all file related activities.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>.
 */
public
class FtpFilePanel extends PluginPanel {

    private static final String CLEAR_IMG = "org/apache/ftpserver/gui/clear.gif";

    private FtpFileTableModel mModel;
    private String mstHeader;

    /**
     * Instantiate login panel.
     */
    public FtpFilePanel(CommonHandler commonHandler, JTree tree, FtpFileTableModel model, String header) {
        super(commonHandler, tree);
        mModel = model;
        mstHeader = header;
        initComponents();
    }

    /**
     * Initialize UI components
     */
    private void initComponents() {

        setLayout(new BorderLayout());

        JPanel topPane = new JPanel();
        JLabel headerLab = new JLabel(mstHeader);
        headerLab.setForeground(Color.black);
        headerLab.setFont(new Font(null, Font.BOLD, 12));
        topPane.add(headerLab);
        add(topPane, BorderLayout.NORTH);

        JTable fileTable = new JTable(mModel);
        fileTable.setPreferredScrollableViewportSize(new Dimension(470, 300));
        fileTable.setColumnSelectionAllowed(false);
        JScrollPane scrollPane = new JScrollPane(fileTable,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);


        // button
        JPanel bottomPanel = new JPanel();
        JButton jResetBtn = new JButton("Clear", GuiUtils.createImageIcon(CLEAR_IMG));
        bottomPanel.add(jResetBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        // event handler
        jResetBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                mModel.reset();
            }
        });
    }

    /**
     * Refresh window.
     */
    public void refresh() {
        mModel.reset();
    }

    /**
     * Close panel
     */
    public void close() {
        mModel.close();
    }

}
