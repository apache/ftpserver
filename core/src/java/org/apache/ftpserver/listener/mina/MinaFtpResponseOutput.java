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

package org.apache.ftpserver.listener.mina;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.apache.ftpserver.FtpWriter;
import org.apache.ftpserver.ftplet.FtpReply;
import org.apache.mina.common.IoSession;

/**
 * Output for MINA, sends replies to the {@link FtpResponseEncoder} 
 * and then to the non-blocking socket
 */
public class MinaFtpResponseOutput extends FtpWriter {

    private IoSession session;
    
    public MinaFtpResponseOutput(IoSession session) {
        this.session = session;
    }

    public void write(FtpReply response) throws IOException {
        session.write(response).join();
    }

    protected InetAddress getFallbackServerAddress() {
        if(session.getLocalAddress() instanceof InetSocketAddress) {
            InetSocketAddress inetSocket = (InetSocketAddress) session.getLocalAddress();
            return inetSocket.getAddress();
        } else {
            return null;
        }
    }

    public void close() {
        // do nothing
    }

    
}
