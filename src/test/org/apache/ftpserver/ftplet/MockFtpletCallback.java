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
import java.util.ArrayList;
import java.util.List;

public class MockFtpletCallback implements Ftplet {

    public static List calls = new ArrayList();
    
    public void destroy() {
        System.out.println("##### destroy");
        
        calls.add("destroy");
    }

    public void init(FtpConfig ftpConfig, Configuration config) throws FtpException {
        System.out.println("##### init");

        calls.add("init");
    }

    public FtpletEnum onAppendEnd(FtpRequest request, FtpResponse response) throws FtpException, IOException {
        System.out.println("##### onAppendEnd");

        calls.add("onAppendEnd");
        
        return null;
    }

    public FtpletEnum onAppendStart(FtpRequest request, FtpResponse response) throws FtpException, IOException {
        System.out.println("##### onAppendStart");
        calls.add("init");

        return null;
    }

    public FtpletEnum onConnect(FtpRequest request, FtpResponse response) throws FtpException, IOException {
        System.out.println("##### onConnect");
        calls.add("init");

        return null;
    }

    public FtpletEnum onDeleteEnd(FtpRequest request, FtpResponse response) throws FtpException, IOException {
        System.out.println("##### onDeleteEnd");
        calls.add("init");

        return null;
    }

    public FtpletEnum onDeleteStart(FtpRequest request, FtpResponse response) throws FtpException, IOException {
        System.out.println("##### onDeleteStart");
        calls.add("init");

        return null;
    }

    public FtpletEnum onDisconnect(FtpRequest request, FtpResponse response) throws FtpException, IOException {
        System.out.println("##### onDisconnect");
        calls.add("init");

        return null;
    }

    public FtpletEnum onDownloadEnd(FtpRequest request, FtpResponse response) throws FtpException, IOException {
        System.out.println("##### onDownloadEnd");
        calls.add("init");

        return null;
    }

    public FtpletEnum onDownloadStart(FtpRequest request, FtpResponse response) throws FtpException, IOException {
        System.out.println("##### onDownloadStart");
        calls.add("init");

        return null;
    }

    public FtpletEnum onLogin(FtpRequest request, FtpResponse response) throws FtpException, IOException {
        System.out.println("##### onLogin");
        calls.add("init");

        return null;
    }

    public FtpletEnum onMkdirEnd(FtpRequest request, FtpResponse response) throws FtpException, IOException {
        System.out.println("##### onMkdirEnd");
        calls.add("init");

        return null;
    }

    public FtpletEnum onMkdirStart(FtpRequest request, FtpResponse response) throws FtpException, IOException {
        System.out.println("##### onMkdirStart");
        calls.add("init");

        return null;
    }

    public FtpletEnum onRenameEnd(FtpRequest request, FtpResponse response) throws FtpException, IOException {
        System.out.println("##### onRenameEnd");
        calls.add("init");

        return null;
    }

    public FtpletEnum onRenameStart(FtpRequest request, FtpResponse response) throws FtpException, IOException {
        System.out.println("##### onRenameStart");
        calls.add("init");

        return null;
    }

    public FtpletEnum onRmdirEnd(FtpRequest request, FtpResponse response) throws FtpException, IOException {
        System.out.println("##### onRmdirEnd");
        calls.add("init");

        return null;
    }

    public FtpletEnum onRmdirStart(FtpRequest request, FtpResponse response) throws FtpException, IOException {
        System.out.println("##### onRmdirStart");
        calls.add("init");

        return null;
    }

    public FtpletEnum onSite(FtpRequest request, FtpResponse response) throws FtpException, IOException {
        System.out.println("##### onSite");
        calls.add("init");

        return null;
    }

    public FtpletEnum onUploadEnd(FtpRequest request, FtpResponse response) throws FtpException, IOException {
        System.out.println("##### onUploadEnd");
        calls.add("init");

        return null;
    }

    public FtpletEnum onUploadStart(FtpRequest request, FtpResponse response) throws FtpException, IOException {
        System.out.println("##### onUploadStart");
        calls.add("init");

        return null;
    }

    public FtpletEnum onUploadUniqueEnd(FtpRequest request, FtpResponse response) throws FtpException, IOException {
        System.out.println("##### onUploadUniqueEnd");
        calls.add("init");

        return null;
    }

    public FtpletEnum onUploadUniqueStart(FtpRequest request, FtpResponse response) throws FtpException, IOException {
        System.out.println("##### onUploadUniqueStart");
        calls.add("init");

        return null;
    }


}
