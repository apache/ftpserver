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

import org.apache.mina.filter.codec.demux.DemuxingProtocolCodecFactory;

/**
 * Provides a protocol codec for HTTP server.
 * 
 * @author The Apache Directory Project (mina-dev@directory.apache.org)
 * @version $Rev: 448080 $, $Date: 2006-09-20 08:03:48 +0200 (on, 20 sep 2006) $
 */
public class FtpServerProtocolCodecFactory extends
        DemuxingProtocolCodecFactory
{
    public FtpServerProtocolCodecFactory()
    {
        super.register( FtpRequestDecoder.class );
        super.register( FtpResponseEncoder.class );
    }
}
