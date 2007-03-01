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

package org.apache.ftpserver.ftplet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface DataConnection {

    /**
     * Transfer data from the client (e.g. STOR).
     * @param out The {@link OutputStream} containing the destination
     * of the data from the client.
     * @return The length of the transefered data
     * @throws IOException
     */
    long transferFromClient(OutputStream out) throws IOException;

    /**
     * Transfer data to the client (e.g. RETR).
     * @param in Data to be transfered to the client
     * @return The length of the transefered data
     * @throws IOException
     */
    long transferToClient(InputStream in) throws IOException;

    /**
     * Transfer a string to the client, e.g. during LIST
     * @param str The string to transfer
     * @throws IOException
     */
    void transferToClient(String str) throws IOException;

}