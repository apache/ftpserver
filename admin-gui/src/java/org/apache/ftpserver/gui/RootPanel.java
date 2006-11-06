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
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;

import org.apache.ftpserver.FtpConfigImpl;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.config.PropertiesConfiguration;
import org.apache.ftpserver.config.XmlConfigurationHandler;
import org.apache.ftpserver.ftplet.Configuration;
import org.apache.ftpserver.interfaces.IFtpConfig;
import org.apache.ftpserver.util.IoUtils;

/**
 * This is the tree root panel. It is responsible to load 
 * configuration file and start/stop the server. 
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class RootPanel extends PluginPanel {
    
    private static final long serialVersionUID = 6048031185986894615L;

    private JTextField cfgFileTxt;
    
    private JRadioButton cfgPropRadio;
    private JRadioButton cfgXmlRadio;
    
    private JRadioButton cfgFileRadio;
    private JRadioButton cfgClasspathRadio;
    
    private JButton browseButton;
    private JButton startButton;
    private JButton stopButton;
    private JToggleButton suspendButton; 
    
    private FtpServer server;
    
    
    /**
     * Constructor - create all UI components.
     */
    public RootPanel(PluginPanelContainer container) {
        super(container);
        initComponents();
    }
    
    
    /**
     * Initialize UI components.
     */
    public void initComponents() {
        
        setLayout(new BorderLayout());
        
        // top panel
        JPanel topPnl = new JPanel(new GridBagLayout());
        add(topPnl, BorderLayout.NORTH);
        GridBagConstraints gc = new GridBagConstraints();
        int yindex = -1; 
        gc.insets = new Insets(0, 0, 0, 10);
        
        // configuration label
        JLabel cfgLab = new JLabel("Configuration", JLabel.CENTER);
        cfgLab.setForeground(Color.darkGray);
        cfgLab.setFont(new Font(null, Font.BOLD, 14));
        gc.gridx = 0;
        gc.gridy = ++yindex;
        gc.gridwidth = 4;
        gc.fill = GridBagConstraints.HORIZONTAL;
        topPnl.add(cfgLab, gc);
        
        // file label
        JLabel cfgFileLab = new JLabel("File :: ", JLabel.RIGHT);
        cfgFileLab.setFont(new Font(null, Font.BOLD, 12));
        gc.gridx = 0;
        gc.gridy = ++yindex;
        gc.gridwidth = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        topPnl.add(cfgFileLab, gc);
        
        // file text field
        cfgFileTxt = new JTextField();
        cfgFileTxt.setText("./res/conf/ftpd.properties");
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridx = 1;
        gc.gridy = yindex;
        gc.gridwidth = 2;
        topPnl.add(cfgFileTxt, gc);
        
        // browse button
        browseButton = new JButton("Browse");
        browseButton.setPreferredSize(new Dimension(80, 22));
        browseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                File currFile = new File(cfgFileTxt.getText());
                JFileChooser fileChoose = new JFileChooser(currFile.getParentFile());
                fileChoose.setFileSelectionMode(JFileChooser.FILES_ONLY);
                int returnVal = fileChoose.showOpenDialog(RootPanel.this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    String path = fileChoose.getSelectedFile().getAbsolutePath();
                    cfgFileTxt.setText(path);
                }
            }
        });
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridx = 3;
        gc.gridy = yindex;
        gc.gridwidth = 1;
        topPnl.add(browseButton, gc);
        
        // source label
        JLabel cfgSrcLab = new JLabel("Source :: ", JLabel.RIGHT);
        cfgSrcLab.setFont(new Font(null, Font.BOLD, 12));
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridx = 0;
        gc.gridy = ++yindex;
        gc.gridwidth = 1;
        topPnl.add(cfgSrcLab, gc);
        
        // file system radio button
        cfgFileRadio = new JRadioButton("File System");
        cfgFileRadio.setFont(new Font(null, Font.BOLD, 12));
        cfgFileRadio.setSelected(true);
        cfgFileRadio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                browseButton.setEnabled(cfgFileRadio.isSelected());
            }
        });
        gc.gridx = 1;
        gc.gridy = yindex;
        gc.gridwidth = 1;
        topPnl.add(cfgFileRadio, gc);
        
        // classpath radio button
        cfgClasspathRadio = new JRadioButton("Classpath");
        cfgClasspathRadio.setFont(new Font(null, Font.BOLD, 12));
        cfgClasspathRadio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                browseButton.setEnabled(!cfgClasspathRadio.isSelected());
            }
        });
        gc.gridx = 2;
        gc.gridy = yindex;
        gc.gridwidth = 1;
        topPnl.add(cfgClasspathRadio, gc);
        
        // type label
        JLabel cfgTypLab = new JLabel("Type :: ", JLabel.RIGHT);
        cfgTypLab.setFont(new Font(null, Font.BOLD, 12));
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridx = 0;
        gc.gridy = ++yindex;
        gc.gridwidth = 1;
        topPnl.add(cfgTypLab, gc);
        
        // properties radio button
        cfgPropRadio = new JRadioButton("Properties");
        cfgPropRadio.setFont(new Font(null, Font.BOLD, 12));
        cfgPropRadio.setSelected(true);
        gc.gridx = 1;
        gc.gridy = yindex;
        gc.gridwidth = 1;
        topPnl.add(cfgPropRadio, gc);
        
        // xml radio button
        cfgXmlRadio = new JRadioButton("XML");
        cfgXmlRadio.setFont(new Font(null, Font.BOLD, 12));
        gc.gridx = 2;
        gc.gridy = yindex;
        gc.gridwidth = 1;
        topPnl.add(cfgXmlRadio, gc);
        
        // button panel
        JPanel btnPnl = new JPanel(new FlowLayout(FlowLayout.CENTER));
        gc.gridx = 0;
        gc.gridy = ++yindex;
        gc.gridwidth = 4;
        topPnl.add(btnPnl, gc);
        
        // start button
        startButton = new JButton("Start");
        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                startServer();
            }
        });
        btnPnl.add(startButton);
        
        // stop button
        stopButton = new JButton("Stop");
        stopButton.setEnabled(false);
        stopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                stopServer();
            }
        });
        btnPnl.add(stopButton);
        
        // suspend button
        suspendButton = new JToggleButton("Suspend");
        suspendButton.setEnabled(false);
        suspendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                suspendServer();
            }
        });
        btnPnl.add(suspendButton);
        
        // bottom license text area
        JTextArea txtArea = new JTextArea();
        txtArea.setFont(new Font("Monospaced", Font.PLAIN, 10));
        txtArea.setEditable(false);
        
        // load start.html
        InputStream is = null;
        InputStreamReader rd = null;
        try {
            is = getClass().getClassLoader().getResourceAsStream("org/apache/ftpserver/gui/LICENSE.txt");
            if (is != null) {
                rd = new InputStreamReader(is);
                txtArea.read(rd, null);
            }
        }
        catch(IOException ex) {
        }
        finally {
            IoUtils.close(rd);
            IoUtils.close(is);
        }
        
        // editor scroll pane
        JScrollPane editorScrollPane = new JScrollPane(txtArea);
        add(editorScrollPane, BorderLayout.CENTER);
        
        // type button group 
        ButtonGroup typeButtonGroup = new ButtonGroup();
        typeButtonGroup.add(cfgPropRadio);
        typeButtonGroup.add(cfgXmlRadio);
        
        // source button group
        ButtonGroup sourceButtonGroup = new ButtonGroup();
        sourceButtonGroup.add(cfgFileRadio);
        sourceButtonGroup.add(cfgClasspathRadio);
    }
    
    
    /**
     * Refresh the panel with the new ftp config.
     */
    public void refresh(IFtpConfig ftpCOnfig) {
    }
    
    
    /**
     * This panel can always be displayed.
     */
    public boolean canBeDisplayed() {
        return true;
    }
    
    
    /**
     * Start the server.
     */
    public void startServer() {
        
        // already started - no need to do it again
        if(server != null) {
            return;
        }
        
        InputStream in = null;
        try {
            
            // get appropriate stream
            String path = cfgFileTxt.getText();
            if(cfgFileRadio.isSelected()) {
                in = new FileInputStream(path);
            }
            else {
                // strip the first slash (/) if exists
                if( (path.length() > 0) && (path.charAt(0) == '/') ) {
                    path = path.substring(1);
                }
                in = getClass().getClassLoader().getResourceAsStream(path);
                if (in == null) {
                    GuiUtils.showErrorMessage(RootPanel.this,
                                    "Configuration file " + path
                                  + " not found in classpath"
                                  + " nor inside any of classpath JARs.");
                    return;
                }
            }
            
            // create configuration object
            Configuration config = null;
            if(cfgPropRadio.isSelected()) {
                config = new PropertiesConfiguration(in);
            }
            else {
                XmlConfigurationHandler xmlHandler = new XmlConfigurationHandler(in);
                config = xmlHandler.parse();
            }
            
            // create ftp configuration object
            IFtpConfig fconfig = new FtpConfigImpl(config);
            
            // start server
            server = new FtpServer(fconfig);
            server.start();
            
            // enabled/disable components
            cfgFileTxt.setEnabled(false);
            cfgPropRadio.setEnabled(false);
            cfgXmlRadio.setEnabled(false);
            cfgFileRadio.setEnabled(false);
            cfgClasspathRadio.setEnabled(false);
            browseButton.setEnabled(false);
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            suspendButton.setEnabled(true);
            suspendButton.setSelected(false);
            
            // refresh the container
            getContainer().refresh(server.getFtpConfig());
        }
        catch(Exception ex) {
            server = null;
            ex.printStackTrace();
            GuiUtils.showErrorMessage(RootPanel.this, "Cannot start FTP server.");
        }
        finally {
            IoUtils.close(in);
        }
    }
    
    /**
     * Stop the server.
     */
    public void stopServer() {
        
        // not running - no need to stop it.
        if(server == null) {
            return;
        }
        
        server.stop();
        server = null;
    
        // enable/disable components
        cfgFileTxt.setEnabled(true);
        cfgPropRadio.setEnabled(true);
        cfgXmlRadio.setEnabled(true);
        cfgFileRadio.setEnabled(true);
        cfgClasspathRadio.setEnabled(true);
        browseButton.setEnabled(true);
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        suspendButton.setEnabled(false);
        suspendButton.setSelected(false);
        
        // refresh the container
        getContainer().refresh(null);
    }
    
    
    /**
     * Suspend server.
     */
    private void suspendServer() {
        
        // not running - no need to do anything.
        if(server == null) {
            return;
        }
        
        if(suspendButton.isSelected()) {
            server.suspend();
        }
        else {
            server.resume();
        }
    }
    
    
    /**
     * Get the panel name.
     */
    public String toString() {
        return "FTP";
    }
}
