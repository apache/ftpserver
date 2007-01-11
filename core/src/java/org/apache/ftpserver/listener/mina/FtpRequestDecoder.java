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

import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import org.apache.ftpserver.FtpRequestImpl;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.demux.MessageDecoderAdapter;
import org.apache.mina.filter.codec.demux.MessageDecoderResult;

public class FtpRequestDecoder extends MessageDecoderAdapter
{
    private CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();

    public MessageDecoderResult decodable( IoSession session, ByteBuffer in )
    {
        try {
            String line = in.getString(decoder);
            System.out.println(line);

            if(line.endsWith("\n")) {
                return MessageDecoderResult.OK;
            } else {
                return MessageDecoderResult.NEED_DATA;
            }
        } catch (CharacterCodingException e) {
            return MessageDecoderResult.NOT_OK;
        }
    }

    public MessageDecoderResult decode( IoSession session, ByteBuffer in,
            ProtocolDecoderOutput out ) throws Exception
    {
        String line = in.getString(decoder);
        System.out.println(line);
        
        out.write(new FtpRequestImpl(line));

        return MessageDecoderResult.OK;
    }
}
