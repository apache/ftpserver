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

import java.io.IOException;

/**
 * @author Paul Hammant
 * @version $Revision$
 */
public interface CommandHandlerMonitor {
    void unknownResponseException(String message, Throwable th);

    void ipBlockException(String message, IOException ex);

    void addUserException(String message, UserManagerException ex);

    void setAttrException(String message, UserManagerException ex);

    void deleteUserException(String message, UserManagerException ex);
}
