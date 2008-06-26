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

package org.apache.ftpserver;

import java.io.IOException;

import org.apache.ftpserver.ftplet.FtpReply;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.interfaces.Command;
import org.apache.ftpserver.interfaces.CommandFactory;
import org.apache.ftpserver.interfaces.FtpIoSession;
import org.apache.ftpserver.interfaces.FtpServerContext;
import org.apache.ftpserver.interfaces.ServerFtpStatistics;
import org.apache.ftpserver.listener.Listener;
import org.apache.ftpserver.util.FtpReplyUtil;
import org.apache.mina.core.session.IdleStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultFtpHandler implements FtpHandler {
	
	private final Logger LOG = LoggerFactory.getLogger(DefaultFtpHandler.class);
	
	private FtpServerContext context;
	private Listener listener;
	
    public void init(FtpServerContext context, Listener listener) {
    	this.context = context;
    	this.listener = listener;
    }

	public void sessionCreated(FtpIoSession session) throws Exception {
    	session.setListener(listener);
    }

    public void sessionOpened(FtpIoSession session) throws Exception {
        context.getFtpletContainer().onConnect(session.getFtpletSession());
        
    	session.write(FtpReplyUtil.translate(session,  null, context, FtpReply.REPLY_220_SERVICE_READY, null, null));
    }

    public void sessionClosed(FtpIoSession session) throws Exception {
        try {
            context.getFtpletContainer().onDisconnect(session.getFtpletSession());
        } catch(Exception e) {
            // shallow the exception, we're closing down the session anyways
            LOG.warn("Ftplet threw an exception on disconnect", e);
        }
        
        ServerFtpStatistics stats = ((ServerFtpStatistics)context.getFtpStatistics());
    	
    	if(stats != null) {
    		stats.setLogout(session);
    	}
    }

    public void exceptionCaught( FtpIoSession session, Throwable cause ) throws Exception {
    	LOG.error("Exception caught, closing session", cause);
    	session.closeOnFlush().awaitUninterruptibly(10000);
    }

    public void messageReceived( FtpIoSession session, FtpRequest request ) throws Exception {
        try {
            String commandName = request.getCommand();
            CommandFactory commandFactory = context.getCommandFactory();
            Command command = commandFactory.getCommand(commandName);
            
            
            if(command != null) {
            	synchronized (session) {
            		command.execute(session, context, request);
				}
            }
            else {
                session.write(FtpReplyUtil.translate(session, request, context, FtpReply.REPLY_502_COMMAND_NOT_IMPLEMENTED, "not.implemented", null));
            }
        }
        catch(Exception ex) {
            
            // send error reply
            try { 
                session.write(FtpReplyUtil.translate(session, request, context, FtpReply.REPLY_550_REQUESTED_ACTION_NOT_TAKEN, null, null));
            }
            catch(Exception ex1) {
            }
            
            if (ex instanceof java.io.IOException) {
               throw (IOException)ex;
            }
            else {
                LOG.warn("RequestHandler.service()", ex);
            }
        }

    }

    public void sessionIdle( FtpIoSession session, IdleStatus status ) throws Exception {
    	LOG.info("Session idle, closing");
        session.closeOnFlush().awaitUninterruptibly(10000);
    }

	public void messageSent(FtpIoSession session, FtpReply reply)
			throws Exception {
		// do nothing
		
	}
}
