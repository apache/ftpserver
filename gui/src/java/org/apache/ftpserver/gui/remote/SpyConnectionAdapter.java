/* ====================================================================
 * Copyright 2002 - 2004
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * $Id$
 */

package org.apache.ftpserver.gui.remote;

import org.apache.ftpserver.remote.interfaces.ConnectionServiceInterface;
import org.apache.ftpserver.remote.interfaces.SpyConnectionInterface;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * This class is used to monitor user activities - remote adapter.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public
class SpyConnectionAdapter implements SpyConnectionInterface {

    private SpyConnectionInterface mSpy;
    private ConnectionServiceInterface mConService;
    private String mstSessionId;

    /**
     * Constructor - set the actual object.
     */
    public SpyConnectionAdapter(ConnectionServiceInterface conService,
                                String sessionId,
                                SpyConnectionInterface spy) throws RemoteException {
        mSpy = spy;
        mstSessionId = sessionId;
        mConService = conService;

        UnicastRemoteObject.exportObject(this);
        mConService.setSpyObject(sessionId, this);
    }

    /**
     * Write user request
     */
    public void request(final String msg) throws IOException {
        mSpy.request(msg);
    }

    /**
     * Write server response
     */
    public void response(final String msg) throws IOException {
        mSpy.response(msg);
    }

    /**
     * Close - unexport the object
     */
    public void close() {
        System.out.println("Closing spy listener...");
        try {
            mConService.setSpyObject(mstSessionId, null);
        } catch (Exception ex) {
            //ex.printStackTrace();
        }

        try {
            UnicastRemoteObject.unexportObject(this, true);
        } catch (Exception ex) {
            //ex.printStackTrace();
        }
    }
}
