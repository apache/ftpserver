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
package org.apache.ftpserver.util;

import java.io.File;
import java.io.Writer;
import java.io.IOException;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Arrays;
import java.util.Date;
import java.util.StringTokenizer;

/**
 * This class is responsible to handle all virtual directory activities.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public
class VirtualDirectory implements Serializable {

    private static final String NEWLINE  = "\r\n";
    private static final String DELIM    = " ";

    private String mstRoot        = "/";
    private String mstCurrDir     = "/";

    private boolean mbWritePerm   = false;


    /**
     * Default constructor does nothing
     */
    public VirtualDirectory() {
    }

    /**
     * Set write permission.
     */
    public void setWritePermission(boolean perm) {
        mbWritePerm = perm;
    }

    /**
     * Set root directory. Root directory string will always
     * end with '/'.
     */
    public void setRootDirectory(File root) {

       if(root == null) {
           root = new File("/");
       }
       mstRoot = normalizeSeparateChar(root.getAbsolutePath());

       // if not ends with '/' - add one
       if(mstRoot.charAt(mstRoot.length()-1) != '/') {
           mstRoot = mstRoot + '/';
       }
       mstCurrDir = "/";
    }

    /**
     * Set root directory.
     */
    public void setRootDirectory(String root) throws IOException {
       mstRoot = normalizeSeparateChar(root);

       // if not ends with '/' - add one
       if(mstRoot.charAt(mstRoot.length()-1) != '/') {
           mstRoot = mstRoot + '/';
       }
       mstCurrDir = "/";
    }

    /**
     * Get write permission in this system
     */
    public boolean getWritePermission() {
        return mbWritePerm;
    }

    /**
     * Get current working directory.
     */
    public String getCurrentDirectory() {
        return mstCurrDir;
    }


    /**
     * Get root directory.
     */
    public String getRootDirectory() {
        return mstRoot;
    }


    /**
     * Get physical name (wrt the machine root).
     */
    public String getPhysicalName(String virtualName) {
        virtualName = normalizeSeparateChar(virtualName);
        return replaceDots(virtualName);
    }


    /**
     * Get virtual name (wrt the virtual root).
     * The return value will never end with '/' unless it is '/'.
     */
    public String getAbsoluteName(String virtualName) {
        virtualName = normalizeSeparateChar(virtualName);
        String physicalName = replaceDots(virtualName);

        String absoluteName = physicalName.substring(mstRoot.length()-1).trim();
        return removeLastSlash(absoluteName);
    }


    /**
     * Get virtual name (wrt the virtual root). The virtual
     * name will never end with '/' unless it is '/'.
     */
    public String getVirtualName(String physicalName) {
        physicalName = normalizeSeparateChar(physicalName);
        if (!physicalName.startsWith(mstRoot)) {
            return null;
        }

        String virtualName = physicalName.substring(mstRoot.length()-1).trim();
        return removeLastSlash(virtualName);
    }


    /**
     * Change directory. The current directory will never have '/'
     * at the end unless it is '/'.
     * @param dirName change the current working directory.
     * @return true if success
     */
    public boolean changeDirectory(String virtualDir) {

        String physcialDir = getPhysicalName(virtualDir);
        if (physcialDir.equals("")) {
            physcialDir = "/";
        }

        File dirFl = new File(physcialDir);
        if (dirFl.exists() && dirFl.isDirectory()) {
            mstCurrDir = physcialDir.substring(mstRoot.length() - 1).trim();
            mstCurrDir = removeLastSlash(mstCurrDir);
            return true;
        }

        return false;
    }


    /**
     * Check read permission.
     */
    public boolean hasReadPermission(String fileName, boolean bPhysical) {
        if(bPhysical) {
            fileName = normalizeSeparateChar(fileName);
        }
        else {
            fileName = getPhysicalName(fileName);
        }

        if(!fileName.startsWith(mstRoot)) {
            return false;
        }

        return new File(fileName).canRead();
    }


    /**
     * Chech file write/delete permission.
     */
    public boolean hasWritePermission(String fileName, boolean bPhysical) {

        // no write permission
        if(!mbWritePerm) {
            return false;
        }

        // if virtual name - get the physical name
        if(bPhysical) {
            fileName = normalizeSeparateChar(fileName);
        }
        else {
            fileName = getPhysicalName(fileName);
        }

        if(!fileName.startsWith(mstRoot)) {
            return false;
        }

        return new File(fileName).canWrite();
    }


    /**
     * Check file create permission.
     */
    public boolean hasCreatePermission(String fileName, boolean bPhysical) {

        // no write permission
        if(!mbWritePerm) {
            return false;
        }

        // if virtual name - get the physical name
        if(bPhysical) {
            fileName = normalizeSeparateChar(fileName);
        }
        else {
            fileName = getPhysicalName(fileName);
        }

        return fileName.startsWith(mstRoot);
    }


    /**
     * Print file list. Detail listing.
     * <pre>
     *   -a : display all (including hidden files)
     * </pre>
     * @return true if success
     */
    public boolean printList(String argument, Writer out) throws IOException {

        FileLister lister = new FileLister(argument);
        File[] flLst = lister.getFiles();
        if (flLst == null) {
            return false;
        }
        else {
            for(int i=0; i<flLst.length; i++) {
                if ( (!lister.isAll()) && flLst[i].isHidden() ) {
                    continue;
                }
                printLine(flLst[i], out);
                out.write(NEWLINE);
            }
            return true;
        }
    }


    /**
     * Print file list.
     * <pre>
     *   -l : detail listing
     *   -a : display all (including hidden files)
     * </pre>
     * @return true if success
     */
    public boolean printNList(String argument, Writer out) throws IOException {

        FileLister lister = new FileLister(argument);
        File[] flLst = lister.getFiles();
        if (flLst == null) {
            return false;
        }
        else {
            for(int i=0; i<flLst.length; i++) {
                if ( (!lister.isAll()) && flLst[i].isHidden() ) {
                    continue;
                }
                if(lister.isDetail()) {
                    printLine(flLst[i], out);
                }
                else {
                    out.write(getName(flLst[i]));
                }
                out.write(NEWLINE);
            }
            return true;
        }
    }

    /**
     * Get file owner.
     */
    private String getOwner(File fl) {
        return "user";
    }


    /**
     * Get group name
     */
    private String getGroup(File fl) {
        return "group";
    }


    /**
     * Get link count
     */
    private String getLinkCount(File fl) {
        if(fl.isDirectory()) {
            return String.valueOf(3);
        }
        else {
            return String.valueOf(1);
        }
    }


    /**
     * Get size
     */
    private String getLength(File fl) {
        String initStr = "            ";
        long sz = 0;
        if(fl.isFile()) {
            sz = fl.length();
        }
        String szStr = String.valueOf(sz);
        if(szStr.length() > initStr.length()) {
            return szStr;
        }
        return initStr.substring(0, initStr.length() - szStr.length()) + szStr;
    }


    /**
     * Get last modified date string.
     */
    private String getLastModified(File fl) {
        long modTime = fl.lastModified();
        Date date = new Date(modTime);
        return DateUtils.getUnixDate(date);
    }


    /**
     * Get file name.
     */
    private String getName(File fl) {
        return fl.getName();
    }


    /**
     * Get permission string.
     */
    private String getPermission(File fl) {

        StringBuffer sb = new StringBuffer(13);
        if(fl.isDirectory()) {
            sb.append('d');
        }
        else {
            sb.append('-');
        }

        if (fl.canRead()) {
            sb.append('r');
        }
        else {
            sb.append('-');
        }

        if (mbWritePerm && fl.canWrite()) {
            sb.append('w');
        }
        else {
            sb.append('-');
        }
        sb.append("-------");
        return sb.toString();
    }


    /**
     * Normalize separate characher. Separate character should be '/' always.
     */
    private String normalizeSeparateChar(String pathName) {
       pathName = pathName.replace(File.separatorChar, '/');
       pathName = pathName.replace('\\', '/');
       return pathName;
    }


    /**
     * Replace dots. Returns physical name.
     * @param inArg the virtaul name
     */
    private String replaceDots(String inArg) {

        // get the starting directory
        String resArg;
        if(inArg.charAt(0) != '/') {
            resArg = mstRoot + mstCurrDir.substring(1);
        }
        else {
            resArg = mstRoot;
        }

        // strip last '/'
        if(resArg.charAt(resArg.length() -1) == '/') {
            resArg = resArg.substring(0, resArg.length()-1);
        }

        // replace ., ~ and ..
        StringTokenizer st = new StringTokenizer(inArg, "/");
        while(st.hasMoreTokens()) {

            String tok = st.nextToken().trim();

            // . => current directory
            if(tok.equals(".")) {
                continue;
            }

            // .. => parent directory (if not root)
            if(tok.equals("..")) {
                if(resArg.startsWith(mstRoot)) {
                  int slashIndex = resArg.lastIndexOf('/');
                  if(slashIndex != -1) {
                    resArg = resArg.substring(0, slashIndex);
                  }
                }
                continue;
            }

            // ~ => home directory (in this case /)
            if (tok.equals("~")) {
                resArg = mstRoot.substring(0, mstRoot.length()-1).trim();
                continue;
            }

            resArg = resArg + '/' + tok;
        }

        // add last slash if necessary
        if( !inArg.equals("") && (inArg.charAt(inArg.length()-1)=='/') ) {
            resArg = resArg + '/';
        }

        // final check
        if (resArg.length() < mstRoot.length()) {
            resArg = mstRoot;
        }

        return resArg;
    }


    /**
     * Get each directory line.
     */
    private void printLine(File fl, Writer out) throws IOException {
        out.write(getPermission(fl));
        out.write(DELIM);
        out.write(DELIM);
        out.write(DELIM);
        out.write(getLinkCount(fl));
        out.write(DELIM);
        out.write(getOwner(fl));
        out.write(DELIM);
        out.write(getGroup(fl));
        out.write(DELIM);
        out.write(getLength(fl));
        out.write(DELIM);
        out.write(getLastModified(fl));
        out.write(DELIM);
        out.write(getName(fl));
    }

    /**
     * If the string is not '/', remove last slash.
     */
    private String removeLastSlash(String str) {
        if ( (str.length()>1) && (str.charAt(str.length()-1)=='/') ) {
            str = str.substring(0, str.length()-1);
        }
        return str;
    }



    //////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////
    /**
     * Inner class to compare files
     */
    private class FileComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            String s1 = ((File)o1).getName();
            String s2 = ((File)o2).getName();
            return s1.compareTo(s2);
        }
    }


    /////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////
    /**
     * File lister to list files properly
     */
    private class FileLister {

        private File[] files = null;
        private boolean bAll = false;
        private boolean bDetail = false;

        /**
         * Parse arguments - get options and get file list.
         */
        public FileLister(String argument) {
            String lsDirName = "./";
            String options = "";
            String pattern   = "*";

            // get options, directory name and pattern
            if(argument != null) {
                argument = argument.trim();
                StringBuffer optionsSb = new StringBuffer(4);
                StringTokenizer st = new StringTokenizer(argument, " ");
                while(st.hasMoreTokens()) {
                    String token = st.nextToken();
                    if(token.charAt(0) == '-') {
                        if (token.length() > 1) {
                            optionsSb.append(token.substring(1));
                        }
                    }
                    else {
                       lsDirName = token;
                    }
                }
                options = optionsSb.toString();
            }

            // check options
            bAll = options.indexOf('a') != -1;
            bDetail = options.indexOf('l') != -1;

            // check pattern
            lsDirName = getPhysicalName(lsDirName);
            File lstDirObj = new File(lsDirName);
            if ( !(lstDirObj.exists() && lstDirObj.isDirectory()) ) {
                int slashIndex = lsDirName.lastIndexOf('/');
                if( (slashIndex != -1) && (slashIndex != (lsDirName.length() -1)) ) {
                    pattern = lsDirName.substring(slashIndex+1);
                    lsDirName = lsDirName.substring(0, slashIndex+1);
                }
            }

            // check directory
            lstDirObj = new File(lsDirName);
            if(!lstDirObj.exists()) {
                return;
            }
            if(!lstDirObj.isDirectory()) {
                return;
            }

            // get file list
            if ( (pattern == null) || pattern.equals("*") || pattern.equals("") ) {
                files = lstDirObj.listFiles();
            }
            else {
                files = lstDirObj.listFiles(new FileRegularFilter(pattern));
            }
            //Arrays.sort(files, new FileComparator());
        }


        /**
         * Get files
         */
        public File[] getFiles() {
            return files;
        }

        /**
         * Display all flag
         */
        public boolean isAll() {
            return bAll;
        }

        /**
         * Display detail flag
         */
        public boolean isDetail() {
            return bDetail;
        }
    }

}
