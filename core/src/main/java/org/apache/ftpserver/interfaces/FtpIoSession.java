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
import org.apache.mina.common.filterchain.IoFilterChain;
import org.apache.mina.common.future.CloseFuture;
import org.apache.mina.common.future.ReadFuture;
import org.apache.mina.common.future.WriteFuture;
import org.apache.mina.common.service.IoHandler;
import org.apache.mina.common.service.IoService;
import org.apache.mina.common.service.TransportMetadata;
import org.apache.mina.common.session.IdleStatus;
import org.apache.mina.common.session.IoSession;
import org.apache.mina.common.session.IoSessionConfig;
import org.apache.mina.common.session.TrafficMask;
import org.apache.mina.common.write.WriteRequest;
import org.apache.mina.filter.ssl.SslFilter;

public class FtpIoSession implements IoSession {

    /**
     * Contains user name between USER and PASS commands
     */
	public static final String ATTRIBUTE_PREFIX = "org.apache.ftpserver.";
	private static final String ATTRIBUTE_USER_ARGUMENT 		= ATTRIBUTE_PREFIX + "user-argument";
	private static final String ATTRIBUTE_USER							= ATTRIBUTE_PREFIX + "user";
	private static final String ATTRIBUTE_LANGUAGE			 		= ATTRIBUTE_PREFIX + "language";
	private static final String ATTRIBUTE_LOGIN_TIME		 		= ATTRIBUTE_PREFIX + "login-time";
	private static final String ATTRIBUTE_DATA_CONNECTION	= ATTRIBUTE_PREFIX + "data-connection";
	private static final String ATTRIBUTE_FILE_SYSTEM		 		= ATTRIBUTE_PREFIX + "file-system";
	private static final String ATTRIBUTE_RENAME_FROM	 		= ATTRIBUTE_PREFIX + "rename-from";
	private static final String ATTRIBUTE_FILE_OFFSET		 		= ATTRIBUTE_PREFIX + "file-offset";
	private static final String ATTRIBUTE_DATA_TYPE			 		= ATTRIBUTE_PREFIX + "data-type";
	private static final String ATTRIBUTE_STRUCTURE		 		= ATTRIBUTE_PREFIX + "structure";
	private static final String ATTRIBUTE_FAILED_LOGINS	 		= ATTRIBUTE_PREFIX + "failed-logins";
    private static final String ATTRIBUTE_LISTENER			 		= ATTRIBUTE_PREFIX + "listener";
    private static final String ATTRIBUTE_MAX_IDLE_TIME 			= ATTRIBUTE_PREFIX + "max-idle-time";
	private static final String ATTRIBUTE_LAST_ACCESS_TIME 	= ATTRIBUTE_PREFIX + "last-access-time";
	private static final String ATTRIBUTE_CACHED_REMOTE_ADDRESS     = ATTRIBUTE_PREFIX + "cached-remote-address";

	private IoSession wrappedSession;
	private FtpServerContext context;
	
    /* Begin wrapped IoSession methods */
    
	/**
	 * @see IoSession#close()
	 */
	public CloseFuture close() {
		return wrappedSession.close();
	}

	/**
	 * @see IoSession#close(boolean)
	 */
	public CloseFuture close(boolean immediately) {
		return wrappedSession.close(immediately);
	}

	/**
	 * @see IoSession#closeOnFlush()
	 */
	public CloseFuture closeOnFlush() {
		return wrappedSession.closeOnFlush();
	}

	/**
	 * @see IoSession#containsAttribute(Object)
	 */
	public boolean containsAttribute(Object key) {
		return wrappedSession.containsAttribute(key);
	}

	/**
	 * @see IoSession#getAttachment()
	 */
	@SuppressWarnings("deprecation")
	public Object getAttachment() {
		return wrappedSession.getAttachment();
	}

	/**
	 * @see IoSession#getAttribute(Object)
	 */
	public Object getAttribute(Object key) {
		return wrappedSession.getAttribute(key);
	}

	/**
	 * @see IoSession#getAttribute(Object, Object)
	 */
	public Object getAttribute(Object key, Object defaultValue) {
		return wrappedSession.getAttribute(key, defaultValue);
	}

	/**
	 * @see IoSession#getAttributeKeys()
	 */
	public Set<Object> getAttributeKeys() {
		return wrappedSession.getAttributeKeys();
	}

	/**
	 * @see IoSession#getBothIdleCount()
	 */
	public int getBothIdleCount() {
		return wrappedSession.getBothIdleCount();
	}

	/**
	 * @see IoSession#getCloseFuture()
	 */
	public CloseFuture getCloseFuture() {
		return wrappedSession.getCloseFuture();
	}

	/**
	 * @see IoSession#getConfig()
	 */
	public IoSessionConfig getConfig() {
		return wrappedSession.getConfig();
	}

