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

package org.apache.ftpserver.listing;

import java.io.IOException;

import org.apache.ftpserver.ftplet.FileObject;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;

/**
 * This class prints file listing.
 */
public 
class DirectoryLister {

    
    private String traverseFiles(final FileObject[] files, final FileFilter filter, final FileFormater formater) {
        StringBuffer sb = new StringBuffer();
        for(int i=0; i<files.length; i++) {
            if(files[i] == null) {
                continue;
            }
            
            if(filter == null || filter.accept(files[i])) {
                sb.append(formater.format(files[i]));
            }
         }
        
        return sb.toString();
    }
    
    public String listFiles(final ListArgument argument, final FileSystemView fileSystemView, final FileFormater formater) throws IOException {
       
        StringBuffer sb = new StringBuffer();
        
        // get all the file objects
        FileObject[] files = listFiles(fileSystemView, argument.getFile());
        if(files != null) {
            FileFilter filter = null;
            if ( (argument.hasOption('a'))) {
                filter = new VisibleFileFilter();
            }
            if(argument.getPattern() != null) {
                filter = new RegexFileFilter(argument.getPattern(), filter);
            }
            
            sb.append(traverseFiles(files, filter, formater));
        }
        
        return sb.toString();
    }
    
    /**
     * Get the file list.
     */
    private FileObject[] listFiles(FileSystemView fileSystemView, String file) {
    	FileObject[] files = null;
    	try {
	    	FileObject virtualFile = fileSystemView.getFileObject(file);
	    	if(virtualFile.isFile()) {
	    		files = new FileObject[] {virtualFile};
	    	}
	    	else {
	    		files = virtualFile.listFiles();
	    	}
    	}
    	catch(FtpException ex) {
    	}
    	return files;
    }
}
