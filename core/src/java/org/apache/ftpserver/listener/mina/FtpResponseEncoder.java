/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *  
 *    http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License. 
 *  
 */
package org.apache.ftpserver.listener.mina;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.ftpserver.ftplet.FtpResponse;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.apache.mina.filter.codec.demux.MessageEncoder;

/**
 * A {@link MessageEncoder} that encodes {@link FtpResponse}.
 */
public class FtpResponseEncoder implements MessageEncoder
{
    private static final CharsetEncoder ENCODER = Charset.forName("UTF-8").newEncoder();

    private static final Set TYPES;

    static
    {
        Set types = new HashSet();
        types.add( FtpResponse.class );
        TYPES = Collections.unmodifiableSet( types );
    }

    public FtpResponseEncoder()
    {
    }

    public void encode( IoSession session, Object message,
            ProtocolEncoderOutput out ) throws Exception
    {
        FtpResponse ftpResponse = (FtpResponse) message;
        
        ByteBuffer buf = ByteBuffer.allocate( 256 );

        buf.putString( ftpResponse.toString(), ENCODER );
        
        buf.flip();
        out.write(buf);

    }

    public Set getMessageTypes()
    {
        return TYPES;
    }
}
