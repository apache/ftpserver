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

import java.util.Vector;
import java.awt.Component;
import javax.swing.JTree;
import javax.swing.JPanel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeModel;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.event.TreeModelListener;
import org.apache.ftpserver.remote.interfaces.RemoteHandlerInterface;

/**
 * This is FTP user interface tree structure.
 * It looks like:
 * <pre>
 *   FTP
 *    |
 *    +-- User (User management)
 *    |
 *    +-- Ip (IP restrictions)
 *    |
 *    +-- Connection (Connection monitor)
 *    |
 *    +-- Spy (Spy user activities)
 *    |
 *    +-- Statistics (Global statistics)
 *    |
 *    +-- Upload (File upload statistics)
 *    |
 *    +-- Download (File download statistics)
 *    |
 *    +-- Delete (File deletion statistics)
 *    |
 *    +-- About (Ftp server summary)
 * </pre>
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */

public
class FtpTree extends JTree implements TreeModel {

    public static final String[] CHILDREN = {
        "User",
        "IP",
        "Connection",
        "Spy",
        "Statistics",
        "Upload",
        "Download",
        "Delete",
        "About"
    };

    private Vector mListenrList;

    private Component mTopFrame;
    private FtpRootPanel  mRootPanel;
    private PluginPanel[] mPluginPanels;
    private CommonHandler mCommonHandler;

    /**
     * create this tree model
     */
    public FtpTree(CommonHandler commonHandler) {
        mCommonHandler = commonHandler;
        mListenrList = new Vector();
        setModel(this);

        setSelectionPath(new TreePath(RemoteHandlerInterface.DISPLAY_NAME));
        putClientProperty("JTree.lineStyle", "Angled");

        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
        renderer.setLeafIcon(null);
        renderer.setOpenIcon(null);
        renderer.setClosedIcon(null);
        setCellRenderer(renderer);

        initPlugins();
        mRootPanel = new FtpRootPanel(mCommonHandler, this);
    }

    /**
     * Initialize all plugin panels
     */
    private void initPlugins() {
        mPluginPanels = new PluginPanel[CHILDREN.length];

        mPluginPanels[0] = new FtpUserPanel(mCommonHandler, this);
        mPluginPanels[1] = new FtpIpPanel(mCommonHandler, this);
        mPluginPanels[2] = new FtpConnectionPanel(mCommonHandler, this);
        mPluginPanels[3] = new FtpSpyContainerPanel(mCommonHandler, this);
        mPluginPanels[4] = new FtpStatisticsPanel(mCommonHandler, this);
        mPluginPanels[5] = new FtpFilePanel(mCommonHandler, this, ((FtpStatisticsPanel)mPluginPanels[4]).getUploadModel(),   "Uploaded Files");
        mPluginPanels[6] = new FtpFilePanel(mCommonHandler, this, ((FtpStatisticsPanel)mPluginPanels[4]).getDownloadModel(), "Downloaded Files");
        mPluginPanels[7] = new FtpFilePanel(mCommonHandler, this, ((FtpStatisticsPanel)mPluginPanels[4]).getDeleteModel(),   "Deleted Files");
        mPluginPanels[8] = new FtpAboutPanel(mCommonHandler, this);
    }


    /**
     * get root object
     */
    public Object getRoot() {
        return RemoteHandlerInterface.DISPLAY_NAME;
    }

    /**
     * get child object
     */
    public Object getChild(Object parent, int index) {
        return CHILDREN[index];
    }

    /**
     * get child count
     */
    public int getChildCount(Object parent) {
        if(parent.equals(RemoteHandlerInterface.DISPLAY_NAME)) {
            return CHILDREN.length;
        }
        return 0;
    }

    /**
     * is a leaf or node
     */
    public boolean isLeaf(Object node) {
       return !node.equals(RemoteHandlerInterface.DISPLAY_NAME);
    }

    /**
     * get child index
     */
    public int getIndexOfChild(Object parent, Object child) {
       int childIdx = -1;
       for(int i=0; i<CHILDREN.length; i++) {
           if(CHILDREN[i].equals(child)) {
               childIdx = i;
               break;
           }
       }
       return childIdx;
    }

    /**
     * Object changed. In our case it is not possible - so igmore it.
     */
    public void valueForPathChanged(TreePath path, Object newValue) {
    }

    /**
     * add a listener
     */
    public void addTreeModelListener(TreeModelListener l) {
        mListenrList.add(l);
    }

    /**
     * remove a listener
     */
    public void removeTreeModelListener(TreeModelListener l) {
        mListenrList.remove(l);
    }

    /**
     * Get root panel.
     */
    public FtpRootPanel getRootPanel() {
        return mRootPanel;
    }


    /**
     * Get top frame
     */
    public Component getTopFrame() {
        if(mTopFrame == null) {
            mTopFrame = GuiUtils.getFrame(this);
        }
        return mTopFrame;
    }


    /**
     * Get plugin panel.
     */
    public PluginPanel getPluginPanel(String panelName) {
        PluginPanel panel = null;
        for(int i=0; i<CHILDREN.length; i++) {
            if (CHILDREN[i].equals(panelName)) {
                panel = mPluginPanels[i];
                break;
            }
        }
        return panel;
    }


    /**
     * Get the selected panel.
     */
    public JPanel getSelectedPanel() {
        Object node = getSelectionPath().getLastPathComponent();

        JPanel dispPane = null;
        if(getRoot().equals(node)) {
            dispPane = mRootPanel;
        }
        else {
            dispPane = getPluginPanel(node.toString());
            if ( dispPane == null ) {
                dispPane = mRootPanel;
            }
        }

        return dispPane;
    }

    /**
     * Close all panels
     */
    public void close() {
        if (mRootPanel != null) {
            mRootPanel.close();
        }

        for(int i=mPluginPanels.length; --i>=0; ) {
            PluginPanel panel = mPluginPanels[i];
            if (panel != null) {
                panel.close();
            }
        }
    }

}
