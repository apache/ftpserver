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

package org.apache.ftpserver.core;

import org.apache.ftpserver.User;
import org.apache.ftpserver.util.AsciiOutputStream;
import org.apache.ftpserver.util.IoUtils;

import java.io.OutputStream;
import java.io.Serializable;


/**
 * Ftp user class. It handles all user specific file system task.
 * It supports user virtual root directory.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public
class UserImpl extends org.apache.ftpserver.usermanager.User implements Serializable, User {

    private char mcDataType = 'A';
    private char mcStructure = 'F';
    private char mcMode = 'S';

    /**
     * Constructor - does nothing.
     */
    public UserImpl() {
    }

    /**
     * Get the user data type.
     */
    public char getType() {
        return mcDataType;
    }

    /**
     * Set the data type. Supported types are A (ascii) and I (binary).
     *
     * @return true if success
     */
    public boolean setType(char type) {
        type = Character.toUpperCase(type);
        if ((type != 'A') && (type != 'I')) {
            return false;
        }
        mcDataType = type;
        return true;
    }


    /**
     * Get the file structure.
     */
    public char getStructure() {
        return mcStructure;
    }

    /**
     * Set the file structure. Supported structure type is F (file).
     *
     * @return true if success
     */
    public boolean setStructure(char stru) {
        stru = Character.toUpperCase(stru);
        if (stru != 'F') {
            return false;
        }
        mcStructure = stru;
        return true;
    }


    /**
     * Get the transfer mode.
     */
    public char getMode() {
        return mcMode;
    }

    /**
     * Set the transfer type. Supported transfer type is S (stream).
     *
     * @return true if success
     */
    public boolean setMode(char md) {
        md = Character.toUpperCase(md);
        if (md != 'S') {
            return false;
        }
        mcMode = md;
        return true;
    }

    /**
     * Get output stream. Returns <code>ftpserver.util.AsciiOutputStream</code>
     * if the transfer type is ASCII.
     */
    public OutputStream getOutputStream(OutputStream os) {
        os = IoUtils.getBufferedOutputStream(os);
        if (mcDataType == 'A') {
            os = new AsciiOutputStream(os);
        }
        return os;
    }

    /**
     * Is an anonymous user?
     */
    public boolean getIsAnonymous() {
        return ANONYMOUS.equals(getName());
    }
}


