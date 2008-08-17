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
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This ftplet calls other ftplet methods and returns appropriate return value.
 *
 * @author The Apache MINA Project (dev@mina.apache.org)
 * @version $Rev$, $Date$
 */
public class DefaultFtpletContainer implements FtpletContainer {

    private final Logger LOG = LoggerFactory
            .getLogger(DefaultFtpletContainer.class);

    private Map<String, Ftplet> ftplets = new ConcurrentHashMap<String, Ftplet>();

    public void dispose() {

        for (Entry<String, Ftplet> entry : ftplets.entrySet()) {
            try {
                entry.getValue().destroy();
            } catch (Exception ex) {
                LOG.error(entry.getKey() + " :: FtpletHandler.destroy()", ex);
            }
        }
        ftplets.clear();
    }

    public void addFtplet(String name, Ftplet ftplet) {
        if (getFtplet(name) != null) {
            throw new IllegalArgumentException("Ftplet with name \"" + name
                    + "\" already registred with container");
        }

        ftplets.put(name, ftplet);
    }

    public Ftplet removeFtplet(String name) {
        Ftplet ftplet = ftplets.get(name);

        if (ftplet != null) {
            ftplets.remove(name);
            return ftplet;
        } else {
            return null;
        }
    }

    /**
     * Get Ftplet for the given name.
     */
    public Ftplet getFtplet(String name) {
        if (name == null) {
            return null;
        }

        return ftplets.get(name);
    }

    public void init(FtpletContext ftpletContext) throws FtpException {
        // dummy, forced by Ftplet API
    }

    /**
     * @see FtpletContainer#getFtplets()
     */
    public Map<String, Ftplet> getFtplets() {
        return ftplets;
    }

    /**
     * @see FtpletContainer#setFtplets(Map)
     */
    public void setFtplets(Map<String, Ftplet> ftplets) {
        this.ftplets = ftplets;
    }

    /**
     * Destroy all ftplets.
     */
    public void destroy() {
        dispose();
    }

    /**
     * Call ftplet onConnect.
     */
    public FtpletEnum onConnect(FtpSession session) throws FtpException,
            IOException {
        FtpletEnum retVal = FtpletEnum.RET_DEFAULT;
        for (Entry<String, Ftplet> entry : ftplets.entrySet()) {
            retVal = entry.getValue().onConnect(session);
            if (retVal == null) {
                retVal = FtpletEnum.RET_DEFAULT;
            }

            // proceed only if the return value is FtpletEnum.RET_DEFAULT
            if (retVal != FtpletEnum.RET_DEFAULT) {
                break;
            }
        }
        return retVal;
    }

    /**
     * Call ftplet onDisconnect.
     */
    public FtpletEnum onDisconnect(FtpSession session) throws FtpException,
            IOException {
        FtpletEnum retVal = FtpletEnum.RET_DEFAULT;
        for (Entry<String, Ftplet> entry : ftplets.entrySet()) {

            retVal = entry.getValue().onDisconnect(session);
            if (retVal == null) {
                retVal = FtpletEnum.RET_DEFAULT;
            }

            // proceed only if the return value is FtpletEnum.RET_DEFAULT
            if (retVal != FtpletEnum.RET_DEFAULT) {
                break;
            }
        }
        return retVal;
    }

    public FtpletEnum afterCommand(FtpSession session, FtpRequest request)
            throws FtpException, IOException {
        FtpletEnum retVal = FtpletEnum.RET_DEFAULT;
        for (Entry<String, Ftplet> entry : ftplets.entrySet()) {

            retVal = entry.getValue().afterCommand(session, request);
            if (retVal == null) {
                retVal = FtpletEnum.RET_DEFAULT;
            }

            // proceed only if the return value is FtpletEnum.RET_DEFAULT
            if (retVal != FtpletEnum.RET_DEFAULT) {
                break;
            }
        }
        return retVal;
    }

    public FtpletEnum beforeCommand(FtpSession session, FtpRequest request)
            throws FtpException, IOException {
        FtpletEnum retVal = FtpletEnum.RET_DEFAULT;
        for (Entry<String, Ftplet> entry : ftplets.entrySet()) {

            retVal = entry.getValue().beforeCommand(session, request);
            if (retVal == null) {
                retVal = FtpletEnum.RET_DEFAULT;
            }

            // proceed only if the return value is FtpletEnum.RET_DEFAULT
            if (retVal != FtpletEnum.RET_DEFAULT) {
                break;
            }
        }
        return retVal;
    }

}
