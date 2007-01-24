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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

import javax.net.SocketFactory;

import org.apache.ftpserver.AbstractFtpServerTestTemplate;
import org.apache.ftpserver.interfaces.Command;

/**
 * Abstract {@link Command} test case
 */
public abstract class AbstractCommandTestTemplate extends AbstractFtpServerTestTemplate {

    private static final String ENCODING = "ISO-8859-1";

    protected InputStream in;

    protected OutputStream out;

    private Socket socket;

    public AbstractCommandTestTemplate() {
        super();
    }

    protected final Socket getSocket() {
        return socket;
    }

    protected void sendCommand(String command) throws IOException {
        out.write((command + "\n").getBytes(ENCODING));
    }

    protected void setUp() throws Exception {
        super.setUp();

        final SocketFactory socketFactory = SocketFactory.getDefault();
        this.socket = socketFactory.createSocket(getServerAddress(),
                getServerPort());

        socket.setKeepAlive(false);
        this.in = socket.getInputStream();
        this.out = socket.getOutputStream();
    }

    protected void tearDown() throws Exception {
        socket.close();
        super.tearDown();
    }

    protected void wait(String template, int timeout) throws IOException {
        final int oldTimeout = socket.getSoTimeout();
        try {
            socket.setSoTimeout(timeout);
            final InputStream inputStream = socket.getInputStream();
            byte[] bs = new byte[1024];
            StringBuffer stringBuffer = new StringBuffer();
            while (stringBuffer.indexOf(template) == -1) {
                int read = 0;
                try {
                    read = inputStream.read(bs, 0, 1024);
                } catch (SocketTimeoutException exc) {
                    assertEquals(template, stringBuffer);
                }
                if (read == -1) {
                    assertEquals(template, stringBuffer);
                }
                stringBuffer.append(new String(bs, 0, read, ENCODING));
            }
        } finally {
            socket.setSoTimeout(oldTimeout);
        }
    }

}