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

import java.io.IOException;
import java.io.Writer;
import java.util.StringTokenizer;

import org.apache.ftpserver.ftplet.FileObject;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.util.DateUtils;
import org.apache.ftpserver.util.RegularExpr;

/**
 * This class prints file listing.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 * @author Birkir A. Barkarson
 */
public 
class DirectoryLister {

    private final static char[] NEWLINE  = {'\r', '\n'};
    private final static char DELIM    = ' ';
    private final static String[] AVAILABLE_TYPES = {
        "Size",
        "Modify",
        "Type",
        "Perm"
    };
    
    
    private FileSystemView m_fileSystemView;
    
    private boolean m_isAllOption;
    private boolean m_isDetailOption; 
    
    private String m_file;
    private String m_pattern;
    private char m_permission[] = new char[10];
    
    private String[] m_selectedTypes = new String[] {"Size", "Modify", "Type"};
    
    
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
     * Get selected types.
     */
    public String[] getSelectedTypes() {
        String types[] = new String[m_selectedTypes.length];
        System.arraycopy(m_selectedTypes, 0, types, 0, m_selectedTypes.length);
        return types;
    }
    
    /**
     * Returns true if, and only if, the string passed was
     * successfully parsed as valid types.
     */
    public boolean setSelectedTypes(String types[]) {
        
        // ignore null types
        if(types == null) {
            return false;
        }
        
        // check all the types
        for(int i=0; i<types.length; ++i) {
            boolean bMatch = false;
            for(int j=0; j<AVAILABLE_TYPES.length; ++j) {
                if(AVAILABLE_TYPES[j].equals(types[i])) {
                    bMatch = true;
                    break;
                }
            }
            if(!bMatch) {
                return false;
            }
        }
        
        // set the user types
        m_selectedTypes = new String[types.length];
        System.arraycopy(types, 0, m_selectedTypes, 0, types.length);
        return true;
    }
    
    /**
     * Print file list. The server will return a stream of names of 
     * files and no other information if detail listing flag is false.
     * <pre>
     *   -l : detail listing
     *   -a : display all (including hidden files)
     * </pre>
     */
    public boolean doNLST(String argument, Writer out) throws IOException {
        
        // parse argument
        if(!parse(argument)) {
            return false;
        }
        
        // get all the file objects
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
    public boolean doLIST(String argument, Writer out) throws IOException {
        
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
     * Print path info. Machine listing. It prints the listing
     * information for a single file.
     *
     * @return true if success
     */
    public boolean doMLST(String argument, Writer out) throws IOException {

        // check argument
        if(argument == null) {
            argument = "./";
        }
        
        FileObject file = null;
        try {
            file = m_fileSystemView.getFileObject(argument);
            if(file == null) {
                return false;
            }
            if(!file.doesExist()) {
                return false;
            }
        }
        catch(FtpException ex) {
        }
        if(file == null) {
            return false;
        }
        
        printMLine(file, out);
        return true;
    }
    
    /**
     * Print directory contents. Machine listing.
     * <pre>
     *   -a : display all (including hidden files)
     * </pre>
     * 
     * @return true if success
     */
    public boolean doMLSD(String argument, Writer out) throws IOException {
        
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
            printMLine(files[i], out);
            out.write(NEWLINE);
         }
        out.flush();
        return true;
    }
    
    /**
     * Parse argument.
     */
    private boolean parse(String argument) {
        String lsFileName = "./";
        String options = "";
        String pattern = "*";
        
        // find options and file name (may have regular expression)
        if(argument != null) {
            argument = argument.trim();
            StringBuffer optionsSb = new StringBuffer(4);
            StringBuffer lsFileNameSb = new StringBuffer(16);
            StringTokenizer st = new StringTokenizer(argument, " ", true);
            while(st.hasMoreTokens()) {
                String token = st.nextToken();
                
                if(lsFileNameSb.length() != 0) {
                    // file name started - append to file name buffer
                    lsFileNameSb.append(token);
                }
                else if(token.equals(" ")) {
                    // delimiter and file not started - ignore
                    continue;
                } 
                else if(token.charAt(0) == '-') {
                    // token and file name not started - append to options buffer
                    if (token.length() > 1) {
                        optionsSb.append(token.substring(1));
                    }
                }
                else {
                    // filename - append to the filename buffer
                    lsFileNameSb.append(token);
                }
            }
            
            if(lsFileNameSb.length() != 0) {
                lsFileName = lsFileNameSb.toString();
            }
            options = optionsSb.toString();
        }
        m_isAllOption = options.indexOf('a') != -1;
        m_isDetailOption = options.indexOf('l') != -1;
        
        // check regular expression
        if( (lsFileName.indexOf('*') == -1) &&
            (lsFileName.indexOf('?') == -1) &&
            (lsFileName.indexOf('[') == -1) ) {
            m_pattern = null;
            m_file = lsFileName;
            return true;
        }
        
        // get the directory part and the egular expression part
        try {
            int slashIndex = lsFileName.lastIndexOf('/');
            if(slashIndex == -1) {
                pattern = lsFileName;
                lsFileName = "./";
            }
            else if( slashIndex != (lsFileName.length() -1) ) {
                pattern = lsFileName.substring(slashIndex+1);
                lsFileName = lsFileName.substring(0, slashIndex+1);
            }
            else {
                return false;
            }
            
            // check path
            FileObject file = m_fileSystemView.getFileObject(lsFileName);
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

        m_file = lsFileName;
        if( "*".equals(pattern) || "".equals(pattern) ) {
            m_pattern = null;
        }
        else {
            m_pattern = pattern;
        }
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
    
    /**
     * Print each file line.
     */
    private void printMLine(FileObject file, Writer out) throws IOException {
        for(int i=0; i<m_selectedTypes.length; ++i) {
            String type = m_selectedTypes[i];
            if(type.equalsIgnoreCase("size")) {
                out.write("Size=");
                out.write(String.valueOf(file.getSize()));
                out.write(';');
            }
            else if(type.equalsIgnoreCase("modify")) {
                String timeStr = DateUtils.getFtpDate( file.getLastModified() );
                out.write("Modify=");
                out.write(timeStr);
                out.write(';');
            }
            else if(type.equalsIgnoreCase("type")) {
                if(file.isFile()) {
                    out.write("Type=file;");
                }
                else if(file.isDirectory()) {
                    out.write("Type=dir;");
                }
            }
            else if(type.equalsIgnoreCase("perm")) {
                out.write("Perm=");
                if(file.hasReadPermission()) {
                    if(file.isFile()) {
                        out.write('r');
                    }
                    else if(file.isDirectory()) {
                        out.write('e');
                        out.write('l');
                    }
                }
                if(file.hasWritePermission()) {
                    if(file.isFile()) {
                        out.write('a');
                        out.write('d');
                        out.write('f');
                        out.write('w');
                    }
                    else if(file.isDirectory()) {
                        out.write('f');
                        out.write('p');
                        out.write('c');
                        out.write('m');
                    }
                }
                out.write(';');
            }
        }
        out.write(' ');
        out.write(file.getShortName());
    }
}
