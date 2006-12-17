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

    public FtpletEnum onAppendEnd(FtpRequest request, FtpResponse response) throws FtpException, IOException {
        return returnValue;
    }

    public FtpletEnum onAppendStart(FtpRequest request, FtpResponse response) throws FtpException, IOException {
        return returnValue;
    }

    public FtpletEnum onConnect(FtpRequest request, FtpResponse response) throws FtpException, IOException {
        return returnValue;
    }

    public FtpletEnum onDeleteEnd(FtpRequest request, FtpResponse response) throws FtpException, IOException {
        return returnValue;
    }

    public FtpletEnum onDeleteStart(FtpRequest request, FtpResponse response) throws FtpException, IOException {
        return returnValue;
    }

    public FtpletEnum onDisconnect(FtpRequest request, FtpResponse response) throws FtpException, IOException {
        return returnValue;
    }

    public FtpletEnum onDownloadEnd(FtpRequest request, FtpResponse response) throws FtpException, IOException {
        return returnValue;
    }

    public FtpletEnum onDownloadStart(FtpRequest request, FtpResponse response) throws FtpException, IOException {
        return returnValue;
    }

    public FtpletEnum onLogin(FtpRequest request, FtpResponse response) throws FtpException, IOException {
        return returnValue;
    }

    public FtpletEnum onMkdirEnd(FtpRequest request, FtpResponse response) throws FtpException, IOException {
        return returnValue;
    }

    public FtpletEnum onMkdirStart(FtpRequest request, FtpResponse response) throws FtpException, IOException {
        return returnValue;
    }

    public FtpletEnum onRenameEnd(FtpRequest request, FtpResponse response) throws FtpException, IOException {
        return returnValue;
    }

    public FtpletEnum onRenameStart(FtpRequest request, FtpResponse response) throws FtpException, IOException {
        return returnValue;
    }

    public FtpletEnum onRmdirEnd(FtpRequest request, FtpResponse response) throws FtpException, IOException {
        return returnValue;
    }

    public FtpletEnum onRmdirStart(FtpRequest request, FtpResponse response) throws FtpException, IOException {
        return returnValue;
    }

    public FtpletEnum onSite(FtpRequest request, FtpResponse response) throws FtpException, IOException {
        return returnValue;
    }

    public FtpletEnum onUploadEnd(FtpRequest request, FtpResponse response) throws FtpException, IOException {
        return returnValue;
    }

    public FtpletEnum onUploadStart(FtpRequest request, FtpResponse response) throws FtpException, IOException {
        return returnValue;
    }

    public FtpletEnum onUploadUniqueEnd(FtpRequest request, FtpResponse response) throws FtpException, IOException {
        return returnValue;
    }

    public FtpletEnum onUploadUniqueStart(FtpRequest request, FtpResponse response) throws FtpException, IOException {
        return returnValue;
    }


}
