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

import java.awt.Font;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTree;

/**
 * This panel displays all file related activities.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>.
 */
public
class FtpFilePanel extends PluginPanel {

    private static final String CLEAR_IMG = "org/apache/ftpserver/gui/clear.gif";

    private FtpFileTableModel mModel;
    private String            mstHeader;

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
