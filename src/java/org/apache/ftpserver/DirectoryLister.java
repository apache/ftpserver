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
package org.apache.ftpserver;

import org.apache.ftpserver.ftplet.FileObject;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.util.DateUtils;
import org.apache.ftpserver.util.RegularExpr;

import java.io.IOException;
import java.io.Writer;
import java.util.StringTokenizer;

/**
 * This class prints file listing.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class DirectoryLister {

    private final static char[] NEWLINE  = {'\r', '\n'};
    private final static char DELIM    = ' ';
    
    private FileSystemView m_fileSystemView;
    
    private boolean m_isAllOption;
    private boolean m_isDetailOption; 
    
    private String m_file;
    private String m_pattern;
    private char m_permission[] = new char[10];
    
    
    /**
     * Constructor - set the file system view.
     */
    public DirectoryLister(FileSystemView fileSystemView) {
        m_fileSystemView = fileSystemView;
        for(int i=3; i<10; ++i) {
            m_permission[i] = '-';
        }
    }
    
    /**
     * Parse argument.
     */
    private boolean parse(String argument) {
        String lsDirName = "./";
        String options = "";
        String pattern = "*";
        
        // get different tokens
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
        m_isAllOption = options.indexOf('a') != -1;
        m_isDetailOption = options.indexOf('l') != -1;
        
        // check regular expression
        if( !isRegexp(lsDirName) ) {
            m_pattern = null;
            m_file = lsDirName;
            return true;
        }
        
        try {
            
            // get the directory part and the egular expression part
            int slashIndex = lsDirName.lastIndexOf('/');
            if(slashIndex == -1) {
                pattern = lsDirName;
                lsDirName = "./";
            }
            else if( slashIndex != (lsDirName.length() -1) ) {
                pattern = lsDirName.substring(slashIndex+1);
                lsDirName = lsDirName.substring(0, slashIndex+1);
            }
            else {
                return false;
            }
            
            // check path
            FileObject file = m_fileSystemView.getFileObject(lsDirName);
            if(file == null) {
                return false;
            }
            
            // parent is not a directory - no need to process
            if(!file.isDirectory()) {
                return false;
            }
        }
        catch(FtpException ex) {
            ex.printStackTrace();
            return false;
        }

        m_file = lsDirName;
        if(pattern.equals("*") || pattern.equals("")) {
            m_pattern = null;
        }
        else {
            m_pattern = pattern;
        }
        return true;
    }
    
    
    /**
     * Is a regular expression?
     */
    private boolean isRegexp(String str) {
        return (str.indexOf('*') != -1) ||
               (str.indexOf('?') != -1) ||
               (str.indexOf('[') != -1);
    }
    
    /**
     * Print file list.
     * <pre>
     *   -l : detail listing
     *   -a : display all (including hidden files)
     * </pre>
     */
    public boolean printNList(String argument, Writer out) throws IOException {
        
        // parse argument
        if(!parse(argument)) {
            return false;
        }
        
        FileObject[] files = null;
        try {
            files = m_fileSystemView.listFiles(m_file);
        }
        catch(FtpException ex) {
        }
        if(files == null) {
            return false;
        }
        
        RegularExpr regexp = null;
        if(m_pattern != null) {
            regexp = new RegularExpr(m_pattern);
        }
        for(int i=0; i<files.length; i++) {
            if(files[i] == null) {
                continue;
            }
            if ( (!m_isAllOption) && files[i].isHidden() ) {
                continue;
            }
            if( (regexp != null) && (!regexp.isMatch(files[i].getShortName())) ) {
                continue;
            }
            
            if(m_isDetailOption) {
                printLine(files[i], out);
            }
            else {
                out.write(files[i].getShortName());
            }
            out.write(NEWLINE);
        }
        out.flush();
        return true;
    }
    
    /**
     * Print file list. Detail listing.
     * <pre>
     *   -a : display all (including hidden files)
     * </pre>
     * @return true if success
     */
    public boolean printList(String argument, Writer out) throws IOException {
        
        // parse argument
        if(!parse(argument)) {
            return false;
        }
        
        FileObject[] files = null;
        try {
            files = m_fileSystemView.listFiles(m_file);
        }
        catch(FtpException ex) {
        }
        if(files == null) {
            return false;
        }
        
        // Arrays.sort(files, new FileComparator());
        RegularExpr regexp = null;
        if(m_pattern != null) {
            regexp = new RegularExpr(m_pattern);
        }
        for(int i=0; i<files.length; i++) {
            if(files[i] == null) {
                continue;
            }
            if ( (!m_isAllOption) && files[i].isHidden() ) {
                continue;
            }
            if( (regexp != null) && (!regexp.isMatch(files[i].getShortName())) ) {
                continue;
            }
            printLine(files[i], out);
            out.write(NEWLINE);
         }
        out.flush();
        return true;
    }
    
    /**
     * Get each directory line.
     */
    private void printLine(FileObject file, Writer out) throws IOException {
        out.write(getPermission(file));
        out.write(DELIM);
        out.write(DELIM);
        out.write(DELIM);
        out.write(String.valueOf(file.getLinkCount()));
        out.write(DELIM);
        out.write(file.getOwnerName());
        out.write(DELIM);
        out.write(file.getGroupName());
        out.write(DELIM);
        out.write(getLength(file));
        out.write(DELIM);
        out.write(getLastModified(file));
        out.write(DELIM);
        out.write(file.getShortName());
    }
    
    
    /**
     * Get permission string.
     */
    private char[] getPermission(FileObject file) {
        m_permission[0] = file.isDirectory() ? 'd' : '-';
        m_permission[1] = file.hasReadPermission() ? 'r' : '-';
        m_permission[2] = file.hasWritePermission() ? 'w' : '-';
        return m_permission;
    }
    
    /**
     * Get size
     */
    private String getLength(FileObject file) {
        String initStr = "            ";
        long sz = 0;
        if(file.isFile()) {
            sz = file.getSize();
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
    private String getLastModified(FileObject file) {
        return DateUtils.getUnixDate( file.getLastModified() );
    }
}
