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
package org.apache.ftpserver.ip;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.ftpserver.util.IoUtils;
import org.apache.ftpserver.util.RegularExpr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Collection;
import java.util.Vector;

/**
 * This class provides IP restriction functionality.
 *
 * @phoenix:block
 * @phoenix:service name="org.apache.ftpserver.ip.IpRestrictorInterface"
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public class AvalonFileIpRestrictor extends AbstractIpRestrictor implements Contextualizable, Configurable, LogEnabled {

    private static final String LINE_SEP = System.getProperty("line.separator", "\n");

    private File mIpFile           = null;
    private Vector mAllEntries     = new Vector();
    private Logger logger;

    public void enableLogging(Logger logger) {
        this.logger = logger;
    }

    /**
     * Set application context.
     */
    public void contextualize(Context context) throws ContextException {
        File appDir = (File)context.get("app.home");
        if(!appDir.exists()) {
            appDir.mkdirs();
        }
        mIpFile = new File(appDir, "ip.properties" );
        try {
            reload();
        }
        catch(IOException ex) {
            logger.error("IpRestrictor:contextualize()", ex);
            throw new ContextException("IpRestrictor:contextualize()", ex);
        }
        logger.info("IP restrictor file = " + mIpFile);
    }

    /**
     * Read the list from the file.
     */
    public synchronized void reload() throws IOException {
        BufferedReader br = null;
        Vector newEntries = new Vector();
        try {
            if (mIpFile.exists()) {
                br = IoUtils.getBufferedReader(new FileReader(mIpFile));
                String line = null;
                while((line = br.readLine()) != null) {
                    line = line.trim();
                    if(!line.equals("")) {
                        newEntries.add(line);
                    }
                }
            }
            mAllEntries = newEntries;
        }
        finally {
          IoUtils.close(br);
        }
    }

    /**
     * Get IP resrictor file object.
     */
    public File getFile() {
        return mIpFile;
    }

    /**
     * Save this IP restriction list.
     */
    public synchronized void save() throws IOException {
        FileWriter fw = null;
        try {
            fw = new FileWriter(mIpFile);
            Object[] entries = mAllEntries.toArray();
            for(int i=entries.length; --i>=0; ) {
                fw.write(entries[i].toString());
                fw.write(LINE_SEP);
            }
        }
        finally {
            IoUtils.close(fw);
        }
    }

    /**
     * Check IP permission. Compare it with all the entries in the list.
     */
    public boolean hasPermission(InetAddress addr) {
       boolean bMatch = false;
       Object[] entries = mAllEntries.toArray();
       for(int i=entries.length; --i>=0; ) {
           RegularExpr regExp = new RegularExpr(entries[i].toString());
           bMatch = regExp.isMatch(addr.getHostAddress());
           if(bMatch) {
               break;
           }
       }

       if (isAllowIp()) {
           return bMatch;
       }
       else {
           return !bMatch;
       }
    }

    /**
     * Add a new entry.
     */
    public void addEntry(String entry) {
        entry = entry.trim();
        if(entry.equals("")) {
            return;
        }
        mAllEntries.add(entry);
    }

    /**
     * Remove entry
     */
    public void removeEntry(String entry) {
        mAllEntries.remove(entry);
    }

    /**
     * Get all entries
     */
    public Collection getAllEntries() {
        return (Collection)mAllEntries.clone();
    }

    /**
     * Remove all entries
     */
    public void clear() {
       mAllEntries.clear();
    }

    /**
     * Configure user manager - third step.
     */
    public void configure(Configuration config) throws ConfigurationException {

        // get server address
        Configuration tmpConf = config.getChild("allow-ip", false);
        mbAllowIp = false;
        if(tmpConf != null) {
            mbAllowIp = tmpConf.getValueAsBoolean(mbAllowIp);
        }
    }
}
