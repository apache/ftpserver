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

public class MockFtpletCallback implements Ftplet {

    public static FtpletEnum returnValue;
    
    public void destroy() {
    }

    public void init(FtpletContext ftpletContext, Configuration config) throws FtpException {
    }

    public FtpletEnum onAppendEnd(FtpSession session, FtpRequest request, FtpResponse response) throws FtpException, IOException {
        return returnValue;
    }

    public FtpletEnum onAppendStart(FtpSession session, FtpRequest request, FtpResponse response) throws FtpException, IOException {
        return returnValue;
    }

    public FtpletEnum onConnect(FtpSession session, FtpResponse response) throws FtpException, IOException {
        return returnValue;
    }

    public FtpletEnum onDeleteEnd(FtpSession session, FtpRequest request, FtpResponse response) throws FtpException, IOException {
        return returnValue;
    }

    public FtpletEnum onDeleteStart(FtpSession session, FtpRequest request, FtpResponse response) throws FtpException, IOException {
        return returnValue;
    }

    public FtpletEnum onDisconnect(FtpSession session, FtpResponse response) throws FtpException, IOException {
        return returnValue;
    }

    public FtpletEnum onDownloadEnd(FtpSession session, FtpRequest request, FtpResponse response) throws FtpException, IOException {
        return returnValue;
    }

    public FtpletEnum onDownloadStart(FtpSession session, FtpRequest request, FtpResponse response) throws FtpException, IOException {
        return returnValue;
    }

    public FtpletEnum onLogin(FtpSession session, FtpRequest request, FtpResponse response) throws FtpException, IOException {
        return returnValue;
    }

    public FtpletEnum onMkdirEnd(FtpSession session, FtpRequest request, FtpResponse response) throws FtpException, IOException {
        return returnValue;
    }

    public FtpletEnum onMkdirStart(FtpSession session, FtpRequest request, FtpResponse response) throws FtpException, IOException {
        return returnValue;
    }

    public FtpletEnum onRenameEnd(FtpSession session, FtpRequest request, FtpResponse response) throws FtpException, IOException {
        return returnValue;
    }

    public FtpletEnum onRenameStart(FtpSession session, FtpRequest request, FtpResponse response) throws FtpException, IOException {
        return returnValue;
    }

    public FtpletEnum onRmdirEnd(FtpSession session, FtpRequest request, FtpResponse response) throws FtpException, IOException {
        return returnValue;
    }

    public FtpletEnum onRmdirStart(FtpSession session, FtpRequest request, FtpResponse response) throws FtpException, IOException {
        return returnValue;
    }

    public FtpletEnum onSite(FtpSession session, FtpRequest request, FtpResponse response) throws FtpException, IOException {
        return returnValue;
    }

    public FtpletEnum onUploadEnd(FtpSession session, FtpRequest request, FtpResponse response) throws FtpException, IOException {
        return returnValue;
    }

    public FtpletEnum onUploadStart(FtpSession session, FtpRequest request, FtpResponse response) throws FtpException, IOException {
        return returnValue;
    }

    public FtpletEnum onUploadUniqueEnd(FtpSession session, FtpRequest request, FtpResponse response) throws FtpException, IOException {
        return returnValue;
    }

    public FtpletEnum onUploadUniqueStart(FtpSession session, FtpRequest request, FtpResponse response) throws FtpException, IOException {
        return returnValue;
    }


}
