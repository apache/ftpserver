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

import org.apache.ftpserver.remote.interfaces.RemoteHandlerInterface;

import javax.swing.*;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.Vector;

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
    private FtpRootPanel mRootPanel;
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
        mPluginPanels[5] = new FtpFilePanel(mCommonHandler, this, ((FtpStatisticsPanel) mPluginPanels[4]).getUploadModel(), "Uploaded Files");
        mPluginPanels[6] = new FtpFilePanel(mCommonHandler, this, ((FtpStatisticsPanel) mPluginPanels[4]).getDownloadModel(), "Downloaded Files");
        mPluginPanels[7] = new FtpFilePanel(mCommonHandler, this, ((FtpStatisticsPanel) mPluginPanels[4]).getDeleteModel(), "Deleted Files");
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
        if (parent.equals(RemoteHandlerInterface.DISPLAY_NAME)) {
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
        for (int i = 0; i < CHILDREN.length; i++) {
            if (CHILDREN[i].equals(child)) {
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
        if (mTopFrame == null) {
            mTopFrame = GuiUtils.getFrame(this);
        }
        return mTopFrame;
    }


    /**
     * Get plugin panel.
     */
    public PluginPanel getPluginPanel(String panelName) {
        PluginPanel panel = null;
        for (int i = 0; i < CHILDREN.length; i++) {
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
        if (getRoot().equals(node)) {
            dispPane = mRootPanel;
        } else {
            dispPane = getPluginPanel(node.toString());
            if (dispPane == null) {
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

        for (int i = mPluginPanels.length; --i >= 0;) {
            PluginPanel panel = mPluginPanels[i];
            if (panel != null) {
                panel.close();
            }
        }
    }

}
