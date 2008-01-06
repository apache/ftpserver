package org.apache.ftpserver.interfaces;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.cert.Certificate;
import java.util.Date;
import java.util.Set;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;

import org.apache.ftpserver.FtpSessionImpl;
import org.apache.ftpserver.IODataConnectionFactory;
import org.apache.ftpserver.ServerDataConnectionFactory;
import org.apache.ftpserver.ftplet.DataType;
import org.apache.ftpserver.ftplet.FileObject;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpSession;
import org.apache.ftpserver.ftplet.Structure;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.listener.Listener;
import org.apache.mina.common.CloseFuture;
import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoFilterChain;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoService;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.IoSessionConfig;
import org.apache.mina.common.ReadFuture;
import org.apache.mina.common.TrafficMask;
import org.apache.mina.common.TransportMetadata;
import org.apache.mina.common.WriteFuture;
import org.apache.mina.filter.ssl.SslFilter;

public class FtpIoSession implements IoSession {

    /**
     * Contains user name between USER and PASS commands
     */
	public static final  String ATTRIBUTE_PREFIX = "org.apache.ftpserver.";
	private static final  String ATTRIBUTE_USER_ARGUMENT 		= ATTRIBUTE_PREFIX + "user-argument";
	private static final  String ATTRIBUTE_USER							= ATTRIBUTE_PREFIX + "user";
	private static final  String ATTRIBUTE_LANGUAGE			 		= ATTRIBUTE_PREFIX + "language";
	private static final  String ATTRIBUTE_LOGIN_TIME		 		= ATTRIBUTE_PREFIX + "login-time";
	private static final  String ATTRIBUTE_DATA_CONNECTION	= ATTRIBUTE_PREFIX + "data-connection";
	private static final  String ATTRIBUTE_FILE_SYSTEM		 		= ATTRIBUTE_PREFIX + "file-system";
	private static final  String ATTRIBUTE_RENAME_FROM	 		= ATTRIBUTE_PREFIX + "rename-from";
	private static final  String ATTRIBUTE_FILE_OFFSET		 		= ATTRIBUTE_PREFIX + "file-offset";
	private static final  String ATTRIBUTE_DATA_TYPE			 		= ATTRIBUTE_PREFIX + "data-type";
	private static final  String ATTRIBUTE_STRUCTURE		 		= ATTRIBUTE_PREFIX + "structure";
	private static final  String ATTRIBUTE_FAILED_LOGINS	 		= ATTRIBUTE_PREFIX + "failed-logins";
    private static final  String ATTRIBUTE_LISTENER			 		= ATTRIBUTE_PREFIX + "listener";
    private static final Object ATTRIBUTE_MAX_IDLE_TIME 			= ATTRIBUTE_PREFIX + "max-idle-time";
	private static final Object ATTRIBUTE_LAST_ACCESS_TIME 	= ATTRIBUTE_PREFIX + "last-access-time";

	private IoSession wrappedSession;
	private FtpServerContext context;
	
    /* Begin wrapped IoSession methods */
    
	public CloseFuture close() {
		return wrappedSession.close();
	}

	public CloseFuture close(boolean immediately) {
		return wrappedSession.close(immediately);
	}

	public CloseFuture closeOnFlush() {
		return wrappedSession.closeOnFlush();
	}

	public boolean containsAttribute(Object key) {
		return wrappedSession.containsAttribute(key);
	}

	@SuppressWarnings("deprecation")
	public Object getAttachment() {
		return wrappedSession.getAttachment();
	}

	public Object getAttribute(Object key) {
		return wrappedSession.getAttribute(key);
	}

	public Object getAttribute(Object key, Object defaultValue) {
		return wrappedSession.getAttribute(key, defaultValue);
	}

	public Set<Object> getAttributeKeys() {
		return wrappedSession.getAttributeKeys();
	}

	public int getBothIdleCount() {
		return wrappedSession.getBothIdleCount();
	}

	public CloseFuture getCloseFuture() {
		return wrappedSession.getCloseFuture();
	}

	public IoSessionConfig getConfig() {
		return wrappedSession.getConfig();
	}

	public long getCreationTime() {
		return wrappedSession.getCreationTime();
	}

	public IoFilterChain getFilterChain() {
		return wrappedSession.getFilterChain();
	}

	public IoHandler getHandler() {
		return wrappedSession.getHandler();
	}

	public long getId() {
		return wrappedSession.getId();
	}

	public int getIdleCount(IdleStatus status) {
		return wrappedSession.getIdleCount(status);
	}

	public long getLastBothIdleTime() {
		return wrappedSession.getLastBothIdleTime();
	}

	public long getLastIdleTime(IdleStatus status) {
		return wrappedSession.getLastIdleTime(status);
	}

	public long getLastIoTime() {
		return wrappedSession.getLastIoTime();
	}

