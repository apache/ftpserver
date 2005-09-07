// $Id$
/*
 * Copyright 2004 The Apache Software Foundation
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
 */
package org.apache.ftpserver.gui;

import org.apache.ftpserver.interfaces.IFtpConfig;

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
    
    private ArrayList m_pluginPanels = new ArrayList();
    private Vector m_treeListenrs = new Vector();
    
    private IFtpConfig m_fconfig;
    
    private JPanel m_rightPane;
    private JTree m_tree;
    
    /**
     * Add plugin panel.
     */
    public void add(PluginPanel panel) {
        m_pluginPanels.add(panel);
    }
    
    /**
     * Get the plugin panel at the specifid index.
     */
    public PluginPanel getPluginPanel(int index) {
        return (PluginPanel)m_pluginPanels.get(index);
    }
    
    /**
     * Get container panel.
     */
    public JComponent getComponent() {
        setDividerSize(2);
        setDividerLocation(110);
        
        m_tree = new JTree(this);
        putClientProperty("JTree.lineStyle", "Angled");
        
        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
        renderer.setLeafIcon(null);
        renderer.setOpenIcon(null);
        renderer.setClosedIcon(null);
        m_tree.setCellRenderer(renderer);
        m_tree.addTreeSelectionListener(this);
        
        JScrollPane custPane = new JScrollPane(m_tree, 
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        setLeftComponent(custPane);
        
        m_rightPane = new JPanel(new BorderLayout());
        setRightComponent(m_rightPane);
        return this;
    }
    
    /**
     * Get the selected index.
     */
    public int getSelectedIndex() {
        Object node = m_tree.getSelectionPath().getLastPathComponent();
        return m_pluginPanels.indexOf(node);
    }
    
    /**
     * Set the selected index.
     */
    public void setSelectedIndex(int index) {
        m_tree.setSelectionRow(index);
    }
    
    /**
     * Handle tree selection
     */
    public void valueChanged(TreeSelectionEvent e) {
        
        // check selection value
        int rows[] = m_tree.getSelectionRows();
        if( (rows == null) || (rows.length == 0) ) {
            return;
        }
        
        // return the selected plugin panel
        PluginPanel panel = (PluginPanel)m_pluginPanels.get(rows[rows.length - 1]);
        if(panel.canBeDisplayed()) {
            GuiUtils.showNewPanel(m_rightPane, panel);
        }
    }
    
    /**
     * get root object
     */
    public Object getRoot() {
        return m_pluginPanels.get(0);
    }
    
    /**
     * get child count
     */
    public int getChildCount(Object parent) {
        if(parent == m_pluginPanels.get(0)) {
            return m_pluginPanels.size() - 1;
        }
        return 0;
    }
    
    /**
     * is a leaf or node
     */
    public boolean isLeaf(Object node) {
       return (node != m_pluginPanels.get(0));
    }
    
    /**
     * get child index
     */
    public int getIndexOfChild(Object parent, Object child) {
        return m_pluginPanels.indexOf(child) - 1;
    }
    
    /**
     * add a listener
     */
    public void addTreeModelListener(TreeModelListener l) {
        m_treeListenrs.add(l);
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
        m_treeListenrs.remove(l);
    }
    
    /** 
     * get child object
     */
    public Object getChild(Object parent, int index) {
        return m_pluginPanels.get(index + 1);
    }
    
    /**
     * Get ftp config.
     */
    public void refresh(IFtpConfig ftpConfig) {
        m_fconfig = ftpConfig;
        for(int i=0; i<m_pluginPanels.size(); ++i) {
            PluginPanel ppanel = (PluginPanel)m_pluginPanels.get(i);
            ppanel.refresh(m_fconfig);
        }
    }
    
    /**
     * Get ftp config.
     */
    public IFtpConfig getFtpConfig() {
        return m_fconfig;
    }
}
