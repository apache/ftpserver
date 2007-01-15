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

    public void init(FtpletContext ftpletContext, Configuration config) throws FtpException {}
    public void destroy() {}
    
    public FtpletEnum onConnect(FtpSession session, FtpResponseOutput response) throws FtpException, IOException {return null;}
    public FtpletEnum onDisconnect(FtpSession session, FtpResponseOutput response) throws FtpException, IOException {return null;}
    public FtpletEnum onLogin(FtpSession session, FtpRequest request, FtpResponseOutput response) throws FtpException, IOException {return null;}
    
    public FtpletEnum onDeleteStart(FtpSession session, FtpRequest request, FtpResponseOutput response) throws FtpException, IOException {return null;}
    public FtpletEnum onDeleteEnd(FtpSession session, FtpRequest request, FtpResponseOutput response) throws FtpException, IOException {return null;}
    
    public FtpletEnum onUploadStart(FtpSession session, FtpRequest request, FtpResponseOutput response) throws FtpException, IOException {return null;}
    public FtpletEnum onUploadEnd(FtpSession session, FtpRequest request, FtpResponseOutput response) throws FtpException, IOException {return null;}
    
    public FtpletEnum onDownloadStart(FtpSession session, FtpRequest request, FtpResponseOutput response) throws FtpException, IOException {return null;}
    public FtpletEnum onDownloadEnd(FtpSession session, FtpRequest request, FtpResponseOutput response) throws FtpException, IOException {return null;}
    
    public FtpletEnum onRmdirStart(FtpSession session, FtpRequest request, FtpResponseOutput response) throws FtpException, IOException {return null;}
    public FtpletEnum onRmdirEnd(FtpSession session, FtpRequest request, FtpResponseOutput response) throws FtpException, IOException {return null;}
    
    public FtpletEnum onMkdirStart(FtpSession session, FtpRequest request, FtpResponseOutput response) throws FtpException, IOException {return null;}
    public FtpletEnum onMkdirEnd(FtpSession session, FtpRequest request, FtpResponseOutput response) throws FtpException, IOException {return null;}
        
    public FtpletEnum onAppendStart(FtpSession session, FtpRequest request, FtpResponseOutput response) throws FtpException, IOException {return null;}
    public FtpletEnum onAppendEnd(FtpSession session, FtpRequest request, FtpResponseOutput response) throws FtpException, IOException {return null;}
    
    public FtpletEnum onUploadUniqueStart(FtpSession session, FtpRequest request, FtpResponseOutput response) throws FtpException, IOException {return null;}
    public FtpletEnum onUploadUniqueEnd(FtpSession session, FtpRequest request, FtpResponseOutput response) throws FtpException, IOException {return null;}
    
    public FtpletEnum onRenameStart(FtpSession session, FtpRequest request, FtpResponseOutput response) throws FtpException, IOException {return null;}
    public FtpletEnum onRenameEnd(FtpSession session, FtpRequest request, FtpResponseOutput response) throws FtpException, IOException {return null;}
    
    public FtpletEnum onSite(FtpSession session, FtpRequest request, FtpResponseOutput response) throws FtpException, IOException {return null;}
}