	public long getLastReadTime() {
		return wrappedSession.getLastReadTime();
	}

	public long getLastReaderIdleTime() {
		return wrappedSession.getLastReaderIdleTime();
	}

	public long getLastWriteTime() {
		return wrappedSession.getLastWriteTime();
	}

	public long getLastWriterIdleTime() {
		return wrappedSession.getLastWriterIdleTime();
	}

	public SocketAddress getLocalAddress() {
		return wrappedSession.getLocalAddress();
	}

	public long getReadBytes() {
		return wrappedSession.getReadBytes();
	}

	public double getReadBytesThroughput() {
		return wrappedSession.getReadBytesThroughput();
	}

	public long getReadMessages() {
		return wrappedSession.getReadMessages();
	}

	public double getReadMessagesThroughput() {
		return wrappedSession.getReadMessagesThroughput();
	}

	public int getReaderIdleCount() {
		return wrappedSession.getReaderIdleCount();
	}

	public SocketAddress getRemoteAddress() {
		return wrappedSession.getRemoteAddress();
	}

	public long getScheduledWriteBytes() {
		return wrappedSession.getScheduledWriteBytes();
	}

	public int getScheduledWriteMessages() {
		return wrappedSession.getScheduledWriteMessages();
	}

	public IoService getService() {
		return wrappedSession.getService();
	}

	public SocketAddress getServiceAddress() {
		return wrappedSession.getServiceAddress();
	}

	public TrafficMask getTrafficMask() {
		return wrappedSession.getTrafficMask();
	}

	public TransportMetadata getTransportMetadata() {
		return wrappedSession.getTransportMetadata();
	}

	public int getWriterIdleCount() {
		return wrappedSession.getWriterIdleCount();
	}

	public long getWrittenBytes() {
		return wrappedSession.getWrittenBytes();
	}

	public double getWrittenBytesThroughput() {
		return wrappedSession.getWrittenBytesThroughput();
	}

	public long getWrittenMessages() {
		return wrappedSession.getWrittenMessages();
	}

	public double getWrittenMessagesThroughput() {
		return wrappedSession.getWrittenMessagesThroughput();
	}

	public boolean isClosing() {
		return wrappedSession.isClosing();
	}

	public boolean isConnected() {
		return wrappedSession.isConnected();
	}

	public boolean isIdle(IdleStatus status) {
		return wrappedSession.isIdle(status);
	}

	public ReadFuture read() {
		return wrappedSession.read();
	}

	public Object removeAttribute(Object key) {
		return wrappedSession.removeAttribute(key);
	}

	public boolean removeAttribute(Object key, Object value) {
		return wrappedSession.removeAttribute(key, value);
	}

	public boolean replaceAttribute(Object key, Object oldValue, Object newValue) {
		return wrappedSession.replaceAttribute(key, oldValue, newValue);
	}

	public void resumeRead() {
		wrappedSession.resumeRead();
	}

	public void resumeWrite() {
		wrappedSession.resumeWrite();
	}

	@SuppressWarnings("deprecation")
	public Object setAttachment(Object attachment) {
		return wrappedSession.setAttachment(attachment);
	}

	public Object setAttribute(Object key) {
		return wrappedSession.setAttribute(key);
	}

	public Object setAttribute(Object key, Object value) {
		return wrappedSession.setAttribute(key, value);
	}

	public Object setAttributeIfAbsent(Object key) {
		return wrappedSession.setAttributeIfAbsent(key);
	}

	public Object setAttributeIfAbsent(Object key, Object value) {
		return wrappedSession.setAttributeIfAbsent(key, value);
	}

	public void setTrafficMask(TrafficMask trafficMask) {
		wrappedSession.setTrafficMask(trafficMask);
	}

	public void suspendRead() {
		wrappedSession.suspendRead();
	}

	public void suspendWrite() {
		wrappedSession.suspendWrite();
	}

	public WriteFuture write(Object message) {
		return wrappedSession.write(message);
	}

	public WriteFuture write(Object message, SocketAddress destination) {
		return wrappedSession.write(message, destination);
	}
	
	/* End wrapped IoSession methods */

	public void resetState() {
		removeAttribute(ATTRIBUTE_RENAME_FROM);
		removeAttribute(ATTRIBUTE_FILE_OFFSET);
	}

	public synchronized ServerDataConnectionFactory getDataConnection() {
		if(containsAttribute(ATTRIBUTE_DATA_CONNECTION)) {
			return (ServerDataConnectionFactory) getAttribute(ATTRIBUTE_DATA_CONNECTION);
		} else {
			IODataConnectionFactory dataCon = new IODataConnectionFactory(context, this);
			dataCon.setServerControlAddress(((InetSocketAddress)getLocalAddress()).getAddress());
			setAttribute(ATTRIBUTE_DATA_CONNECTION, dataCon);
			
			return dataCon;
		}
	}

