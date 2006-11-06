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

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

/**
 * Common UI utility methods.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class GuiUtils {

    /**
     * Create image icon.
     */
    public final static ImageIcon createImageIcon(String resource) {
        ImageIcon icon = null;
        URL imgUrl = GuiUtils.class.getClassLoader().getResource(resource);
        if(imgUrl != null) {
            try {
                icon = new ImageIcon(imgUrl);
            }
            catch(Exception ex) {
            }
        }
        return icon;
    }
    
    /**
     * Display error message.
     */
    public final static void showErrorMessage(Component parent, String str) {
        JOptionPane.showMessageDialog(parent, str, "Error!",
                                      JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Display warning message.
     */
    public final static void showWarningMessage(Component parent, String str) {
        JOptionPane.showMessageDialog(parent, str, "Warning!",
                                      JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Display information.
     */
    public final static void showInformationMessage(Component parent, String str) {
        JOptionPane.showMessageDialog(parent, str, "Information!",
                                      JOptionPane.INFORMATION_MESSAGE );
    }

    /**
     * Get confirmation.
     */
    public final static boolean getConfirmation(Component parent, String str) {

        int res = JOptionPane.showConfirmDialog(parent, 
                                                str,
                                                "Confirmation",
                                                JOptionPane.YES_NO_OPTION, 
                                                JOptionPane.QUESTION_MESSAGE 
                                               );
        return(res == JOptionPane.YES_OPTION); 
    }
    
    /**
     * Position properly - center.
     */
    public final static void setLocation(Component comp) {
        Dimension cDim = comp.getSize();
        Dimension wDim = Toolkit.getDefaultToolkit().getScreenSize();
        comp.setLocation((wDim.width - cDim.width)/2, (wDim.height - cDim.height)/2);
    }
    
    /**
     * Position with respect to the parent component.
     */
    public final static void setLocation(Component comp, Component parent)  {
        Dimension cDim = comp.getSize();
        Rectangle pRect = parent.getBounds();
        int x = pRect.x + (pRect.width - cDim.width)/2;
        int y = pRect.y + (pRect.height - cDim.height)/2;
        comp.setLocation(x, y);
    }
    
    /**
     * Display a new panel. First removes all children, then add.
     */
    public final static void showNewPanel(Container parent, Component child) {
        parent.removeAll();
        parent.add(child);
        parent.validate();
        parent.repaint();
    }
}
