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

public class MockFtplet implements Ftplet {

    protected static MockFtpletCallback callback = new MockFtpletCallback();
    
    public void destroy() {
        callback.destroy();
    }

    public void init(FtpConfig ftpConfig, Configuration config) throws FtpException {
        callback.init(ftpConfig, config);
    }

    public FtpletEnum onAppendEnd(FtpRequest request, FtpResponse response) throws FtpException, IOException {
        return callback.onAppendEnd(request, response);
    }

    public FtpletEnum onAppendStart(FtpRequest request, FtpResponse response) throws FtpException, IOException {
        return callback.onAppendStart(request, response);
    }

    public FtpletEnum onConnect(FtpRequest request, FtpResponse response) throws FtpException, IOException {
        return callback.onConnect(request, response);
    }

    public FtpletEnum onDeleteEnd(FtpRequest request, FtpResponse response) throws FtpException, IOException {
        return callback.onDeleteEnd(request, response);
    }

    public FtpletEnum onDeleteStart(FtpRequest request, FtpResponse response) throws FtpException, IOException {
        return callback.onDeleteStart(request, response);
    }

    public FtpletEnum onDisconnect(FtpRequest request, FtpResponse response) throws FtpException, IOException {
        return callback.onDisconnect(request, response);
    }

    public FtpletEnum onDownloadEnd(FtpRequest request, FtpResponse response) throws FtpException, IOException {
        return callback.onDownloadEnd(request, response);
    }

    public FtpletEnum onDownloadStart(FtpRequest request, FtpResponse response) throws FtpException, IOException {
        return callback.onDownloadStart(request, response);
    }

    public FtpletEnum onLogin(FtpRequest request, FtpResponse response) throws FtpException, IOException {
        return callback.onLogin(request, response);
    }

    public FtpletEnum onMkdirEnd(FtpRequest request, FtpResponse response) throws FtpException, IOException {
        return callback.onMkdirEnd(request, response);
    }

    public FtpletEnum onMkdirStart(FtpRequest request, FtpResponse response) throws FtpException, IOException {
        return callback.onMkdirStart(request, response);
    }

    public FtpletEnum onRenameEnd(FtpRequest request, FtpResponse response) throws FtpException, IOException {
        return callback.onRenameEnd(request, response);
    }

    public FtpletEnum onRenameStart(FtpRequest request, FtpResponse response) throws FtpException, IOException {
        return callback.onRenameStart(request, response);
    }

    public FtpletEnum onRmdirEnd(FtpRequest request, FtpResponse response) throws FtpException, IOException {
        return callback.onRmdirEnd(request, response);
    }

    public FtpletEnum onRmdirStart(FtpRequest request, FtpResponse response) throws FtpException, IOException {
        return callback.onRmdirStart(request, response);
    }

    public FtpletEnum onSite(FtpRequest request, FtpResponse response) throws FtpException, IOException {
        return callback.onSite(request, response);
    }

    public FtpletEnum onUploadEnd(FtpRequest request, FtpResponse response) throws FtpException, IOException {
        return callback.onUploadEnd(request, response);
    }

    public FtpletEnum onUploadStart(FtpRequest request, FtpResponse response) throws FtpException, IOException {
        return callback.onUploadStart(request, response);
    }

    public FtpletEnum onUploadUniqueEnd(FtpRequest request, FtpResponse response) throws FtpException, IOException {
        return callback.onUploadUniqueEnd(request, response);
    }

    public FtpletEnum onUploadUniqueStart(FtpRequest request, FtpResponse response) throws FtpException, IOException {
        return callback.onUploadUniqueStart(request, response);
    }
}

