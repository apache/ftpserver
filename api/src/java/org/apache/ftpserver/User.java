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
package org.apache.ftpserver;

import java.io.OutputStream;
import java.net.InetAddress;

/**
 * @author Vladimirov
 */
public interface User {

    String ANONYMOUS = "anonymous";

    /**
     * Get the user data type.
     */
    char getType();

    /**
     * Set the data type. Supported types are A (ascii) and I (binary).
     *
     * @return true if success
     */
    boolean setType(char type);

    /**
     * Get the file structure.
     */
    char getStructure();

    /**
     * Set the file structure. Supported structure type is F (file).
     *
     * @return true if success
     */
    boolean setStructure(char stru);

    /**
     * Get the transfer mode.
     */
    char getMode();

    /**
     * Set the transfer type. Supported transfer type is S (stream).
     *
     * @return true if success
     */
    boolean setMode(char md);

    /**
     * Get output stream. Returns <code>ftpserver.util.AsciiOutputStream</code>
     * if the transfer type is ASCII.
     */
    OutputStream getOutputStream(OutputStream os);

    /**
     * Is an anonymous user?
     */
    boolean getIsAnonymous();

    /**
     * Get the user name.
     */
    String getName();

    /**
     * Get the user password.
     */
    String getPassword();

    /**
     * Get client address
     */
    InetAddress getClientAddress();
}