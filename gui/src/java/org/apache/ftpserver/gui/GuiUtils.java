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

import org.apache.ftpserver.util.IoUtils;

import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This is the GUI utility class.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public
class GuiUtils {

    private static JFileChooser mDirChoose = null;
    private static JFileChooser mFileChoose = null;

    private static final GuiUtils SELF_REF = new GuiUtils();

    /**
     * Create image icon.
     */
    public static ImageIcon createImageIcon(String imgResource) {
        InputStream is = null;
        ByteArrayOutputStream out = null;
        try {
            is = SELF_REF.getClass().getClassLoader().getResourceAsStream(imgResource);
            if (is != null) {
                out = new ByteArrayOutputStream();
                byte buff[] = new byte[2048];
                int count = 0;
                while (-1 != (count = is.read(buff))) {
                    out.write(buff, 0, count);
                }
                buff = out.toByteArray();
                if (buff.length != 0) {
                    return new ImageIcon(buff);
                }
            }
        } catch (IOException ex) {
        } finally {
            IoUtils.close(is);
            IoUtils.close(out);
        }
        return null;
    }


    /**
     * Create splash window. Returns null if image not found.
     */
    public static JWindow createSplashWindow(String imgResource) {

        ImageIcon icon = createImageIcon(imgResource);
        if (icon == null) {
            return null;
        }

        JLabel lab = new JLabel();
        lab.setIcon(icon);

        Dimension iDim = new Dimension(icon.getIconWidth(), icon.getIconHeight());

        JWindow splashWin = new JWindow();
        splashWin.getContentPane().add(lab);
        splashWin.setSize(iDim);
        setLocation(splashWin);
        return splashWin;
    }

    /**
     * Get top frame. May return null, if parent frame not found.
     */
    public static Component getFrame(Component comp) {
        Component frameComp = comp;
        while ((frameComp != null) && !(frameComp instanceof java.awt.Frame)) {
            frameComp = frameComp.getParent();
        }
        return frameComp;
    }


    /**
     * Display error message.
     */
    public static void showErrorMessage(Component parent, String str) {
        JOptionPane.showMessageDialog(parent, str, "Error!",
                JOptionPane.ERROR_MESSAGE);
    }


    /**
     * Display warning message.
     */
    public static void showWarningMessage(Component parent, String str) {
        JOptionPane.showMessageDialog(parent, str, "Warning!",
                JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Display information.
     */
    public static void showInformationMessage(Component parent, String str) {
        JOptionPane.showMessageDialog(parent, str, "Information!",
                JOptionPane.INFORMATION_MESSAGE);
    }


    /**
     * Get confirmation.
     */
    public static boolean getConfirmation(Component parent, String str) {

        int res = JOptionPane.showConfirmDialog(parent,
                str,
                "Confirmation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        return (res == JOptionPane.YES_OPTION);
    }


    /**
     * Get file name.
     */
    public static String getFileName(Component parent) {

        if (mFileChoose == null) {
            mFileChoose = new JFileChooser();
            mFileChoose.setFileSelectionMode(JFileChooser.FILES_ONLY);
        }

        int returnVal = mFileChoose.showOpenDialog(parent);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            return mFileChoose.getSelectedFile().getAbsolutePath();
        } else {
            return null;
        }
    }

    /**
     * Get directory name.
     */
    public static String getDirName(Component parent) {

        if (mDirChoose == null) {
            mDirChoose = new JFileChooser();
            mDirChoose.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        }

        int returnVal = mDirChoose.showOpenDialog(parent);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            return mDirChoose.getSelectedFile().getAbsolutePath();
        } else {
            return null;
        }
    }

    /**
     * Update component look & feel.
     */
    public static void updateLnF() {

        if (mDirChoose != null) {
            SwingUtilities.updateComponentTreeUI(mDirChoose);
        }
        if (mFileChoose != null) {
            SwingUtilities.updateComponentTreeUI(mFileChoose);
        }
    }

    /**
     * Position properly - center.
     */
    public static void setLocation(Component comp) {
        Dimension cDim = comp.getSize();
        Dimension wDim = Toolkit.getDefaultToolkit().getScreenSize();
        comp.setLocation((wDim.width - cDim.width) / 2, (wDim.height - cDim.height) / 2);
    }

    /**
     * Position with respect to the parent component.
     */
    public static void setLocation(Component comp, Component parent) {
        Dimension cDim = comp.getSize();
        Rectangle pRect = parent.getBounds();
        int x = pRect.x + (pRect.width - cDim.width) / 2;
        int y = pRect.y + (pRect.height - cDim.height) / 2;
        comp.setLocation(x, y);
    }

    /**
     * Display a new panel. First removes all children, then add.
     */
    public static void showNewPanel(JPanel parent, JPanel child) {
        parent.removeAll();
        parent.add(child);
        parent.validate();
        parent.repaint();
    }

}
