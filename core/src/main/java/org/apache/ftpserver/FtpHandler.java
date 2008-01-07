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
import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FtpHandler extends IoHandlerAdapter
{
	
	
	
	private final Logger LOG = LoggerFactory.getLogger(FtpHandler.class);
	
	private FtpServerContext context;
	private Listener listener;
	
	
    public FtpHandler(FtpServerContext context, Listener listener) {
		this.context = context;
		this.listener = listener;
	}

	public void sessionCreated(IoSession session) throws Exception {
    	FtpIoSession ftpSession = new FtpIoSession(session, context);
    	ftpSession.setListener(listener);
    }

    public void sessionOpened(IoSession session) throws Exception {
    	FtpIoSession ftpSession = new FtpIoSession(session, context);
    	
    	session.write(FtpReplyUtil.translate(ftpSession,  null, context, FtpReply.REPLY_220_SERVICE_READY, null, null));
    }

    public void sessionClosed(IoSession session) throws Exception {
    	FtpIoSession ftpSession = new FtpIoSession(session, context);
    	
    	ServerFtpStatistics stats = ((ServerFtpStatistics)context.getFtpStatistics());
    	
    	if(stats != null) {
    		stats.setLogout(ftpSession);
    	}
    }

	
    @Override
    public void exceptionCaught( IoSession session, Throwable cause ) throws Exception
    {
        cause.printStackTrace();
        session.closeOnFlush().awaitUninterruptibly(10000);
    }

    @Override
    public void messageReceived( IoSession session, Object message ) throws Exception
    {
    	FtpRequest request = new FtpRequestImpl(message.toString());

    	FtpIoSession ftpSession = new FtpIoSession(session, context);
    	
        try {
            String commandName = request.getCommand();
            CommandFactory commandFactory = context.getCommandFactory();
            Command command = commandFactory.getCommand(commandName);
            
            
            if(command != null) {
                command.execute(ftpSession, context, request);
            }
            else {
                session.write(FtpReplyUtil.translate(ftpSession, request, context, FtpReply.REPLY_502_COMMAND_NOT_IMPLEMENTED, "not.implemented", null));
            }
        }
        catch(Exception ex) {
            
            // send error reply
            try { 
                session.write(FtpReplyUtil.translate(ftpSession, request, context, FtpReply.REPLY_550_REQUESTED_ACTION_NOT_TAKEN, null, null));
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

    @Override
    public void sessionIdle( IoSession session, IdleStatus status ) throws Exception
    {
        session.closeOnFlush();
    }
}