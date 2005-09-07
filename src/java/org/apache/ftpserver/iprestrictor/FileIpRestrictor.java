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
package org.apache.ftpserver.iprestrictor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;

import org.apache.ftpserver.ftplet.Configuration;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.Logger;
import org.apache.ftpserver.interfaces.IIpRestrictor;
import org.apache.ftpserver.util.IoUtils;
import org.apache.ftpserver.util.RegularExpr;

/**
 * File based IP restrictor.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class FileIpRestrictor implements IIpRestrictor {

    private final static String LINE_SEP = System.getProperty("line.separator", "\n");
    
    private String m_file;
    private Object[][] m_permissions;
    private Logger m_logger;
    
    
    /**
     * Set logger.
     */
    public void setLogger(Logger logger) {
        m_logger = logger;
    }
    
    /**
     * Configure the IP restrictor.
     */
    public void configure(Configuration config) throws FtpException {
        m_file = config.getString("file", "./res/ip.gen");
        File dir = new File(m_file).getParentFile();
        if( (!dir.exists()) && (!dir.mkdirs()) ) {
            String dirName = dir.getAbsolutePath();
            m_logger.error("Cannot create directory - " + dirName);
            throw new FtpException("Cannot create directory : " + dirName);
        }
        
        m_permissions = getPermissions();
    }
    
    /**
     * Has the permission?
     */
    public boolean hasPermission(InetAddress address) throws FtpException {
        String addressStr = address.getHostAddress();
        boolean retVal = true;
        for(int i=0; i<m_permissions.length; ++i) {
            String ipPattern = (String)m_permissions[i][0];
            RegularExpr regexp = new RegularExpr(ipPattern);
            if(regexp.isMatch(addressStr)) {
                retVal = ((Boolean)m_permissions[i][1]).booleanValue();
                break;
            }
        }
        return retVal;
    }
    
    /**
     * Get permission array.
     */
    public Object[][] getPermissions() throws FtpException {
        
        ArrayList permList = new ArrayList();
        if(new File(m_file).exists()) {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(m_file));
                String line = null;
                while((line = br.readLine()) != null) {
                    line = line.trim();
                    if(line.equals("")) {
                        continue;
                    }
                    
                    int spaceIndex = line.indexOf(' ');
                    if(spaceIndex == -1) {
                        continue;
                    }
                    
                    String boolStr = line.substring(0, spaceIndex);
                    String regexpStr = line.substring(spaceIndex + 1);
                    Object[] entry = { regexpStr, new Boolean(boolStr.equals("true")) };
                    permList.add(entry);
                }
            }
            catch(IOException ex) {
                m_logger.error("FileIpRestrictor.getPermissions()", ex);
                throw new FtpException("FileIpRestrictor.getPermissions()", ex);
            }
            finally {
              IoUtils.close(br);
            }
        }
        
        // return array
        Object[][] permissions = new Object[permList.size()][2];
        for(int i=0; i<permissions.length; ++i) {
            permissions[i] = (Object[])permList.get(i);
        }
        return permissions;
    }   
    
    /**
     * Set permission array.
     */
    public void setPermissions(Object[][] permissions) throws FtpException {
        FileWriter fw = null;
        try {
            fw = new FileWriter(m_file);
            for(int i=0; i<permissions.length; ++i) {
                fw.write(String.valueOf(permissions[i][1]));
                fw.write(' ');
                fw.write(String.valueOf(permissions[i][0]));
                fw.write(LINE_SEP);
            }
            m_permissions = permissions;
        }
        catch(IOException ex) {
            m_logger.error("FileIpRestrictor.setPermissions()", ex);
            throw new FtpException("FileIpRestrictor.setPermissions()", ex);
        }
        finally {
            IoUtils.close(fw);
        }
    }
    
    /**
     * Release all the resources
     */
    public void dispose() {
    }
}
