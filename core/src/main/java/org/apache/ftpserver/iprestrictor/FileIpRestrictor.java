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

package org.apache.ftpserver.iprestrictor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;

import org.apache.ftpserver.ftplet.Component;
import org.apache.ftpserver.ftplet.Configuration;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.interfaces.IpRestrictor;
import org.apache.ftpserver.util.IoUtils;
import org.apache.ftpserver.util.RegularExpr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * File based IP restrictor.
 */
public 
class FileIpRestrictor implements IpRestrictor, Component {

    private final Logger LOG = LoggerFactory.getLogger(FileIpRestrictor.class);
    
    private final static String LINE_SEP = System.getProperty("line.separator", "\n");
    
    private String file;
    private Object[][] permissions;
    
    /**
     * Configure the IP restrictor.
     */
    public void configure(Configuration config) throws FtpException {
        file = config.getString("file", "./res/ip.gen");
        File dir = new File(file).getParentFile();
        if( (!dir.exists()) && (!dir.mkdirs()) ) {
            String dirName = dir.getAbsolutePath();
            LOG.error("Cannot create directory - " + dirName);
            throw new FtpException("Cannot create directory : " + dirName);
        }
        
        permissions = getPermissions();
    }
    
    /**
     * Has the permission?
     */
    public boolean hasPermission(InetAddress address) throws FtpException {
        String addressStr = address.getHostAddress();
        boolean retVal = true;
        for(int i=0; i<permissions.length; ++i) {
            String ipPattern = (String)permissions[i][0];
            RegularExpr regexp = new RegularExpr(ipPattern);
            if(regexp.isMatch(addressStr)) {
                retVal = ((Boolean)permissions[i][1]).booleanValue();
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
        if(new File(file).exists()) {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(file));
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
                LOG.error("FileIpRestrictor.getPermissions()", ex);
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
            fw = new FileWriter(file);
            for(int i=0; i<permissions.length; ++i) {
                fw.write(String.valueOf(permissions[i][1]));
                fw.write(' ');
                fw.write(String.valueOf(permissions[i][0]));
                fw.write(LINE_SEP);
            }
            this.permissions = permissions;
        }
        catch(IOException ex) {
            LOG.error("FileIpRestrictor.setPermissions()", ex);
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
