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
package org.apache.ftpserver.remote.adapter;

import java.io.IOException;
import java.rmi.RemoteException;
import org.apache.ftpserver.remote.interfaces.SpyConnectionInterface;

/**
 * This remote spy user adapter.
 */
public
class SpyConnectionAdapter implements org.apache.ftpserver.interfaces.SpyConnectionInterface {

    private SpyConnectionInterface mSpy;

    /**
     * Default constructor.
     */
    public SpyConnectionAdapter(SpyConnectionInterface spy) {
        mSpy = spy;
    }

    /**
     * Get spy user
     */
    public SpyConnectionInterface getSpyObject() {
        return mSpy;
    }

    /**
     * Get spy user
     */
    public void setSpyObject(SpyConnectionInterface spy) {
        mSpy = spy;
    }

    /**
     * Write user request.
     */
    public void request(final String msg) throws IOException {
        SpyConnectionInterface spy = mSpy;
        if(spy != null) {
            try {
                spy.request(msg);
            }
            catch(RemoteException ex) {
                mSpy = null;
            }
        }
    }


    /**
     * Write server response.
     */
    public void response(final String msg) throws IOException {
        SpyConnectionInterface spy = mSpy;
        if(spy != null) {
            try {
                spy.response(msg);
            }
            catch(RemoteException ex) {
                mSpy = null;
            }
        }
    }
}
