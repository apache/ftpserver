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

    public void init(FtpletContext ftpletContext, Configuration config) throws FtpException {
        callback.init(ftpletContext, config);
    }

    public FtpletEnum onAppendEnd(FtpSession session, FtpRequest request, FtpReplyOutput response) throws FtpException, IOException {
        return callback.onAppendEnd(session, request, response);
    }

    public FtpletEnum onAppendStart(FtpSession session, FtpRequest request, FtpReplyOutput response) throws FtpException, IOException {
        return callback.onAppendStart(session, request, response);
    }

    public FtpletEnum onConnect(FtpSession session, FtpReplyOutput response) throws FtpException, IOException {
        return callback.onConnect(session, response);
    }

    public FtpletEnum onDeleteEnd(FtpSession session, FtpRequest request, FtpReplyOutput response) throws FtpException, IOException {
        return callback.onDeleteEnd(session, request, response);
    }

    public FtpletEnum onDeleteStart(FtpSession session, FtpRequest request, FtpReplyOutput response) throws FtpException, IOException {
        return callback.onDeleteStart(session, request, response);
    }

    public FtpletEnum onDisconnect(FtpSession session, FtpReplyOutput response) throws FtpException, IOException {
        return callback.onDisconnect(session, response);
    }

    public FtpletEnum onDownloadEnd(FtpSession session, FtpRequest request, FtpReplyOutput response) throws FtpException, IOException {
        return callback.onDownloadEnd(session, request, response);
    }

    public FtpletEnum onDownloadStart(FtpSession session, FtpRequest request, FtpReplyOutput response) throws FtpException, IOException {
        return callback.onDownloadStart(session, request, response);
    }

    public FtpletEnum onLogin(FtpSession session, FtpRequest request, FtpReplyOutput response) throws FtpException, IOException {
        return callback.onLogin(session, request, response);
    }

    public FtpletEnum onMkdirEnd(FtpSession session, FtpRequest request, FtpReplyOutput response) throws FtpException, IOException {
        return callback.onMkdirEnd(session, request, response);
    }

    public FtpletEnum onMkdirStart(FtpSession session, FtpRequest request, FtpReplyOutput response) throws FtpException, IOException {
        return callback.onMkdirStart(session, request, response);
    }

    public FtpletEnum onRenameEnd(FtpSession session, FtpRequest request, FtpReplyOutput response) throws FtpException, IOException {
        return callback.onRenameEnd(session, request, response);
    }

    public FtpletEnum onRenameStart(FtpSession session, FtpRequest request, FtpReplyOutput response) throws FtpException, IOException {
        return callback.onRenameStart(session, request, response);
    }

    public FtpletEnum onRmdirEnd(FtpSession session, FtpRequest request, FtpReplyOutput response) throws FtpException, IOException {
        return callback.onRmdirEnd(session, request, response);
    }

    public FtpletEnum onRmdirStart(FtpSession session, FtpRequest request, FtpReplyOutput response) throws FtpException, IOException {
        return callback.onRmdirStart(session, request, response);
    }

    public FtpletEnum onSite(FtpSession session, FtpRequest request, FtpReplyOutput response) throws FtpException, IOException {
        return callback.onSite(session, request, response);
    }

    public FtpletEnum onUploadEnd(FtpSession session, FtpRequest request, FtpReplyOutput response) throws FtpException, IOException {
        return callback.onUploadEnd(session, request, response);
    }

    public FtpletEnum onUploadStart(FtpSession session, FtpRequest request, FtpReplyOutput response) throws FtpException, IOException {
        return callback.onUploadStart(session, request, response);
    }

    public FtpletEnum onUploadUniqueEnd(FtpSession session, FtpRequest request, FtpReplyOutput response) throws FtpException, IOException {
        return callback.onUploadUniqueEnd(session, request, response);
    }

    public FtpletEnum onUploadUniqueStart(FtpSession session, FtpRequest request, FtpReplyOutput response) throws FtpException, IOException {
        return callback.onUploadUniqueStart(session, request, response);
    }
}

