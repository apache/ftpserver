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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;


/**
 * Generic directory chooser dialog panel.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class DirChooser extends JDialog implements TreeSelectionListener,
                                            TreeWillExpandListener,
                                            FileFilter {
    
    private static final long serialVersionUID = 7363990421558828943L;

    private final static FileSystemView FILE_VIEW = FileSystemView.getFileSystemView();
    
    private JTree m_tree;
    private JTextField m_dirText;
    private JScrollPane m_scrollPane;
    
    private JButton m_selectButton;
    private JButton m_cancelButton;
    
    private boolean m_showHidden = false; 
    private String m_selectedDir = null; 
    
    
    /**
     * Constructor - set the selected directory.
     */
    private DirChooser(Component comp, String title, File selDir) {
        super(JOptionPane.getFrameForComponent(comp), title, true);
        initComponents();
        selectDirectory(selDir);
    }

    /**
     * Initialize UI components
     */
    private void initComponents() {
        
        Container container = getContentPane();
        container.setLayout(new BorderLayout());
        
        // add the tabbed pane
        JTabbedPane tabPane = new JTabbedPane();
        container.add(tabPane, BorderLayout.CENTER);
        
        // top pane
        JPanel topPanel = new JPanel(new BorderLayout());
        tabPane.add("Directory", topPanel);
        
        // file tree
        m_tree = new JTree();
        m_scrollPane = new JScrollPane(m_tree);
        topPanel.add(m_scrollPane, BorderLayout.CENTER);
        
        // directory text panel
        JPanel dirTextPanel = new JPanel(new BorderLayout());
        dirTextPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        topPanel.add(dirTextPanel, BorderLayout.SOUTH);
        
        // directory label
        JLabel dirLab = new JLabel("Directory:  ");
        dirTextPanel.add(dirLab, BorderLayout.WEST);
        
        // directory text filed
        m_dirText = new JTextField();
        dirTextPanel.add(m_dirText, BorderLayout.CENTER);
        
        // add the bottom pane
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        container.add(bottomPanel, BorderLayout.SOUTH);
        
        // select button
        m_selectButton = new JButton("Select");
        m_selectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                m_selectedDir = m_dirText.getText();
                DirChooser.this.dispose();
            }
        });
        bottomPanel.add(m_selectButton);
        
        // cancel button
        m_cancelButton = new JButton("Cancel");
        m_cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                m_selectedDir = null;
                DirChooser.this.dispose();
            }
        });
        bottomPanel.add(m_cancelButton);
        
        // initialize 
        populateTree();
        setSize(300, 300);
    }
    
    
    /**
     * Populate tree
     */
    private void populateTree() {
        
        // create model
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("My Computer");
        DefaultTreeModel model = new DefaultTreeModel(rootNode, true);
        
        // add root drives
        File rootDrives[] = File.listRoots();
        DefaultMutableTreeNode driveNodes[] = new DefaultMutableTreeNode[rootDrives.length];
        for(int i=0; i<rootDrives.length; ++i) {
            driveNodes[i] = new DefaultMutableTreeNode(rootDrives[i]);
            rootNode.add(driveNodes[i]);
        }
        
        m_tree.setModel(model);
        m_tree.setRootVisible(false);
        m_tree.setExpandsSelectedPaths(true);
        m_tree.addTreeWillExpandListener(this);
        m_tree.setCellRenderer(new DirTreeCellRenderer());
        m_tree.addTreeSelectionListener(this);
    }
    
    
    /**
     * Select directory.
     */
    public void selectDirectory(File dir) {
        
        // error check
        if(dir == null) {
            return;
        }
        if(!dir.exists()) {
            return;
        }
        
        // resolve the path name
        try {
            dir = dir.getCanonicalFile();
        }
        catch(Exception ex) {
           return;
        }
        
        if(!dir.isDirectory()) {
            dir = dir.getParentFile();
            if(dir == null) {
                return;
            }
        }
        
        ArrayList dirs = new ArrayList();
        while(true) {
            dirs.add(dir);
            if(FILE_VIEW.isRoot(dir)) {
                break;
            }
            
            File parent = dir.getParentFile();
            if(parent == null) {
                break;
            }
            if(!parent.exists()) {
                return;
            }
            dir = parent;
        }
        
        JViewport viewPort = m_scrollPane.getViewport();
        TreeModel model = m_tree.getModel();
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)model.getRoot();
        for(int i=dirs.size(); --i>=0; ) {
            File currDir = (File)dirs.get(i);
            m_tree.expandPath(new TreePath(node.getPath()));
            int count = node.getChildCount();
            int j;
            for(j=0; j<count; ++j) {
                DefaultMutableTreeNode currNode = (DefaultMutableTreeNode)node.getChildAt(j);
                if(currNode.getUserObject().equals(currDir)) {
                    node = currNode;
                    TreePath currPath = new TreePath(currNode.getPath());
                    m_tree.setSelectionPath(currPath);
                    Rectangle rect = m_tree.getPathBounds(currPath);
                    if(rect != null) {
                        viewPort.setViewPosition(rect.getLocation());
                    }
                    break;
                }
            }
            if(j == count) {
                break;
            }
        }
    }
    
    
    /**
     * Handle dialog closing event.
     */ 
    protected void processWindowEvent(WindowEvent e) {
        int id = e.getID();
        if (id == WindowEvent.WINDOW_CLOSING) {
            m_selectedDir = null;
            dispose();
        } 
        else {
            super.processWindowEvent(e);
        }
    }
    
    
    /**
     * Show dialog
     */
    public static String showDialog(Component parent, String title, File initFile) {
        DirChooser chooser = new DirChooser(parent, title, initFile);
        chooser.setLocationRelativeTo(parent);
        
        chooser.setVisible(true);
        return chooser.m_selectedDir;
    }
    
    
    ////////////////////////////////////////////////////////////////////////////
    /////////////////// Tree selection listener implementation /////////////////
    /**
     * Write the dir selection in the text field.
     */
    public void valueChanged(TreeSelectionEvent e) {
        TreePath path = e.getPath(); 
        if(path != null) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
            File dir = (File)node.getUserObject();
            m_dirText.setText(dir.getAbsolutePath());
        }
    } 

    
    /////////////////////////////////////////////////////////////////////////
    /////////////// Tree Will Expand Listener implementation ////////////////
    public void treeWillCollapse(TreeExpansionEvent e) {
    }
    
    
    public void treeWillExpand(TreeExpansionEvent e) {
        
        // get the tree path which will be expanded
        TreePath path = e.getPath();
        if(path == null) {
            return;
        }
        
        // get the directory at the node
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
        File selDir = (File)node.getUserObject();
        
        // remove old entries
        if(node.getChildCount() > 0) {
            node.removeAllChildren();
        }
        
        // get all the subdirectories
        File childDirs[] = selDir.listFiles(this);
        if(childDirs == null) {
            return;
        }
        
        // add the subdirectories
        Arrays.sort(childDirs);
        for(int i = 0; i < childDirs.length; i++) {
            if(childDirs[i].isHidden()) {
                if(m_showHidden) {
                    node.add(new DefaultMutableTreeNode(childDirs[i]));
                }
            }
            else {
                node.add(new DefaultMutableTreeNode(childDirs[i]));
            }
        }
    }
    

    ///////////////////////////////////////////////////////////////////////////
    ////////////////////// File filter implementation /////////////////////////
    public boolean accept(File pathname) {
        return pathname.isDirectory();
    }
    
    
    /////////////////////////////////////////////////////////////////////////////
    /////////////////////////// Tree Renderer Component /////////////////////////
    static class DirTreeCellRenderer extends DefaultTreeCellRenderer {
        
        private static final long serialVersionUID = -200713666464104466L;

        public Component getTreeCellRendererComponent(JTree tree, 
                                                      Object value, 
                                                      boolean selected, 
                                                      boolean expanded, 
                                                      boolean leaf, 
                                                      int row, 
                                                      boolean hasFocus) {
            
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
            File dir = (File)node.getUserObject();
            
            String name = dir.getName().trim();
            if( (name == null) || name.equals("")) {
                name = dir.getAbsolutePath();
            }
            return super.getTreeCellRendererComponent(tree, name, selected, expanded, leaf, row, hasFocus);
        } 

    }
}
