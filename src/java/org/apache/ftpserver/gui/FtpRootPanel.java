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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.rmi.RemoteException;

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
