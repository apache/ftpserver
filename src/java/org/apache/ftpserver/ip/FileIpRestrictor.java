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
package org.apache.ftpserver.ip;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Collection;
import java.util.Vector;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.ftpserver.util.IoUtils;
import org.apache.ftpserver.util.RegularExpr;
import org.apache.avalon.phoenix.BlockContext;


/**
 * This class provides IP restriction functionality.
 *
 * @phoenix:block
 * @phoenix:service name="org.apache.ftpserver.ip.IpRestrictorInterface"
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public class FileIpRestrictor extends AbstractIpRestrictor {

    private static final String LINE_SEP = System.getProperty("line.separator", "\n");

    private File mIpFile           = null;
    private Vector mAllEntries     = new Vector();

    /**
     * Set application context.
     */
    public void contextualize(Context context) throws ContextException {
        super.contextualize(context);
        mIpFile = new File( ((BlockContext)context).getBaseDirectory(), "ip.properties" );
        try {
            reload();
        }
        catch(IOException ex) {
            getLogger().error("IpRestrictor:contextualize()", ex);
            throw new ContextException("IpRestrictor:contextualize()", ex);
        }
        getLogger().info("IP restrictor file = " + mIpFile);
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
}
