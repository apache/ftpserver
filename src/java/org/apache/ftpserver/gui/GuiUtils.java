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

import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Rectangle;
import javax.swing.JFileChooser;
import javax.swing.ImageIcon;
import javax.swing.JWindow;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.ftpserver.util.IoUtils;

/**
 * This is the GUI utility class.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public
class GuiUtils {

    private static JFileChooser mDirChoose   = null;
    private static JFileChooser mFileChoose  = null;

    private static final GuiUtils SELF_REF   = new GuiUtils();

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
                while ( -1 != (count = is.read(buff)) ) {
                    out.write(buff, 0, count);
                }
                buff = out.toByteArray();
                if (buff.length != 0){
                   return new ImageIcon(buff);
                }
            }
        }
        catch(IOException ex) {
        }
        finally {
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
        while( (frameComp != null) && !(frameComp instanceof java.awt.Frame) ) {
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
                                      JOptionPane.INFORMATION_MESSAGE );
    }



    /**
     * Get confirmation.
     */
    public static boolean getConfirmation(Component parent, String str) {

        int res = JOptionPane.showConfirmDialog(parent,
                                                str,
                                                "Confirmation",
                                                JOptionPane.YES_NO_OPTION,
                                                JOptionPane.QUESTION_MESSAGE
                                               );
        return(res == JOptionPane.YES_OPTION);
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
        comp.setLocation((wDim.width - cDim.width)/2, (wDim.height - cDim.height)/2);
    }

    /**
     * Position with respect to the parent component.
     */
    public static void setLocation(Component comp, Component parent)  {
        Dimension cDim = comp.getSize();
        Rectangle pRect = parent.getBounds();
        int x = pRect.x + (pRect.width - cDim.width)/2;
        int y = pRect.y + (pRect.height - cDim.height)/2;
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