	public FileSystemView getFileSystemView() {
		return (FileSystemView) getAttribute(ATTRIBUTE_FILE_SYSTEM);
	}

	public User getUser() {
		return (User) getAttribute(ATTRIBUTE_USER);
	}

    /**
     * Is logged-in
     */
    public boolean isLoggedIn() {
        return containsAttribute(ATTRIBUTE_LOGIN_TIME);
    }

	public Listener getListener() {
		return (Listener) getAttribute(ATTRIBUTE_LISTENER);
	}

	public void setListener(Listener listener) {
		setAttribute(ATTRIBUTE_LISTENER, listener);
	}

	
	public FtpSession getFtpletSession() {
		return new FtpSessionImpl(this);
	}

	public String getLanguage() {
		return (String) getAttribute(ATTRIBUTE_LANGUAGE);
	}

	public void setLanguage(String language) {
		setAttribute(ATTRIBUTE_LANGUAGE, language);
		
	}

	public String getUserArgument() {
		return (String) getAttribute(ATTRIBUTE_USER_ARGUMENT);
	}

	public void setUser(User user) {
		setAttribute(ATTRIBUTE_USER, user);
		
	}

	public void setUserArgument(String userArgument) {
		setAttribute(ATTRIBUTE_USER_ARGUMENT, userArgument);
		
	}

	public int getMaxIdleTime() {
		return (Integer) getAttribute(ATTRIBUTE_MAX_IDLE_TIME, 0);
	}

	public void setMaxIdleTime(int maxIdleTime) {
		setAttribute(ATTRIBUTE_MAX_IDLE_TIME, maxIdleTime);
		
	}

	public synchronized void increaseFailedLogins() {
		int failedLogins = (Integer) getAttribute(ATTRIBUTE_FAILED_LOGINS, 0);
		failedLogins++;
		setAttribute(ATTRIBUTE_FAILED_LOGINS, failedLogins);
	}

	public int getFailedLogins() {
		return (Integer) getAttribute(ATTRIBUTE_FAILED_LOGINS, 0);
	}

	public void setLogin(FileSystemView fsview) {
		setAttribute(ATTRIBUTE_FILE_SYSTEM, fsview);
	}

	public void reinitialize() {
		removeAttribute(ATTRIBUTE_USER);
		removeAttribute(ATTRIBUTE_USER_ARGUMENT);
		removeAttribute(ATTRIBUTE_LOGIN_TIME);
		removeAttribute(ATTRIBUTE_FILE_SYSTEM);
		removeAttribute(ATTRIBUTE_RENAME_FROM);
        removeAttribute(ATTRIBUTE_FILE_OFFSET);
	}

	public void setFileOffset(long fileOffset) {
		setAttribute(ATTRIBUTE_FILE_OFFSET, fileOffset);
		
	}

	public void setRenameFrom(FileObject renFr) {
		setAttribute(ATTRIBUTE_RENAME_FROM, renFr);
		
	}

	public FileObject getRenameFrom() {
		return (FileObject) getAttribute(ATTRIBUTE_RENAME_FROM);
	}

	public long getFileOffset() {
		return (Long) getAttribute(ATTRIBUTE_FILE_OFFSET, 0L);
	}

	public void setStructure(Structure structure) {
		setAttribute(ATTRIBUTE_STRUCTURE, structure);
	}

	public void setDataType(DataType dataType) {
		setAttribute(ATTRIBUTE_DATA_TYPE, dataType);
		
	}

	public FtpIoSession(IoSession wrappedSession, FtpServerContext context) {
		this.wrappedSession = wrappedSession;
		this.context = context;
	}

	public Structure getStructure() {
		return (Structure) getAttribute(ATTRIBUTE_STRUCTURE, Structure.FILE);
	}
	public DataType getDataType() {
		return (DataType) getAttribute(ATTRIBUTE_DATA_TYPE, DataType.ASCII);
	}

	public Date getLoginTime() {
		return (Date) getAttribute(ATTRIBUTE_LOGIN_TIME);
	}

	public Date getLastAccessTime() {
		return (Date) getAttribute(ATTRIBUTE_LAST_ACCESS_TIME);
	}

    public Certificate[] getClientCertificates() {
        if(getFilterChain().contains("sslFilter")) {
            SslFilter sslFilter = (SslFilter) getFilterChain().get("sslFilter");
            
            SSLSession sslSession = sslFilter.getSslSession(this);
            
            if(sslSession != null) {
                try {
                    return sslSession.getPeerCertificates();
                } catch(SSLPeerUnverifiedException e) {
                    // ignore, certificate will not be available to the session
                }
            }
            
        }

        // no certificates available
        return null;

    }

	public void updateLastAccessTime() {
		setAttribute(ATTRIBUTE_LAST_ACCESS_TIME, new Date());
		
	}

}
