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
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.apache.ftpserver.interfaces.FtpServerContext;

/**
 * Tree plugin panel container.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class TreePluginPanelContainer extends JSplitPane 
                               implements PluginPanelContainer, 
                                          TreeModel, 
                                          TreeSelectionListener {
    
    private static final long serialVersionUID = -6807863406907626635L;
    
    private ArrayList pluginPanels = new ArrayList();
    private Vector treeListeners = new Vector();
    
    private FtpServerContext fconfig;
    
    private JPanel rightPane;
    private JTree tree;
    
    /**
     * Add plugin panel.
     */
    public void add(PluginPanel panel) {
        pluginPanels.add(panel);
    }
    
    /**
     * Get the plugin panel at the specifid index.
     */
    public PluginPanel getPluginPanel(int index) {
        return (PluginPanel)pluginPanels.get(index);
    }
    
    /**
     * Get container panel.
     */
    public JComponent getComponent() {
        setDividerSize(2);
        setDividerLocation(110);
        
        tree = new JTree(this);
        putClientProperty("JTree.lineStyle", "Angled");
        
        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
        renderer.setLeafIcon(null);
        renderer.setOpenIcon(null);
        renderer.setClosedIcon(null);
        tree.setCellRenderer(renderer);
        tree.addTreeSelectionListener(this);
        
        JScrollPane custPane = new JScrollPane(tree, 
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        setLeftComponent(custPane);
        
        rightPane = new JPanel(new BorderLayout());
        setRightComponent(rightPane);
        return this;
    }
    
    /**
     * Get the selected index.
     */
    public int getSelectedIndex() {
        Object node = tree.getSelectionPath().getLastPathComponent();
        return pluginPanels.indexOf(node);
    }
    
    /**
     * Set the selected index.
     */
    public void setSelectedIndex(int index) {
        tree.setSelectionRow(index);
    }
    
    /**
     * Handle tree selection
     */
    public void valueChanged(TreeSelectionEvent e) {
        
        // check selection value
        int rows[] = tree.getSelectionRows();
        if( (rows == null) || (rows.length == 0) ) {
            return;
        }
        
        // return the selected plugin panel
        PluginPanel panel = (PluginPanel)pluginPanels.get(rows[rows.length - 1]);
        if(panel.canBeDisplayed()) {
            GuiUtils.showNewPanel(rightPane, panel);
        }
    }
    
    /**
     * get root object
     */
    public Object getRoot() {
        return pluginPanels.get(0);
    }
    
    /**
     * get child count
     */
    public int getChildCount(Object parent) {
        if(parent == pluginPanels.get(0)) {
            return pluginPanels.size() - 1;
        }
        return 0;
    }
    
    /**
     * is a leaf or node
     */
    public boolean isLeaf(Object node) {
       return (node != pluginPanels.get(0));
    }
    
    /**
     * get child index
     */
    public int getIndexOfChild(Object parent, Object child) {
        return pluginPanels.indexOf(child) - 1;
    }
    
    /**
     * add a listener
     */
    public void addTreeModelListener(TreeModelListener l) {
        treeListeners.add(l);
    }

    /**
     * Object changed. In our case it is not possible - so igmore it.
     */
    public void valueForPathChanged(TreePath path, Object newValue) {
    }
    
    /**
     * remove a listener
     */
    public void removeTreeModelListener(TreeModelListener l) {
        treeListeners.remove(l);
    }
    
    /** 
     * get child object
     */
    public Object getChild(Object parent, int index) {
        return pluginPanels.get(index + 1);
    }
    
    /**
     * Get ftp config.
     */
    public void refresh(FtpServerContext ftpConfig) {
        fconfig = ftpConfig;
        for(int i=0; i<pluginPanels.size(); ++i) {
            PluginPanel ppanel = (PluginPanel)pluginPanels.get(i);
            ppanel.refresh(fconfig);
        }
    }
    
    /**
     * Get ftp config.
     */
    public FtpServerContext getFtpConfig() {
        return fconfig;
    }
}
