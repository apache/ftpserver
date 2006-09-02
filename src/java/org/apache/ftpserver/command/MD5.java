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

package org.apache.ftpserver.command;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.ftpserver.FtpRequestImpl;
import org.apache.ftpserver.FtpWriter;
import org.apache.ftpserver.RequestHandler;
import org.apache.ftpserver.ftplet.FileObject;
import org.apache.ftpserver.interfaces.ICommand;
import org.apache.ftpserver.util.IoUtils;

/**
 * <code>MD5 &lt;SP&gt; &lt;pathname&gt; &lt;CRLF&gt;</code><br>
 * <code>MMD5 &lt;SP&gt; &lt;pathnames&gt; &lt;CRLF&gt;</code><br>
 *
 * Returns the MD5 value for a file or multiple files according to 
 * draft-twine-ftpmd5-00.txt.
 * 
 */
public 
class MD5 implements ICommand {

    /**
     * Execute command.
     */
    public void execute(RequestHandler handler,
                        FtpRequestImpl request, 
                        FtpWriter out) throws IOException {
        
        // reset state variables
        request.resetState();

        boolean isMMD5 = false;
        
        if("MMD5".equals(request.getCommand())) {
            isMMD5 = true;
        }
        
        // print file information
        String argument = request.getArgument();

        String[] fileNames = null;
        if(isMMD5) {
            fileNames = argument.split(",");
        } else {
            fileNames = new String[]{argument};
        }

        StringBuffer sb = new StringBuffer();
        for(int i = 0; i<fileNames.length; i++) {
            String fileName = fileNames[i].trim();
            
            // get file object
            FileObject file = null;
            
            try {
                file = request.getFileSystemView().getFileObject(fileName);
            }
            catch(Exception ex) {
                // TODO: handle exception
            }
            
            if(file == null) {
                out.send(504, "MD5.invalid", fileName);
                return;
            }
    
            // check file
            if(!file.isFile()) {
                out.send(504, "MD5.invalid", fileName);
                return;
            }
            
            InputStream is = null;
            ByteArrayOutputStream baos = null;
            try{
                is = file.createInputStream(0);
                baos = new ByteArrayOutputStream();
                
                IoUtils.copy(is, baos, 1024);
                
                // try to close before returning command, 
                // potentital race condition otherwise 
                IoUtils.close(is);
                IoUtils.close(baos);
                
                String md5Hash = DigestUtils.md5Hex(baos.toByteArray());
                md5Hash = md5Hash.toUpperCase();
    
                if(i > 0) {
                    sb.append(", ");
                }

                sb.append(fileName);
                sb.append(' ');
                sb.append(md5Hash);
                
                
            } finally {
                // Just to make sure
                IoUtils.close(is);
                IoUtils.close(baos);
            }
        }
        if(isMMD5) {
            out.send(252, "MMD5", sb.toString());
        } else {
            out.send(251, "MD5", sb.toString());
        }
        return;
    }   
}
