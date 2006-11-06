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

package org.apache.ftpserver.ftplet;

import java.io.IOException;

/**
 * Default ftplet implementation. All the callback method returns null. 
 * It is just an empty implementation. You can derive your ftplet implementation
 * from this class.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class DefaultFtplet implements Ftplet {

    public void init(FtpConfig ftpConfig, Configuration config) throws FtpException {}
    public void destroy() {}
    
    public FtpletEnum onConnect(FtpRequest request, FtpResponse response) throws FtpException, IOException {return null;}
    public FtpletEnum onDisconnect(FtpRequest request, FtpResponse response) throws FtpException, IOException {return null;}
    public FtpletEnum onLogin(FtpRequest request, FtpResponse response) throws FtpException, IOException {return null;}
    
    public FtpletEnum onDeleteStart(FtpRequest request, FtpResponse response) throws FtpException, IOException {return null;}
    public FtpletEnum onDeleteEnd(FtpRequest request, FtpResponse response) throws FtpException, IOException {return null;}
    
    public FtpletEnum onUploadStart(FtpRequest request, FtpResponse response) throws FtpException, IOException {return null;}
    public FtpletEnum onUploadEnd(FtpRequest request, FtpResponse response) throws FtpException, IOException {return null;}
    
    public FtpletEnum onDownloadStart(FtpRequest request, FtpResponse response) throws FtpException, IOException {return null;}
    public FtpletEnum onDownloadEnd(FtpRequest request, FtpResponse response) throws FtpException, IOException {return null;}
    
    public FtpletEnum onRmdirStart(FtpRequest request, FtpResponse response) throws FtpException, IOException {return null;}
    public FtpletEnum onRmdirEnd(FtpRequest request, FtpResponse response) throws FtpException, IOException {return null;}
    
    public FtpletEnum onMkdirStart(FtpRequest request, FtpResponse response) throws FtpException, IOException {return null;}
    public FtpletEnum onMkdirEnd(FtpRequest request, FtpResponse response) throws FtpException, IOException {return null;}
        
    public FtpletEnum onAppendStart(FtpRequest request, FtpResponse response) throws FtpException, IOException {return null;}
    public FtpletEnum onAppendEnd(FtpRequest request, FtpResponse response) throws FtpException, IOException {return null;}
    
    public FtpletEnum onUploadUniqueStart(FtpRequest request, FtpResponse response) throws FtpException, IOException {return null;}
    public FtpletEnum onUploadUniqueEnd(FtpRequest request, FtpResponse response) throws FtpException, IOException {return null;}
    
    public FtpletEnum onRenameStart(FtpRequest request, FtpResponse response) throws FtpException, IOException {return null;}
    public FtpletEnum onRenameEnd(FtpRequest request, FtpResponse response) throws FtpException, IOException {return null;}
    
    public FtpletEnum onSite(FtpRequest request, FtpResponse response) throws FtpException, IOException {return null;}
}