	/**
	 * @see IoSession#getCreationTime()
	 */
	public long getCreationTime() {
		return wrappedSession.getCreationTime();
	}

	/**
	 * @see IoSession#getFilterChain()
	 */
	public IoFilterChain getFilterChain() {
		return wrappedSession.getFilterChain();
	}

	/**
	 * @see IoSession#getHandler()
	 */
	public IoHandler getHandler() {
		return wrappedSession.getHandler();
	}

	/**
	 * @see IoSession#getId()
	 */
	public long getId() {
		return wrappedSession.getId();
	}

	/**
	 * @see IoSession#getIdleCount(IdleStatus)
	 */
	public int getIdleCount(IdleStatus status) {
		return wrappedSession.getIdleCount(status);
	}

	/**
	 * @see IoSession#getLastBothIdleTime()
	 */
	public long getLastBothIdleTime() {
		return wrappedSession.getLastBothIdleTime();
	}

	/**
	 * @see IoSession#getLastIdleTime(IdleStatus)
	 */
	public long getLastIdleTime(IdleStatus status) {
		return wrappedSession.getLastIdleTime(status);
	}

	/**
	 * @see IoSession#getLastIoTime()
	 */
	public long getLastIoTime() {
		return wrappedSession.getLastIoTime();
	}

	/**
	 * @see IoSession#getLastReadTime()
	 */
	public long getLastReadTime() {
		return wrappedSession.getLastReadTime();
	}

	/**
	 * @see IoSession#getLastReaderIdleTime()
	 */
	public long getLastReaderIdleTime() {
		return wrappedSession.getLastReaderIdleTime();
	}

	/**
	 * @see IoSession#getLastWriteTime()
	 */
	public long getLastWriteTime() {
		return wrappedSession.getLastWriteTime();
	}

	/**
	 * @see IoSession#getLastWriterIdleTime()
	 */
	public long getLastWriterIdleTime() {
		return wrappedSession.getLastWriterIdleTime();
	}

	/**
	 * @see IoSession#getLocalAddress()
	 */
	public SocketAddress getLocalAddress() {
		return wrappedSession.getLocalAddress();
	}

	/**
	 * @see IoSession#getReadBytes()
	 */
	public long getReadBytes() {
		return wrappedSession.getReadBytes();
	}

	/**
	 * @see IoSession#getReadBytesThroughput()
	 */
	public double getReadBytesThroughput() {
		return wrappedSession.getReadBytesThroughput();
	}

	/**
	 * @see IoSession#getReadMessages()
	 */
	public long getReadMessages() {
		return wrappedSession.getReadMessages();
	}

	/**
	 * @see IoSession#getReadMessagesThroughput()
	 */
	public double getReadMessagesThroughput() {
		return wrappedSession.getReadMessagesThroughput();
	}

	/**
	 * @see IoSession#getReaderIdleCount()
	 */
	public int getReaderIdleCount() {
		return wrappedSession.getReaderIdleCount();
	}

	/**
	 * @see IoSession#getRemoteAddress()
	 */
	public SocketAddress getRemoteAddress() {
	    // when closing a socket, the remote address might be reset to null
	    // therefore, we attempt to keep a cached copy around
	    
	    SocketAddress address = wrappedSession.getRemoteAddress();
	    if(address == null && containsAttribute(ATTRIBUTE_CACHED_REMOTE_ADDRESS)) {
	        return (SocketAddress) getAttribute(ATTRIBUTE_CACHED_REMOTE_ADDRESS);
	    } else {
	        setAttribute(ATTRIBUTE_CACHED_REMOTE_ADDRESS, address);
	        return address;
	    }
	}

	/**
	 * @see IoSession#getScheduledWriteBytes()
	 */
	public long getScheduledWriteBytes() {
		return wrappedSession.getScheduledWriteBytes();
	}

	/**
	 * @see IoSession#getScheduledWriteMessages()
	 */
	public int getScheduledWriteMessages() {
		return wrappedSession.getScheduledWriteMessages();
	}

	/**
	 * @see IoSession#getService()
	 */
	public IoService getService() {
		return wrappedSession.getService();
	}

	/**
	 * @see IoSession#getServiceAddress()
	 */
	public SocketAddress getServiceAddress() {
		return wrappedSession.getServiceAddress();
	}

	/**
	 * @see IoSession#getTrafficMask()
	 */
	public TrafficMask getTrafficMask() {
		return wrappedSession.getTrafficMask();
	}

	/**
	 * @see IoSession#getTransportMetadata()
	 */
	public TransportMetadata getTransportMetadata() {
		return wrappedSession.getTransportMetadata();
	}

	/**
	 * @see IoSession#getWriterIdleCount()
	 */
	public int getWriterIdleCount() {
		return wrappedSession.getWriterIdleCount();
	}

