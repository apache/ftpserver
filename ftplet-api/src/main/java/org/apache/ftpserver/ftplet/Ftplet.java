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
 * Defines methods that all ftplets must implement. 
 *
 * A servlet is a small Java program that runs within a FTP server. 
 * Ftplets receive and respond to requests from FTP clients.
 *
 * This interface defines methods to initialize a ftplet, to service requests, 
 * and to remove a ftplet from the server. These are known as life-cycle methods 
 * and are called in the following sequence: 
 *
 * <ol>
 *   <li>The ftplet is constructed.</li>
 *   <li>Then initialized with the init method.</li>
 *   <li>All the callback methods will be invoked.</li>
 *   <li>The ftplet is taken out of service, then destroyed with the destroy method.</li>
 *   <li>Then garbage collected and finalized.</li>
 * </ol>
 * 
 * All the callback methods return FtpletEnum. If it returns null FtpletEnum.RET_DEFAULT
 * will be assumed. If any ftplet callback method throws exception, that particular connection 
 * will be disconnected. 
 */
public 
interface Ftplet {
    
    /**
     * Called by the ftplet container to indicate to a ftplet that the ftplet 
     * is being placed into service. 
     * The ftplet container calls the init method exactly once after instantiating 
     * the ftplet. The init method must complete successfully before the ftplet can 
     * receive any requests.
     */
    void init(FtpletContext ftpletContext, Configuration config) throws FtpException;
    
    /**
     * Called by the servlet container to indicate to a ftplet that the ftplet is 
     * being taken out of service. This method is only called once all threads within 
     * the ftplet's service method have exited. After the ftplet container calls this 
     * method, callback methods will not be executed. If the ftplet initialization 
     * method fails, this method will not be called. 
     */
    void destroy();
    
    /**
     * Client connect notification method.
     */
    FtpletEnum onConnect(FtpSession session) throws FtpException, IOException;
    
    /**
     * Client disconnect notification method. This is the last callback method.
     */
    FtpletEnum onDisconnect(FtpSession session) throws FtpException, IOException;
    
    /**
     * Client successful login notification method. 
     * If the user has successfully authenticated, the 
     * {@link FtpSession#getUser()} method will return the user,
     * otherwise it will return null.
     */
    FtpletEnum onLogin(FtpSession session, FtpRequest request) throws FtpException, IOException;
            
    /**
     * File delete request notification method.
     */
    FtpletEnum onDeleteStart(FtpSession session, FtpRequest request) throws FtpException, IOException;
    
    /**
     * File delete success notification method.
     */
    FtpletEnum onDeleteEnd(FtpSession session, FtpRequest request) throws FtpException, IOException;
    
    /**
     * File upload request notification method.
     */
    FtpletEnum onUploadStart(FtpSession session, FtpRequest request) throws FtpException, IOException;

    /**
     * File upload success notification method.
     */
    FtpletEnum onUploadEnd(FtpSession session, FtpRequest request) throws FtpException, IOException;
    
    /**
     * File download request notification method.
     */
    FtpletEnum onDownloadStart(FtpSession session, FtpRequest request) throws FtpException, IOException;
    
    /**
     * File download success notification method.
     */
    FtpletEnum onDownloadEnd(FtpSession session, FtpRequest request) throws FtpException, IOException;
    
    /**
     * Remove directory request notification method.
     */
    FtpletEnum onRmdirStart(FtpSession session, FtpRequest request) throws FtpException, IOException;
    
    /**
     * Directory removal success notification method.
     */
    FtpletEnum onRmdirEnd(FtpSession session, FtpRequest request) throws FtpException, IOException;
    
    /**
     * Directory creation request notification method.
     */
    FtpletEnum onMkdirStart(FtpSession session, FtpRequest request) throws FtpException, IOException;
    
    /**
     * Directory creation success notification method.
     */
    FtpletEnum onMkdirEnd(FtpSession session, FtpRequest request) throws FtpException, IOException;
            
    /**
     * File append request notification method.
     */
    FtpletEnum onAppendStart(FtpSession session, FtpRequest request) throws FtpException, IOException;
    
    /**
     * File append success notification method.
     */
    FtpletEnum onAppendEnd(FtpSession session, FtpRequest request) throws FtpException, IOException;
    
    /**
     * Unique file create request notification method.
     */
    FtpletEnum onUploadUniqueStart(FtpSession session, FtpRequest request) throws FtpException, IOException;
    
    /**
     * Unique file create success notification method.
     */
    FtpletEnum onUploadUniqueEnd(FtpSession session, FtpRequest request) throws FtpException, IOException;
    
    /**
     * Rename start notification method.
     */
    FtpletEnum onRenameStart(FtpSession session, FtpRequest request) throws FtpException, IOException;
    
    /**
     * Rename end notification method.
     */
    FtpletEnum onRenameEnd(FtpSession session, FtpRequest request) throws FtpException, IOException;
    
    /**
     * SITE command notification method.
     */
    FtpletEnum onSite(FtpSession session, FtpRequest request) throws FtpException, IOException;
}
