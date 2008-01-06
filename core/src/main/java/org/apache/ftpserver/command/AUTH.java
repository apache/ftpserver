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

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpReply;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.interfaces.FtpIoSession;
import org.apache.ftpserver.interfaces.FtpServerContext;
import org.apache.ftpserver.ssl.ClientAuth;
import org.apache.ftpserver.ssl.Ssl;
import org.apache.ftpserver.util.FtpReplyUtil;
import org.apache.mina.filter.ssl.SslFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This server supports explicit SSL support.
 */
public 
class AUTH extends AbstractCommand {

    private final Logger LOG = LoggerFactory.getLogger(AUTH.class);

    /**
     * Execute command
     */
    public void execute(FtpIoSession session,
                        FtpServerContext context, 
                        FtpRequest request) throws IOException, FtpException {
        
        // reset state variables
        session.resetState();
        
        // argument check
        if(!request.hasArgument()) {
            session.write(FtpReplyUtil.translate(session, request, context, FtpReply.REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS, "AUTH", null));
            return;  
        }
        
        // check SSL configuration
        if(session.getListener().getSsl() == null) {
            session.write(FtpReplyUtil.translate(session, request, context, 431, "AUTH", null));
            return;
        }
        
        // check parameter
        String authType = request.getArgument().toUpperCase();
        if(authType.equals("SSL")) {
            try {
                secureSession(session, "SSL");
                session.write(FtpReplyUtil.translate(session, request, context, 234, "AUTH.SSL", null));
            } catch(FtpException ex) {
                throw ex;
            } catch(Exception ex) {
                LOG.warn("AUTH.execute()", ex);
                throw new FtpException("AUTH.execute()", ex);
            }
        }
        else if(authType.equals("TLS")) {
            try {
                secureSession(session, "TLS");
                session.write(FtpReplyUtil.translate(session, request, context, 234, "AUTH.TLS", null));
            } catch(FtpException ex) {
                throw ex;
            } catch(Exception ex) {
                LOG.warn("AUTH.execute()", ex);
                throw new FtpException("AUTH.execute()", ex);
            }
        }
        else {
            session.write(FtpReplyUtil.translate(session, request, context, FtpReply.REPLY_502_COMMAND_NOT_IMPLEMENTED, "AUTH", null));
        }
    }
    
    private void secureSession(FtpIoSession session, String type) throws GeneralSecurityException, FtpException {
        Ssl ssl = session.getListener().getSsl();
        
        if(ssl != null) {
            session.setAttribute(SslFilter.DISABLE_ENCRYPTION_ONCE);
            
            SslFilter sslFilter = new SslFilter( ssl.getSSLContext() );
            if(ssl.getClientAuth() == ClientAuth.NEED) {
                sslFilter.setNeedClientAuth(true);
            } else if(ssl.getClientAuth() == ClientAuth.WANT) {
                sslFilter.setWantClientAuth(true);
            }
            
            if(ssl.getEnabledCipherSuites() != null) {
                sslFilter.setEnabledCipherSuites(ssl.getEnabledCipherSuites());
            }
            session.getFilterChain().addFirst("sslSessionFilter", sslFilter);

        } else {
            throw new FtpException("Socket factory SSL not configured");
        }
    }
}
