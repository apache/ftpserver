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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTree;

import org.apache.ftpserver.remote.interfaces.IpRestrictorInterface;

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
        }
        catch(Exception ex) {
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
        if(mRestrictor.isAllowIp()) {
            headerStr = "Allow IP listed";
        }
        else {
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
        if(mRestrictor != null) {
            try {
                mRestrictor.clear();
                StringTokenizer st = new StringTokenizer(mjIpTxt.getText(), "\r\n");
                while(st.hasMoreTokens()) {
                    mRestrictor.addEntry(st.nextToken());
                }
                mRestrictor.save();
            }
            catch(Exception ex) {
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
            while(ipRestrictorIt.hasNext()) {
                mjIpTxt.append(ipRestrictorIt.next().toString());
                mjIpTxt.append("\n");
            }
        }
        catch(Exception ex) {
            getCommonHandler().handleException(ex);
        }
    }

}