	/**
	 * @see IoSession#getWrittenBytes()
	 */
	public long getWrittenBytes() {
		return wrappedSession.getWrittenBytes();
	}

	/**
	 * @see IoSession#getWrittenBytesThroughput()
	 */
	public double getWrittenBytesThroughput() {
		return wrappedSession.getWrittenBytesThroughput();
	}

	/**
	 * @see IoSession#getWrittenMessages()
	 */
	public long getWrittenMessages() {
		return wrappedSession.getWrittenMessages();
	}

	/**
	 * @see IoSession#getWrittenMessagesThroughput()
	 */
	public double getWrittenMessagesThroughput() {
		return wrappedSession.getWrittenMessagesThroughput();
	}

	/**
	 * @see IoSession#isClosing()
	 */
	public boolean isClosing() {
		return wrappedSession.isClosing();
	}

	/**
	 * @see IoSession#isConnected()
	 */
	public boolean isConnected() {
		return wrappedSession.isConnected();
	}

	/**
	 * @see IoSession#isIdle(IdleStatus)
	 */
	public boolean isIdle(IdleStatus status) {
		return wrappedSession.isIdle(status);
	}

	/**
	 * @see IoSession#read()
	 */
	public ReadFuture read() {
		return wrappedSession.read();
	}

	/**
	 * @see IoSession#removeAttribute(Object)
	 */
	public Object removeAttribute(Object key) {
		return wrappedSession.removeAttribute(key);
	}

	/**
	 * @see IoSession#removeAttribute(Object, Object)
	 */
	public boolean removeAttribute(Object key, Object value) {
		return wrappedSession.removeAttribute(key, value);
	}

	/**
	 * @see IoSession#replaceAttribute(Object, Object, Object)
	 */
	public boolean replaceAttribute(Object key, Object oldValue, Object newValue) {
		return wrappedSession.replaceAttribute(key, oldValue, newValue);
	}

	/**
	 * @see IoSession#resumeRead()
	 */
	public void resumeRead() {
		wrappedSession.resumeRead();
	}

	/**
	 * @see IoSession#resumeWrite()
	 */
	public void resumeWrite() {
		wrappedSession.resumeWrite();
	}

	/**
	 * @see IoSession#setAttachment(Object)
	 */
	@SuppressWarnings("deprecation")
	public Object setAttachment(Object attachment) {
		return wrappedSession.setAttachment(attachment);
	}

	/**
	 * @see IoSession#setAttribute(Object)
	 */
	public Object setAttribute(Object key) {
		return wrappedSession.setAttribute(key);
	}

	/**
	 * @see IoSession#setAttribute(Object, Object)
	 */
	public Object setAttribute(Object key, Object value) {
		return wrappedSession.setAttribute(key, value);
	}

	/**
	 * @see IoSession#setAttributeIfAbsent(Object)
	 */
	public Object setAttributeIfAbsent(Object key) {
		return wrappedSession.setAttributeIfAbsent(key);
	}

	/**
	 * @see IoSession#setAttributeIfAbsent(Object, Object)
	 */
	public Object setAttributeIfAbsent(Object key, Object value) {
		return wrappedSession.setAttributeIfAbsent(key, value);
	}

	/**
	 * @see IoSession#setTrafficMask(TrafficMask)
	 */
	public void setTrafficMask(TrafficMask trafficMask) {
		wrappedSession.setTrafficMask(trafficMask);
	}

	/**
	 * @see IoSession#suspendRead()
	 */
	public void suspendRead() {
		wrappedSession.suspendRead();
	}

	/**
	 * @see IoSession#suspendWrite()
	 */
	public void suspendWrite() {
		wrappedSession.suspendWrite();
	}

	/**
	 * @see IoSession#write(Object)
	 */
	public WriteFuture write(Object message) {
		return wrappedSession.write(message);
	}

	/**
	 * @see IoSession#write(Object, SocketAddress)
	 */
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
        return containsAttribute(ATTRIBUTE_USER);
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
		setAttribute(ATTRIBUTE_LOGIN_TIME, new Date());
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

	/**
	 * @see IoSession#getCurrentWriteMessage()
	 */
	public Object getCurrentWriteMessage() {
		return wrappedSession.getCurrentWriteMessage();
	}

	/**
	 * @see IoSession#getCurrentWriteRequest()
	 */
	public WriteRequest getCurrentWriteRequest() {
		return wrappedSession.getCurrentWriteRequest();
	}

	/**
	 * @see IoSession#isBothIdle()
	 */
	public boolean isBothIdle() {
		return wrappedSession.isBothIdle();
	}

	/**
	 * @see IoSession#isReaderIdle()
	 */
	public boolean isReaderIdle() {
		return wrappedSession.isReaderIdle();
	}

	/**
	 * @see IoSession#isWriterIdle()
	 */
	public boolean isWriterIdle() {
		return wrappedSession.isWriterIdle();
	}

}
