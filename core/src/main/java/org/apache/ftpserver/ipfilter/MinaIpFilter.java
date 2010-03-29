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

package org.apache.ftpserver.ipfilter;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.session.IoSession;

/**
 * An implementation of Mina Filter to filter clients based on the originating
 * IP address.
 * 
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 * 
 */

public class MinaIpFilter extends IoFilterAdapter {

	/**
	 * The actual <code>IpFilter</code> used by this filter.
	 */
	private IpFilter filter = null;

	/**
	 * Creates a new instance of <code>MinaIpFilter</code>.
	 * 
	 * @param filter
	 *            the filter
	 */
	public MinaIpFilter(IpFilter filter) {
		this.filter = filter;
	}

	@Override
	public void sessionCreated(NextFilter nextFilter, IoSession session) {
		SocketAddress remoteAddress = session.getRemoteAddress();
		if (remoteAddress instanceof InetSocketAddress) {
			InetAddress ipAddress = ((InetSocketAddress) remoteAddress).getAddress();
			// TODO we probably have to check if the InetAddress is a version 4
			// address, or else, the result would probably be unknown.
			if (!filter.accept(ipAddress)) {
				session.close(true);
			}
			else {
				nextFilter.sessionCreated(session);
			}
		}
	}
}
